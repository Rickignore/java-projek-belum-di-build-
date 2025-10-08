package javaapplication3;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private ControlPanel controlPanel;
    private HexViewPanel hexViewPanel;
    private ResultPanel resultPanel;

    public MainFrame() {
        setTitle("Simple YARA Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Set icon frame
        setAppIcon();  // <-- Sesuaikan path dan tambahkan pengecekan

        // Set background untuk content pane (frame utama)
        getContentPane().setBackground(Color.GRAY);

        controlPanel = new ControlPanel();
        hexViewPanel = new HexViewPanel();
        resultPanel = new ResultPanel();

        // Set background untuk masing-masing panel
        controlPanel.setBackground(Color.GRAY);
        hexViewPanel.setBackground(Color.ORANGE);
        resultPanel.setBackground(Color.YELLOW);

        // Hook: when control requests analysis, callbacks to UI components
        controlPanel.setAnalysisListener((samplePath, rulePath) -> {
            // load hex view (async-ish but we will run quickly)
            hexViewPanel.showFile(samplePath);
            // run YARA and other analysis
            String yaraOut = YaraEngine.runYara(rulePath, samplePath);
            String strings = Utils.extractPrintableStrings(samplePath, 4, 100_000); // limit read
            double entropy = Utils.calculateEntropy(samplePath, 100_000); // sample limit
            resultPanel.setResult(yaraOut, strings, entropy);
        });

        // layout: top = control, center splitpane (hex | results)
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(hexViewPanel), new JScrollPane(resultPanel));
        split.setDividerLocation(500);
        add(split, BorderLayout.CENTER);
    }

    private void setAppIcon() {
        java.net.URL iconURL = getClass().getResource("/javaapplication3/resources/icon.png");

        if (iconURL == null) {
            System.err.println("Icon tidak ditemukan! Periksa path dan lokasi file.");
            return;
        } else {
            System.out.println("Icon ditemukan di: " + iconURL);
        }

        Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
        setIconImage(icon);
    }
}
