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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ini4j.Ini;

public class JMkvpropedit {

    private static final String VERSION_NUMBER = "v2.5.1";
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

    private FileFilter MATROSKA_EXT_FILTER = new FileNameExtensionFilter(
            "Matroska files (*.mkv; *.mka; *.mk3d; *.webm; *.mks)", "mkv", "mka", "mk3d", "webm", "mks");

    private IOFileFilter MATROSKA_FILE_FILTER = WildcardFileFilter.builder()
            .setWildcards("*.mkv", "*.mka", "*.mk3d", ".webm", ".mks")
            .setIoCase(IOCase.INSENSITIVE)
            .get();

    // Refactored tab panels
    private InputTabPanel inputTabPanel;
    private GeneralTabPanel generalTabPanel;
    private OptionsTabPanel optionsTabPanel;
    private TrackTabPanel videoTabPanel;
    private TrackTabPanel audioTabPanel;
    private TrackTabPanel subtitleTabPanel;
    private AttachmentsTabPanel attachmentsTabPanel;

    private List<String> cmdLineBatch = null;
    private List<String> cmdLineBatchOpt = null;

    private ProfileManager profileManager;

    // Window controls
    private Dimension frmJMkvpropeditDim = new Dimension(0, 0);
    private JFrame frmJMkvpropedit;
    private JTabbedPane pnlTabs;
    private JButton btnProcessFiles;
    private JButton btnGenerateCmdLine;

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

        // Input tab
        inputTabPanel = new InputTabPanel(frmJMkvpropedit, getChooser(), mkvStrings,
                MATROSKA_FILE_FILTER, MATROSKA_EXT_FILTER);
        pnlTabs.addTab(LanguageManager.getString("tab.input"), null, inputTabPanel, null);

        // General tab
        generalTabPanel = new GeneralTabPanel(frmJMkvpropedit, getChooser());
        pnlTabs.addTab(LanguageManager.getString("tab.general"), null, generalTabPanel, null);

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

        // Options tab
        optionsTabPanel = new OptionsTabPanel(frmJMkvpropedit, getChooser(), iniService);
        optionsTabPanel.setOnLanguageApply(() -> {
            String langCode = optionsTabPanel.getSelectedLanguageCode();
            var attachTable = attachmentsTabPanel.getTableAdd();
            int[] widths = new int[attachTable.getColumnCount()];
            for (int i = 0; i < attachTable.getColumnCount(); i++) {
                widths[i] = attachTable.getColumnModel().getColumn(i).getPreferredWidth();
            }
            saveLanguage(langCode);
            frmJMkvpropedit.dispose();
            LanguageManager.setLocale(Locale.forLanguageTag(langCode));
            initialize();
            frmJMkvpropedit.setVisible(true);
            if (widths.length == attachTable.getColumnCount()) {
                for (int i = 0; i < attachTable.getColumnCount(); i++) {
                    attachTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
                }
            }
            readIniFile();
        });
        pnlTabs.addTab(LanguageManager.getString("tab.options"), null, optionsTabPanel, null);

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

        // Right-click menu for output
        Utils.addRCMenuMouseListener(txtOutput);

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

