package javaapplication3;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple Java-level sandbox based on SecurityManager.
 *
 * Activation:
 *  - via VM options (no source changes): 
 *      -Denable.sandbox=true 
 *      -Dsandbox.allowed.sample=C:\path\to\sample.bin 
 *      -Dsandbox.allowed.rule=C:\path\to\rules.yar
 *
 * Or programmatically:
 *  Sandbox.enableFor(samplePath, rulePath);
 *
 * NOTE: SecurityManager is deprecated in newer JDKs and may be removed in future.
 * This provides basic in-JVM restrictions (file read/write/exec/network/process/exit).
 */
public final class Sandbox {

    private static volatile boolean enabled = false;
    private static final Set<String> allowedReadCanonical = new HashSet<>();
    private static final Object lock = new Object();

    // static initializer: check system property for auto-enable
    static {
        try {
            String enable = System.getProperty("enable.sandbox");
            if ("true".equalsIgnoreCase(enable)) {
                String sample = System.getProperty("sandbox.allowed.sample");
                String rule = System.getProperty("sandbox.allowed.rule");
                enableFor(sample, rule);
            }
        } catch (Throwable t) {
            System.err.println("Sandbox static init error: " + t.getMessage());
        }
    }

    private Sandbox() {
        // no instantiation
    }

    /**
     * Programmatically enable sandbox and set allowed file paths for read.
     * You can pass null/empty for either path (then it's not allowed).
     */
    public static void enableFor(String samplePath, String rulePath) {
        synchronized (lock) {
            allowedReadCanonical.clear();
            addAllowed(samplePath);
            addAllowed(rulePath);

            if (!enabled) {
                System.setSecurityManager(new RestrictiveSecurityManager());
                enabled = true;
                System.err.println("Sandbox enabled. Allowed read files: " + allowedReadCanonical);
            }
        }
    }

    /**
     * Disable sandbox (will remove SecurityManager) - use with caution.
     * Note: removing SecurityManager may be restricted by current SecurityManager itself.
     */
    public static void disable() {
        synchronized (lock) {
            try {
                System.setSecurityManager(null);
                enabled = false;
                allowedReadCanonical.clear();
                System.err.println("Sandbox disabled.");
            } catch (SecurityException se) {
                System.err.println("Unable to disable sandbox: " + se.getMessage());
            }
        }
    }

    private static void addAllowed(String path) {
        if (path == null) return;
        try {
            File f = new File(path);
            String can = f.getCanonicalPath();
            allowedReadCanonical.add(can);
            // also allow parent directory (so reading same-dir auxiliary files like .sig if needed)
            File p = f.getParentFile();
            if (p != null) allowedReadCanonical.add(p.getCanonicalPath());
        } catch (IOException e) {
            // ignore invalid paths
            System.err.println("Sandbox: invalid allowed path: " + path + " (" + e.getMessage() + ")");
        }
    }

    private static boolean isAllowedRead(String filePath) {
        if (filePath == null) return false;
        try {
            String can = new File(filePath).getCanonicalPath();
            // exact file or inside allowed directories
            for (String allowed : allowedReadCanonical) {
                if (can.equals(allowed)) return true;
                if (can.startsWith(allowed + File.separator)) return true;
            }
        } catch (IOException e) {
            // treat as not allowed
        }
        return false;
    }

    // The custom SecurityManager implementation
    private static class RestrictiveSecurityManager extends SecurityManager {

        @Override
        public void checkRead(String file) {
            // allow reading of JRE / classpath resources implicitly
            if (isJreOrClasspathResource(file)) {
                return;
            }
            if (!isAllowedRead(file)) {
                throw new SecurityException("Read access denied to file: " + file);
            }
        }

        @Override
        public void checkRead(String file, Object context) {
            checkRead(file);
        }

        @Override
        public void checkWrite(String file) {
            throw new SecurityException("Write access denied to file: " + file);
        }

        @Override
        public void checkDelete(String file) {
            throw new SecurityException("Delete access denied to file: " + file);
        }

        @Override
        public void checkExec(String cmd) {
            throw new SecurityException("Execution of external commands denied: " + cmd);
        }

        @Override
        public void checkConnect(String host, int port) {
            throw new SecurityException("Network connections are denied: " + host + ":" + port);
        }

        @Override
        public void checkListen(int port) {
            throw new SecurityException("Listening on sockets denied: " + port);
        }

        @Override
        public void checkAccept(String host, int port) {
            throw new SecurityException("Accepting socket connections denied: " + host + ":" + port);
        }

        @Override
        public void checkExit(int status) {
            throw new SecurityException("Exit VM denied");
        }

        // Allow most other runtime permissions required for Swing and classloading.
        @Override
        public void checkPermission(java.security.Permission perm) {
            // allow everything else by default to avoid breaking Swing / reflection tasks,
            // but keep file/network/process checks above enforced.
            // If you want stricter control, add specific permission checks here.
        }

        private boolean isJreOrClasspathResource(String file) {
            if (file == null) return false;
            try {
                String can = new File(file).getCanonicalPath();
                // Allow reading from java.home (JRE libs) and current working dir (app resources)
                String javaHome = System.getProperty("java.home");
                if (javaHome != null && can.startsWith(new File(javaHome).getCanonicalPath())) return true;
                String userDir = System.getProperty("user.dir");
                if (userDir != null && can.startsWith(new File(userDir).getCanonicalPath())) return true;
            } catch (IOException e) {
                // ignore
            }
            return false;
        }
    }
}
