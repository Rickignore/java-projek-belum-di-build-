package javaapplication3;

import javax.swing.SwingUtilities;

public class JavaApplication3 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
