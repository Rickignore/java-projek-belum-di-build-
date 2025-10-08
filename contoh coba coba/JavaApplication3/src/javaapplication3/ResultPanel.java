package javaapplication3;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends JPanel {

    private JTextArea txtYaraOutput;
    private JTextArea txtStrings;
    private JLabel lblEntropy;

    public ResultPanel() {
        setLayout(new BorderLayout());

        txtYaraOutput = new JTextArea();
        txtYaraOutput.setEditable(false);
        txtYaraOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        txtStrings = new JTextArea();
        txtStrings.setEditable(false);
        txtStrings.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        lblEntropy = new JLabel("Entropy: N/A");

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(txtYaraOutput),
                new JScrollPane(txtStrings));
        split.setDividerLocation(250);

        add(lblEntropy, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    public void setResult(String yaraOutput, String strings, double entropy) {
        txtYaraOutput.setText(yaraOutput == null ? "[no output]" : yaraOutput);
        txtStrings.setText(strings == null ? "" : strings);
        lblEntropy.setText(String.format("Entropy (sampled): %.4f bits/byte", entropy));
        revalidate();
    }
}
