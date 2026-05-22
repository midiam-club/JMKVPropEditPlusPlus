package io.github.brunorex;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.List;

public class GeneralTabPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JFrame parentFrame;
    private final JFileChooser chooser;
    private final FileFilter TXT_EXT_FILTER = new FileNameExtensionFilter("Plain text files (*.txt)", "txt");
    private final FileFilter XML_EXT_FILTER = new FileNameExtensionFilter("XML files (*.xml)", "xml");

    private JCheckBox chbTitleGeneral;
    private JTextField txtTitleGeneral;
    private JCheckBox chbNumbGeneral;
    private JLabel lblNumbStartGeneral;
    private JTextField txtNumbStartGeneral;
    private JLabel lblNumbPadGeneral;
    private JTextField txtNumbPadGeneral;
    private JLabel lblNumbExplainGeneral;
    private JCheckBox chbChapters;
    private JComboBox<String> cbChapters;
    private JButton btnBrowseChapters;
    private JComboBox<String> cbExtChapters;
    private JTextField txtChapters;
    private JCheckBox chbTags;
    private JComboBox<String> cbTags;
    private JTextField txtTags;
    private JButton btnBrowseTags;
    private JComboBox<String> cbExtTags;
    private JCheckBox chbExtraCmdGeneral;
    private JTextField txtExtraCmdGeneral;

    private String[] cmdLineGeneral;
    private String[] cmdLineGeneralOpt;

    public GeneralTabPanel(JFrame parentFrame, JFileChooser chooser) {
        this.parentFrame = parentFrame;
        this.chooser = chooser;
        buildUI();
        setupListeners();
    }

    private void buildUI() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[] { 75, 655, 0 };
        gbl.rowHeights = new int[] { 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0 };
        gbl.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
        setLayout(gbl);

        chbTitleGeneral = new JCheckBox(LanguageManager.getString("general.title"));
        GridBagConstraints gbc_chbTitleGeneral = new GridBagConstraints();
        gbc_chbTitleGeneral.insets = new Insets(0, 0, 5, 5);
        gbc_chbTitleGeneral.anchor = GridBagConstraints.WEST;
        gbc_chbTitleGeneral.gridx = 0;
        gbc_chbTitleGeneral.gridy = 0;
        add(chbTitleGeneral, gbc_chbTitleGeneral);

        txtTitleGeneral = new JTextField();
        txtTitleGeneral.setEnabled(false);
        GridBagConstraints gbc_txtTitleGeneral = new GridBagConstraints();
        gbc_txtTitleGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_txtTitleGeneral.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTitleGeneral.gridx = 1;
        gbc_txtTitleGeneral.gridy = 0;
        add(txtTitleGeneral, gbc_txtTitleGeneral);
        txtTitleGeneral.setColumns(10);

        JPanel pnlNumbControlsGeneral = new JPanel();
        FlowLayout fl_pnlNumbControlsGeneral = (FlowLayout) pnlNumbControlsGeneral.getLayout();
        fl_pnlNumbControlsGeneral.setVgap(0);
        fl_pnlNumbControlsGeneral.setAlignment(FlowLayout.LEFT);
        GridBagConstraints gbc_pnlNumbControlsGeneral = new GridBagConstraints();
        gbc_pnlNumbControlsGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_pnlNumbControlsGeneral.fill = GridBagConstraints.BOTH;
        gbc_pnlNumbControlsGeneral.gridx = 1;
        gbc_pnlNumbControlsGeneral.gridy = 1;
        add(pnlNumbControlsGeneral, gbc_pnlNumbControlsGeneral);

        chbNumbGeneral = new JCheckBox(LanguageManager.getString("general.numbering"));
        chbNumbGeneral.setEnabled(false);
        pnlNumbControlsGeneral.add(chbNumbGeneral);

        Component horizontalStrut1 = Box.createHorizontalStrut(10);
        pnlNumbControlsGeneral.add(horizontalStrut1);

        lblNumbStartGeneral = new JLabel(LanguageManager.getString("general.numbering.start"));
        lblNumbStartGeneral.setEnabled(false);
        pnlNumbControlsGeneral.add(lblNumbStartGeneral);

        txtNumbStartGeneral = new JTextField();
        txtNumbStartGeneral.setEnabled(false);
        txtNumbStartGeneral.setText("1");
        pnlNumbControlsGeneral.add(txtNumbStartGeneral);
        txtNumbStartGeneral.setColumns(10);

        Component horizontalStrut2 = Box.createHorizontalStrut(5);
        pnlNumbControlsGeneral.add(horizontalStrut2);

        lblNumbPadGeneral = new JLabel(LanguageManager.getString("general.numbering.padding"));
        lblNumbPadGeneral.setEnabled(false);
        pnlNumbControlsGeneral.add(lblNumbPadGeneral);

        txtNumbPadGeneral = new JTextField();
        txtNumbPadGeneral.setEnabled(false);
        txtNumbPadGeneral.setText("1");
        txtNumbPadGeneral.setColumns(10);
        pnlNumbControlsGeneral.add(txtNumbPadGeneral);

        lblNumbExplainGeneral = new JLabel(
                "      " + LanguageManager.getString("general.numbering.explain"));
        lblNumbExplainGeneral.setEnabled(false);
        GridBagConstraints gbc_lblNumbExplainGeneral = new GridBagConstraints();
        gbc_lblNumbExplainGeneral.insets = new Insets(0, 0, 10, 0);
        gbc_lblNumbExplainGeneral.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblNumbExplainGeneral.gridx = 1;
        gbc_lblNumbExplainGeneral.gridy = 2;
        add(lblNumbExplainGeneral, gbc_lblNumbExplainGeneral);

        chbChapters = new JCheckBox(LanguageManager.getString("general.chapters"));
        GridBagConstraints gbc_chbChapters = new GridBagConstraints();
        gbc_chbChapters.anchor = GridBagConstraints.WEST;
        gbc_chbChapters.insets = new Insets(0, 0, 5, 5);
        gbc_chbChapters.gridx = 0;
        gbc_chbChapters.gridy = 3;
        add(chbChapters, gbc_chbChapters);

        cbChapters = new JComboBox<String>();
        cbChapters.setEnabled(false);
        cbChapters.setModel(new DefaultComboBoxModel<String>(
                new String[] { LanguageManager.getString("general.chapters.remove"),
                        LanguageManager.getString("general.chapters.from.file"),
                        LanguageManager.getString("general.chapters.match.suffix") }));
        cbChapters.setPrototypeDisplayValue(LanguageManager.getString("general.chapters.match.suffix") + "  ");
        GridBagConstraints gbc_cbChapters = new GridBagConstraints();
        gbc_cbChapters.insets = new Insets(0, 0, 5, 0);
        gbc_cbChapters.anchor = GridBagConstraints.WEST;
        gbc_cbChapters.gridx = 1;
        gbc_cbChapters.gridy = 3;
        add(cbChapters, gbc_cbChapters);

        Component verticalStrut7 = Box.createVerticalStrut(35);
        GridBagConstraints gbc_verticalStrut7 = new GridBagConstraints();
        gbc_verticalStrut7.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut7.gridx = 0;
        gbc_verticalStrut7.gridy = 4;
        add(verticalStrut7, gbc_verticalStrut7);

        JPanel pnlChapControlsGeneral = new JPanel();
        GridBagConstraints gbc_pnlChapControlsGeneral = new GridBagConstraints();
        gbc_pnlChapControlsGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_pnlChapControlsGeneral.fill = GridBagConstraints.BOTH;
        gbc_pnlChapControlsGeneral.gridx = 1;
        gbc_pnlChapControlsGeneral.gridy = 4;
        add(pnlChapControlsGeneral, gbc_pnlChapControlsGeneral);
        GridBagLayout gbl_pnlChapControlsGeneral = new GridBagLayout();
        gbl_pnlChapControlsGeneral.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlChapControlsGeneral.rowHeights = new int[] { 0, 0 };
        gbl_pnlChapControlsGeneral.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_pnlChapControlsGeneral.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlChapControlsGeneral.setLayout(gbl_pnlChapControlsGeneral);

        txtChapters = new JTextField();
        txtChapters.setVisible(false);
        GridBagConstraints gbc_txtChapters = new GridBagConstraints();
        gbc_txtChapters.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtChapters.insets = new Insets(0, 0, 8, 5);
        gbc_txtChapters.gridx = 0;
        gbc_txtChapters.gridy = 0;
        pnlChapControlsGeneral.add(txtChapters, gbc_txtChapters);
        txtChapters.setColumns(10);

        btnBrowseChapters = new JButton(LanguageManager.getString("general.browse"));
        btnBrowseChapters.setVisible(false);
        GridBagConstraints gbc_btnBrowseChapters = new GridBagConstraints();
        gbc_btnBrowseChapters.insets = new Insets(0, 5, 10, 5);
        gbc_btnBrowseChapters.anchor = GridBagConstraints.EAST;
        gbc_btnBrowseChapters.gridx = 1;
        gbc_btnBrowseChapters.gridy = 0;
        pnlChapControlsGeneral.add(btnBrowseChapters, gbc_btnBrowseChapters);

        cbExtChapters = new JComboBox<String>();
        cbExtChapters.setVisible(false);
        cbExtChapters.setModel(new DefaultComboBoxModel<String>(new String[] { ".xml", ".txt" }));
        GridBagConstraints gbc_cbExtChapters = new GridBagConstraints();
        gbc_cbExtChapters.insets = new Insets(0, 0, 8, 0);
        gbc_cbExtChapters.gridx = 2;
        gbc_cbExtChapters.gridy = 0;
        pnlChapControlsGeneral.add(cbExtChapters, gbc_cbExtChapters);

        chbTags = new JCheckBox(LanguageManager.getString("general.tags"));
        GridBagConstraints gbc_chbTags = new GridBagConstraints();
        gbc_chbTags.anchor = GridBagConstraints.WEST;
        gbc_chbTags.insets = new Insets(0, 0, 5, 5);
        gbc_chbTags.gridx = 0;
        gbc_chbTags.gridy = 5;
        add(chbTags, gbc_chbTags);

        cbTags = new JComboBox<String>();
        cbTags.setEnabled(false);
        cbTags.setModel(new DefaultComboBoxModel<String>(
                new String[] { LanguageManager.getString("general.chapters.remove"),
                        LanguageManager.getString("general.chapters.from.file"),
                        LanguageManager.getString("general.chapters.match.suffix") }));
        cbTags.setPrototypeDisplayValue(LanguageManager.getString("general.chapters.match.suffix") + "  ");
        GridBagConstraints gbc_cbTags = new GridBagConstraints();
        gbc_cbTags.insets = new Insets(0, 0, 5, 0);
        gbc_cbTags.anchor = GridBagConstraints.WEST;
        gbc_cbTags.gridx = 1;
        gbc_cbTags.gridy = 5;
        add(cbTags, gbc_cbTags);

        Component verticalStrut8 = Box.createVerticalStrut(35);
        GridBagConstraints gbc_verticalStrut8 = new GridBagConstraints();
        gbc_verticalStrut8.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut8.gridx = 0;
        gbc_verticalStrut8.gridy = 6;
        add(verticalStrut8, gbc_verticalStrut8);

        JPanel pnlTagControlsGeneral = new JPanel();
        GridBagConstraints gbc_pnlTagControlsGeneral = new GridBagConstraints();
        gbc_pnlTagControlsGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_pnlTagControlsGeneral.fill = GridBagConstraints.BOTH;
        gbc_pnlTagControlsGeneral.gridx = 1;
        gbc_pnlTagControlsGeneral.gridy = 6;
        add(pnlTagControlsGeneral, gbc_pnlTagControlsGeneral);
        GridBagLayout gbl_pnlTagControlsGeneral = new GridBagLayout();
        gbl_pnlTagControlsGeneral.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlTagControlsGeneral.rowHeights = new int[] { 0, 0 };
        gbl_pnlTagControlsGeneral.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_pnlTagControlsGeneral.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlTagControlsGeneral.setLayout(gbl_pnlTagControlsGeneral);

        txtTags = new JTextField();
        txtTags.setVisible(false);
        txtTags.setColumns(10);
        GridBagConstraints gbc_txtTags = new GridBagConstraints();
        gbc_txtTags.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTags.insets = new Insets(0, 0, 8, 5);
        gbc_txtTags.gridx = 0;
        gbc_txtTags.gridy = 0;
        pnlTagControlsGeneral.add(txtTags, gbc_txtTags);

        btnBrowseTags = new JButton(LanguageManager.getString("general.browse"));
        btnBrowseTags.setVisible(false);
        GridBagConstraints gbc_btnBrowseTags = new GridBagConstraints();
        gbc_btnBrowseTags.insets = new Insets(0, 5, 10, 5);
        gbc_btnBrowseTags.anchor = GridBagConstraints.EAST;
        gbc_btnBrowseTags.gridx = 1;
        gbc_btnBrowseTags.gridy = 0;
        pnlTagControlsGeneral.add(btnBrowseTags, gbc_btnBrowseTags);

        cbExtTags = new JComboBox<String>();
        cbExtTags.setVisible(false);
        cbExtTags.setModel(new DefaultComboBoxModel<String>(new String[] { ".xml", ".txt" }));
        GridBagConstraints gbc_cbExtTags = new GridBagConstraints();
        gbc_cbExtTags.insets = new Insets(0, 0, 8, 0);
        gbc_cbExtTags.gridx = 2;
        gbc_cbExtTags.gridy = 0;
        pnlTagControlsGeneral.add(cbExtTags, gbc_cbExtTags);

        chbExtraCmdGeneral = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
        GridBagConstraints gbc_chbExtraCmdGeneral = new GridBagConstraints();
        gbc_chbExtraCmdGeneral.anchor = GridBagConstraints.WEST;
        gbc_chbExtraCmdGeneral.insets = new Insets(0, 0, 5, 5);
        gbc_chbExtraCmdGeneral.gridx = 0;
        gbc_chbExtraCmdGeneral.gridy = 7;
        add(chbExtraCmdGeneral, gbc_chbExtraCmdGeneral);

        txtExtraCmdGeneral = new JTextField();
        txtExtraCmdGeneral.setEnabled(false);
        GridBagConstraints gbc_txtExtraCmdGeneral = new GridBagConstraints();
        gbc_txtExtraCmdGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_txtExtraCmdGeneral.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtExtraCmdGeneral.gridx = 1;
        gbc_txtExtraCmdGeneral.gridy = 7;
        add(txtExtraCmdGeneral, gbc_txtExtraCmdGeneral);
        txtExtraCmdGeneral.setColumns(10);

        Utils.addRCMenuMouseListener(txtTitleGeneral);
        Utils.addRCMenuMouseListener(txtNumbStartGeneral);
        Utils.addRCMenuMouseListener(txtNumbPadGeneral);
        Utils.addRCMenuMouseListener(txtChapters);
        Utils.addRCMenuMouseListener(txtTags);
        Utils.addRCMenuMouseListener(txtExtraCmdGeneral);
    }

    private void setupListeners() {
        chbTitleGeneral.addActionListener(e -> {
            boolean state = txtTitleGeneral.isEnabled();
            if (txtTitleGeneral.isEnabled() || chbTitleGeneral.isSelected()) {
                txtTitleGeneral.setEnabled(!state);
                chbNumbGeneral.setEnabled(!state);
                if (chbNumbGeneral.isSelected()) {
                    lblNumbStartGeneral.setEnabled(!state);
                    txtNumbStartGeneral.setEnabled(!state);
                    lblNumbPadGeneral.setEnabled(!state);
                    txtNumbPadGeneral.setEnabled(!state);
                    lblNumbExplainGeneral.setEnabled(!state);
                }
            }
        });

        chbNumbGeneral.addActionListener(e -> {
            boolean state = txtNumbStartGeneral.isEnabled();
            lblNumbStartGeneral.setEnabled(!state);
            txtNumbStartGeneral.setEnabled(!state);
            lblNumbPadGeneral.setEnabled(!state);
            txtNumbPadGeneral.setEnabled(!state);
            lblNumbExplainGeneral.setEnabled(!state);
        });

        txtNumbStartGeneral.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    if (Integer.parseInt(txtNumbStartGeneral.getText()) < 0) {
                        txtNumbStartGeneral.setText("1");
                    }
                } catch (NumberFormatException e1) {
                    txtNumbStartGeneral.setText("1");
                }
            }
        });

        txtNumbPadGeneral.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    if (Integer.parseInt(txtNumbPadGeneral.getText()) < 0) {
                        txtNumbPadGeneral.setText("1");
                    }
                } catch (NumberFormatException e1) {
                    txtNumbPadGeneral.setText("1");
                }
            }
        });

        chbChapters.addActionListener(e -> {
            boolean state = cbChapters.isEnabled();
            cbChapters.setEnabled(!state);
            if (cbChapters.getSelectedIndex() == 1) {
                txtChapters.setEditable(false);
                txtChapters.setVisible(true);
                txtChapters.setEnabled(!state);
                btnBrowseChapters.setVisible(true);
                btnBrowseChapters.setEnabled(!state);
                cbExtChapters.setVisible(false);
            } else if (cbChapters.getSelectedIndex() == 2) {
                txtChapters.setEditable(true);
                txtChapters.setVisible(true);
                txtChapters.setEnabled(!state);
                btnBrowseChapters.setVisible(false);
                btnBrowseChapters.setEnabled(!state);
                cbExtChapters.setVisible(true);
                cbExtChapters.setEnabled(!state);
            } else if (!chbChapters.isSelected()) {
                txtChapters.setVisible(false);
                btnBrowseChapters.setVisible(false);
                cbExtChapters.setVisible(false);
            }
        });

        cbChapters.addActionListener(e -> {
            if (cbChapters.getSelectedIndex() == 0) {
                txtChapters.setVisible(false);
                btnBrowseChapters.setVisible(false);
                cbExtChapters.setVisible(false);
            } else if (cbChapters.getSelectedIndex() == 1) {
                txtChapters.setText("");
                txtChapters.setEditable(false);
                txtChapters.setVisible(true);
                btnBrowseChapters.setVisible(true);
                cbExtChapters.setVisible(false);
            } else {
                txtChapters.setText("-chapters");
                txtChapters.setEditable(true);
                txtChapters.setVisible(true);
                btnBrowseChapters.setVisible(false);
                cbExtChapters.setVisible(true);
            }
        });

        btnBrowseChapters.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.chapters"));
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.resetChoosableFileFilters();
            chooser.setFileFilter(TXT_EXT_FILTER);
            chooser.setFileFilter(XML_EXT_FILTER);
            int open = chooser.showOpenDialog(parentFrame);
            if (open == JFileChooser.APPROVE_OPTION) {
                if (chooser.getSelectedFile().exists()) {
                    txtChapters.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        chbTags.addActionListener(e -> {
            boolean state = cbTags.isEnabled();
            cbTags.setEnabled(!state);
            if (cbTags.getSelectedIndex() == 1) {
                txtTags.setEditable(false);
                txtTags.setVisible(true);
                txtTags.setEnabled(!state);
                btnBrowseTags.setVisible(true);
                btnBrowseTags.setEnabled(!state);
                cbExtTags.setVisible(false);
            } else if (cbTags.getSelectedIndex() == 2) {
                txtTags.setEditable(true);
                txtTags.setVisible(true);
                txtTags.setEnabled(!state);
                btnBrowseTags.setVisible(false);
                btnBrowseTags.setEnabled(!state);
                cbExtTags.setVisible(true);
                cbExtTags.setEnabled(!state);
            } else if (!chbTags.isSelected()) {
                txtTags.setVisible(false);
                btnBrowseTags.setVisible(false);
                cbExtTags.setVisible(false);
            }
        });

        cbTags.addActionListener(e -> {
            if (cbTags.getSelectedIndex() == 0) {
                txtTags.setVisible(false);
                btnBrowseTags.setVisible(false);
                cbExtTags.setVisible(false);
            } else if (cbTags.getSelectedIndex() == 1) {
                txtTags.setText("");
                txtTags.setEditable(false);
                txtTags.setVisible(true);
                btnBrowseTags.setVisible(true);
                cbExtTags.setVisible(false);
            } else {
                txtTags.setText("-tags");
                txtTags.setEditable(true);
                txtTags.setVisible(true);
                btnBrowseTags.setVisible(false);
                cbExtTags.setVisible(true);
            }
        });

        btnBrowseTags.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.tags"));
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.resetChoosableFileFilters();
            chooser.setFileFilter(TXT_EXT_FILTER);
            chooser.setFileFilter(XML_EXT_FILTER);
            int open = chooser.showOpenDialog(parentFrame);
            if (open == JFileChooser.APPROVE_OPTION) {
                if (chooser.getSelectedFile().exists()) {
                    txtTags.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        chbExtraCmdGeneral.addActionListener(e -> {
            boolean state = txtExtraCmdGeneral.isEnabled();
            txtExtraCmdGeneral.setEnabled(!state);
        });
    }

    public void updateCmdLines(int fileCount, List<String> filePaths) {
        cmdLineGeneral = new String[fileCount];
        cmdLineGeneralOpt = new String[fileCount];
        int start = Integer.parseInt(txtNumbStartGeneral.getText());

        for (int i = 0; i < fileCount; i++) {
            cmdLineGeneral[i] = "";
            cmdLineGeneralOpt[i] = "";

            if (chbTags.isSelected()) {
                switch (cbTags.getSelectedIndex()) {
                    case 0:
                        cmdLineGeneral[i] += " --tags all:";
                        cmdLineGeneralOpt[i] += " --tags all:";
                        break;
                    case 1:
                        if (txtTags.getText().trim().isEmpty()) {
                            cmdLineGeneral[i] += " --tags all:";
                            cmdLineGeneralOpt[i] += " --tags all:";
                        } else {
                            if (Utils.isWindows()) {
                                cmdLineGeneral[i] += " --tags all:\"" + txtTags.getText() + "\"";
                                cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(txtTags.getText()) + "\"";
                            } else {
                                cmdLineGeneral[i] += " --tags all:\"" + Utils.escapeQuotes(txtTags.getText()) + "\"";
                                cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(txtTags.getText()) + "\"";
                            }
                        }
                        break;
                    case 2:
                        String tmpTags = Utils.getPathWithoutExt(filePaths.get(i)) + txtTags.getText()
                                + cbExtTags.getSelectedItem();
                        if (Utils.isWindows()) {
                            cmdLineGeneral[i] += " --tags all:\"" + tmpTags + "\"";
                            cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(tmpTags) + "\"";
                        } else {
                            cmdLineGeneral[i] += " --tags all:\"" + Utils.escapeQuotes(tmpTags) + "\"";
                            cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(tmpTags) + "\"";
                        }
                        break;
                }
            }

            if (chbChapters.isSelected()) {
                switch (cbChapters.getSelectedIndex()) {
                    case 0:
                        cmdLineGeneral[i] += " --chapters \"\"";
                        cmdLineGeneralOpt[i] += " --chapters ''";
                        break;
                    case 1:
                        if (txtChapters.getText().trim().isEmpty()) {
                            cmdLineGeneral[i] += " --chapters \"\"";
                            cmdLineGeneralOpt[i] += " --chapters ''";
                        } else {
                            if (Utils.isWindows()) {
                                cmdLineGeneral[i] += " --chapters \"" + txtChapters.getText() + "\"";
                                cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText()) + "\"";
                            } else {
                                cmdLineGeneral[i] += " --chapters \"" + Utils.escapeQuotes(txtChapters.getText()) + "\"";
                                cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText()) + "\"";
                            }
                        }
                        break;
                    case 2:
                        String tmpChaps = Utils.getPathWithoutExt(filePaths.get(i)) + txtChapters.getText()
                                + cbExtChapters.getSelectedItem();
                        if (Utils.isWindows()) {
                            cmdLineGeneral[i] += " --chapters \"" + tmpChaps + "\"";
                            cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(tmpChaps) + "\"";
                        } else {
                            cmdLineGeneral[i] += " --chapters \"" + Utils.escapeQuotes(tmpChaps) + "\"";
                            cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(tmpChaps) + "\"";
                        }
                        break;
                }
            }

            if (chbTitleGeneral.isSelected()) {
                cmdLineGeneral[i] += " --edit info";
                cmdLineGeneralOpt[i] += " --edit info";

                String newTitle = txtTitleGeneral.getText();

                if (chbNumbGeneral.isSelected()) {
                    int pad = Integer.parseInt(txtNumbPadGeneral.getText());
                    newTitle = newTitle.replace("{num}", Utils.padNumber(pad, start));
                    start++;
                }

                newTitle = newTitle.replace("{file_name}", Utils.getFileNameWithoutExt(filePaths.get(i)));

                cmdLineGeneral[i] += " --set title=\"" + Utils.escapeQuotes(newTitle) + "\"";
                cmdLineGeneralOpt[i] += " --set title=\"" + Utils.escapeName(newTitle) + "\"";
            }

            if (chbExtraCmdGeneral.isSelected() && !txtExtraCmdGeneral.getText().trim().isEmpty()) {
                cmdLineGeneral[i] += " " + txtExtraCmdGeneral.getText();
                cmdLineGeneralOpt[i] += " " + Utils.escapeName(txtExtraCmdGeneral.getText());
            }
        }
    }

    public String[] getCmdLineGeneral() {
        return cmdLineGeneral;
    }

    public String[] getCmdLineGeneralOpt() {
        return cmdLineGeneralOpt;
    }
}
