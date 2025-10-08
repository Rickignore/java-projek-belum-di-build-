package javaapplication3;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class YaraEngine {

    /**
     * Run `yara -s rulePath samplePath` and return stdout+stderr.
     * Requires yara installed and available in PATH.
     */
    public static String runYara(String rulePath, String samplePath) {
        File rule = new File(rulePath);
        File sample = new File(samplePath);
        if (!rule.exists() || !sample.exists()) {
            return "Rule or sample file not found.";
        }

        try {
            // Use -s to show matched strings; you can add -w or others as needed
            ProcessBuilder pb = new ProcessBuilder("yara", "-s", rule.getAbsolutePath(), sample.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder out = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line).append(System.lineSeparator());
                }
            }

            int code = p.waitFor();
            out.append(String.format("Process exit code: %d%n", code));
            return out.toString();
        } catch (Exception ex) {
            return "Error running yara: " + ex.getMessage();
        }
    }
}
