package javaapplication3;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HexViewPanel extends JTextArea {

    public HexViewPanel() {
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setEditable(false);
    }

    public void showFile(String path) {
        if (path == null || path.isEmpty()) {
            setText("");
            return;
        }
        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            setText("File tidak ditemukan: " + path);
            return;
        }

        try {
            // read up to limit to avoid freezing large files
            int limit = 200_000; // bytes to display (adjust)
            byte[] data = new byte[(int)Math.min(f.length(), limit)];
            try (FileInputStream fis = new FileInputStream(f)) {
                int read = fis.read(data);
                if (read < data.length) {
                    byte[] tmp = new byte[read];
                    System.arraycopy(data, 0, tmp, 0, read);
                    data = tmp;
                }
            }

            String hex = Utils.hexDump(data);
            StringBuilder sb = new StringBuilder();
            sb.append("File: ").append(f.getName()).append(" (").append(f.length()).append(" bytes)\n\n");
            if (f.length() > limit) {
                sb.append("[Menampilkan ").append(data.length).append(" byte pertama]\n\n");
            }
            sb.append(hex);
            setText(sb.toString());
            setCaretPosition(0);
        } catch (IOException ex) {
            setText("Error membaca file: " + ex.getMessage());
        }
    }
}
