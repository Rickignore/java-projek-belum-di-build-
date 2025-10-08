package javaapplication3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Utils {

    // hex dump for byte array
    public static String hexDump(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        while (offset < data.length) {
            int rowLen = Math.min(16, data.length - offset);
            sb.append(String.format("%08X  ", offset));
            // hex bytes
            for (int i = 0; i < 16; i++) {
                if (i < rowLen) {
                    sb.append(String.format("%02X ", data[offset + i]));
                } else {
                    sb.append("   ");
                }
                if (i == 7) sb.append(" ");
            }
            sb.append(" ");
            // ascii
            for (int i = 0; i < rowLen; i++) {
                byte b = data[offset + i];
                if (b >= 32 && b < 127) sb.append((char)b);
                else sb.append('.');
            }
            sb.append(System.lineSeparator());
            offset += rowLen;
        }
        return sb.toString();
    }

    // extract printable strings (like `strings` util). limitBytes: read at most this many bytes from file
    public static String extractPrintableStrings(String path, int minLen, int limitBytes) {
        File f = new File(path);
        if (!f.exists()) return "";
        try (FileInputStream fis = new FileInputStream(f)) {
            int toRead = (int) Math.min(f.length(), limitBytes);
            byte[] data = new byte[toRead];
            int read = fis.read(data);
            if (read <= 0) return "";

            StringBuilder sb = new StringBuilder();
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < read; i++) {
                int c = data[i] & 0xFF;
                if (c >= 32 && c < 127) {
                    cur.append((char)c);
                } else {
                    if (cur.length() >= minLen) {
                        sb.append(cur.toString()).append(System.lineSeparator());
                    }
                    cur.setLength(0);
                }
            }
            if (cur.length() >= minLen) sb.append(cur.toString()).append(System.lineSeparator());
            return sb.toString();
        } catch (IOException ex) {
            return "Error extracting strings: " + ex.getMessage();
        }
    }

    // simple entropy calc (bits per byte) over up to limitBytes of file
    public static double calculateEntropy(String path, int limitBytes) {
        File f = new File(path);
        if (!f.exists()) return 0.0;
        try (FileInputStream fis = new FileInputStream(f)) {
            int toRead = (int) Math.min(f.length(), limitBytes);
            byte[] data = new byte[toRead];
            int read = fis.read(data);
            if (read <= 0) return 0.0;

            int[] counts = new int[256];
            for (int i = 0; i < read; i++) counts[data[i] & 0xFF]++;

            double entropy = 0.0;
            for (int c : counts) {
                if (c == 0) continue;
                double p = (double)c / read;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
            return entropy; // bits per byte
        } catch (IOException ex) {
            return 0.0;
        }
    }
}
