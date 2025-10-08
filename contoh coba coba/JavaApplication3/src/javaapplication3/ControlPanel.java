package javaapplication3;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.BiConsumer;

public class ControlPanel extends JPanel {

    private JTextField txtSample;
    private JTextField txtRule;
    private JButton btnChooseSample;
    private JButton btnChooseRule;
    private JButton btnAnalyze;

    // callback: (samplePath, rulePath)
    private BiConsumer<String, String> analysisListener;

    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        txtSample = new JTextField(30);
        txtRule = new JTextField(30);
        btnChooseSample = new JButton("Pilih File Sample...");
        btnChooseRule = new JButton("Pilih File Rule YARA...");
        btnAnalyze = new JButton("Run Analysis");

        add(new JLabel("Sample:"));
        add(txtSample);
        add(btnChooseSample);

        add(new JLabel("YARA Rule:"));
        add(txtRule);
        add(btnChooseRule);

        add(btnAnalyze);

        btnChooseSample.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                txtSample.setText(f.getAbsolutePath());
            }
        });

        btnChooseRule.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                txtRule.setText(f.getAbsolutePath());
            }
        });

        btnAnalyze.addActionListener(e -> {
            String sample = txtSample.getText().trim();
            String rule = txtRule.getText().trim();
            if (sample.isEmpty() || rule.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih sample dan rule YARA terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (analysisListener != null) {
                // run callback (synchronous here; for large files you may want to run in background thread)
                analysisListener.accept(sample, rule);
            }
        });
    }

    public void setAnalysisListener(BiConsumer<String, String> listener) {
        this.analysisListener = listener;
    }
}
