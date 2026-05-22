/*
 * Copyright (c) 2012-2018 Bruno Barbieri
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package io.github.brunorex;

import io.github.brunorex.profiles.ProfileManager;
import io.github.brunorex.profiles.ProfileManager.ProfileType;
import io.github.brunorex.profiles.TrackProfile;

import java.util.Locale;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class JMkvpropedit {

    private static final String VERSION_NUMBER = "v2.4.0";
    private static final int MAX_STREAMS = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(JMkvpropedit.class);
    private static String[] argsArray;

    private SwingWorker<Void, Void> worker = null;

    private File iniFile = new File("JMkvpropedit.ini");
    private IniPersistenceService iniService = new IniPersistenceService(iniFile);
    private static final MkvStrings mkvStrings = new MkvStrings();

    private JFileChooser chooser; // Lazy initialization for faster startup

    private JFileChooser getChooser() {
        if (chooser == null) {
            chooser = new JFileChooser(System.getProperty("user.home")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void approveSelection() {
                    if (!super.isMultiSelectionEnabled() || super.getSelectedFiles().length == 1) {
                        if (!this.getSelectedFile().exists()) {
                            return;
                        }
                    }

                    super.approveSelection();
                }
            };
        }
        return chooser;
    }

    private FileFilter EXE_EXT_FILTER = new FileNameExtensionFilter("Excecutable files (*.exe)", "exe");

    private FileFilter MATROSKA_EXT_FILTER = new FileNameExtensionFilter(
            "Matroska files (*.mkv; *.mka; *.mk3d; *.webm; *.mks)", "mkv", "mka", "mk3d", "webm", "mks");

    private IOFileFilter MATROSKA_FILE_FILTER = WildcardFileFilter.builder()
            .setWildcards("*.mkv", "*.mka", "*.mk3d", ".webm", ".mks")
            .setIoCase(IOCase.INSENSITIVE)
            .get();

    private FileFilter TXT_EXT_FILTER = new FileNameExtensionFilter("Plain text files (*.txt)", "txt");

    private FileFilter XML_EXT_FILTER = new FileNameExtensionFilter("XML files (*.xml)", "xml");

    private String[] cmdLineGeneral = null;
    private String[] cmdLineGeneralOpt = null;

    // Track tabs (refactored into TrackTabPanel)
    private TrackTabPanel videoTabPanel;
    private TrackTabPanel audioTabPanel;
    private TrackTabPanel subtitleTabPanel;

    // Attachments tab (refactored into AttachmentsTabPanel)
    private AttachmentsTabPanel attachmentsTabPanel;

    private List<String> cmdLineBatch = null;
    private List<String> cmdLineBatchOpt = null;

    // Window controls
    private Dimension frmJMkvpropeditDim = new Dimension(0, 0);
    private JFrame frmJMkvpropedit;
    private JTabbedPane pnlTabs;
    private JButton btnProcessFiles;
    private JButton btnGenerateCmdLine;

    // Input tab controls
    private DefaultListModel<String> modelFiles;
    private JList<String> listFiles;
    private JButton btnAddFiles;
    private JButton btnAddFolder;
    private JButton btnRemoveFiles;
    private JButton btnTopFiles;
    private JButton btnUpFiles;
    private JButton btnDownFiles;
    private JButton btnBottomFiles;
    private JButton btnClearFiles;

    // General tab controls
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

    private ProfileManager profileManager;

    // Option tab controls
    private JPanel pnlOptions;
    private JTextField txtMkvPropExe;
    private JCheckBox chbMkvPropExeDef;
    private JComboBox<String> cbLanguage;
    private JButton btnApplyLanguage;

    // Output tab controls
    private JTextArea txtOutput;

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    argsArray = args;
                    // Apply FlatLaf with system theme detection
                    applyFlatLafTheme(null);
                    JMkvpropedit window = new JMkvpropedit();
                    window.frmJMkvpropedit.setVisible(true);
                } catch (Exception e) {
                    LOGGER.error("Error starting application", e);
                }
            }
        });
    }

    /**
     * Applies FlatLaf theme. If theme is null, reads from INI or uses system default.
     */
    private static void applyFlatLafTheme(String theme) {
        try {
            if (theme == null) {
                // Try to read from INI
                theme = new IniPersistenceService(new File("JMkvpropedit.ini")).getTheme(null);
            }

            if ("dark".equalsIgnoreCase(theme)) {
                com.formdev.flatlaf.FlatDarkLaf.setup();
            } else {
                // Default to light theme (includes "light" and "system" / unknown)
                com.formdev.flatlaf.FlatLightLaf.setup();
            }
        } catch (Exception e) {
            // Fallback to system LAF if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.error("Failed to set fallback Look and Feel", ex);
            }
        }
    }

    /**
     * Switches the FlatLaf theme at runtime and persists the preference to INI.
     */
    private void switchTheme(String theme) {
        applyFlatLafTheme(theme);
        com.formdev.flatlaf.FlatLaf.updateUI();
        iniService.saveTheme(theme);
    }

    /**
     * Create the application.
     */
    public JMkvpropedit() {
        initialize();
        parseFiles(argsArray);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        loadLanguage();
        frmJMkvpropedit = new JFrame();
        frmJMkvpropedit.setTitle("JMKVPropedit++ " + VERSION_NUMBER);
        frmJMkvpropedit.setBounds(100, 100, 760, 500);
        frmJMkvpropedit.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Theme menu
        JMenuBar menuBar = new JMenuBar();
        JMenu mnOptions = new JMenu("Options");
        JMenu mnTheme = new JMenu("Theme");
        JMenuItem mntmSystem = new JMenuItem("System");
        JMenuItem mntmLight = new JMenuItem("Light");
        JMenuItem mntmDark = new JMenuItem("Dark");

        mntmSystem.addActionListener(e -> switchTheme("system"));
        mntmLight.addActionListener(e -> switchTheme("light"));
        mntmDark.addActionListener(e -> switchTheme("dark"));

        mnTheme.add(mntmSystem);
        mnTheme.add(mntmLight);
        mnTheme.add(mntmDark);
        mnOptions.add(mnTheme);
        menuBar.add(mnOptions);
        frmJMkvpropedit.setJMenuBar(menuBar);

        getChooser().setDialogType(JFileChooser.OPEN_DIALOG);
        getChooser().setFileHidingEnabled(true);

        pnlTabs = new JTabbedPane(JTabbedPane.TOP);
        pnlTabs.setBorder(new EmptyBorder(10, 10, 0, 10));
        frmJMkvpropedit.getContentPane().add(pnlTabs, BorderLayout.CENTER);

        JPanel pnlInput = new JPanel();
        pnlInput.setBorder(new EmptyBorder(10, 10, 10, 0));
        pnlTabs.addTab(LanguageManager.getString("tab.input"), null, pnlInput, null);
        pnlInput.setLayout(new BorderLayout(0, 0));

        JScrollPane spFiles = new JScrollPane();
        spFiles.setViewportBorder(null);
        pnlInput.add(spFiles);

        modelFiles = new DefaultListModel<String>();
        listFiles = new JList<String>(modelFiles);
        spFiles.setViewportView(listFiles);

        JPanel pnlListToolbar = new JPanel();
        pnlListToolbar.setBorder(new EmptyBorder(0, 5, 0, 5));
        pnlInput.add(pnlListToolbar, BorderLayout.EAST);
        pnlListToolbar.setLayout(new BoxLayout(pnlListToolbar, BoxLayout.Y_AXIS));

        btnAddFiles = new JButton("");
        btnAddFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
        btnAddFiles.setMargin(new Insets(0, 0, 0, 0));
        btnAddFiles.setBorderPainted(false);
        btnAddFiles.setContentAreaFilled(false);
        btnAddFiles.setFocusPainted(false);
        btnAddFiles.setOpaque(false);
        btnAddFiles.setToolTipText("Add files");
        pnlListToolbar.add(btnAddFiles);

        Component verticalStrut1 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut1);

        btnAddFolder = new JButton("");
        btnAddFolder.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add-folder.png")));
        btnAddFolder.setMargin(new Insets(0, 0, 0, 0));
        btnAddFolder.setBorderPainted(false);
        btnAddFolder.setContentAreaFilled(false);
        btnAddFolder.setFocusPainted(false);
        btnAddFolder.setOpaque(false);
        btnAddFolder.setToolTipText("Add folder");
        pnlListToolbar.add(btnAddFolder);

        Component verticalStrut1b = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut1b);

        btnRemoveFiles = new JButton("");
        btnRemoveFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
        btnRemoveFiles.setMargin(new Insets(0, 0, 0, 0));
        btnRemoveFiles.setBorderPainted(false);
        btnRemoveFiles.setContentAreaFilled(false);
        btnRemoveFiles.setFocusPainted(false);
        btnRemoveFiles.setOpaque(false);
        btnRemoveFiles.setToolTipText("Remove selected files");
        pnlListToolbar.add(btnRemoveFiles);

        Component verticalStrut2 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut2);

        btnTopFiles = new JButton("");
        btnTopFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-top.png")));
        btnTopFiles.setMargin(new Insets(0, 0, 0, 0));
        btnTopFiles.setBorderPainted(false);
        btnTopFiles.setContentAreaFilled(false);
        btnTopFiles.setFocusPainted(false);
        btnTopFiles.setOpaque(false);
        btnTopFiles.setToolTipText("Move selected files to the top");
        pnlListToolbar.add(btnTopFiles);

        Component verticalStrut3 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut3);

        btnUpFiles = new JButton("");
        btnUpFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-up.png")));
        btnUpFiles.setMargin(new Insets(0, 0, 0, 0));
        btnUpFiles.setBorderPainted(false);
        btnUpFiles.setContentAreaFilled(false);
        btnUpFiles.setFocusPainted(false);
        btnUpFiles.setOpaque(false);
        btnUpFiles.setToolTipText("Move selected files up");
        pnlListToolbar.add(btnUpFiles);

        Component verticalStrut4 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut4);

        btnDownFiles = new JButton("");
        btnDownFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-down.png")));
        btnDownFiles.setMargin(new Insets(0, 0, 0, 0));
        btnDownFiles.setBorderPainted(false);
        btnDownFiles.setContentAreaFilled(false);
        btnDownFiles.setFocusPainted(false);
        btnDownFiles.setOpaque(false);
        btnDownFiles.setToolTipText("Move selected files down");
        pnlListToolbar.add(btnDownFiles);

        Component verticalStrut5 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut5);

        btnBottomFiles = new JButton("");
        btnBottomFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-bottom.png")));
        btnBottomFiles.setMargin(new Insets(0, 0, 0, 0));
        btnBottomFiles.setBorderPainted(false);
        btnBottomFiles.setContentAreaFilled(false);
        btnBottomFiles.setFocusPainted(false);
        btnBottomFiles.setOpaque(false);
        btnBottomFiles.setToolTipText("Move selected files to the bottom");
        pnlListToolbar.add(btnBottomFiles);

        Component verticalStrut6 = Box.createVerticalStrut(10);
        pnlListToolbar.add(verticalStrut6);

        btnClearFiles = new JButton("");
        btnClearFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/edit-clear.png")));
        btnClearFiles.setMargin(new Insets(0, 0, 0, 0));
        btnClearFiles.setBorderPainted(false);
        btnClearFiles.setContentAreaFilled(false);
        btnClearFiles.setFocusPainted(false);
        btnClearFiles.setOpaque(false);
        btnClearFiles.setToolTipText(LanguageManager.getString("input.clear.tooltip"));
        pnlListToolbar.add(btnClearFiles);

        JPanel pnlGeneral = new JPanel();
        pnlGeneral.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("tab.general"), null, pnlGeneral, null);
        GridBagLayout gbl_pnlGeneral = new GridBagLayout();
        gbl_pnlGeneral.columnWidths = new int[] { 75, 655, 0 };
        gbl_pnlGeneral.rowHeights = new int[] { 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0 };
        gbl_pnlGeneral.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_pnlGeneral.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                Double.MIN_VALUE };
        pnlGeneral.setLayout(gbl_pnlGeneral);

        chbTitleGeneral = new JCheckBox(LanguageManager.getString("general.title"));
        GridBagConstraints gbc_chbTitleGeneral = new GridBagConstraints();
        gbc_chbTitleGeneral.insets = new Insets(0, 0, 5, 5);
        gbc_chbTitleGeneral.anchor = GridBagConstraints.WEST;
        gbc_chbTitleGeneral.gridx = 0;
        gbc_chbTitleGeneral.gridy = 0;
        pnlGeneral.add(chbTitleGeneral, gbc_chbTitleGeneral);

        txtTitleGeneral = new JTextField();
        txtTitleGeneral.setEnabled(false);
        GridBagConstraints gbc_txtTitleGeneral = new GridBagConstraints();
        gbc_txtTitleGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_txtTitleGeneral.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtTitleGeneral.gridx = 1;
        gbc_txtTitleGeneral.gridy = 0;
        pnlGeneral.add(txtTitleGeneral, gbc_txtTitleGeneral);
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
        pnlGeneral.add(pnlNumbControlsGeneral, gbc_pnlNumbControlsGeneral);

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
        pnlGeneral.add(lblNumbExplainGeneral, gbc_lblNumbExplainGeneral);

        chbChapters = new JCheckBox(LanguageManager.getString("general.chapters"));
        GridBagConstraints gbc_chbChapters = new GridBagConstraints();
        gbc_chbChapters.anchor = GridBagConstraints.WEST;
        gbc_chbChapters.insets = new Insets(0, 0, 5, 5);
        gbc_chbChapters.gridx = 0;
        gbc_chbChapters.gridy = 3;
        pnlGeneral.add(chbChapters, gbc_chbChapters);

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
        pnlGeneral.add(cbChapters, gbc_cbChapters);

        Component verticalStrut7 = Box.createVerticalStrut(35);
        GridBagConstraints gbc_verticalStrut7 = new GridBagConstraints();
        gbc_verticalStrut7.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut7.gridx = 0;
        gbc_verticalStrut7.gridy = 4;
        pnlGeneral.add(verticalStrut7, gbc_verticalStrut7);

        JPanel pnlChapControlsGeneral = new JPanel();
        GridBagConstraints gbc_pnlChapControlsGeneral = new GridBagConstraints();
        gbc_pnlChapControlsGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_pnlChapControlsGeneral.fill = GridBagConstraints.BOTH;
        gbc_pnlChapControlsGeneral.gridx = 1;
        gbc_pnlChapControlsGeneral.gridy = 4;
        pnlGeneral.add(pnlChapControlsGeneral, gbc_pnlChapControlsGeneral);
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
        pnlGeneral.add(chbTags, gbc_chbTags);

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
        pnlGeneral.add(cbTags, gbc_cbTags);

        Component verticalStrut8 = Box.createVerticalStrut(35);
        GridBagConstraints gbc_verticalStrut8 = new GridBagConstraints();
        gbc_verticalStrut8.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut8.gridx = 0;
        gbc_verticalStrut8.gridy = 6;
        pnlGeneral.add(verticalStrut8, gbc_verticalStrut8);

        JPanel pnlTagControlsGeneral = new JPanel();
        GridBagConstraints gbc_pnlTagControlsGeneral = new GridBagConstraints();
        gbc_pnlTagControlsGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_pnlTagControlsGeneral.fill = GridBagConstraints.BOTH;
        gbc_pnlTagControlsGeneral.gridx = 1;
        gbc_pnlTagControlsGeneral.gridy = 6;
        pnlGeneral.add(pnlTagControlsGeneral, gbc_pnlTagControlsGeneral);
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
        pnlGeneral.add(chbExtraCmdGeneral, gbc_chbExtraCmdGeneral);

        txtExtraCmdGeneral = new JTextField();
        txtExtraCmdGeneral.setEnabled(false);
        GridBagConstraints gbc_txtExtraCmdGeneral = new GridBagConstraints();
        gbc_txtExtraCmdGeneral.insets = new Insets(0, 0, 5, 0);
        gbc_txtExtraCmdGeneral.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtExtraCmdGeneral.gridx = 1;
        gbc_txtExtraCmdGeneral.gridy = 7;
        pnlGeneral.add(txtExtraCmdGeneral, gbc_txtExtraCmdGeneral);
        txtExtraCmdGeneral.setColumns(10);

        videoTabPanel = new TrackTabPanel(frmJMkvpropedit,
                LanguageManager.getString("track.video.title"), "v",
                ProfileType.VIDEO, profileManager);
        pnlTabs.addTab(LanguageManager.getString("video.tab.title"), null, videoTabPanel, null);

        audioTabPanel = new TrackTabPanel(frmJMkvpropedit,
                LanguageManager.getString("track.audio.title"), "a",
                ProfileType.AUDIO, profileManager);
        pnlTabs.addTab(LanguageManager.getString("audio.tab.title"), null, audioTabPanel, null);

        subtitleTabPanel = new TrackTabPanel(frmJMkvpropedit,
                LanguageManager.getString("track.subtitle.title"), "s",
                ProfileType.SUBTITLE, profileManager);
        pnlTabs.addTab(LanguageManager.getString("subtitle.tab.title"), null, subtitleTabPanel, null);

        attachmentsTabPanel = new AttachmentsTabPanel(frmJMkvpropedit, getChooser(), mkvStrings);
        pnlTabs.addTab(LanguageManager.getString("attachments.tab.title"), null, attachmentsTabPanel, null);

        pnlOptions = new JPanel();
        pnlOptions.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("tab.options"), null, pnlOptions, null);
        GridBagLayout gbl_pnlOptions = new GridBagLayout();
        gbl_pnlOptions.columnWidths = new int[] { 0, 0, 0 };
        gbl_pnlOptions.rowHeights = new int[] { 0, 0, 0, 0, 0 };
        gbl_pnlOptions.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_pnlOptions.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        pnlOptions.setLayout(gbl_pnlOptions);

        JLabel lblMkvPropExe = new JLabel(LanguageManager.getString("options.executable"));
        lblMkvPropExe.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        pnlOptions.add(lblMkvPropExe, gbc_label);

        txtMkvPropExe = new JTextField("mkvpropedit");
        txtMkvPropExe.setEditable(false);
        txtMkvPropExe.setColumns(10);
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.insets = new Insets(0, 0, 5, 0);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 1;
        gbc_textField.gridy = 0;
        pnlOptions.add(txtMkvPropExe, gbc_textField);

        JPanel pnlMkvPropExeControls = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 1;
        pnlOptions.add(pnlMkvPropExeControls, gbc_panel);
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

        JButton btnBrowseMkvPropExe = new JButton(LanguageManager.getString("button.browse"));
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

        // Download button action
        btnDownloadMkvPropExe.addActionListener(e -> {
            btnDownloadMkvPropExe.setEnabled(false);
            progressDownloadMkv.setVisible(true);
            progressDownloadMkv.setValue(0);
            progressDownloadMkv.setString(LanguageManager.getString("options.downloading"));

            // Target directory: ./mkvtools/
            File targetDir = new File(System.getProperty("user.dir"), "mkvtools");

            MkvToolsDownloader downloader = new MkvToolsDownloader(
                    targetDir,
                    status -> javax.swing.SwingUtilities.invokeLater(() -> progressDownloadMkv.setString(status)),
                    progress -> javax.swing.SwingUtilities.invokeLater(() -> progressDownloadMkv.setValue(progress)),
                    error -> javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDownloadMkv.setVisible(false);
                        btnDownloadMkvPropExe.setEnabled(true);
                        JOptionPane.showMessageDialog(frmJMkvpropedit,
                                LanguageManager.getString("options.download.error") + ": " + error,
                                "", JOptionPane.ERROR_MESSAGE);
                    }),
                    () -> javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDownloadMkv.setVisible(false);
                        btnDownloadMkvPropExe.setEnabled(true);

                        // Update path with relative path
                        File mkvpropedit = new File(targetDir, "mkvpropedit.exe");
                        if (mkvpropedit.exists()) {
                            txtMkvPropExe.setText("mkvtools" + File.separator + "mkvpropedit.exe");
                            chbMkvPropExeDef.setSelected(false);
                        }

                        JOptionPane.showMessageDialog(frmJMkvpropedit,
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
        pnlOptions.add(lblLanguage, gbc_lblLanguage);

        JPanel pnlLanguageControls = new JPanel();
        GridBagConstraints gbc_pnlLanguageControls = new GridBagConstraints();
        gbc_pnlLanguageControls.insets = new Insets(0, 0, 5, 0);
        gbc_pnlLanguageControls.fill = GridBagConstraints.BOTH;
        gbc_pnlLanguageControls.gridx = 1;
        gbc_pnlLanguageControls.gridy = 2;
        pnlOptions.add(pnlLanguageControls, gbc_pnlLanguageControls);
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

        btnApplyLanguage = new JButton(LanguageManager.getString("button.apply"));
        btnApplyLanguage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) cbLanguage.getSelectedItem();
                String langCode = "en";
                if ("Español".equals(selected)) {
                    langCode = "es";
                }
                var attachTable = attachmentsTabPanel.getTableAdd();
                int[] widths = new int[attachTable.getColumnCount()];
                for (int i = 0; i < attachTable.getColumnCount(); i++) {
                    widths[i] = attachTable.getColumnModel().getColumn(i).getPreferredWidth();
                }

                saveLanguage(langCode);

                // Instant reload
                frmJMkvpropedit.dispose();
                // Re-initialize logic
                LanguageManager.setLocale(Locale.forLanguageTag(langCode));
                initialize();
                frmJMkvpropedit.setVisible(true);

                if (widths.length == attachTable.getColumnCount()) {
                    for (int i = 0; i < attachTable.getColumnCount(); i++) {
                        attachTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
                    }
                }
                // Restore logic that was in windowOpened
                readIniFile();
                // Tracks are added empty by default initialization or read from INI?
                // WindowOpened calls addVideoTrack, etc. We should replicate that or rely on
                // windowOpened if it fires.
                // Since we created a new JFrame and setVisible(true), windowOpened should fire
                // again?
                // Actually, windowOpened listener is added in initialize().
                // So setVisible(true) will trigger it.
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
        pnlOptions.add(pnlOptionsFiller, gbc_pnlOptionsFiller);

        JPanel pnlOutput = new JPanel();
        pnlOutput.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("tab.output"), null, pnlOutput, null);
        pnlOutput.setLayout(new BorderLayout(0, 0));

        JScrollPane spOutput = new JScrollPane();
        pnlOutput.add(spOutput, BorderLayout.CENTER);

        txtOutput = new JTextArea();
        txtOutput.setLineWrap(true);
        txtOutput.setEditable(false);
        spOutput.setViewportView(txtOutput);

        JPanel pnlButtons = new JPanel();
        frmJMkvpropedit.getContentPane().add(pnlButtons, BorderLayout.SOUTH);

        btnProcessFiles = new JButton(LanguageManager.getString("button.process"));
        pnlButtons.add(btnProcessFiles);

        btnGenerateCmdLine = new JButton(LanguageManager.getString("button.generate.cmd"));
        pnlButtons.add(btnGenerateCmdLine);

        /* Start of mouse events for right-click menu */

        Utils.addRCMenuMouseListener(txtTitleGeneral);
        Utils.addRCMenuMouseListener(txtNumbStartGeneral);
        Utils.addRCMenuMouseListener(txtNumbPadGeneral);
        Utils.addRCMenuMouseListener(txtChapters);
        Utils.addRCMenuMouseListener(txtTags);
        Utils.addRCMenuMouseListener(txtExtraCmdGeneral);
        Utils.addRCMenuMouseListener(txtMkvPropExe);
        Utils.addRCMenuMouseListener(txtOutput);

        /* End of mouse events for right-click menu */

        frmJMkvpropedit.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Resize the window to make sure the components fit
                // frmJMkvpropedit.pack();

                // Don't allow the window to be resized to a dimension smaller than the original
                frmJMkvpropedit.setMinimumSize(new Dimension(frmJMkvpropedit.getWidth(), frmJMkvpropedit.getHeight()));

                // Center the window on the screen
                frmJMkvpropedit.setLocationRelativeTo(null);

                readIniFile();
                videoTabPanel.addTrack();
                audioTabPanel.addTrack();
                subtitleTabPanel.addTrack();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                boolean wRunning;

                try {
                    wRunning = !worker.isDone();
                } catch (Exception e1) {
                    wRunning = false;
                }

                if (wRunning) {
                    int choice = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("confirm.exit.msg"),
                            LanguageManager.getString("confirm.exit.title"),
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        worker.cancel(true);
                        frmJMkvpropedit.dispose();
                        System.exit(0);
                    }
                } else {
                    frmJMkvpropedit.dispose();
                    System.exit(0);
                }
            }
        });

        frmJMkvpropedit.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Check if window width changed before resizing columns
                if (frmJMkvpropedit.getWidth() != frmJMkvpropeditDim.getWidth()) {
                    resizeColumns(attachmentsTabPanel.getTableAdd(), attachmentsTabPanel.getColumnSizesAdd());
                    resizeColumns(attachmentsTabPanel.getTableReplace(), attachmentsTabPanel.getColumnSizesReplace());
                    resizeColumns(attachmentsTabPanel.getTableDelete(), attachmentsTabPanel.getColumnSizesDelete());
                }

                // Store new dimensions
                frmJMkvpropeditDim = new Dimension(frmJMkvpropedit.getWidth(), frmJMkvpropedit.getHeight());
            }
        });

        new FileDrop(listFiles, new FileDrop.Listener() {
            public void filesDropped(File[] files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        addMkvFilesFromFolder(files[i]);
                    } else {
                        addFile(files[i], true);
                    }
                }
            }
        });

        btnAddFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File[] files = null;

                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.file"));
                getChooser().setMultiSelectionEnabled(true);
                getChooser().setAcceptAllFileFilterUsed(false);
                getChooser().resetChoosableFileFilters();
                getChooser().setFileFilter(MATROSKA_EXT_FILTER);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    files = getChooser().getSelectedFiles();
                    for (int i = 0; i < files.length; i++) {
                        try {
                            if (!modelFiles.contains(files[i].getCanonicalPath()) && files[i].exists()) {
                                modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
                            }
                        } catch (IOException e1) {
                        }
                    }
                }

            }
        });

        btnAddFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File folder = null;

                getChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.folder"));
                getChooser().setAcceptAllFileFilterUsed(false);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    folder = getChooser().getSelectedFile();
                    addMkvFilesFromFolder(folder);
                }

            }
        });

        btnRemoveFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (modelFiles.getSize() > 0) {
                    while (listFiles.getSelectedIndex() != -1) {
                        int[] idx = listFiles.getSelectedIndices();
                        modelFiles.remove(idx[0]);
                    }
                }
            }
        });

        btnClearFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modelFiles.removeAllElements();
            }
        });

        btnTopFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] idx = listFiles.getSelectedIndices();

                for (int i = 0; i < idx.length; i++) {
                    int pos = idx[i];

                    if (pos > 0) {
                        String temp = modelFiles.remove(pos);
                        modelFiles.add(i, temp);
                        listFiles.ensureIndexIsVisible(0);
                        idx[i] = i;
                    }
                }

                listFiles.setSelectedIndices(idx);
            }
        });

        btnUpFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] idx = listFiles.getSelectedIndices();

                for (int i = 0; i < idx.length; i++) {
                    int pos = idx[i];

                    if (pos > 0 && listFiles.getMinSelectionIndex() != 0) {
                        String temp = modelFiles.remove(pos);
                        modelFiles.add(pos - 1, temp);
                        listFiles.ensureIndexIsVisible(pos - 1);
                        idx[i]--;
                    }
                }

                listFiles.setSelectedIndices(idx);
            }
        });

        btnDownFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] idx = listFiles.getSelectedIndices();

                for (int i = idx.length - 1; i > -1; i--) {
                    int pos = idx[i];

                    if (pos < modelFiles.getSize() - 1
                            && listFiles.getMaxSelectionIndex() != modelFiles.getSize() - 1) {
                        String temp = modelFiles.remove(pos);
                        modelFiles.add(pos + 1, temp);
                        listFiles.ensureIndexIsVisible(pos + 1);
                        idx[i]++;
                    }
                }

                listFiles.setSelectedIndices(idx);
            }
        });

        btnBottomFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] idx = listFiles.getSelectedIndices();
                int j = 0;

                for (int i = idx.length - 1; i > -1; i--) {
                    int pos = idx[i];

                    if (pos < modelFiles.getSize()) {
                        String temp = modelFiles.remove(pos);
                        modelFiles.add(modelFiles.getSize() - j, temp);
                        j++;
                        listFiles.ensureIndexIsVisible(modelFiles.getSize() - 1);
                        idx[i] = modelFiles.getSize() - j;
                    }
                }

                listFiles.setSelectedIndices(idx);
            }
        });

        chbTitleGeneral.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        chbNumbGeneral.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = txtNumbStartGeneral.isEnabled();
                lblNumbStartGeneral.setEnabled(!state);
                txtNumbStartGeneral.setEnabled(!state);
                lblNumbPadGeneral.setEnabled(!state);
                txtNumbPadGeneral.setEnabled(!state);
                lblNumbExplainGeneral.setEnabled(!state);
            }
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

        chbChapters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        cbChapters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        btnBrowseChapters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.chapters"));
                getChooser().setMultiSelectionEnabled(false);
                getChooser().setAcceptAllFileFilterUsed(false);
                getChooser().resetChoosableFileFilters();
                getChooser().setFileFilter(TXT_EXT_FILTER);
                getChooser().setFileFilter(XML_EXT_FILTER);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (getChooser().getSelectedFile().exists()) {
                        txtChapters.setText(getChooser().getSelectedFile().toString());
                    }
                }
            }
        });

        chbTags.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        cbTags.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        btnBrowseTags.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.tags"));
                getChooser().setMultiSelectionEnabled(false);
                getChooser().setAcceptAllFileFilterUsed(false);
                getChooser().resetChoosableFileFilters();
                getChooser().setFileFilter(TXT_EXT_FILTER);
                getChooser().setFileFilter(XML_EXT_FILTER);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (getChooser().getSelectedFile().exists()) {
                        txtTags.setText(getChooser().getSelectedFile().toString());
                    }
                }
            }
        });

        chbExtraCmdGeneral.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean state = txtExtraCmdGeneral.isEnabled();
                txtExtraCmdGeneral.setEnabled(!state);
            }
        });

        chbMkvPropExeDef.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtMkvPropExe.setText("mkvpropedit");
                chbMkvPropExeDef.setEnabled(false);
                defaultIniFile();
            }
        });

        btnBrowseMkvPropExe.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.exe"));
                getChooser().setMultiSelectionEnabled(false);
                getChooser().setAcceptAllFileFilterUsed(false);
                getChooser().resetChoosableFileFilters();

                if (Utils.isWindows()) {
                    getChooser().setFileFilter(EXE_EXT_FILTER);
                }

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (getChooser().getSelectedFile().exists()) {
                        saveIniFile(getChooser().getSelectedFile());
                    }
                }
            }
        });

        btnProcessFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (modelFiles.getSize() == 0) {
                    JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.list.empty"),
                            LanguageManager.getString("error.title.empty"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    setCmdLine();

                    if (cmdLineBatchOpt.size() == 0) {
                        JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.nothing.to.do"),
                                "",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        if (isExecutableInPath(txtMkvPropExe.getText())) {
                            executeBatch();
                        } else {
                            JOptionPane.showMessageDialog(frmJMkvpropedit,
                                    LanguageManager.getString("error.executable.cmd.not.found"),
                                    "", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

            }
        });

        btnGenerateCmdLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (modelFiles.getSize() == 0) {
                    JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.list.empty"),
                            LanguageManager.getString("error.title.empty"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    setCmdLine();

                    if (cmdLineBatch.size() == 0) {
                        JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.nothing.to.do"),
                                "",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        txtOutput.setText("");

                        if (cmdLineBatch.size() > 0) {
                            for (int i = 0; i < modelFiles.size(); i++) {
                                txtOutput.append(cmdLineBatch.get(i) + "\n");
                            }

                            pnlTabs.setSelectedIndex(pnlTabs.getTabCount() - 1);
                        }
                    }
                }
            }
        });
    }

    /* Start of command line methods */

    private void setCmdLineGeneral() {
        cmdLineGeneral = new String[modelFiles.size()];
        cmdLineGeneralOpt = new String[modelFiles.size()];
        int start = Integer.parseInt(txtNumbStartGeneral.getText());

        for (int i = 0; i < modelFiles.size(); i++) {
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
                        String tmpTags = Utils.getPathWithoutExt(modelFiles.get(i)) + txtTags.getText()
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
                                cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText())
                                        + "\"";
                            } else {
                                cmdLineGeneral[i] += " --chapters \"" + Utils.escapeQuotes(txtChapters.getText())
                                        + "\"";
                                cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText())
                                        + "\"";
                            }
                        }
                        break;
                    case 2:
                        String tmpChaps = Utils.getPathWithoutExt(modelFiles.get(i)) + txtChapters.getText()
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
                    int pad = 0;

                    pad = Integer.parseInt(txtNumbPadGeneral.getText());
                    newTitle = newTitle.replace("{num}", Utils.padNumber(pad, start));

                    start++;
                }

                newTitle = newTitle.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(i)));

                cmdLineGeneral[i] += " --set title=\"" + Utils.escapeQuotes(newTitle) + "\"";
                cmdLineGeneralOpt[i] += " --set title=\"" + Utils.escapeName(newTitle) + "\"";
            }

            if (chbExtraCmdGeneral.isSelected() && !txtExtraCmdGeneral.getText().trim().isEmpty()) {
                cmdLineGeneral[i] += " " + txtExtraCmdGeneral.getText();
                cmdLineGeneralOpt[i] += " " + Utils.escapeName(txtExtraCmdGeneral.getText());
            }
        }

    }

    private void setCmdLine() {
        // Security: validate executable path before building any command
        try {
            InputValidator.validateMkvpropeditExecutablePath(txtMkvPropExe.getText());
        } catch (MkvPropeditException e) {
            JOptionPane.showMessageDialog(frmJMkvpropedit, e.getMessage(),
                    LanguageManager.getString("error.title.security"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Security: validate tag/chapter inputs to prevent path traversal
        try {
            if (chbTags.isSelected() && cbTags.getSelectedIndex() > 0) {
                InputValidator.validateNoPathTraversal(txtTags.getText());
            }
            if (chbChapters.isSelected() && cbChapters.getSelectedIndex() > 0) {
                InputValidator.validateNoPathTraversal(txtChapters.getText());
            }
        } catch (MkvPropeditException e) {
            JOptionPane.showMessageDialog(frmJMkvpropedit, e.getMessage(),
                    LanguageManager.getString("error.title.security"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Security: validate extra commands to prevent argument injection
        try {
            if (chbExtraCmdGeneral.isSelected()) {
                InputValidator.validateSafeExtraCommand(txtExtraCmdGeneral.getText());
            }
            for (TrackControls tc : videoTabPanel.getTracks()) {
                if (tc.chbExtraCmd.isSelected()) {
                    InputValidator.validateSafeExtraCommand(tc.txtExtraCmd.getText());
                }
            }
            for (TrackControls tc : audioTabPanel.getTracks()) {
                if (tc.chbExtraCmd.isSelected()) {
                    InputValidator.validateSafeExtraCommand(tc.txtExtraCmd.getText());
                }
            }
            for (TrackControls tc : subtitleTabPanel.getTracks()) {
                if (tc.chbExtraCmd.isSelected()) {
                    InputValidator.validateSafeExtraCommand(tc.txtExtraCmd.getText());
                }
            }
        } catch (MkvPropeditException e) {
            JOptionPane.showMessageDialog(frmJMkvpropedit, e.getMessage(),
                    LanguageManager.getString("error.title.security"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCmdLineGeneral();
        attachmentsTabPanel.updateCommandLines();

        // Build track command lines via TrackTabPanel
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < modelFiles.size(); i++) {
            fileNames.add(Utils.getFileNameWithoutExt(modelFiles.get(i)));
        }

        String[][] videoLines = videoTabPanel.buildCommandLines(modelFiles.size(), fileNames);
        String[][] audioLines = audioTabPanel.buildCommandLines(modelFiles.size(), fileNames);
        String[][] subtitleLines = subtitleTabPanel.buildCommandLines(modelFiles.size(), fileNames);

        cmdLineBatch = new ArrayList<String>();
        cmdLineBatchOpt = new ArrayList<String>();

        String attachDel = attachmentsTabPanel.getCommandLineDelete();
        String attachAdd = attachmentsTabPanel.getCommandLineAdd();
        String attachRep = attachmentsTabPanel.getCommandLineReplace();

        String cmdTemp = cmdLineGeneral[0] + attachDel + attachAdd
                + attachRep + videoLines[0][0] + audioLines[0][0] + subtitleLines[0][0];

        if (!cmdTemp.isEmpty()) {
            String attachDelOpt = attachmentsTabPanel.getCommandLineDeleteOpt();
            String attachAddOpt = attachmentsTabPanel.getCommandLineAddOpt();
            String attachRepOpt = attachmentsTabPanel.getCommandLineReplaceOpt();

            for (int i = 0; i < modelFiles.getSize(); i++) {
                String cmdLineAll = cmdLineGeneral[i] + attachDel + attachAdd
                        + attachRep + videoLines[0][i] + audioLines[0][i] + subtitleLines[0][i];

                String cmdLineAllOpt = cmdLineGeneralOpt[i] + attachDelOpt + attachAddOpt
                        + attachRepOpt + videoLines[1][i] + audioLines[1][i] + subtitleLines[1][i];

                if (Utils.isWindows()) {
                    cmdLineBatch.add("\"" + txtMkvPropExe.getText() + "\" \"" + modelFiles.get(i) + "\"" + cmdLineAll);
                    cmdLineBatchOpt.add("\"" + Utils.escapeName(modelFiles.get(i)) + "\"" + cmdLineAllOpt);
                } else {
                    cmdLineBatch.add("\"" + Utils.escapeQuotes(txtMkvPropExe.getText()) + "\" " + "\""
                            + Utils.escapeQuotes(modelFiles.get(i)) + "\"" + cmdLineAll);

                    cmdLineBatchOpt.add("\"" + Utils.escapeName(modelFiles.get(i)) + "\"" + cmdLineAllOpt);
                }
            }
        }
    }

    private void executeBatch() {
        worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    txtOutput.setText("");
                    pnlTabs.setSelectedIndex(pnlTabs.getTabCount() - 1);
                    pnlTabs.setEnabled(false);
                    btnProcessFiles.setEnabled(false);
                    btnGenerateCmdLine.setEnabled(false);
                });

                java.util.List<String> files = java.util.Collections.list(modelFiles.elements());
                var executor = new BatchExecutorService(
                        txtMkvPropExe.getText(), cmdLineBatch, cmdLineBatchOpt, files);

                executor.executeAll(result -> {
                    StringBuilder outputChunk = new StringBuilder();
                    outputChunk.append("File: ").append(result.filePath()).append("\n");
                    outputChunk.append("Command line: ").append(result.commandLine()).append("\n\n");
                    outputChunk.append(result.output());
                    outputChunk.append("\n--------------\n\n");

                    javax.swing.SwingUtilities.invokeLater(() -> txtOutput.append(outputChunk.toString()));
                });

                return null;
            }

            @Override
            protected void done() {
                pnlTabs.setEnabled(true);
                btnProcessFiles.setEnabled(true);
                btnGenerateCmdLine.setEnabled(true);
            }
        };

        worker.execute();
    }

    private void parseFiles(String[] argsArray) {
        if (argsArray.length > 0) {
            File file = null;

            for (String arg : argsArray) {
                try {
                    file = new File(arg);

                    if (!file.exists()) {
                        continue;
                    }

                    if (file.isDirectory()) {
                        addMkvFilesFromFolder(file);
                    } else {
                        addFile(file, true);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private boolean isExecutableInPath(final String exe) {
        // Security: reject suspicious executable paths before attempting execution
        try {
            InputValidator.validateMkvpropeditExecutablePath(exe);
        } catch (MkvPropeditException e) {
            return false;
        }

        // Use BatchExecutorService for a non-blocking, EDT-safe check
        return BatchExecutorService.isExecutableAvailable(exe);
    }

    /* End of command line methods */

    /* Start of INI configuration file methods */

    private void readIniFile() {
        // Use IniPersistenceService to read/create the INI
        org.ini4j.Ini ini = iniService.readOrCreateIni();

        if (ini != null) {
            profileManager = new ProfileManager(ini);

            String exePath = iniService.getExecutablePath(ini);

            if (exePath.equals("mkvpropedit")) {
                chbMkvPropExeDef.setSelected(true);
                chbMkvPropExeDef.setEnabled(false);
            } else {
                txtMkvPropExe.setText(exePath);
                chbMkvPropExeDef.setSelected(false);
                chbMkvPropExeDef.setEnabled(true);
            }
        } else {
            profileManager = null;
        }

        loadProfilesToModel();
    }

    private void loadProfilesToModel() {
        if (profileManager == null)
            return;
        loadProfileModel(videoTabPanel.getProfileModel(), ProfileType.VIDEO);
        loadProfileModel(audioTabPanel.getProfileModel(), ProfileType.AUDIO);
        loadProfileModel(subtitleTabPanel.getProfileModel(), ProfileType.SUBTITLE);
    }

    private void loadProfileModel(DefaultListModel<TrackProfile> model, ProfileType type) {
        if (model != null) {
            model.clear();
            for (TrackProfile p : profileManager.getProfiles(type)) {
                model.addElement(p);
            }
        }
    }

    /**
     * Persists the executable path and updates the UI accordingly.
     */
    private void saveIniFile(File exeFile) {
        txtMkvPropExe.setText(exeFile.toString());
        chbMkvPropExeDef.setSelected(false);
        chbMkvPropExeDef.setEnabled(true);
        iniService.saveExecutablePath(exeFile.toString());
    }

    /**
     * Writes a default INI with "mkvpropedit" as the executable.
     * Called only when the user explicitly resets to defaults.
     */
    private void defaultIniFile() {
        iniService.saveExecutablePath("mkvpropedit");
    }

    /* End of INI configuration file methods */

    /* Start of table methods */

    private void resizeColumns(JTable table, double[] colSizes) {
        TableColumnModel columnModel = table.getColumnModel();
        int[] colWidths = new int[colSizes.length];

        int parWidth = table.getParent().getWidth();

        int total = 0;
        for (int i = 0; i < colSizes.length; i++) {
            colWidths[i] = (int) (parWidth * colSizes[i]);
            total += colWidths[i];
        }

        colWidths[colWidths.length - 1] += parWidth - total;

        for (int i = 0; i < colSizes.length; i++) {
            // Set minimum size for column
            columnModel.getColumn(i).setMinWidth(colWidths[i]);

            // Set prefered size for column
            columnModel.getColumn(i).setPreferredWidth(colWidths[i]);
        }

        table.revalidate();
    }

    /* End of table methods */

    /* Start of file methods */

    private void addFile(File file, boolean checkExtension) {
        try {
            if (!modelFiles.contains(file.getCanonicalPath()) && !checkExtension) {
                modelFiles.add(modelFiles.getSize(), file.getCanonicalPath());
            } else if (!modelFiles.contains(file.getCanonicalPath()) && MATROSKA_EXT_FILTER.accept(file)) {
                modelFiles.add(modelFiles.getSize(), file.getCanonicalPath());
            }
        } catch (IOException e) {
        }
    }

    private void addMkvFilesFromFolder(final File folder) {
        Runnable tmpWorker = new Runnable() {
            @Override
            public void run() {
                Iterator<File> mkvFiles = FileUtils.iterateFiles(folder, MATROSKA_FILE_FILTER, TrueFileFilter.INSTANCE);

                while (mkvFiles.hasNext()) {
                    addFile(mkvFiles.next(), false);
                }
            }
        };

        SwingUtilities.invokeLater(tmpWorker);
    }

    /* End of file methods */

    private void loadLanguage() {
        if (iniFile.exists()) {
            try {
                Ini ini = new Ini(iniFile);
                String lang = ini.get("General", "language");
                if (lang != null && !lang.isEmpty()) {
                    LanguageManager.setLocale(Locale.of(lang));
                }
            } catch (Exception e) {
                LOGGER.error("Error loading language preference", e);
            }
        }
    }

    private void saveLanguage(String langCode) {
        try {
            if (!iniFile.exists()) {
                iniFile.createNewFile();
            }
            Ini ini = new Ini(iniFile);
            ini.put("General", "language", langCode);
            // Preserve mkvpropedit path if it exists
            String exePath = txtMkvPropExe.getText();
            if (exePath != null && !exePath.isEmpty() && !"mkvpropedit".equals(exePath)) {
                ini.put("General", "mkvpropedit", exePath);
            }
            ini.store();
        } catch (Exception e) {
            LOGGER.error("Error saving language preference", e);
        }
    }

}
