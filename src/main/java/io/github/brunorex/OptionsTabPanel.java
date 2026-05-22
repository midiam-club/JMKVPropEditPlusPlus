package io.github.brunorex;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OptionsTabPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JFrame parentFrame;
    private final JFileChooser chooser;
    private final IniPersistenceService iniService;

    private JTextField txtMkvPropExe;
    private JCheckBox chbMkvPropExeDef;
    private JComboBox<String> cbLanguage;
    private Runnable onLanguageApply;

    public OptionsTabPanel(JFrame parentFrame, JFileChooser chooser, IniPersistenceService iniService) {
        this.parentFrame = parentFrame;
        this.chooser = chooser;
        this.iniService = iniService;
        initComponents();
    }

    private void initComponents() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[] { 0, 0, 0 };
        gbl.rowHeights = new int[] { 0, 0, 0, 0, 0 };
        gbl.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        setLayout(gbl);

        JLabel lblMkvPropExe = new JLabel(LanguageManager.getString("options.executable"));
        lblMkvPropExe.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        add(lblMkvPropExe, gbc_label);

        txtMkvPropExe = new JTextField("mkvpropedit");
        txtMkvPropExe.setEditable(false);
        txtMkvPropExe.setColumns(10);
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.insets = new Insets(0, 0, 5, 0);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = 0;
        add(txtMkvPropExe, gbc_textField);

        Utils.addRCMenuMouseListener(txtMkvPropExe);

        JPanel pnlMkvPropExeControls = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 1;
        add(pnlMkvPropExeControls, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_panel.rowHeights = new int[] { 0, 0, 0 };
        gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
        pnlMkvPropExeControls.setLayout(gbl_panel);

        chbMkvPropExeDef = new JCheckBox(LanguageManager.getString("options.use.default"));
        chbMkvPropExeDef.setSelected(true);
        chbMkvPropExeDef.setEnabled(false);
        GridBagConstraints gbc_checkBox = new GridBagConstraints();
        gbc_checkBox.anchor = GridBagConstraints.WEST;
        gbc_checkBox.insets = new Insets(0, 0, 5, 5);
        gbc_checkBox.gridx = 0;
        gbc_checkBox.gridy = 0;
        pnlMkvPropExeControls.add(chbMkvPropExeDef, gbc_checkBox);

        FileNameExtensionFilter exeFilter = new FileNameExtensionFilter("Executable files (*.exe)", "exe");

        JButton btnBrowseMkvPropExe = new JButton(LanguageManager.getString("button.browse"));
        btnBrowseMkvPropExe.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.exe"));
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.resetChoosableFileFilters();
            if (Utils.isWindows()) {
                chooser.setFileFilter(exeFilter);
            }
            int open = chooser.showOpenDialog(parentFrame);
            if (open == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f.exists()) {
                    txtMkvPropExe.setText(f.toString());
                    chbMkvPropExeDef.setSelected(false);
                    chbMkvPropExeDef.setEnabled(true);
                    iniService.saveExecutablePath(f.toString());
                }
            }
        });
        GridBagConstraints gbc_button = new GridBagConstraints();
        gbc_button.insets = new Insets(0, 0, 5, 5);
        gbc_button.gridx = 1;
        gbc_button.gridy = 0;
        pnlMkvPropExeControls.add(btnBrowseMkvPropExe, gbc_button);

        JButton btnDownloadMkvPropExe = new JButton(LanguageManager.getString("options.download"));
        btnDownloadMkvPropExe.setToolTipText(LanguageManager.getString("options.download.tooltip"));
        GridBagConstraints gbc_btnDownload = new GridBagConstraints();
        gbc_btnDownload.insets = new Insets(0, 0, 5, 0);
        gbc_btnDownload.gridx = 2;
        gbc_btnDownload.gridy = 0;
        pnlMkvPropExeControls.add(btnDownloadMkvPropExe, gbc_btnDownload);

        JProgressBar progressDownloadMkv = new JProgressBar();
        progressDownloadMkv.setStringPainted(true);
        progressDownloadMkv.setVisible(false);
        GridBagConstraints gbc_progressDownload = new GridBagConstraints();
        gbc_progressDownload.fill = GridBagConstraints.HORIZONTAL;
        gbc_progressDownload.gridwidth = 3;
        gbc_progressDownload.gridx = 0;
        gbc_progressDownload.gridy = 1;
        pnlMkvPropExeControls.add(progressDownloadMkv, gbc_progressDownload);

        btnDownloadMkvPropExe.addActionListener(e -> {
            btnDownloadMkvPropExe.setEnabled(false);
            progressDownloadMkv.setVisible(true);
            progressDownloadMkv.setValue(0);
            progressDownloadMkv.setString(LanguageManager.getString("options.downloading"));

            File targetDir = new File(System.getProperty("user.dir"), "mkvtools");

            MkvToolsDownloader downloader = new MkvToolsDownloader(
                    targetDir,
                    status -> javax.swing.SwingUtilities.invokeLater(() -> progressDownloadMkv.setString(status)),
                    progress -> javax.swing.SwingUtilities.invokeLater(() -> progressDownloadMkv.setValue(progress)),
                    error -> javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDownloadMkv.setVisible(false);
                        btnDownloadMkvPropExe.setEnabled(true);
                        JOptionPane.showMessageDialog(parentFrame,
                                LanguageManager.getString("options.download.error") + ": " + error,
                                "", JOptionPane.ERROR_MESSAGE);
                    }),
                    () -> javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDownloadMkv.setVisible(false);
                        btnDownloadMkvPropExe.setEnabled(true);

                        File mkvpropedit = new File(targetDir, "mkvpropedit.exe");
                        if (mkvpropedit.exists()) {
                            txtMkvPropExe.setText("mkvtools" + File.separator + "mkvpropedit.exe");
                            chbMkvPropExeDef.setSelected(false);
                        }

                        JOptionPane.showMessageDialog(parentFrame,
                                LanguageManager.getString("options.download.complete"),
                                "", JOptionPane.INFORMATION_MESSAGE);
                    }));

            downloader.execute();
        });

        JLabel lblLanguage = new JLabel(LanguageManager.getString("label.language"));
        GridBagConstraints gbc_lblLanguage = new GridBagConstraints();
        gbc_lblLanguage.anchor = GridBagConstraints.WEST;
        gbc_lblLanguage.insets = new Insets(0, 0, 5, 5);
        gbc_lblLanguage.gridx = 0;
        gbc_lblLanguage.gridy = 2;
        add(lblLanguage, gbc_lblLanguage);

        JPanel pnlLanguageControls = new JPanel();
        GridBagConstraints gbc_pnlLanguageControls = new GridBagConstraints();
        gbc_pnlLanguageControls.insets = new Insets(0, 0, 5, 0);
        gbc_pnlLanguageControls.fill = GridBagConstraints.BOTH;
        gbc_pnlLanguageControls.gridx = 1;
        gbc_pnlLanguageControls.gridy = 2;
        add(pnlLanguageControls, gbc_pnlLanguageControls);
        GridBagLayout gbl_pnlLanguageControls = new GridBagLayout();
        gbl_pnlLanguageControls.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlLanguageControls.rowHeights = new int[] { 0, 0 };
        gbl_pnlLanguageControls.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
        gbl_pnlLanguageControls.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlLanguageControls.setLayout(gbl_pnlLanguageControls);

        cbLanguage = new JComboBox<String>();
        cbLanguage.setModel(new DefaultComboBoxModel<String>(new String[] { "English", "Español" }));
        if (LanguageManager.getLocale().getLanguage().equals("es")) {
            cbLanguage.setSelectedItem("Español");
        } else {
            cbLanguage.setSelectedItem("English");
        }
        GridBagConstraints gbc_cbLanguage = new GridBagConstraints();
        gbc_cbLanguage.insets = new Insets(0, 0, 0, 5);
        gbc_cbLanguage.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbLanguage.gridx = 0;
        gbc_cbLanguage.gridy = 0;
        pnlLanguageControls.add(cbLanguage, gbc_cbLanguage);

        JButton btnApplyLanguage = new JButton(LanguageManager.getString("button.apply"));
        btnApplyLanguage.addActionListener(e -> {
            if (onLanguageApply != null) {
                onLanguageApply.run();
            }
        });
        GridBagConstraints gbc_btnApplyLanguage = new GridBagConstraints();
        gbc_btnApplyLanguage.gridx = 1;
        gbc_btnApplyLanguage.gridy = 0;
        gbc_btnApplyLanguage.insets = new Insets(0, 0, 0, 5);
        pnlLanguageControls.add(btnApplyLanguage, gbc_btnApplyLanguage);

        JPanel pnlLanguageFiller = new JPanel();
        GridBagConstraints gbc_pnlLanguageFiller = new GridBagConstraints();
        gbc_pnlLanguageFiller.fill = GridBagConstraints.BOTH;
        gbc_pnlLanguageFiller.gridx = 2;
        gbc_pnlLanguageFiller.gridy = 0;
        pnlLanguageControls.add(pnlLanguageFiller, gbc_pnlLanguageFiller);

        JPanel pnlOptionsFiller = new JPanel();
        GridBagConstraints gbc_pnlOptionsFiller = new GridBagConstraints();
        gbc_pnlOptionsFiller.fill = GridBagConstraints.BOTH;
        gbc_pnlOptionsFiller.gridx = 0;
        gbc_pnlOptionsFiller.gridy = 3;
        gbc_pnlOptionsFiller.gridwidth = 2;
        add(pnlOptionsFiller, gbc_pnlOptionsFiller);
    }

    public String getExecutablePath() {
        return txtMkvPropExe.getText();
    }

    public void setExecutablePath(String path) {
        txtMkvPropExe.setText(path);
    }

    public boolean isDefaultExecutableSelected() {
        return chbMkvPropExeDef.isSelected();
    }

    public void setDefaultExecutable(boolean selected) {
        chbMkvPropExeDef.setSelected(selected);
    }

    public void setDefaultExecutableEnabled(boolean enabled) {
        chbMkvPropExeDef.setEnabled(enabled);
    }

    public JTextField getExecutableTextField() {
        return txtMkvPropExe;
    }

    public void setOnLanguageApply(Runnable callback) {
        this.onLanguageApply = callback;
    }

    public String getSelectedLanguageCode() {
        String selected = (String) cbLanguage.getSelectedItem();
        if ("Español".equals(selected)) {
            return "es";
        }
        return "en";
    }
}