        btnProcessFiles.addActionListener(e -> {
            if (inputTabPanel.getModel().getSize() == 0) {
                JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.list.empty"),
                        LanguageManager.getString("error.title.empty"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                setCmdLine();
                if (cmdLineBatchOpt.size() == 0) {
                    JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.nothing.to.do"),
                            "", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    if (isExecutableInPath(optionsTabPanel.getExecutablePath())) {
                        executeBatch();
                    } else {
                        JOptionPane.showMessageDialog(frmJMkvpropedit,
                                LanguageManager.getString("error.executable.cmd.not.found"),
                                "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnGenerateCmdLine.addActionListener(e -> {
            if (inputTabPanel.getModel().getSize() == 0) {
                JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.list.empty"),
                        LanguageManager.getString("error.title.empty"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                setCmdLine();
                if (cmdLineBatch.size() == 0) {
                    JOptionPane.showMessageDialog(frmJMkvpropedit, LanguageManager.getString("error.nothing.to.do"),
                            "", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    txtOutput.setText("");
                    if (cmdLineBatch.size() > 0) {
                        for (int i = 0; i < inputTabPanel.getModel().getSize(); i++) {
                            txtOutput.append(cmdLineBatch.get(i) + "\n");
                        }
                        pnlTabs.setSelectedIndex(pnlTabs.getTabCount() - 1);
                    }
                }
            }
        });
    }

    /* Start of command line methods */

    private void setCmdLineGeneral() {
        List<String> modelPaths = new ArrayList<>();
        DefaultListModel<String> model = inputTabPanel.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            modelPaths.add(model.get(i));
        }
        generalTabPanel.updateCmdLines(modelPaths.size(), modelPaths);
    }

    private void setCmdLine() {
        DefaultListModel<String> model = inputTabPanel.getModel();

        // Security: validate executable path before building any command
        try {
            InputValidator.validateMkvpropeditExecutablePath(optionsTabPanel.getExecutablePath());
        } catch (MkvPropeditException e) {
            JOptionPane.showMessageDialog(frmJMkvpropedit, e.getMessage(),
                    LanguageManager.getString("error.title.security"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCmdLineGeneral();
        attachmentsTabPanel.updateCommandLines();

        // Build track command lines via TrackTabPanel
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            fileNames.add(Utils.getFileNameWithoutExt(model.get(i)));
        }

        String[][] videoLines = videoTabPanel.buildCommandLines(model.getSize(), fileNames);
        String[][] audioLines = audioTabPanel.buildCommandLines(model.getSize(), fileNames);
        String[][] subtitleLines = subtitleTabPanel.buildCommandLines(model.getSize(), fileNames);

        String[] cmdLineGeneral = generalTabPanel.getCmdLineGeneral();
        String[] cmdLineGeneralOpt = generalTabPanel.getCmdLineGeneralOpt();

        cmdLineBatch = new ArrayList<String>();
        cmdLineBatchOpt = new ArrayList<String>();

        String attachDel = attachmentsTabPanel.getCommandLineDelete();
        String attachAdd = attachmentsTabPanel.getCommandLineAdd();
        String attachRep = attachmentsTabPanel.getCommandLineReplace();

        String exePath = optionsTabPanel.getExecutablePath();

        if (model.getSize() > 0 && cmdLineGeneral != null && cmdLineGeneral.length > 0) {
            String cmdTemp = cmdLineGeneral[0] + attachDel + attachAdd
                    + attachRep + videoLines[0][0] + audioLines[0][0] + subtitleLines[0][0];

            if (!cmdTemp.isEmpty()) {
                String attachDelOpt = attachmentsTabPanel.getCommandLineDeleteOpt();
                String attachAddOpt = attachmentsTabPanel.getCommandLineAddOpt();
                String attachRepOpt = attachmentsTabPanel.getCommandLineReplaceOpt();

                for (int i = 0; i < model.getSize(); i++) {
                    String cmdLineAll = cmdLineGeneral[i] + attachDel + attachAdd
                            + attachRep + videoLines[0][i] + audioLines[0][i] + subtitleLines[0][i];

                    String cmdLineAllOpt = cmdLineGeneralOpt[i] + attachDelOpt + attachAddOpt
                            + attachRepOpt + videoLines[1][i] + audioLines[1][i] + subtitleLines[1][i];

                    if (Utils.isWindows()) {
                        cmdLineBatch.add("\"" + exePath + "\" \"" + model.get(i) + "\"" + cmdLineAll);
                        cmdLineBatchOpt.add("\"" + Utils.escapeName(model.get(i)) + "\"" + cmdLineAllOpt);
                    } else {
                        cmdLineBatch.add("\"" + Utils.escapeQuotes(exePath) + "\" " + "\""
                                + Utils.escapeQuotes(model.get(i)) + "\"" + cmdLineAll);
                        cmdLineBatchOpt.add("\"" + Utils.escapeName(model.get(i)) + "\"" + cmdLineAllOpt);
                    }
                }
            }
        }
    }

    private void executeBatch() {
        DefaultListModel<String> model = inputTabPanel.getModel();
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

                java.util.List<String> files = java.util.Collections.list(model.elements());
                var executor = new BatchExecutorService(
                        optionsTabPanel.getExecutablePath(), cmdLineBatch, cmdLineBatchOpt, files);

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
            for (String arg : argsArray) {
                try {
                    File file = new File(arg);
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        inputTabPanel.addMkvFilesFromFolder(file);
                    } else {
                        inputTabPanel.addFile(file, true);
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
        org.ini4j.Ini ini = iniService.readOrCreateIni();

        if (ini != null) {
            profileManager = new ProfileManager(ini);

            String exePath = iniService.getExecutablePath(ini);

            if (exePath.equals("mkvpropedit")) {
                optionsTabPanel.setExecutablePath("mkvpropedit");
                optionsTabPanel.setDefaultExecutable(true);
                optionsTabPanel.setDefaultExecutableEnabled(false);
            } else {
                optionsTabPanel.setExecutablePath(exePath);
                optionsTabPanel.setDefaultExecutable(false);
                optionsTabPanel.setDefaultExecutableEnabled(true);
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
        optionsTabPanel.setExecutablePath(exeFile.toString());
        optionsTabPanel.setDefaultExecutable(false);
        optionsTabPanel.setDefaultExecutableEnabled(true);
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

    /* File methods are now in InputTabPanel */

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
            String exePath = optionsTabPanel.getExecutablePath();
            if (exePath != null && !exePath.isEmpty() && !"mkvpropedit".equals(exePath)) {
                ini.put("General", "mkvpropedit", exePath);
            }
            ini.store();
        } catch (Exception e) {
            LOGGER.error("Error saving language preference", e);
        }
    }

}
