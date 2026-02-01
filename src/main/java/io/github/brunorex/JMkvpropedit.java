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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import javax.swing.JList;
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

    private static final String VERSION_NUMBER = "v1.2.0";
    private static final int MAX_STREAMS = 200;
    private static String[] argsArray;

    private Process proc = null;
    private SwingWorker<Void, Void> worker = null;
    private ProcessBuilder pb = new ProcessBuilder();

    private boolean exeFound = true;

    private File iniFile = new File("JMkvpropedit.ini");
    private static final MkvStrings mkvStrings = new MkvStrings();

    private JFileChooser chooser = new JFileChooser(System.getProperty("user.home")) {
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

    private FileFilter EXE_EXT_FILTER = new FileNameExtensionFilter("Excecutable files (*.exe)", "exe");

    private FileFilter MATROSKA_EXT_FILTER = new FileNameExtensionFilter(
            "Matroska files (*.mkv; *.mka; *.mk3d; *.webm; *.mks)", "mkv", "mka", "mk3d", "webm", "mks");

    private IOFileFilter MATROSKA_FILE_FILTER = WildcardFileFilter.builder()
            .setWildcards("*.mkv", "*.mka", "*.mk3d", ".webm", ".mks")
            .setIoCase(IOCase.INSENSITIVE)
            .get();

    private FileFilter TXT_EXT_FILTER = new FileNameExtensionFilter("Plain text files (*.txt)", "txt");

    private FileFilter XML_EXT_FILTER = new FileNameExtensionFilter("XML files (*.xml)", "xml");

    private static final String[] COLUMNS_ATTACHMENTS_ADD = { "File", "Name", "Description", "MIME Type" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_ADD = { 0.35, 0.20, 0.25, 0.20 };
    private DefaultTableModel modelAttachmentsAdd = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_ADD) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

    };

    private static final String[] COLUMNS_ATTACHMENTS_REPLACE = { "Type", "Original Value", "Replacement", "Name",
            "Description", "MIME Type" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_REPLACE = { 0.15, 0.15, 0.20, 0.20, 0.15, 0.15 };
    private DefaultTableModel modelAttachmentsReplace = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_REPLACE) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

    };

    private static final String[] COLUMNS_ATTACHMENTS_DELETE = { "Type", "Value" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_DELETE = { 0.40, 0.60 };
    private DefaultTableModel modelAttachmentsDelete = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_DELETE) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

    };

    private String[] cmdLineGeneral = null;
    private String[] cmdLineGeneralOpt = null;

    private String[] cmdLineVideo = null;
    private String[] cmdLineVideoOpt = null;
    private int nVideo = 0;

    private String[] cmdLineAudio = null;
    private String[] cmdLineAudioOpt = null;
    private int nAudio = 0;

    private String[] cmdLineSubtitle = null;
    private String[] cmdLineSubtitleOpt = null;
    private int nSubtitle = 0;

    private String cmdLineAttachmentsAdd = null;
    private String cmdLineAttachmentsAddOpt = null;

    private String cmdLineAttachmentsReplace = null;
    private String cmdLineAttachmentsReplaceOpt = null;

    private String cmdLineAttachmentsDelete = null;
    private String cmdLineAttachmentsDeleteOpt = null;

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

    // Video tab controls
    private JComboBox<String> cbVideo;
    private JButton btnAddVideo;
    private JButton btnRemoveVideo;
    private CardLayout lytLyrdPnlVideo;
    private JPanel lyrdPnlVideo;

    private JPanel[] subPnlVideo = new JPanel[MAX_STREAMS];
    private JCheckBox[] chbEditVideo = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbEnableVideo = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesEnableVideo = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoEnableVideo = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbEnableVideo = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbDefaultVideo = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesDefVideo = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoDefVideo = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbDefVideo = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbForcedVideo = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesForcedVideo = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoForcedVideo = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbForcedVideo = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbNameVideo = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtNameVideo = new JTextField[MAX_STREAMS];
    private JCheckBox[] chbNumbVideo = new JCheckBox[MAX_STREAMS];
    private JLabel[] lblNumbStartVideo = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbStartVideo = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbPadVideo = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbPadVideo = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbExplainVideo = new JLabel[MAX_STREAMS];
    private JCheckBox[] chbLangVideo = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbExtraCmdVideo = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtExtraCmdVideo = new JTextField[MAX_STREAMS];
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] cbLangVideo = new JComboBox[MAX_STREAMS];

    // Audio tab controls
    private JComboBox<String> cbAudio;
    private JButton btnAddAudio;
    private JButton btnRemoveAudio;
    private CardLayout lytLyrdPnlAudio;
    private JPanel lyrdPnlAudio;

    private JPanel[] subPnlAudio = new JPanel[MAX_STREAMS];
    private JCheckBox[] chbEditAudio = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbEnableAudio = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesEnableAudio = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoEnableAudio = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbEnableAudio = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbDefaultAudio = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesDefAudio = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoDefAudio = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbDefAudio = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbForcedAudio = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesForcedAudio = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoForcedAudio = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbForcedAudio = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbNameAudio = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtNameAudio = new JTextField[MAX_STREAMS];
    private JCheckBox[] chbNumbAudio = new JCheckBox[MAX_STREAMS];
    private JLabel[] lblNumbStartAudio = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbStartAudio = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbPadAudio = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbPadAudio = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbExplainAudio = new JLabel[MAX_STREAMS];
    private JCheckBox[] chbLangAudio = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbExtraCmdAudio = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtExtraCmdAudio = new JTextField[MAX_STREAMS];
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] cbLangAudio = new JComboBox[MAX_STREAMS];

    private ProfileManager profileManager;

    private JList<TrackProfile> listVideoProfiles;
    private DefaultListModel<TrackProfile> modelVideoProfiles;
    private JPanel pnlVideoProfiles;

    private JList<TrackProfile> listAudioProfiles;
    private DefaultListModel<TrackProfile> modelAudioProfiles;
    private JPanel pnlAudioProfiles;

    private JList<TrackProfile> listSubtitleProfiles;
    private DefaultListModel<TrackProfile> modelSubtitleProfiles;
    private JPanel pnlSubtitleProfiles;

    // Subtitle tab controls
    private JComboBox<String> cbSubtitle;
    private AbstractButton btnAddSubtitle;
    private AbstractButton btnRemoveSubtitle;
    private CardLayout lytLyrdPnlSubtitle;
    private JPanel lyrdPnlSubtitle;

    private JPanel[] subPnlSubtitle = new JPanel[MAX_STREAMS];
    private JCheckBox[] chbEditSubtitle = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbEnableSubtitle = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesEnableSubtitle = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoEnableSubtitle = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbEnableSubtitle = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbDefaultSubtitle = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesDefSubtitle = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoDefSubtitle = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbDefSubtitle = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbForcedSubtitle = new JCheckBox[MAX_STREAMS];
    private JRadioButton[] rbYesForcedSubtitle = new JRadioButton[MAX_STREAMS];
    private JRadioButton[] rbNoForcedSubtitle = new JRadioButton[MAX_STREAMS];
    private ButtonGroup[] bgRbForcedSubtitle = new ButtonGroup[MAX_STREAMS];
    private JCheckBox[] chbNameSubtitle = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtNameSubtitle = new JTextField[MAX_STREAMS];
    private JCheckBox[] chbNumbSubtitle = new JCheckBox[MAX_STREAMS];
    private JLabel[] lblNumbStartSubtitle = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbStartSubtitle = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbPadSubtitle = new JLabel[MAX_STREAMS];
    private JTextField[] txtNumbPadSubtitle = new JTextField[MAX_STREAMS];
    private JLabel[] lblNumbExplainSubtitle = new JLabel[MAX_STREAMS];
    private JCheckBox[] chbLangSubtitle = new JCheckBox[MAX_STREAMS];
    private JCheckBox[] chbExtraCmdSubtitle = new JCheckBox[MAX_STREAMS];
    private JTextField[] txtExtraCmdSubtitle = new JTextField[MAX_STREAMS];
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] cbLangSubtitle = new JComboBox[MAX_STREAMS];

    // Attachments tab controls
    private JTabbedPane pnlAttachments;
    private JPanel pnlAttachAdd;
    private JScrollPane spAttachAdd;
    private JTable tblAttachAdd;
    private JPanel pnlAttachAddControls;
    private JLabel lblAttachAddFile;
    private JTextField txtAttachAddFile;
    private JButton btnBrowseAttachAddFile;
    private JLabel lblAttachAddName;
    private JTextField txtAttachAddName;
    private JLabel lblAttachAddDesc;
    private JTextField txtAttachAddDesc;
    private JLabel lblAttachAddMime;
    private JComboBox<String> cbAttachAddMime;
    private JPanel pnlAttachAddControlsBottom;
    private JButton btnAttachAddAdd;
    private JButton btnAttachAddRemove;
    private JButton btnAttachAddEdit;
    private JButton btnAttachAddCancel;

    private JPanel pnlAttachReplace;
    private JScrollPane spAttachReplace;
    private JTable tblAttachReplace;
    private JPanel pnlAttachReplaceControls;
    private JLabel lblAttachReplaceType;
    private JPanel pnlAttachReplaceType;
    private ButtonGroup bgAttachReplaceType = new ButtonGroup();
    private JRadioButton rbAttachReplaceID;
    private JRadioButton rbAttachReplaceName;
    private JRadioButton rbAttachReplaceMime;
    private JPanel pnlAttachReplaceOrig;
    private JLabel lblAttachReplaceOrig;
    private JTextField txtAttachReplaceOrig;
    private JComboBox<String> cbAttachReplaceOrig;
    private JLabel lblAttachReplaceNew;
    private JTextField txtAttachReplaceNew;
    private JButton btnAttachReplaceNewBrowse;
    private JLabel lblAttachReplaceName;
    private JTextField txtAttachReplaceName;
    private JLabel lblAttachReplaceDesc;
    private JTextField txtAttachReplaceDesc;
    private JLabel lblAttachReplaceMime;
    private JComboBox<String> cbAttachReplaceMime;
    private JPanel pnlAttachReplaceControlsBottom;
    private JButton btnAttachReplaceAdd;
    private JButton btnAttachReplaceEdit;
    private JButton btnAttachReplaceRemove;
    private JButton btnAttachReplaceCancel;

    private JPanel pnlAttachDelete;
    private JScrollPane spAttachDelete;
    private JTable tblAttachDelete;
    private JPanel pnlAttachDeleteControls;
    private ButtonGroup bgAttachDeleteType = new ButtonGroup();
    private JLabel lblAttachDeleteType;
    private JPanel pnlAttachDeleteType;
    private JRadioButton rbAttachDeleteName;
    private JRadioButton rbAttachDeleteID;
    private JRadioButton rbAttachDeleteMime;
    private JLabel lblAttachDeleteValue;
    private JPanel pnlAttachDeleteValue;
    private JTextField txtAttachDeleteValue;
    private JComboBox<String> cbAttachDeleteValue;
    private JPanel pnlAttachDeleteControlsBottom;
    private JButton btnAttachDeleteAdd;
    private JButton btnAttachDeleteEdit;
    private JButton btnAttachDeleteRemove;
    private JButton btnAttachDeleteCancel;

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
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    JMkvpropedit window = new JMkvpropedit();
                    window.frmJMkvpropedit.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

        modelAudioProfiles = new DefaultListModel<>();
        listAudioProfiles = new JList<>(modelAudioProfiles);

        modelVideoProfiles = new DefaultListModel<>();
        listVideoProfiles = new JList<>(modelVideoProfiles);

        modelSubtitleProfiles = new DefaultListModel<>();
        listSubtitleProfiles = new JList<>(modelSubtitleProfiles);

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileHidingEnabled(true);

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

        JPanel pnlVideo = new JPanel();
        pnlVideo.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("video.tab.title"), null, pnlVideo, null);
        GridBagLayout gbl_pnlVideo = new GridBagLayout();
        gbl_pnlVideo.columnWidths = new int[] { 500, 200, 0 };
        gbl_pnlVideo.rowHeights = new int[] { 30, 283, 0 };
        gbl_pnlVideo.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlVideo.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        pnlVideo.setLayout(gbl_pnlVideo);

        JPanel pnlControlsVideo = new JPanel();
        GridBagConstraints gbc_pnlControlsVideo = new GridBagConstraints();
        gbc_pnlControlsVideo.insets = new Insets(0, 0, 5, 0);
        gbc_pnlControlsVideo.fill = GridBagConstraints.BOTH;
        gbc_pnlControlsVideo.gridx = 0;
        gbc_pnlControlsVideo.gridy = 0;
        pnlVideo.add(pnlControlsVideo, gbc_pnlControlsVideo);
        pnlControlsVideo.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        cbVideo = new JComboBox<String>();
        cbVideo.setPreferredSize(new Dimension(150, cbVideo.getPreferredSize().height));
        pnlControlsVideo.add(cbVideo);

        btnAddVideo = new JButton("");
        btnAddVideo.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
        btnAddVideo.setMargin(new Insets(0, 5, 0, 5));
        btnAddVideo.setBorderPainted(false);
        btnAddVideo.setContentAreaFilled(false);
        btnAddVideo.setFocusPainted(false);
        btnAddVideo.setOpaque(false);
        pnlControlsVideo.add(btnAddVideo);

        btnRemoveVideo = new JButton("");
        btnRemoveVideo.setEnabled(false);
        btnRemoveVideo.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
        btnRemoveVideo.setMargin(new Insets(0, 0, 0, 0));
        btnRemoveVideo.setBorderPainted(false);
        btnRemoveVideo.setContentAreaFilled(false);
        btnRemoveVideo.setFocusPainted(false);
        btnRemoveVideo.setOpaque(false);
        btnRemoveVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cbVideo.getItemCount() > 0) {
                    int response = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("delete.track.confirm"),
                            LanguageManager.getString("delete.track.title"),
                            JOptionPane.YES_NO_OPTION);

                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }

                    int idx = cbVideo.getItemCount() - 1;
                    cbVideo.removeItemAt(idx);
                    lyrdPnlVideo.remove(idx);
                    nVideo--;

                    if (cbVideo.getItemCount() == 0) {
                        btnRemoveVideo.setEnabled(false);
                    }

                    if (cbVideo.getItemCount() < MAX_STREAMS && !btnAddVideo.isEnabled()) {
                        btnAddVideo.setEnabled(true);
                    }

                    System.gc();
                }
            }
        });
        pnlControlsVideo.add(btnRemoveVideo);

        lyrdPnlVideo = new JPanel();
        GridBagConstraints gbc_lyrdPnlVideo = new GridBagConstraints();
        gbc_lyrdPnlVideo.fill = GridBagConstraints.BOTH;
        gbc_lyrdPnlVideo.gridx = 0;
        gbc_lyrdPnlVideo.gridy = 1;
        pnlVideo.add(lyrdPnlVideo, gbc_lyrdPnlVideo);
        lytLyrdPnlVideo = new CardLayout(0, 0);
        lyrdPnlVideo.setLayout(lytLyrdPnlVideo);

        pnlVideoProfiles = createProfilePanel(ProfileType.VIDEO, listVideoProfiles, modelVideoProfiles,
                () -> cbVideo.getSelectedIndex(),
                (p, idx) -> {
                    p.setEnableTrack(rbYesEnableVideo[idx].isSelected());
                    p.setUseEnableTrack(chbEnableVideo[idx].isSelected());

                    p.setDefaultTrack(rbYesDefVideo[idx].isSelected());
                    p.setUseDefaultTrack(chbDefaultVideo[idx].isSelected());

                    p.setForcedTrack(rbYesForcedVideo[idx].isSelected());
                    p.setUseForcedTrack(chbForcedVideo[idx].isSelected());

                    p.setTrackName(txtNameVideo[idx].getText());
                    p.setUseName(chbNameVideo[idx].isSelected());

                    p.setLanguage((String) cbLangVideo[idx].getSelectedItem());
                    p.setUseLanguage(chbLangVideo[idx].isSelected());
                },
                (p, idx) -> applyVideoProfile(p, idx));
        GridBagConstraints gbc_pnlVideoProfiles = new GridBagConstraints();
        gbc_pnlVideoProfiles.gridheight = 2;
        gbc_pnlVideoProfiles.fill = GridBagConstraints.BOTH;
        gbc_pnlVideoProfiles.weightx = 0.0;
        gbc_pnlVideoProfiles.weighty = 1.0;
        gbc_pnlVideoProfiles.gridx = 1;
        gbc_pnlVideoProfiles.gridy = 0;
        gbc_pnlVideoProfiles.insets = new Insets(0, 5, 0, 0);
        pnlVideo.add(pnlVideoProfiles, gbc_pnlVideoProfiles);

        JPanel pnlAudio = new JPanel();
        pnlAudio.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("audio.tab.title"), null, pnlAudio, null);
        GridBagLayout gbl_pnlAudio = new GridBagLayout();
        gbl_pnlAudio.columnWidths = new int[] { 500, 200, 0 };
        gbl_pnlAudio.rowHeights = new int[] { 30, 283, 0 };
        gbl_pnlAudio.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAudio.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        pnlAudio.setLayout(gbl_pnlAudio);

        JPanel pnlControlsAudio = new JPanel();
        GridBagConstraints gbc_pnlControlsAudio = new GridBagConstraints();
        gbc_pnlControlsAudio.insets = new Insets(0, 0, 5, 0);
        gbc_pnlControlsAudio.fill = GridBagConstraints.BOTH;
        gbc_pnlControlsAudio.gridx = 0;
        gbc_pnlControlsAudio.gridy = 0;
        pnlAudio.add(pnlControlsAudio, gbc_pnlControlsAudio);
        pnlControlsAudio.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        cbAudio = new JComboBox<String>();
        cbAudio.setPreferredSize(new Dimension(150, cbAudio.getPreferredSize().height));
        pnlControlsAudio.add(cbAudio);

        btnAddAudio = new JButton("");
        btnAddAudio.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
        btnAddAudio.setMargin(new Insets(0, 5, 0, 5));
        btnAddAudio.setBorderPainted(false);
        btnAddAudio.setContentAreaFilled(false);
        btnAddAudio.setFocusPainted(false);
        btnAddAudio.setOpaque(false);
        pnlControlsAudio.add(btnAddAudio);

        btnRemoveAudio = new JButton("");
        btnRemoveAudio.setEnabled(false);
        btnRemoveAudio.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
        btnRemoveAudio.setMargin(new Insets(0, 0, 0, 0));
        btnRemoveAudio.setBorderPainted(false);
        btnRemoveAudio.setContentAreaFilled(false);
        btnRemoveAudio.setFocusPainted(false);
        btnRemoveAudio.setOpaque(false);
        btnRemoveAudio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cbAudio.getItemCount() > 0) {
                    int response = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("delete.track.confirm"),
                            LanguageManager.getString("delete.track.title"),
                            JOptionPane.YES_NO_OPTION);

                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }

                    int idx = cbAudio.getItemCount() - 1;
                    cbAudio.removeItemAt(idx);
                    lyrdPnlAudio.remove(idx);
                    nAudio--;

                    if (cbAudio.getItemCount() == 0) {
                        btnRemoveAudio.setEnabled(false);
                    }

                    if (cbAudio.getItemCount() < MAX_STREAMS && !btnAddAudio.isEnabled()) {
                        btnAddAudio.setEnabled(true);
                    }

                    System.gc();
                }
            }
        });
        pnlControlsAudio.add(btnRemoveAudio);

        lyrdPnlAudio = new JPanel();
        GridBagConstraints gbc_lyrdPnlAudio = new GridBagConstraints();
        gbc_lyrdPnlAudio.fill = GridBagConstraints.BOTH;
        gbc_lyrdPnlAudio.gridx = 0;
        gbc_lyrdPnlAudio.gridy = 1;
        pnlAudio.add(lyrdPnlAudio, gbc_lyrdPnlAudio);
        lytLyrdPnlAudio = new CardLayout(0, 0);
        lyrdPnlAudio.setLayout(lytLyrdPnlAudio);

        pnlAudioProfiles = createProfilePanel(ProfileType.AUDIO, listAudioProfiles, modelAudioProfiles,
                () -> cbAudio.getSelectedIndex(),
                (p, idx) -> {
                    p.setEnableTrack(rbYesEnableAudio[idx].isSelected());
                    p.setUseEnableTrack(chbEnableAudio[idx].isSelected());

                    p.setDefaultTrack(rbYesDefAudio[idx].isSelected());
                    p.setUseDefaultTrack(chbDefaultAudio[idx].isSelected());

                    p.setForcedTrack(rbYesForcedAudio[idx].isSelected());
                    p.setUseForcedTrack(chbForcedAudio[idx].isSelected());

                    p.setTrackName(txtNameAudio[idx].getText());
                    p.setUseName(chbNameAudio[idx].isSelected());

                    p.setLanguage((String) cbLangAudio[idx].getSelectedItem());
                    p.setUseLanguage(chbLangAudio[idx].isSelected());
                },
                (p, idx) -> applyAudioProfile(p, idx));
        GridBagConstraints gbc_pnlAudioProfiles = new GridBagConstraints();
        gbc_pnlAudioProfiles.gridheight = 2;
        gbc_pnlAudioProfiles.fill = GridBagConstraints.BOTH;
        gbc_pnlAudioProfiles.weightx = 0.0;
        gbc_pnlAudioProfiles.weighty = 1.0;
        gbc_pnlAudioProfiles.gridx = 1;
        gbc_pnlAudioProfiles.gridy = 0;
        gbc_pnlAudioProfiles.insets = new Insets(0, 5, 0, 0);
        pnlAudio.add(pnlAudioProfiles, gbc_pnlAudioProfiles);

        JPanel pnlSubtitle = new JPanel();
        pnlSubtitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlTabs.addTab(LanguageManager.getString("subtitle.tab.title"), null, pnlSubtitle, null);
        GridBagLayout gbl_pnlSubtitle = new GridBagLayout();
        gbl_pnlSubtitle.columnWidths = new int[] { 500, 200, 0 };
        gbl_pnlSubtitle.rowHeights = new int[] { 30, 283, 0 };
        gbl_pnlSubtitle.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlSubtitle.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        pnlSubtitle.setLayout(gbl_pnlSubtitle);

        JPanel pnlControlsSubtitle = new JPanel();
        GridBagConstraints gbc_pnlControlsSubtitle = new GridBagConstraints();
        gbc_pnlControlsSubtitle.insets = new Insets(0, 0, 5, 0);
        gbc_pnlControlsSubtitle.fill = GridBagConstraints.BOTH;
        gbc_pnlControlsSubtitle.gridx = 0;
        gbc_pnlControlsSubtitle.gridy = 0;
        pnlSubtitle.add(pnlControlsSubtitle, gbc_pnlControlsSubtitle);
        pnlControlsSubtitle.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        cbSubtitle = new JComboBox<String>();
        cbSubtitle.setPreferredSize(new Dimension(150, cbSubtitle.getPreferredSize().height));
        pnlControlsSubtitle.add(cbSubtitle);

        btnAddSubtitle = new JButton("");
        btnAddSubtitle.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
        btnAddSubtitle.setMargin(new Insets(0, 5, 0, 5));
        btnAddSubtitle.setBorderPainted(false);
        btnAddSubtitle.setContentAreaFilled(false);
        btnAddSubtitle.setFocusPainted(false);
        btnAddSubtitle.setOpaque(false);
        pnlControlsSubtitle.add(btnAddSubtitle);

        btnRemoveSubtitle = new JButton("");
        btnRemoveSubtitle.setEnabled(false);
        btnRemoveSubtitle.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
        btnRemoveSubtitle.setMargin(new Insets(0, 0, 0, 0));
        btnRemoveSubtitle.setBorderPainted(false);
        btnRemoveSubtitle.setContentAreaFilled(false);
        btnRemoveSubtitle.setFocusPainted(false);
        btnRemoveSubtitle.setOpaque(false);
        btnRemoveSubtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cbSubtitle.getItemCount() > 0) {
                    int response = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("delete.track.confirm"),
                            LanguageManager.getString("delete.track.title"),
                            JOptionPane.YES_NO_OPTION);

                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }

                    int idx = cbSubtitle.getItemCount() - 1;
                    cbSubtitle.removeItemAt(idx);
                    lyrdPnlSubtitle.remove(idx);
                    nSubtitle--;

                    if (cbSubtitle.getItemCount() == 0) {
                        btnRemoveSubtitle.setEnabled(false);
                    }

                    if (cbSubtitle.getItemCount() < MAX_STREAMS && !btnAddSubtitle.isEnabled()) {
                        btnAddSubtitle.setEnabled(true);
                    }

                    System.gc();
                }
            }
        });
        pnlControlsSubtitle.add(btnRemoveSubtitle);

        lyrdPnlSubtitle = new JPanel();
        GridBagConstraints gbc_lyrdPnlSubtitle = new GridBagConstraints();
        gbc_lyrdPnlSubtitle.fill = GridBagConstraints.BOTH;
        gbc_lyrdPnlSubtitle.gridx = 0;
        gbc_lyrdPnlSubtitle.gridy = 1;
        pnlSubtitle.add(lyrdPnlSubtitle, gbc_lyrdPnlSubtitle);
        lytLyrdPnlSubtitle = new CardLayout(0, 0);
        lyrdPnlSubtitle.setLayout(lytLyrdPnlSubtitle);

        pnlSubtitleProfiles = createProfilePanel(ProfileType.SUBTITLE, listSubtitleProfiles, modelSubtitleProfiles,
                () -> cbSubtitle.getSelectedIndex(),
                (p, idx) -> {
                    p.setEnableTrack(rbYesEnableSubtitle[idx].isSelected());
                    p.setUseEnableTrack(chbEnableSubtitle[idx].isSelected());

                    p.setDefaultTrack(rbYesDefSubtitle[idx].isSelected());
                    p.setUseDefaultTrack(chbDefaultSubtitle[idx].isSelected());

                    p.setForcedTrack(rbYesForcedSubtitle[idx].isSelected());
                    p.setUseForcedTrack(chbForcedSubtitle[idx].isSelected());

                    p.setTrackName(txtNameSubtitle[idx].getText());
                    p.setUseName(chbNameSubtitle[idx].isSelected());

                    p.setLanguage((String) cbLangSubtitle[idx].getSelectedItem());
                    p.setUseLanguage(chbLangSubtitle[idx].isSelected());
                },
                (p, idx) -> applySubtitleProfile(p, idx));
        GridBagConstraints gbc_pnlSubtitleProfiles = new GridBagConstraints();
        gbc_pnlSubtitleProfiles.gridheight = 2;
        gbc_pnlSubtitleProfiles.fill = GridBagConstraints.BOTH;
        gbc_pnlSubtitleProfiles.weightx = 0.0;
        gbc_pnlSubtitleProfiles.weighty = 1.0;
        gbc_pnlSubtitleProfiles.gridx = 1;
        gbc_pnlSubtitleProfiles.gridy = 0;
        gbc_pnlSubtitleProfiles.insets = new Insets(0, 5, 0, 0);
        pnlSubtitle.add(pnlSubtitleProfiles, gbc_pnlSubtitleProfiles);

        pnlAttachments = new JTabbedPane(JTabbedPane.TOP);
        pnlTabs.addTab(LanguageManager.getString("attachments.tab.title"), null, pnlAttachments, null);

        pnlAttachAdd = new JPanel();
        pnlAttachments.addTab(LanguageManager.getString("attachments.tab.add"), null, pnlAttachAdd, null);
        pnlAttachAdd.setLayout(new BorderLayout(0, 0));

        spAttachAdd = new JScrollPane();
        pnlAttachAdd.add(spAttachAdd, BorderLayout.CENTER);

        tblAttachAdd = new JTable();
        tblAttachAdd.setShowGrid(false);
        tblAttachAdd.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachAdd.setModel(modelAttachmentsAdd);
        tblAttachAdd.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachAdd.setAutoscrolls(false);
        tblAttachAdd.setFillsViewportHeight(true);

        spAttachAdd.setViewportView(tblAttachAdd);

        pnlAttachAddControls = new JPanel();
        pnlAttachAddControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachAdd.add(pnlAttachAddControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachAddControls = new GridBagLayout();
        gbl_pnlAttachAddControls.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachAddControls.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
        gbl_pnlAttachAddControls.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachAddControls.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
        pnlAttachAddControls.setLayout(gbl_pnlAttachAddControls);

        lblAttachAddFile = new JLabel(LanguageManager.getString("attachments.file"));
        GridBagConstraints gbc_lblAttachAddFile = new GridBagConstraints();
        gbc_lblAttachAddFile.anchor = GridBagConstraints.WEST;
        gbc_lblAttachAddFile.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachAddFile.gridx = 0;
        gbc_lblAttachAddFile.gridy = 0;
        pnlAttachAddControls.add(lblAttachAddFile, gbc_lblAttachAddFile);

        txtAttachAddFile = new JTextField();
        txtAttachAddFile.setEditable(false);
        GridBagConstraints gbc_txtAttachAddFile = new GridBagConstraints();
        gbc_txtAttachAddFile.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachAddFile.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachAddFile.gridx = 1;
        gbc_txtAttachAddFile.gridy = 0;
        pnlAttachAddControls.add(txtAttachAddFile, gbc_txtAttachAddFile);
        txtAttachAddFile.setColumns(10);

        btnBrowseAttachAddFile = new JButton(LanguageManager.getString("button.browse"));
        GridBagConstraints gbc_btnBrowseAttachAddFile = new GridBagConstraints();
        gbc_btnBrowseAttachAddFile.insets = new Insets(0, 0, 5, 0);
        gbc_btnBrowseAttachAddFile.gridx = 2;
        gbc_btnBrowseAttachAddFile.gridy = 0;
        pnlAttachAddControls.add(btnBrowseAttachAddFile, gbc_btnBrowseAttachAddFile);

        lblAttachAddName = new JLabel(LanguageManager.getString("attachments.name"));
        GridBagConstraints gbc_lblAttachAddName = new GridBagConstraints();
        gbc_lblAttachAddName.anchor = GridBagConstraints.WEST;
        gbc_lblAttachAddName.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachAddName.gridx = 0;
        gbc_lblAttachAddName.gridy = 1;
        pnlAttachAddControls.add(lblAttachAddName, gbc_lblAttachAddName);

        txtAttachAddName = new JTextField();
        GridBagConstraints gbc_txtAttachAddName = new GridBagConstraints();
        gbc_txtAttachAddName.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachAddName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachAddName.gridx = 1;
        gbc_txtAttachAddName.gridy = 1;
        pnlAttachAddControls.add(txtAttachAddName, gbc_txtAttachAddName);
        txtAttachAddName.setColumns(10);

        lblAttachAddDesc = new JLabel(LanguageManager.getString("attachments.description"));
        GridBagConstraints gbc_lblAttachAddDesc = new GridBagConstraints();
        gbc_lblAttachAddDesc.anchor = GridBagConstraints.EAST;
        gbc_lblAttachAddDesc.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachAddDesc.gridx = 0;
        gbc_lblAttachAddDesc.gridy = 2;
        pnlAttachAddControls.add(lblAttachAddDesc, gbc_lblAttachAddDesc);

        txtAttachAddDesc = new JTextField();
        GridBagConstraints gbc_txtAttachAddDesc = new GridBagConstraints();
        gbc_txtAttachAddDesc.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachAddDesc.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachAddDesc.gridx = 1;
        gbc_txtAttachAddDesc.gridy = 2;
        pnlAttachAddControls.add(txtAttachAddDesc, gbc_txtAttachAddDesc);
        txtAttachAddDesc.setColumns(10);

        lblAttachAddMime = new JLabel(LanguageManager.getString("attachments.mime"));
        GridBagConstraints gbc_lblAttachAddMime = new GridBagConstraints();
        gbc_lblAttachAddMime.anchor = GridBagConstraints.EAST;
        gbc_lblAttachAddMime.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachAddMime.gridx = 0;
        gbc_lblAttachAddMime.gridy = 3;
        pnlAttachAddControls.add(lblAttachAddMime, gbc_lblAttachAddMime);

        cbAttachAddMime = new JComboBox<String>();
        cbAttachAddMime.setModel(new DefaultComboBoxModel<String>(mkvStrings.getMimeTypes()));
        GridBagConstraints gbc_cbAttachAddMime = new GridBagConstraints();
        gbc_cbAttachAddMime.insets = new Insets(0, 0, 5, 5);
        gbc_cbAttachAddMime.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbAttachAddMime.gridx = 1;
        gbc_cbAttachAddMime.gridy = 3;
        pnlAttachAddControls.add(cbAttachAddMime, gbc_cbAttachAddMime);

        pnlAttachAddControlsBottom = new JPanel();
        GridBagConstraints gbc_pnlAttachAddControlsBottom = new GridBagConstraints();
        gbc_pnlAttachAddControlsBottom.insets = new Insets(0, 0, 0, 5);
        gbc_pnlAttachAddControlsBottom.fill = GridBagConstraints.BOTH;
        gbc_pnlAttachAddControlsBottom.gridx = 1;
        gbc_pnlAttachAddControlsBottom.gridy = 4;
        pnlAttachAddControls.add(pnlAttachAddControlsBottom, gbc_pnlAttachAddControlsBottom);
        GridBagLayout gbl_pnlAttachAddControlsBottom = new GridBagLayout();
        gbl_pnlAttachAddControlsBottom.columnWidths = new int[] { 0, 0, 0, 0, 0 };
        gbl_pnlAttachAddControlsBottom.rowHeights = new int[] { 0, 0 };
        gbl_pnlAttachAddControlsBottom.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachAddControlsBottom.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlAttachAddControlsBottom.setLayout(gbl_pnlAttachAddControlsBottom);

        btnAttachAddAdd = new JButton(LanguageManager.getString("button.add"));
        GridBagConstraints gbc_btnAttachAddAdd = new GridBagConstraints();
        gbc_btnAttachAddAdd.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachAddAdd.gridx = 0;
        gbc_btnAttachAddAdd.gridy = 0;
        pnlAttachAddControlsBottom.add(btnAttachAddAdd, gbc_btnAttachAddAdd);

        btnAttachAddEdit = new JButton(LanguageManager.getString("button.edit"));
        btnAttachAddEdit.setEnabled(false);
        GridBagConstraints gbc_btnAttachAddEdit = new GridBagConstraints();
        gbc_btnAttachAddEdit.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachAddEdit.gridx = 1;
        gbc_btnAttachAddEdit.gridy = 0;
        pnlAttachAddControlsBottom.add(btnAttachAddEdit, gbc_btnAttachAddEdit);

        btnAttachAddRemove = new JButton(LanguageManager.getString("button.remove"));
        btnAttachAddRemove.setEnabled(false);
        GridBagConstraints gbc_btnAttachAddRemove = new GridBagConstraints();
        gbc_btnAttachAddRemove.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachAddRemove.anchor = GridBagConstraints.SOUTH;
        gbc_btnAttachAddRemove.gridx = 2;
        gbc_btnAttachAddRemove.gridy = 0;
        pnlAttachAddControlsBottom.add(btnAttachAddRemove, gbc_btnAttachAddRemove);

        btnAttachAddCancel = new JButton(LanguageManager.getString("button.cancel"));
        btnAttachAddCancel.setEnabled(false);
        GridBagConstraints gbc_btnAttachAddCancel = new GridBagConstraints();
        gbc_btnAttachAddCancel.gridx = 3;
        gbc_btnAttachAddCancel.gridy = 0;
        pnlAttachAddControlsBottom.add(btnAttachAddCancel, gbc_btnAttachAddCancel);

        pnlAttachReplace = new JPanel();
        pnlAttachments.addTab(LanguageManager.getString("attachments.tab.replace"), null, pnlAttachReplace, null);
        pnlAttachReplace.setLayout(new BorderLayout(0, 0));

        spAttachReplace = new JScrollPane();
        pnlAttachReplace.add(spAttachReplace, BorderLayout.CENTER);

        tblAttachReplace = new JTable();
        tblAttachReplace.setShowGrid(false);
        tblAttachReplace.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachReplace.setModel(modelAttachmentsReplace);
        tblAttachReplace.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachReplace.setAutoscrolls(false);
        tblAttachReplace.setFillsViewportHeight(true);
        spAttachReplace.setViewportView(tblAttachReplace);

        pnlAttachReplaceControls = new JPanel();
        pnlAttachReplaceControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachReplace.add(pnlAttachReplaceControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachReplaceControls = new GridBagLayout();
        gbl_pnlAttachReplaceControls.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachReplaceControls.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_pnlAttachReplaceControls.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachReplaceControls.rowWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        pnlAttachReplaceControls.setLayout(gbl_pnlAttachReplaceControls);

        lblAttachReplaceType = new JLabel(LanguageManager.getString("attachments.type"));
        GridBagConstraints gbc_lblAttachReplaceType = new GridBagConstraints();
        gbc_lblAttachReplaceType.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceType.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceType.gridx = 0;
        gbc_lblAttachReplaceType.gridy = 0;
        pnlAttachReplaceControls.add(lblAttachReplaceType, gbc_lblAttachReplaceType);

        pnlAttachReplaceType = new JPanel();
        GridBagConstraints gbc_pnlAttachReplaceType = new GridBagConstraints();
        gbc_pnlAttachReplaceType.insets = new Insets(0, 0, 5, 5);
        gbc_pnlAttachReplaceType.fill = GridBagConstraints.BOTH;
        gbc_pnlAttachReplaceType.gridx = 1;
        gbc_pnlAttachReplaceType.gridy = 0;
        pnlAttachReplaceControls.add(pnlAttachReplaceType, gbc_pnlAttachReplaceType);
        GridBagLayout gbl_pnlAttachReplaceType = new GridBagLayout();
        gbl_pnlAttachReplaceType.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachReplaceType.rowHeights = new int[] { 0, 0 };
        gbl_pnlAttachReplaceType.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachReplaceType.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlAttachReplaceType.setLayout(gbl_pnlAttachReplaceType);

        rbAttachReplaceName = new JRadioButton(LanguageManager.getString("attachments.type.name"));
        rbAttachReplaceName.setSelected(true);
        GridBagConstraints gbc_rbAttachReplaceName = new GridBagConstraints();
        gbc_rbAttachReplaceName.insets = new Insets(0, 0, 0, 5);
        gbc_rbAttachReplaceName.gridx = 0;
        gbc_rbAttachReplaceName.gridy = 0;
        pnlAttachReplaceType.add(rbAttachReplaceName, gbc_rbAttachReplaceName);
        bgAttachReplaceType.add(rbAttachReplaceName);

        rbAttachReplaceID = new JRadioButton(LanguageManager.getString("attachments.type.id"));
        GridBagConstraints gbc_rbAttachReplaceID = new GridBagConstraints();
        gbc_rbAttachReplaceID.insets = new Insets(0, 0, 0, 5);
        gbc_rbAttachReplaceID.gridx = 1;
        gbc_rbAttachReplaceID.gridy = 0;
        pnlAttachReplaceType.add(rbAttachReplaceID, gbc_rbAttachReplaceID);
        bgAttachReplaceType.add(rbAttachReplaceID);

        rbAttachReplaceMime = new JRadioButton(LanguageManager.getString("attachments.type.mime"));
        GridBagConstraints gbc_rbAttachReplaceMime = new GridBagConstraints();
        gbc_rbAttachReplaceMime.gridx = 2;
        gbc_rbAttachReplaceMime.gridy = 0;
        pnlAttachReplaceType.add(rbAttachReplaceMime, gbc_rbAttachReplaceMime);
        bgAttachReplaceType.add(rbAttachReplaceMime);

        lblAttachReplaceOrig = new JLabel(LanguageManager.getString("attachments.original.value"));
        GridBagConstraints gbc_lblAttachReplaceOrig = new GridBagConstraints();
        gbc_lblAttachReplaceOrig.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceOrig.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceOrig.gridx = 0;
        gbc_lblAttachReplaceOrig.gridy = 1;
        pnlAttachReplaceControls.add(lblAttachReplaceOrig, gbc_lblAttachReplaceOrig);

        pnlAttachReplaceOrig = new JPanel();
        GridBagConstraints gbc_pnlAttachReplaceOrig = new GridBagConstraints();
        gbc_pnlAttachReplaceOrig.insets = new Insets(0, 0, 5, 5);
        gbc_pnlAttachReplaceOrig.fill = GridBagConstraints.BOTH;
        gbc_pnlAttachReplaceOrig.gridx = 1;
        gbc_pnlAttachReplaceOrig.gridy = 1;
        pnlAttachReplaceControls.add(pnlAttachReplaceOrig, gbc_pnlAttachReplaceOrig);
        pnlAttachReplaceOrig.setLayout(new CardLayout(0, 0));

        txtAttachReplaceOrig = new JTextField();
        pnlAttachReplaceOrig.add(txtAttachReplaceOrig, "txtAttachReplaceOrig");
        txtAttachReplaceOrig.setColumns(10);

        cbAttachReplaceOrig = new JComboBox<String>();
        List<String> mimeList = mkvStrings.getMimeTypeList();
        mimeList.remove(0);
        cbAttachReplaceOrig.setModel(new DefaultComboBoxModel<String>(mimeList.toArray(new String[mimeList.size()])));
        cbAttachReplaceOrig.setVisible(false);
        pnlAttachReplaceOrig.add(cbAttachReplaceOrig, "cbAttachReplaceOrig");

        lblAttachReplaceNew = new JLabel(LanguageManager.getString("attachments.replacement"));
        GridBagConstraints gbc_lblAttachReplaceNew = new GridBagConstraints();
        gbc_lblAttachReplaceNew.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceNew.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceNew.gridx = 0;
        gbc_lblAttachReplaceNew.gridy = 2;
        pnlAttachReplaceControls.add(lblAttachReplaceNew, gbc_lblAttachReplaceNew);

        txtAttachReplaceNew = new JTextField();
        txtAttachReplaceNew.setEditable(false);
        GridBagConstraints gbc_txtAttachReplaceNew = new GridBagConstraints();
        gbc_txtAttachReplaceNew.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachReplaceNew.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachReplaceNew.gridx = 1;
        gbc_txtAttachReplaceNew.gridy = 2;
        pnlAttachReplaceControls.add(txtAttachReplaceNew, gbc_txtAttachReplaceNew);
        txtAttachReplaceNew.setColumns(10);

        btnAttachReplaceNewBrowse = new JButton(LanguageManager.getString("button.browse"));
        GridBagConstraints gbc_btnAttachReplaceNewBrowse = new GridBagConstraints();
        gbc_btnAttachReplaceNewBrowse.insets = new Insets(0, 0, 5, 0);
        gbc_btnAttachReplaceNewBrowse.gridx = 2;
        gbc_btnAttachReplaceNewBrowse.gridy = 2;
        pnlAttachReplaceControls.add(btnAttachReplaceNewBrowse, gbc_btnAttachReplaceNewBrowse);

        lblAttachReplaceName = new JLabel(LanguageManager.getString("attachments.name"));
        GridBagConstraints gbc_lblAttachReplaceName = new GridBagConstraints();
        gbc_lblAttachReplaceName.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceName.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceName.gridx = 0;
        gbc_lblAttachReplaceName.gridy = 3;
        pnlAttachReplaceControls.add(lblAttachReplaceName, gbc_lblAttachReplaceName);

        txtAttachReplaceName = new JTextField();
        txtAttachReplaceName.setColumns(10);
        GridBagConstraints gbc_txtAttachReplaceName = new GridBagConstraints();
        gbc_txtAttachReplaceName.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachReplaceName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachReplaceName.gridx = 1;
        gbc_txtAttachReplaceName.gridy = 3;
        pnlAttachReplaceControls.add(txtAttachReplaceName, gbc_txtAttachReplaceName);

        lblAttachReplaceDesc = new JLabel(LanguageManager.getString("attachments.description"));
        GridBagConstraints gbc_lblAttachReplaceDesc = new GridBagConstraints();
        gbc_lblAttachReplaceDesc.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceDesc.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceDesc.gridx = 0;
        gbc_lblAttachReplaceDesc.gridy = 4;
        pnlAttachReplaceControls.add(lblAttachReplaceDesc, gbc_lblAttachReplaceDesc);

        txtAttachReplaceDesc = new JTextField();
        txtAttachReplaceDesc.setColumns(10);
        GridBagConstraints gbc_txtAttachReplaceDesc = new GridBagConstraints();
        gbc_txtAttachReplaceDesc.insets = new Insets(0, 0, 5, 5);
        gbc_txtAttachReplaceDesc.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAttachReplaceDesc.gridx = 1;
        gbc_txtAttachReplaceDesc.gridy = 4;
        pnlAttachReplaceControls.add(txtAttachReplaceDesc, gbc_txtAttachReplaceDesc);

        lblAttachReplaceMime = new JLabel(LanguageManager.getString("attachments.mime"));
        GridBagConstraints gbc_lblAttachReplaceMime = new GridBagConstraints();
        gbc_lblAttachReplaceMime.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceMime.gridx = 0;
        gbc_lblAttachReplaceMime.gridy = 5;
        pnlAttachReplaceControls.add(lblAttachReplaceMime, gbc_lblAttachReplaceMime);

        cbAttachReplaceMime = new JComboBox<String>();
        cbAttachReplaceMime.setModel(new DefaultComboBoxModel<String>(mkvStrings.getMimeTypes()));
        GridBagConstraints gbc_cbAttachReplaceMime = new GridBagConstraints();
        gbc_cbAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
        gbc_cbAttachReplaceMime.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbAttachReplaceMime.gridx = 1;
        gbc_cbAttachReplaceMime.gridy = 5;
        pnlAttachReplaceControls.add(cbAttachReplaceMime, gbc_cbAttachReplaceMime);

        pnlAttachReplaceControlsBottom = new JPanel();
        GridBagConstraints gbc_pnlAttachReplaceControlsBottom = new GridBagConstraints();
        gbc_pnlAttachReplaceControlsBottom.anchor = GridBagConstraints.WEST;
        gbc_pnlAttachReplaceControlsBottom.insets = new Insets(0, 0, 0, 5);
        gbc_pnlAttachReplaceControlsBottom.fill = GridBagConstraints.VERTICAL;
        gbc_pnlAttachReplaceControlsBottom.gridx = 1;
        gbc_pnlAttachReplaceControlsBottom.gridy = 6;
        pnlAttachReplaceControls.add(pnlAttachReplaceControlsBottom, gbc_pnlAttachReplaceControlsBottom);
        GridBagLayout gbl_pnlAttachReplaceControlsBottom = new GridBagLayout();
        gbl_pnlAttachReplaceControlsBottom.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachReplaceControlsBottom.rowHeights = new int[] { 0, 0 };
        gbl_pnlAttachReplaceControlsBottom.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
        gbl_pnlAttachReplaceControlsBottom.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlAttachReplaceControlsBottom.setLayout(gbl_pnlAttachReplaceControlsBottom);

        btnAttachReplaceAdd = new JButton(LanguageManager.getString("button.add"));
        GridBagConstraints gbc_btnAttachReplaceAdd = new GridBagConstraints();
        gbc_btnAttachReplaceAdd.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachReplaceAdd.gridx = 0;
        gbc_btnAttachReplaceAdd.gridy = 0;
        pnlAttachReplaceControlsBottom.add(btnAttachReplaceAdd, gbc_btnAttachReplaceAdd);

        btnAttachReplaceEdit = new JButton(LanguageManager.getString("button.edit"));
        btnAttachReplaceEdit.setEnabled(false);
        GridBagConstraints gbc_btnAttachReplaceEdit = new GridBagConstraints();
        gbc_btnAttachReplaceEdit.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachReplaceEdit.gridx = 1;
        gbc_btnAttachReplaceEdit.gridy = 0;
        pnlAttachReplaceControlsBottom.add(btnAttachReplaceEdit, gbc_btnAttachReplaceEdit);

        btnAttachReplaceRemove = new JButton(LanguageManager.getString("button.remove"));
        btnAttachReplaceRemove.setEnabled(false);
        GridBagConstraints gbc_btnAttachReplaceRemove = new GridBagConstraints();
        gbc_btnAttachReplaceRemove.anchor = GridBagConstraints.SOUTH;
        gbc_btnAttachReplaceRemove.insets = new Insets(0, 0, 0, 5);
        gbc_btnAttachReplaceRemove.gridx = 2;
        gbc_btnAttachReplaceRemove.gridy = 0;
        pnlAttachReplaceControlsBottom.add(btnAttachReplaceRemove, gbc_btnAttachReplaceRemove);

        btnAttachReplaceCancel = new JButton(LanguageManager.getString("button.cancel"));
        btnAttachReplaceCancel.setEnabled(false);
        GridBagConstraints gbc_btnAttachReplaceCancel = new GridBagConstraints();
        gbc_btnAttachReplaceCancel.gridx = 3;
        gbc_btnAttachReplaceCancel.gridy = 0;
        pnlAttachReplaceControlsBottom.add(btnAttachReplaceCancel, gbc_btnAttachReplaceCancel);

        pnlAttachDelete = new JPanel();
        pnlAttachments.addTab(LanguageManager.getString("attachments.tab.delete"), null, pnlAttachDelete, null);
        pnlAttachDelete.setLayout(new BorderLayout(0, 0));

        spAttachDelete = new JScrollPane();
        pnlAttachDelete.add(spAttachDelete, BorderLayout.CENTER);

        tblAttachDelete = new JTable();
        tblAttachDelete.setShowGrid(false);
        tblAttachDelete.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachDelete.setModel(modelAttachmentsDelete);
        tblAttachDelete.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachDelete.setAutoscrolls(false);
        tblAttachDelete.setFillsViewportHeight(true);
        spAttachDelete.setViewportView(tblAttachDelete);

        pnlAttachDeleteControls = new JPanel();
        pnlAttachDeleteControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachDelete.add(pnlAttachDeleteControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachDeleteControls = new GridBagLayout();
        gbl_pnlAttachDeleteControls.columnWidths = new int[] { 0, 0, 0 };
        gbl_pnlAttachDeleteControls.rowHeights = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachDeleteControls.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_pnlAttachDeleteControls.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
        pnlAttachDeleteControls.setLayout(gbl_pnlAttachDeleteControls);

        lblAttachDeleteType = new JLabel(LanguageManager.getString("attachments.type"));
        GridBagConstraints gbc_lblAttachDeleteType = new GridBagConstraints();
        gbc_lblAttachDeleteType.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachDeleteType.gridx = 0;
        gbc_lblAttachDeleteType.gridy = 0;
        pnlAttachDeleteControls.add(lblAttachDeleteType, gbc_lblAttachDeleteType);

        pnlAttachDeleteType = new JPanel();
        GridBagConstraints gbc_pnlAttachDeleteType = new GridBagConstraints();
        gbc_pnlAttachDeleteType.anchor = GridBagConstraints.WEST;
        gbc_pnlAttachDeleteType.insets = new Insets(0, 0, 5, 0);
        gbc_pnlAttachDeleteType.fill = GridBagConstraints.VERTICAL;
        gbc_pnlAttachDeleteType.gridx = 1;
        gbc_pnlAttachDeleteType.gridy = 0;
        pnlAttachDeleteControls.add(pnlAttachDeleteType, gbc_pnlAttachDeleteType);
        GridBagLayout gbl_pnlAttachDeleteType = new GridBagLayout();
        gbl_pnlAttachDeleteType.columnWidths = new int[] { 0, 0, 0 };
        gbl_pnlAttachDeleteType.rowHeights = new int[] { 0, 0 };
        gbl_pnlAttachDeleteType.columnWeights = new double[] { 0.0, 0.0, 0.0 };
        gbl_pnlAttachDeleteType.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlAttachDeleteType.setLayout(gbl_pnlAttachDeleteType);

        rbAttachDeleteName = new JRadioButton(LanguageManager.getString("attachments.type.name"));
        rbAttachDeleteName.setSelected(true);
        GridBagConstraints gbc_rbAttachDeleteName = new GridBagConstraints();
        gbc_rbAttachDeleteName.insets = new Insets(0, 0, 0, 5);
        gbc_rbAttachDeleteName.gridx = 0;
        gbc_rbAttachDeleteName.gridy = 0;
        pnlAttachDeleteType.add(rbAttachDeleteName, gbc_rbAttachDeleteName);
        bgAttachDeleteType.add(rbAttachDeleteName);

        rbAttachDeleteID = new JRadioButton(LanguageManager.getString("attachments.type.id"));
        GridBagConstraints gbc_rbAttachDeleteID = new GridBagConstraints();
        gbc_rbAttachDeleteID.insets = new Insets(0, 0, 0, 5);
        gbc_rbAttachDeleteID.gridx = 1;
        gbc_rbAttachDeleteID.gridy = 0;
        pnlAttachDeleteType.add(rbAttachDeleteID, gbc_rbAttachDeleteID);
        bgAttachDeleteType.add(rbAttachDeleteID);

        rbAttachDeleteMime = new JRadioButton(LanguageManager.getString("attachments.type.mime"));
        GridBagConstraints gbc_rbAttachDeleteMime = new GridBagConstraints();
        gbc_rbAttachDeleteMime.gridx = 2;
        gbc_rbAttachDeleteMime.gridy = 0;
        pnlAttachDeleteType.add(rbAttachDeleteMime, gbc_rbAttachDeleteMime);
        bgAttachDeleteType.add(rbAttachDeleteMime);

        lblAttachDeleteValue = new JLabel(LanguageManager.getString("attachments.original.value"));
        GridBagConstraints gbc_lblAttachDeleteValue = new GridBagConstraints();
        gbc_lblAttachDeleteValue.anchor = GridBagConstraints.EAST;
        gbc_lblAttachDeleteValue.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachDeleteValue.gridx = 0;
        gbc_lblAttachDeleteValue.gridy = 1;
        pnlAttachDeleteControls.add(lblAttachDeleteValue, gbc_lblAttachDeleteValue);

        pnlAttachDeleteValue = new JPanel();
        GridBagConstraints gbc_pnlAttachDeleteValue = new GridBagConstraints();
        gbc_pnlAttachDeleteValue.insets = new Insets(0, 0, 5, 0);
        gbc_pnlAttachDeleteValue.fill = GridBagConstraints.BOTH;
        gbc_pnlAttachDeleteValue.gridx = 1;
        gbc_pnlAttachDeleteValue.gridy = 1;
        pnlAttachDeleteControls.add(pnlAttachDeleteValue, gbc_pnlAttachDeleteValue);
        pnlAttachDeleteValue.setLayout(new CardLayout(0, 0));

        txtAttachDeleteValue = new JTextField();
        pnlAttachDeleteValue.add(txtAttachDeleteValue, "txtAttachDeleteValue");
        txtAttachDeleteValue.setColumns(10);

        cbAttachDeleteValue = new JComboBox<String>();
        cbAttachDeleteValue.setVisible(false);
        cbAttachDeleteValue.setModel(new DefaultComboBoxModel<String>(mimeList.toArray(new String[mimeList.size()])));
        pnlAttachDeleteValue.add(cbAttachDeleteValue, "cbAttachDeleteValue");

        pnlAttachDeleteControlsBottom = new JPanel();
        GridBagConstraints gbc_pnlAttachDeleteControlsBottom = new GridBagConstraints();
        gbc_pnlAttachDeleteControlsBottom.fill = GridBagConstraints.BOTH;
        gbc_pnlAttachDeleteControlsBottom.gridx = 1;
        gbc_pnlAttachDeleteControlsBottom.gridy = 2;
        pnlAttachDeleteControls.add(pnlAttachDeleteControlsBottom, gbc_pnlAttachDeleteControlsBottom);
        GridBagLayout gbl_pnlAttachDeleteControlsBottom = new GridBagLayout();
        gbl_pnlAttachDeleteControlsBottom.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_pnlAttachDeleteControlsBottom.rowHeights = new int[] { 0, 0 };
        gbl_pnlAttachDeleteControlsBottom.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachDeleteControlsBottom.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        pnlAttachDeleteControlsBottom.setLayout(gbl_pnlAttachDeleteControlsBottom);

        btnAttachDeleteAdd = new JButton(LanguageManager.getString("button.add"));
        GridBagConstraints gbc_btnAttachDeleteAdd = new GridBagConstraints();
        gbc_btnAttachDeleteAdd.insets = new Insets(0, 0, 5, 5);
        gbc_btnAttachDeleteAdd.gridx = 0;
        gbc_btnAttachDeleteAdd.gridy = 0;
        pnlAttachDeleteControlsBottom.add(btnAttachDeleteAdd, gbc_btnAttachDeleteAdd);

        btnAttachDeleteEdit = new JButton(LanguageManager.getString("button.edit"));
        btnAttachDeleteEdit.setEnabled(false);
        GridBagConstraints gbc_btnAttachDeleteEdit = new GridBagConstraints();
        gbc_btnAttachDeleteEdit.insets = new Insets(0, 0, 5, 5);
        gbc_btnAttachDeleteEdit.gridx = 1;
        gbc_btnAttachDeleteEdit.gridy = 0;
        pnlAttachDeleteControlsBottom.add(btnAttachDeleteEdit, gbc_btnAttachDeleteEdit);

        btnAttachDeleteRemove = new JButton(LanguageManager.getString("button.remove"));
        btnAttachDeleteRemove.setEnabled(false);
        GridBagConstraints gbc_btnAttachDeleteRemove = new GridBagConstraints();
        gbc_btnAttachDeleteRemove.anchor = GridBagConstraints.SOUTH;
        gbc_btnAttachDeleteRemove.insets = new Insets(0, 0, 5, 5);
        gbc_btnAttachDeleteRemove.gridx = 2;
        gbc_btnAttachDeleteRemove.gridy = 0;
        pnlAttachDeleteControlsBottom.add(btnAttachDeleteRemove, gbc_btnAttachDeleteRemove);

        btnAttachDeleteCancel = new JButton(LanguageManager.getString("button.cancel"));
        btnAttachDeleteCancel.setEnabled(false);
        GridBagConstraints gbc_btnAttachDeleteCancel = new GridBagConstraints();
        gbc_btnAttachDeleteCancel.insets = new Insets(0, 0, 5, 5);
        gbc_btnAttachDeleteCancel.gridx = 3;
        gbc_btnAttachDeleteCancel.gridy = 0;
        pnlAttachDeleteControlsBottom.add(btnAttachDeleteCancel, gbc_btnAttachDeleteCancel);

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
                int[] widths = new int[tblAttachAdd.getColumnCount()];
                for (int i = 0; i < tblAttachAdd.getColumnCount(); i++) {
                    widths[i] = tblAttachAdd.getColumnModel().getColumn(i).getPreferredWidth();
                }

                saveLanguage(langCode);

                // Instant reload
                frmJMkvpropedit.dispose();
                // Re-initialize logic
                nVideo = 0;
                nAudio = 0;
                nSubtitle = 0;
                LanguageManager.setLocale(Locale.forLanguageTag(langCode));
                initialize();
                frmJMkvpropedit.setVisible(true);

                if (widths.length == tblAttachAdd.getColumnCount()) {
                    for (int i = 0; i < tblAttachAdd.getColumnCount(); i++) {
                        tblAttachAdd.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
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
        Utils.addRCMenuMouseListener(txtAttachAddFile);
        Utils.addRCMenuMouseListener(txtAttachAddName);
        Utils.addRCMenuMouseListener(txtAttachAddDesc);
        Utils.addRCMenuMouseListener(txtAttachReplaceOrig);
        Utils.addRCMenuMouseListener(txtAttachReplaceNew);
        Utils.addRCMenuMouseListener(txtAttachReplaceName);
        Utils.addRCMenuMouseListener(txtAttachReplaceDesc);
        Utils.addRCMenuMouseListener(txtAttachDeleteValue);
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
                addVideoTrack();
                addAudioTrack();
                addSubtitleTrack();
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
                    resizeColumns(tblAttachAdd, COLUMN_SIZES_ATTACHMENTS_ADD);
                    resizeColumns(tblAttachReplace, COLUMN_SIZES_ATTACHMENTS_REPLACE);
                    resizeColumns(tblAttachDelete, COLUMN_SIZES_ATTACHMENTS_DELETE);
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

                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.file"));
                chooser.setMultiSelectionEnabled(true);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.resetChoosableFileFilters();
                chooser.setFileFilter(MATROSKA_EXT_FILTER);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    files = chooser.getSelectedFiles();
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

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.folder"));
                chooser.setAcceptAllFileFilterUsed(false);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    folder = chooser.getSelectedFile();
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
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.chapters"));
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.resetChoosableFileFilters();
                chooser.setFileFilter(TXT_EXT_FILTER);
                chooser.setFileFilter(XML_EXT_FILTER);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (chooser.getSelectedFile().exists()) {
                        txtChapters.setText(chooser.getSelectedFile().toString());
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
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.tags"));
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.resetChoosableFileFilters();
                chooser.setFileFilter(TXT_EXT_FILTER);
                chooser.setFileFilter(XML_EXT_FILTER);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (chooser.getSelectedFile().exists()) {
                        txtTags.setText(chooser.getSelectedFile().toString());
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
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.exe"));
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.resetChoosableFileFilters();

                if (Utils.isWindows()) {
                    chooser.setFileFilter(EXE_EXT_FILTER);
                }

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    if (chooser.getSelectedFile().exists()) {
                        saveIniFile(chooser.getSelectedFile());
                    }
                }
            }
        });

        cbVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lytLyrdPnlVideo.show(lyrdPnlVideo, "subPnlVideo[" + cbVideo.getSelectedIndex() + "]");
            }
        });

        btnAddVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addVideoTrack();

                cbVideo.setSelectedIndex(cbVideo.getItemCount() - 1);
                if (cbVideo.getItemCount() == MAX_STREAMS) {
                    btnAddVideo.setEnabled(false);
                }

                if (!btnRemoveVideo.isEnabled()) {
                    btnRemoveVideo.setEnabled(true);
                }
            }

        });

        cbAudio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lytLyrdPnlAudio.show(lyrdPnlAudio, "subPnlAudio[" + cbAudio.getSelectedIndex() + "]");
            }
        });

        btnAddAudio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAudioTrack();

                cbAudio.setSelectedIndex(cbAudio.getItemCount() - 1);
                if (cbAudio.getItemCount() == MAX_STREAMS) {
                    btnAddAudio.setEnabled(false);
                }

                if (!btnRemoveAudio.isEnabled()) {
                    btnRemoveAudio.setEnabled(true);
                }
            }

        });

        cbSubtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lytLyrdPnlSubtitle.show(lyrdPnlSubtitle, "subPnlSubtitle[" + cbSubtitle.getSelectedIndex() + "]");
            }
        });

        btnAddSubtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSubtitleTrack();

                cbSubtitle.setSelectedIndex(cbSubtitle.getItemCount() - 1);
                if (cbSubtitle.getItemCount() == MAX_STREAMS) {
                    btnAddSubtitle.setEnabled(false);
                }

                if (!btnRemoveSubtitle.isEnabled()) {
                    btnRemoveSubtitle.setEnabled(true);
                }
            }

        });

        new FileDrop(txtAttachAddFile, new FileDrop.Listener() {
            public void filesDropped(File[] files) {
                try {
                    if (!files[0].isDirectory()) {
                        txtAttachAddFile.setText(files[0].getCanonicalPath());
                    }
                } catch (IOException e) {
                }
            }
        });

        btnBrowseAttachAddFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.attachment"));
                chooser.setMultiSelectionEnabled(false);
                chooser.resetChoosableFileFilters();
                chooser.setAcceptAllFileFilterUsed(true);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();

                    if (f.exists()) {
                        try {
                            txtAttachAddFile.setText(f.getCanonicalPath());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        tblAttachAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modelAttachmentsAdd.getRowCount() == 0 || !tblAttachAdd.isEnabled()) {
                    return;
                }

                int selection = tblAttachAdd.getSelectedRow();

                if (selection != -1) {
                    String file = modelAttachmentsAdd.getValueAt(selection, 0).toString();
                    String name = modelAttachmentsAdd.getValueAt(selection, 1).toString();
                    String desc = modelAttachmentsAdd.getValueAt(selection, 2).toString();
                    String mime = modelAttachmentsAdd.getValueAt(selection, 3).toString();

                    txtAttachAddFile.setText(file);
                    txtAttachAddName.setText(name);
                    txtAttachAddDesc.setText(desc);
                    cbAttachAddMime.setSelectedItem(mime);

                    tblAttachAdd.setEnabled(false);
                    btnAttachAddAdd.setEnabled(false);
                    btnAttachAddRemove.setEnabled(true);
                    btnAttachAddEdit.setEnabled(true);
                    btnAttachAddCancel.setEnabled(true);
                }
            }
        });

        btnAttachAddAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (txtAttachAddFile.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, LanguageManager.getString("error.attachment.file"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                String[] rowData = { txtAttachAddFile.getText(), txtAttachAddName.getText().trim(),
                        txtAttachAddDesc.getText().trim(), cbAttachAddMime.getSelectedItem().toString() };

                modelAttachmentsAdd.addRow(rowData);

                Utils.adjustColumnPreferredWidths(tblAttachAdd);
                tblAttachAdd.revalidate();

                txtAttachAddFile.setText("");
                txtAttachAddName.setText("");
                txtAttachAddDesc.setText("");
                cbAttachAddMime.setSelectedIndex(0);
            }
        });

        btnAttachAddEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (txtAttachAddFile.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, LanguageManager.getString("error.attachment.file"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                int selection = tblAttachAdd.getSelectedRow();

                String file = txtAttachAddFile.getText().trim();
                String name = txtAttachAddName.getText().trim();
                String desc = txtAttachAddDesc.getText().trim();
                String mime = cbAttachAddMime.getSelectedItem().toString();

                modelAttachmentsAdd.setValueAt(file, selection, 0);
                modelAttachmentsAdd.setValueAt(name, selection, 1);
                modelAttachmentsAdd.setValueAt(desc, selection, 2);
                modelAttachmentsAdd.setValueAt(mime, selection, 3);

                Utils.adjustColumnPreferredWidths(tblAttachAdd);
                tblAttachAdd.revalidate();

                txtAttachAddFile.setText("");
                txtAttachAddName.setText("");
                txtAttachAddDesc.setText("");
                cbAttachAddMime.setSelectedIndex(0);

                tblAttachAdd.setEnabled(true);
                btnAttachAddAdd.setEnabled(true);
                btnAttachAddRemove.setEnabled(false);
                btnAttachAddEdit.setEnabled(false);
                btnAttachAddCancel.setEnabled(false);
                tblAttachAdd.clearSelection();
            }
        });

        btnAttachAddRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selection = tblAttachAdd.getSelectedRow();

                modelAttachmentsAdd.removeRow(selection);

                txtAttachAddFile.setText("");
                txtAttachAddName.setText("");
                txtAttachAddDesc.setText("");
                cbAttachAddMime.setSelectedIndex(0);

                tblAttachAdd.setEnabled(true);
                btnAttachAddAdd.setEnabled(true);
                btnAttachAddRemove.setEnabled(false);
                btnAttachAddEdit.setEnabled(false);
                btnAttachAddCancel.setEnabled(false);
            }
        });

        btnAttachAddCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtAttachAddFile.setText("");
                txtAttachAddName.setText("");
                txtAttachAddDesc.setText("");
                cbAttachAddMime.setSelectedIndex(0);

                tblAttachAdd.setEnabled(true);
                btnAttachAddAdd.setEnabled(true);
                btnAttachAddRemove.setEnabled(false);
                btnAttachAddEdit.setEnabled(false);
                btnAttachAddCancel.setEnabled(false);
                tblAttachAdd.clearSelection();
            }
        });

        rbAttachReplaceName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cbAttachReplaceOrig.setVisible(false);
                txtAttachReplaceOrig.setVisible(true);
                txtAttachReplaceOrig.setText("");
            }
        });

        rbAttachReplaceID.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cbAttachReplaceOrig.setVisible(false);
                txtAttachReplaceOrig.setVisible(true);
                txtAttachReplaceOrig.setText("1");
            }
        });

        rbAttachReplaceMime.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtAttachReplaceOrig.setVisible(false);
                cbAttachReplaceOrig.setVisible(true);
                cbAttachReplaceOrig.setSelectedIndex(0);
            }
        });

        txtAttachReplaceOrig.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!rbAttachReplaceID.isSelected()) {
                    return;
                }

                try {
                    int id = Integer.parseInt(txtAttachReplaceOrig.getText());

                    if (id < 1) {
                        txtAttachReplaceOrig.setText("1");
                    }
                } catch (NumberFormatException e1) {
                    txtAttachReplaceOrig.setText("1");
                }
            }
        });

        new FileDrop(txtAttachReplaceNew, new FileDrop.Listener() {
            public void filesDropped(File[] files) {
                try {
                    if (!files[0].isDirectory()) {
                        txtAttachReplaceNew.setText(files[0].getCanonicalPath());
                    }
                } catch (IOException e) {
                }
            }
        });

        btnAttachReplaceNewBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle(LanguageManager.getString("chooser.title.attachment"));
                chooser.setMultiSelectionEnabled(false);
                chooser.resetChoosableFileFilters();
                chooser.setAcceptAllFileFilterUsed(true);

                int open = chooser.showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();

                    if (f.exists()) {
                        try {
                            txtAttachReplaceNew.setText(f.getCanonicalPath());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        tblAttachReplace.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modelAttachmentsReplace.getRowCount() == 0 || !tblAttachReplace.isEnabled()) {
                    return;
                }

                int selection = tblAttachReplace.getSelectedRow();

                if (selection != -1) {
                    String type = modelAttachmentsReplace.getValueAt(selection, 0).toString();
                    String orig = modelAttachmentsReplace.getValueAt(selection, 1).toString();
                    String replace = modelAttachmentsReplace.getValueAt(selection, 2).toString();
                    String name = modelAttachmentsReplace.getValueAt(selection, 3).toString();
                    String desc = modelAttachmentsReplace.getValueAt(selection, 4).toString();
                    String mime = modelAttachmentsReplace.getValueAt(selection, 5).toString();

                    txtAttachReplaceNew.setText(replace);

                    if (type.equals(rbAttachReplaceName.getText())) {
                        txtAttachReplaceOrig.setVisible(true);
                        cbAttachReplaceOrig.setVisible(false);
                        rbAttachReplaceName.setSelected(true);
                        txtAttachReplaceOrig.setText(orig);
                    } else if (type.equals(rbAttachReplaceID.getText())) {
                        txtAttachReplaceOrig.setVisible(true);
                        cbAttachReplaceOrig.setVisible(false);
                        rbAttachReplaceID.setSelected(true);
                        txtAttachReplaceOrig.setText(orig);
                    } else {
                        txtAttachReplaceOrig.setVisible(false);
                        cbAttachReplaceOrig.setVisible(true);
                        rbAttachReplaceMime.setSelected(true);
                        cbAttachReplaceOrig.setSelectedItem(replace);
                    }

                    txtAttachReplaceName.setText(name);
                    txtAttachReplaceDesc.setText(desc);
                    cbAttachReplaceMime.setSelectedItem(mime);

                    tblAttachReplace.setEnabled(false);
                    btnAttachReplaceAdd.setEnabled(false);
                    btnAttachReplaceRemove.setEnabled(true);
                    btnAttachReplaceEdit.setEnabled(true);
                    btnAttachReplaceCancel.setEnabled(true);
                }
            }
        });

        btnAttachReplaceAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String type = "";
                String orig = "";

                if (rbAttachReplaceName.isSelected()) {
                    type = rbAttachReplaceName.getText();
                    orig = txtAttachReplaceOrig.getText().trim();
                } else if (rbAttachReplaceID.isSelected()) {
                    type = rbAttachReplaceID.getText();
                    orig = txtAttachReplaceOrig.getText();
                } else {
                    type = rbAttachReplaceMime.getText();
                    orig = cbAttachReplaceOrig.getSelectedItem().toString();
                }

                if (orig.isEmpty() || txtAttachReplaceNew.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            LanguageManager.getString("error.attachment.replace"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                String[] rowData = { type, orig, txtAttachReplaceNew.getText(), txtAttachReplaceName.getText().trim(),
                        txtAttachReplaceDesc.getText().trim(), cbAttachReplaceMime.getSelectedItem().toString() };

                modelAttachmentsReplace.addRow(rowData);

                Utils.adjustColumnPreferredWidths(tblAttachReplace);
                tblAttachReplace.revalidate();

                txtAttachReplaceOrig.setText("");
                txtAttachReplaceNew.setText("");
                txtAttachReplaceName.setText("");
                txtAttachReplaceDesc.setText("");
                cbAttachReplaceMime.setSelectedIndex(0);
                rbAttachReplaceName.setSelected(true);
                txtAttachReplaceOrig.setVisible(true);
                cbAttachReplaceOrig.setVisible(false);
            }
        });

        btnAttachReplaceEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String type = "";
                String orig = "";

                if (rbAttachReplaceName.isSelected()) {
                    type = rbAttachReplaceName.getText();
                    orig = txtAttachReplaceOrig.getText().trim();
                } else if (rbAttachReplaceID.isSelected()) {
                    type = rbAttachReplaceID.getText();
                    orig = txtAttachReplaceOrig.getText();
                } else {
                    type = rbAttachReplaceMime.getText();
                    orig = cbAttachReplaceOrig.getSelectedItem().toString();
                }

                int selection = tblAttachReplace.getSelectedRow();

                if (orig.isEmpty() || txtAttachReplaceNew.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            LanguageManager.getString("error.attachment.replace"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                modelAttachmentsReplace.setValueAt(type, selection, 0);
                modelAttachmentsReplace.setValueAt(orig, selection, 1);
                modelAttachmentsReplace.setValueAt(txtAttachReplaceNew.getText(), selection, 2);
                modelAttachmentsReplace.setValueAt(txtAttachReplaceName.getText(), selection, 3);
                modelAttachmentsReplace.setValueAt(txtAttachReplaceDesc.getText(), selection, 4);
                modelAttachmentsReplace.setValueAt(cbAttachReplaceMime.getSelectedItem().toString(), selection, 5);

                Utils.adjustColumnPreferredWidths(tblAttachReplace);
                tblAttachReplace.revalidate();

                tblAttachReplace.setEnabled(true);
                btnAttachReplaceAdd.setEnabled(true);
                btnAttachReplaceEdit.setEnabled(false);
                btnAttachReplaceRemove.setEnabled(false);
                btnAttachReplaceCancel.setEnabled(false);
                tblAttachReplace.clearSelection();

                txtAttachReplaceOrig.setText("");
                txtAttachReplaceNew.setText("");
                txtAttachReplaceName.setText("");
                txtAttachReplaceDesc.setText("");
                cbAttachReplaceMime.setSelectedIndex(0);
                rbAttachReplaceName.setSelected(true);
                txtAttachReplaceOrig.setVisible(true);
                cbAttachReplaceOrig.setVisible(false);
            }
        });

        btnAttachReplaceRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selection = tblAttachReplace.getSelectedRow();

                modelAttachmentsReplace.removeRow(selection);

                tblAttachReplace.setEnabled(true);
                btnAttachReplaceAdd.setEnabled(true);
                btnAttachReplaceEdit.setEnabled(false);
                btnAttachReplaceRemove.setEnabled(false);
                btnAttachReplaceCancel.setEnabled(false);
                tblAttachReplace.clearSelection();

                txtAttachReplaceOrig.setText("");
                txtAttachReplaceNew.setText("");
                txtAttachReplaceName.setText("");
                txtAttachReplaceDesc.setText("");
                cbAttachReplaceMime.setSelectedIndex(0);
                rbAttachReplaceName.setSelected(true);
                txtAttachReplaceOrig.setVisible(true);
                cbAttachReplaceOrig.setVisible(false);
            }
        });

        btnAttachReplaceCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tblAttachReplace.setEnabled(true);
                btnAttachReplaceAdd.setEnabled(true);
                btnAttachReplaceEdit.setEnabled(false);
                btnAttachReplaceRemove.setEnabled(false);
                btnAttachReplaceCancel.setEnabled(false);
                tblAttachReplace.clearSelection();

                txtAttachReplaceOrig.setText("");
                txtAttachReplaceNew.setText("");
                txtAttachReplaceName.setText("");
                txtAttachReplaceDesc.setText("");
                cbAttachReplaceMime.setSelectedIndex(0);
                rbAttachReplaceName.setSelected(true);
                txtAttachReplaceOrig.setVisible(true);
                cbAttachReplaceOrig.setVisible(false);
            }
        });

        tblAttachDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modelAttachmentsDelete.getRowCount() == 0 || !tblAttachDelete.isEnabled()) {
                    return;
                }

                int selection = tblAttachDelete.getSelectedRow();

                if (selection != -1) {
                    String type = modelAttachmentsDelete.getValueAt(selection, 0).toString();
                    String value = modelAttachmentsDelete.getValueAt(selection, 1).toString();

                    if (type.equals(rbAttachDeleteName.getText())) {
                        rbAttachDeleteName.setSelected(true);
                        cbAttachDeleteValue.setVisible(false);
                        txtAttachDeleteValue.setVisible(true);
                        txtAttachDeleteValue.setText(value);
                    } else if (type.equals(rbAttachDeleteID.getText())) {
                        rbAttachDeleteID.setSelected(true);
                        cbAttachDeleteValue.setVisible(false);
                        txtAttachDeleteValue.setVisible(true);
                        txtAttachDeleteValue.setText(value);
                    } else {
                        rbAttachDeleteMime.setSelected(true);
                        txtAttachDeleteValue.setVisible(false);
                        cbAttachDeleteValue.setVisible(true);
                        cbAttachDeleteValue.setSelectedItem(value);
                    }

                    tblAttachDelete.setEnabled(false);
                    btnAttachDeleteAdd.setEnabled(false);
                    btnAttachDeleteEdit.setEnabled(true);
                    btnAttachDeleteRemove.setEnabled(true);
                    btnAttachDeleteCancel.setEnabled(true);
                }
            }
        });

        rbAttachDeleteName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("");
            }
        });

        rbAttachDeleteID.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("1");
            }
        });

        rbAttachDeleteMime.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtAttachDeleteValue.setVisible(false);
                cbAttachDeleteValue.setVisible(true);
                cbAttachDeleteValue.setSelectedIndex(0);
            }
        });

        txtAttachDeleteValue.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!rbAttachDeleteID.isSelected()) {
                    return;
                }

                try {
                    int id = Integer.parseInt(txtAttachDeleteValue.getText());

                    if (id < 1) {
                        txtAttachDeleteValue.setText("1");
                    }
                } catch (NumberFormatException e1) {
                    txtAttachDeleteValue.setText("1");
                }
            }
        });

        btnAttachDeleteAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String type = "";
                String value = "";

                if (rbAttachDeleteName.isSelected()) {
                    type = rbAttachDeleteName.getText();
                    value = txtAttachDeleteValue.getText().trim();
                } else if (rbAttachDeleteID.isSelected()) {
                    type = rbAttachDeleteID.getText();
                    value = txtAttachDeleteValue.getText();
                } else {
                    type = rbAttachDeleteMime.getText();
                    value = cbAttachDeleteValue.getSelectedItem().toString();
                }

                if (value.isEmpty()) {
                    JOptionPane.showMessageDialog(null, LanguageManager.getString("error.attachment.value"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                String[] rowData = { type, value };

                modelAttachmentsDelete.addRow(rowData);

                Utils.adjustColumnPreferredWidths(tblAttachDelete);
                tblAttachDelete.revalidate();

                rbAttachDeleteName.setSelected(true);
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("");
                tblAttachDelete.clearSelection();
            }
        });

        btnAttachDeleteEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String type = "";
                String value = "";

                if (rbAttachDeleteName.isSelected()) {
                    type = rbAttachDeleteName.getText();
                    value = txtAttachDeleteValue.getText().trim();
                } else if (rbAttachDeleteID.isSelected()) {
                    type = rbAttachDeleteID.getText();
                    value = txtAttachDeleteValue.getText();
                } else {
                    type = rbAttachDeleteMime.getText();
                    value = cbAttachDeleteValue.getSelectedItem().toString();
                }

                int selection = tblAttachDelete.getSelectedRow();

                if (value.isEmpty()) {
                    JOptionPane.showMessageDialog(null, LanguageManager.getString("error.attachment.value"), "",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                modelAttachmentsDelete.setValueAt(type, selection, 0);
                modelAttachmentsDelete.setValueAt(value, selection, 1);

                Utils.adjustColumnPreferredWidths(tblAttachDelete);
                tblAttachDelete.revalidate();

                tblAttachDelete.setEnabled(true);
                btnAttachDeleteAdd.setEnabled(true);
                btnAttachDeleteEdit.setEnabled(false);
                btnAttachDeleteRemove.setEnabled(false);
                btnAttachDeleteCancel.setEnabled(false);
                tblAttachDelete.clearSelection();

                rbAttachDeleteName.setSelected(true);
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("");
                tblAttachDelete.clearSelection();
            }
        });

        btnAttachDeleteRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selection = tblAttachDelete.getSelectedRow();

                modelAttachmentsDelete.removeRow(selection);

                tblAttachDelete.setEnabled(true);
                btnAttachDeleteAdd.setEnabled(true);
                btnAttachDeleteEdit.setEnabled(false);
                btnAttachDeleteRemove.setEnabled(false);
                btnAttachDeleteCancel.setEnabled(false);
                tblAttachDelete.clearSelection();

                rbAttachDeleteName.setSelected(true);
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("");
            }
        });

        btnAttachDeleteCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tblAttachDelete.setEnabled(true);
                btnAttachDeleteAdd.setEnabled(true);
                btnAttachDeleteEdit.setEnabled(false);
                btnAttachDeleteRemove.setEnabled(false);
                btnAttachDeleteCancel.setEnabled(false);
                tblAttachDelete.clearSelection();

                rbAttachDeleteName.setSelected(true);
                cbAttachDeleteValue.setVisible(false);
                txtAttachDeleteValue.setVisible(true);
                txtAttachDeleteValue.setText("");
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

    /* Start of track addition methods */

    private void addVideoTrack() {
        if (nVideo < MAX_STREAMS) {
            subPnlVideo[nVideo] = new JPanel();
            lyrdPnlVideo.add(subPnlVideo[nVideo], "subPnlVideo[" + nVideo + "]");
            GridBagLayout gbl_subPnlVideo = new GridBagLayout();
            gbl_subPnlVideo.columnWidths = new int[] { 140, 0, 0 };
            gbl_subPnlVideo.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            gbl_subPnlVideo.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
            gbl_subPnlVideo.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            subPnlVideo[nVideo].setLayout(gbl_subPnlVideo);

            chbEditVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.edit"));
            GridBagConstraints gbc_chbEditVideo = new GridBagConstraints();
            gbc_chbEditVideo.insets = new Insets(0, 0, 10, 5);
            gbc_chbEditVideo.anchor = GridBagConstraints.WEST;
            gbc_chbEditVideo.gridx = 0;
            gbc_chbEditVideo.gridy = 0;
            subPnlVideo[nVideo].add(chbEditVideo[nVideo], gbc_chbEditVideo);

            chbEnableVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.enable"));
            chbEnableVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbEnableVideo = new GridBagConstraints();
            gbc_chbEnableVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbEnableVideo.anchor = GridBagConstraints.WEST;
            gbc_chbEnableVideo.gridx = 0;
            gbc_chbEnableVideo.gridy = 1;
            subPnlVideo[nVideo].add(chbEnableVideo[nVideo], gbc_chbEnableVideo);

            JPanel pnlEnableControlsVideo = new JPanel();
            FlowLayout fl_pnlEnableControlsVideo = (FlowLayout) pnlEnableControlsVideo.getLayout();
            fl_pnlEnableControlsVideo.setAlignment(FlowLayout.LEFT);
            fl_pnlEnableControlsVideo.setVgap(0);
            GridBagConstraints gbc_pnlEnableControlsVideo = new GridBagConstraints();
            gbc_pnlEnableControlsVideo.insets = new Insets(0, 0, 5, 0);
            gbc_pnlEnableControlsVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlEnableControlsVideo.gridx = 1;
            gbc_pnlEnableControlsVideo.gridy = 1;
            subPnlVideo[nVideo].add(pnlEnableControlsVideo, gbc_pnlEnableControlsVideo);

            rbYesEnableVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesEnableVideo[nVideo].setEnabled(false);
            rbYesEnableVideo[nVideo].setSelected(true);
            pnlEnableControlsVideo.add(rbYesEnableVideo[nVideo]);

            rbNoEnableVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoEnableVideo[nVideo].setEnabled(false);
            pnlEnableControlsVideo.add(rbNoEnableVideo[nVideo]);

            bgRbEnableVideo[nVideo] = new ButtonGroup();
            bgRbEnableVideo[nVideo].add(rbYesEnableVideo[nVideo]);
            bgRbEnableVideo[nVideo].add(rbNoEnableVideo[nVideo]);

            chbDefaultVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.default"));
            chbDefaultVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbDefaultVideo = new GridBagConstraints();
            gbc_chbDefaultVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbDefaultVideo.anchor = GridBagConstraints.WEST;
            gbc_chbDefaultVideo.gridx = 0;
            gbc_chbDefaultVideo.gridy = 2;
            subPnlVideo[nVideo].add(chbDefaultVideo[nVideo], gbc_chbDefaultVideo);

            JPanel pnlDefControlsVideo = new JPanel();
            FlowLayout fl_pnlDefControlsVideo = (FlowLayout) pnlDefControlsVideo.getLayout();
            fl_pnlDefControlsVideo.setAlignment(FlowLayout.LEFT);
            fl_pnlDefControlsVideo.setVgap(0);
            GridBagConstraints gbc_pnlDefControlsVideo = new GridBagConstraints();
            gbc_pnlDefControlsVideo.insets = new Insets(0, 0, 5, 0);
            gbc_pnlDefControlsVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlDefControlsVideo.gridx = 1;
            gbc_pnlDefControlsVideo.gridy = 2;
            subPnlVideo[nVideo].add(pnlDefControlsVideo, gbc_pnlDefControlsVideo);

            rbYesDefVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesDefVideo[nVideo].setEnabled(false);
            rbYesDefVideo[nVideo].setSelected(true);
            pnlDefControlsVideo.add(rbYesDefVideo[nVideo]);

            rbNoDefVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoDefVideo[nVideo].setEnabled(false);
            pnlDefControlsVideo.add(rbNoDefVideo[nVideo]);

            bgRbDefVideo[nVideo] = new ButtonGroup();
            bgRbDefVideo[nVideo].add(rbYesDefVideo[nVideo]);
            bgRbDefVideo[nVideo].add(rbNoDefVideo[nVideo]);

            chbForcedVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.forced"));
            chbForcedVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbForcedVideo = new GridBagConstraints();
            gbc_chbForcedVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbForcedVideo.anchor = GridBagConstraints.WEST;
            gbc_chbForcedVideo.gridx = 0;
            gbc_chbForcedVideo.gridy = 3;
            subPnlVideo[nVideo].add(chbForcedVideo[nVideo], gbc_chbForcedVideo);

            JPanel pnlForControlsVideo = new JPanel();
            FlowLayout fl_pnlForControlsVideo = (FlowLayout) pnlForControlsVideo.getLayout();
            fl_pnlForControlsVideo.setAlignment(FlowLayout.LEFT);
            fl_pnlForControlsVideo.setVgap(0);
            GridBagConstraints gbc_pnlForControlsVideo = new GridBagConstraints();
            gbc_pnlForControlsVideo.insets = new Insets(0, 0, 5, 0);
            gbc_pnlForControlsVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlForControlsVideo.gridx = 1;
            gbc_pnlForControlsVideo.gridy = 3;
            subPnlVideo[nVideo].add(pnlForControlsVideo, gbc_pnlForControlsVideo);

            rbYesForcedVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesForcedVideo[nVideo].setEnabled(false);
            rbYesForcedVideo[nVideo].setSelected(true);
            pnlForControlsVideo.add(rbYesForcedVideo[nVideo]);

            rbNoForcedVideo[nVideo] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoForcedVideo[nVideo].setEnabled(false);
            pnlForControlsVideo.add(rbNoForcedVideo[nVideo]);

            bgRbForcedVideo[nVideo] = new ButtonGroup();
            bgRbForcedVideo[nVideo].add(rbYesForcedVideo[nVideo]);
            bgRbForcedVideo[nVideo].add(rbNoForcedVideo[nVideo]);

            chbNameVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.name"));
            chbNameVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbNameVideo = new GridBagConstraints();
            gbc_chbNameVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbNameVideo.anchor = GridBagConstraints.WEST;
            gbc_chbNameVideo.gridx = 0;
            gbc_chbNameVideo.gridy = 4;
            subPnlVideo[nVideo].add(chbNameVideo[nVideo], gbc_chbNameVideo);

            txtNameVideo[nVideo] = new JTextField();
            txtNameVideo[nVideo].setEnabled(false);
            txtNameVideo[nVideo].setColumns(10);
            GridBagConstraints gbc_txtNameVideo = new GridBagConstraints();
            gbc_txtNameVideo.insets = new Insets(0, 0, 5, 0);
            gbc_txtNameVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtNameVideo.gridx = 1;
            gbc_txtNameVideo.gridy = 4;
            subPnlVideo[nVideo].add(txtNameVideo[nVideo], gbc_txtNameVideo);

            chbNumbVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.numbering"));
            chbNumbVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbNumbVideo = new GridBagConstraints();
            gbc_chbNumbVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbNumbVideo.anchor = GridBagConstraints.WEST;
            gbc_chbNumbVideo.gridx = 0;
            gbc_chbNumbVideo.gridy = 5;
            subPnlVideo[nVideo].add(chbNumbVideo[nVideo], gbc_chbNumbVideo);

            JPanel pnlNumbControlsVideo = new JPanel();
            FlowLayout fl_pnlNumbControlsVideo = (FlowLayout) pnlNumbControlsVideo.getLayout();
            fl_pnlNumbControlsVideo.setAlignment(FlowLayout.LEFT);
            fl_pnlNumbControlsVideo.setVgap(0);
            GridBagConstraints gbc_pnlNumbControlsVideo = new GridBagConstraints();
            gbc_pnlNumbControlsVideo.insets = new Insets(0, 0, 5, 0);
            gbc_pnlNumbControlsVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlNumbControlsVideo.gridx = 1;
            gbc_pnlNumbControlsVideo.gridy = 5;
            subPnlVideo[nVideo].add(pnlNumbControlsVideo, gbc_pnlNumbControlsVideo);

            lblNumbStartVideo[nVideo] = new JLabel(LanguageManager.getString("track.numbering.start"));
            lblNumbStartVideo[nVideo].setEnabled(false);
            pnlNumbControlsVideo.add(lblNumbStartVideo[nVideo]);

            txtNumbStartVideo[nVideo] = new JTextField();
            txtNumbStartVideo[nVideo].setText("1");
            txtNumbStartVideo[nVideo].setEnabled(false);
            txtNumbStartVideo[nVideo].setColumns(3);
            pnlNumbControlsVideo.add(txtNumbStartVideo[nVideo]);

            lblNumbPadVideo[nVideo] = new JLabel(LanguageManager.getString("track.numbering.padding"));
            lblNumbPadVideo[nVideo].setEnabled(false);
            pnlNumbControlsVideo.add(lblNumbPadVideo[nVideo]);

            txtNumbPadVideo[nVideo] = new JTextField();
            txtNumbPadVideo[nVideo].setText("1");
            txtNumbPadVideo[nVideo].setEnabled(false);
            txtNumbPadVideo[nVideo].setColumns(3);
            pnlNumbControlsVideo.add(txtNumbPadVideo[nVideo]);

            lblNumbExplainVideo[nVideo] = new JLabel(
                    "<html>" + LanguageManager.getString("track.numbering.explain") + "</html>");
            lblNumbExplainVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_lblNumbExplainVideo = new GridBagConstraints();
            gbc_lblNumbExplainVideo.insets = new Insets(0, 0, 5, 0);
            gbc_lblNumbExplainVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblNumbExplainVideo.gridx = 1;
            gbc_lblNumbExplainVideo.gridy = 6;
            subPnlVideo[nVideo].add(lblNumbExplainVideo[nVideo], gbc_lblNumbExplainVideo);

            chbLangVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.language"));
            chbLangVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbLangVideo = new GridBagConstraints();
            gbc_chbLangVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbLangVideo.anchor = GridBagConstraints.WEST;
            gbc_chbLangVideo.gridx = 0;
            gbc_chbLangVideo.gridy = 7;
            subPnlVideo[nVideo].add(chbLangVideo[nVideo], gbc_chbLangVideo);

            cbLangVideo[nVideo] = new JComboBox<String>();
            cbLangVideo[nVideo]
                    .setModel(new DefaultComboBoxModel<String>(mkvStrings.getLangNameList().toArray(new String[0])));
            cbLangVideo[nVideo].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
            cbLangVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_cbLangVideo = new GridBagConstraints();
            gbc_cbLangVideo.insets = new Insets(0, 0, 5, 0);
            gbc_cbLangVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_cbLangVideo.gridx = 1;
            gbc_cbLangVideo.gridy = 7;
            subPnlVideo[nVideo].add(cbLangVideo[nVideo], gbc_cbLangVideo);

            chbExtraCmdVideo[nVideo] = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
            chbExtraCmdVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbExtraCmdVideo = new GridBagConstraints();
            gbc_chbExtraCmdVideo.insets = new Insets(0, 0, 0, 5);
            gbc_chbExtraCmdVideo.anchor = GridBagConstraints.WEST;
            gbc_chbExtraCmdVideo.gridx = 0;
            gbc_chbExtraCmdVideo.gridy = 8;
            subPnlVideo[nVideo].add(chbExtraCmdVideo[nVideo], gbc_chbExtraCmdVideo);

            txtExtraCmdVideo[nVideo] = new JTextField();
            txtExtraCmdVideo[nVideo].setEnabled(false);
            txtExtraCmdVideo[nVideo].setColumns(10);
            GridBagConstraints gbc_txtExtraCmdVideo = new GridBagConstraints();
            gbc_txtExtraCmdVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtExtraCmdVideo.gridx = 1;
            gbc_txtExtraCmdVideo.gridy = 8;
            subPnlVideo[nVideo].add(txtExtraCmdVideo[nVideo], gbc_txtExtraCmdVideo);

            chbEditVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleVideo(cbVideo.getSelectedIndex());
                }
            });

            chbEnableVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = rbNoEnableVideo[curCbVideo].isEnabled();

                    rbNoEnableVideo[curCbVideo].setEnabled(!state);
                    rbYesEnableVideo[curCbVideo].setEnabled(!state);
                }
            });

            chbDefaultVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = rbNoDefVideo[curCbVideo].isEnabled();

                    rbNoDefVideo[curCbVideo].setEnabled(!state);
                    rbYesDefVideo[curCbVideo].setEnabled(!state);
                }
            });

            chbForcedVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = rbNoForcedVideo[curCbVideo].isEnabled();

                    rbNoForcedVideo[curCbVideo].setEnabled(!state);
                    rbYesForcedVideo[curCbVideo].setEnabled(!state);
                }
            });

            chbNameVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = chbNumbVideo[curCbVideo].isEnabled();

                    chbNumbVideo[curCbVideo].setEnabled(!state);
                    txtNameVideo[curCbVideo].setEnabled(!state);

                    if (chbNumbVideo[curCbVideo].isSelected()) {
                        lblNumbStartVideo[curCbVideo].setEnabled(!state);
                        txtNumbStartVideo[curCbVideo].setEnabled(!state);
                        lblNumbPadVideo[curCbVideo].setEnabled(!state);
                        txtNumbPadVideo[curCbVideo].setEnabled(!state);
                        lblNumbExplainVideo[curCbVideo].setEnabled(!state);
                    }
                }
            });

            chbNumbVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = txtNumbStartVideo[curCbVideo].isEnabled();

                    lblNumbStartVideo[curCbVideo].setEnabled(!state);
                    txtNumbStartVideo[curCbVideo].setEnabled(!state);
                    lblNumbPadVideo[curCbVideo].setEnabled(!state);
                    txtNumbPadVideo[curCbVideo].setEnabled(!state);
                    lblNumbExplainVideo[curCbVideo].setEnabled(!state);
                }
            });

            txtNumbStartVideo[nVideo].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbStartVideo[curCbVideo].getText()) < 0) {
                            txtNumbStartVideo[curCbVideo].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbStartVideo[curCbVideo].setText("1");
                    }
                }
            });

            txtNumbPadVideo[nVideo].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbPadVideo[curCbVideo].getText()) < 0) {
                            txtNumbPadVideo[curCbVideo].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbPadVideo[curCbVideo].setText("1");
                    }
                }
            });

            chbLangVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = cbLangVideo[curCbVideo].isEnabled();

                    cbLangVideo[curCbVideo].setEnabled(!state);
                }
            });

            chbExtraCmdVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = txtExtraCmdVideo[curCbVideo].isEnabled();

                    txtExtraCmdVideo[curCbVideo].setEnabled(!state);
                }
            });

            cbVideo.addItem(LanguageManager.getString("track.video.title") + (nVideo + 1));
            nVideo++;
        }
    }

    private void addAudioTrack() {
        if (nAudio < MAX_STREAMS) {
            subPnlAudio[nAudio] = new JPanel();
            lyrdPnlAudio.add(subPnlAudio[nAudio], "subPnlAudio[" + nAudio + "]");
            GridBagLayout gbl_subPnlAudio = new GridBagLayout();
            gbl_subPnlAudio.columnWidths = new int[] { 140, 0, 0 };
            gbl_subPnlAudio.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            gbl_subPnlAudio.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
            gbl_subPnlAudio.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            subPnlAudio[nAudio].setLayout(gbl_subPnlAudio);

            chbEditAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.edit"));
            GridBagConstraints gbc_chbEditAudio = new GridBagConstraints();
            gbc_chbEditAudio.insets = new Insets(0, 0, 10, 5);
            gbc_chbEditAudio.anchor = GridBagConstraints.WEST;
            gbc_chbEditAudio.gridx = 0;
            gbc_chbEditAudio.gridy = 0;
            subPnlAudio[nAudio].add(chbEditAudio[nAudio], gbc_chbEditAudio);

            chbEnableAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.enable"));
            chbEnableAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbEnableaultAudio = new GridBagConstraints();
            gbc_chbEnableaultAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbEnableaultAudio.anchor = GridBagConstraints.WEST;
            gbc_chbEnableaultAudio.gridx = 0;
            gbc_chbEnableaultAudio.gridy = 1;
            subPnlAudio[nAudio].add(chbEnableAudio[nAudio], gbc_chbEnableaultAudio);

            JPanel pnlEnableControlsAudio = new JPanel();
            FlowLayout fl_pnlEnableControlsAudio = (FlowLayout) pnlEnableControlsAudio.getLayout();
            fl_pnlEnableControlsAudio.setAlignment(FlowLayout.LEFT);
            fl_pnlEnableControlsAudio.setVgap(0);
            GridBagConstraints gbc_pnlEnableControlsAudio = new GridBagConstraints();
            gbc_pnlEnableControlsAudio.insets = new Insets(0, 0, 5, 0);
            gbc_pnlEnableControlsAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlEnableControlsAudio.gridx = 1;
            gbc_pnlEnableControlsAudio.gridy = 1;
            subPnlAudio[nAudio].add(pnlEnableControlsAudio, gbc_pnlEnableControlsAudio);

            rbYesEnableAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesEnableAudio[nAudio].setEnabled(false);
            rbYesEnableAudio[nAudio].setSelected(true);
            pnlEnableControlsAudio.add(rbYesEnableAudio[nAudio]);

            rbNoEnableAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoEnableAudio[nAudio].setEnabled(false);
            pnlEnableControlsAudio.add(rbNoEnableAudio[nAudio]);

            bgRbEnableAudio[nAudio] = new ButtonGroup();
            bgRbEnableAudio[nAudio].add(rbYesEnableAudio[nAudio]);
            bgRbEnableAudio[nAudio].add(rbNoEnableAudio[nAudio]);

            chbDefaultAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.default"));
            chbDefaultAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbDefaultAudio = new GridBagConstraints();
            gbc_chbDefaultAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbDefaultAudio.anchor = GridBagConstraints.WEST;
            gbc_chbDefaultAudio.gridx = 0;
            gbc_chbDefaultAudio.gridy = 2;
            subPnlAudio[nAudio].add(chbDefaultAudio[nAudio], gbc_chbDefaultAudio);

            JPanel pnlDefControlsAudio = new JPanel();
            FlowLayout fl_pnlDefControlsAudio = (FlowLayout) pnlDefControlsAudio.getLayout();
            fl_pnlDefControlsAudio.setAlignment(FlowLayout.LEFT);
            fl_pnlDefControlsAudio.setVgap(0);
            GridBagConstraints gbc_pnlDefControlsAudio = new GridBagConstraints();
            gbc_pnlDefControlsAudio.insets = new Insets(0, 0, 5, 0);
            gbc_pnlDefControlsAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlDefControlsAudio.gridx = 1;
            gbc_pnlDefControlsAudio.gridy = 2;
            subPnlAudio[nAudio].add(pnlDefControlsAudio, gbc_pnlDefControlsAudio);

            rbYesDefAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesDefAudio[nAudio].setEnabled(false);
            rbYesDefAudio[nAudio].setSelected(true);
            pnlDefControlsAudio.add(rbYesDefAudio[nAudio]);

            rbNoDefAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoDefAudio[nAudio].setEnabled(false);
            pnlDefControlsAudio.add(rbNoDefAudio[nAudio]);

            bgRbDefAudio[nAudio] = new ButtonGroup();
            bgRbDefAudio[nAudio].add(rbYesDefAudio[nAudio]);
            bgRbDefAudio[nAudio].add(rbNoDefAudio[nAudio]);

            chbForcedAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.forced"));
            chbForcedAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbForcedAudio = new GridBagConstraints();
            gbc_chbForcedAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbForcedAudio.anchor = GridBagConstraints.WEST;
            gbc_chbForcedAudio.gridx = 0;
            gbc_chbForcedAudio.gridy = 3;
            subPnlAudio[nAudio].add(chbForcedAudio[nAudio], gbc_chbForcedAudio);

            JPanel pnlForControlsAudio = new JPanel();
            FlowLayout fl_pnlForControlsAudio = (FlowLayout) pnlForControlsAudio.getLayout();
            fl_pnlForControlsAudio.setAlignment(FlowLayout.LEFT);
            fl_pnlForControlsAudio.setVgap(0);
            GridBagConstraints gbc_pnlForControlsAudio = new GridBagConstraints();
            gbc_pnlForControlsAudio.insets = new Insets(0, 0, 5, 0);
            gbc_pnlForControlsAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlForControlsAudio.gridx = 1;
            gbc_pnlForControlsAudio.gridy = 3;
            subPnlAudio[nAudio].add(pnlForControlsAudio, gbc_pnlForControlsAudio);

            rbYesForcedAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesForcedAudio[nAudio].setEnabled(false);
            rbYesForcedAudio[nAudio].setSelected(true);
            pnlForControlsAudio.add(rbYesForcedAudio[nAudio]);

            rbNoForcedAudio[nAudio] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoForcedAudio[nAudio].setEnabled(false);
            pnlForControlsAudio.add(rbNoForcedAudio[nAudio]);

            bgRbForcedAudio[nAudio] = new ButtonGroup();
            bgRbForcedAudio[nAudio].add(rbYesForcedAudio[nAudio]);
            bgRbForcedAudio[nAudio].add(rbNoForcedAudio[nAudio]);

            chbNameAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.name"));
            chbNameAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbNameAudio = new GridBagConstraints();
            gbc_chbNameAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbNameAudio.anchor = GridBagConstraints.WEST;
            gbc_chbNameAudio.gridx = 0;
            gbc_chbNameAudio.gridy = 4;
            subPnlAudio[nAudio].add(chbNameAudio[nAudio], gbc_chbNameAudio);

            txtNameAudio[nAudio] = new JTextField();
            txtNameAudio[nAudio].setEnabled(false);
            txtNameAudio[nAudio].setColumns(10);
            GridBagConstraints gbc_txtNameAudio = new GridBagConstraints();
            gbc_txtNameAudio.insets = new Insets(0, 0, 5, 0);
            gbc_txtNameAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtNameAudio.gridx = 1;
            gbc_txtNameAudio.gridy = 4;
            subPnlAudio[nAudio].add(txtNameAudio[nAudio], gbc_txtNameAudio);

            chbNumbAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.numbering"));
            chbNumbAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbNumbAudio = new GridBagConstraints();
            gbc_chbNumbAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbNumbAudio.anchor = GridBagConstraints.WEST;
            gbc_chbNumbAudio.gridx = 0;
            gbc_chbNumbAudio.gridy = 5;
            subPnlAudio[nAudio].add(chbNumbAudio[nAudio], gbc_chbNumbAudio);

            JPanel pnlNumbControlsAudio = new JPanel();
            FlowLayout fl_pnlNumbControlsAudio = (FlowLayout) pnlNumbControlsAudio.getLayout();
            fl_pnlNumbControlsAudio.setAlignment(FlowLayout.LEFT);
            fl_pnlNumbControlsAudio.setVgap(0);
            GridBagConstraints gbc_pnlNumbControlsAudio = new GridBagConstraints();
            gbc_pnlNumbControlsAudio.insets = new Insets(0, 0, 5, 0);
            gbc_pnlNumbControlsAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlNumbControlsAudio.gridx = 1;
            gbc_pnlNumbControlsAudio.gridy = 5;
            subPnlAudio[nAudio].add(pnlNumbControlsAudio, gbc_pnlNumbControlsAudio);

            lblNumbStartAudio[nAudio] = new JLabel(LanguageManager.getString("track.numbering.start"));
            lblNumbStartAudio[nAudio].setEnabled(false);
            pnlNumbControlsAudio.add(lblNumbStartAudio[nAudio]);

            txtNumbStartAudio[nAudio] = new JTextField();
            txtNumbStartAudio[nAudio].setText("1");
            txtNumbStartAudio[nAudio].setEnabled(false);
            txtNumbStartAudio[nAudio].setColumns(3);
            pnlNumbControlsAudio.add(txtNumbStartAudio[nAudio]);

            lblNumbPadAudio[nAudio] = new JLabel(LanguageManager.getString("track.numbering.padding"));
            lblNumbPadAudio[nAudio].setEnabled(false);
            pnlNumbControlsAudio.add(lblNumbPadAudio[nAudio]);

            txtNumbPadAudio[nAudio] = new JTextField();
            txtNumbPadAudio[nAudio].setText("1");
            txtNumbPadAudio[nAudio].setEnabled(false);
            txtNumbPadAudio[nAudio].setColumns(3);
            pnlNumbControlsAudio.add(txtNumbPadAudio[nAudio]);

            lblNumbExplainAudio[nAudio] = new JLabel(
                    "<html>" + LanguageManager.getString("track.numbering.explain") + "</html>");
            lblNumbExplainAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_lblNumbExplainAudio = new GridBagConstraints();
            gbc_lblNumbExplainAudio.insets = new Insets(0, 0, 5, 0);
            gbc_lblNumbExplainAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblNumbExplainAudio.gridx = 1;
            gbc_lblNumbExplainAudio.gridy = 6;
            subPnlAudio[nAudio].add(lblNumbExplainAudio[nAudio], gbc_lblNumbExplainAudio);

            chbLangAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.language"));
            chbLangAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbLangAudio = new GridBagConstraints();
            gbc_chbLangAudio.insets = new Insets(0, 0, 5, 5);
            gbc_chbLangAudio.anchor = GridBagConstraints.WEST;
            gbc_chbLangAudio.gridx = 0;
            gbc_chbLangAudio.gridy = 7;
            subPnlAudio[nAudio].add(chbLangAudio[nAudio], gbc_chbLangAudio);

            cbLangAudio[nAudio] = new JComboBox<String>();
            cbLangAudio[nAudio]
                    .setModel(new DefaultComboBoxModel<String>(mkvStrings.getLangNameList().toArray(new String[0])));
            cbLangAudio[nAudio].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
            cbLangAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_cbLangAudio = new GridBagConstraints();
            gbc_cbLangAudio.insets = new Insets(0, 0, 5, 0);
            gbc_cbLangAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_cbLangAudio.gridx = 1;
            gbc_cbLangAudio.gridy = 7;
            subPnlAudio[nAudio].add(cbLangAudio[nAudio], gbc_cbLangAudio);

            chbExtraCmdAudio[nAudio] = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
            chbExtraCmdAudio[nAudio].setEnabled(false);
            GridBagConstraints gbc_chbExtraCmdAudio = new GridBagConstraints();
            gbc_chbExtraCmdAudio.insets = new Insets(0, 0, 0, 5);
            gbc_chbExtraCmdAudio.anchor = GridBagConstraints.WEST;
            gbc_chbExtraCmdAudio.gridx = 0;
            gbc_chbExtraCmdAudio.gridy = 8;
            subPnlAudio[nAudio].add(chbExtraCmdAudio[nAudio], gbc_chbExtraCmdAudio);

            txtExtraCmdAudio[nAudio] = new JTextField();
            txtExtraCmdAudio[nAudio].setEnabled(false);
            txtExtraCmdAudio[nAudio].setColumns(10);
            GridBagConstraints gbc_txtExtraCmdAudio = new GridBagConstraints();
            gbc_txtExtraCmdAudio.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtExtraCmdAudio.gridx = 1;
            gbc_txtExtraCmdAudio.gridy = 8;
            subPnlAudio[nAudio].add(txtExtraCmdAudio[nAudio], gbc_txtExtraCmdAudio);

            final int currentTrackIdx = nAudio;

            new java.awt.dnd.DropTarget(subPnlAudio[nAudio], new java.awt.dnd.DropTargetListener() {
                public void dragEnter(java.awt.dnd.DropTargetDragEvent dtde) {
                }

                public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
                }

                public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {
                }

                public void dragExit(java.awt.dnd.DropTargetEvent dte) {
                }

                public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
                    try {
                        java.awt.datatransfer.Transferable tr = dtde.getTransferable();
                        if (tr.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                            dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                            String data = (String) tr.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                            if (data.startsWith("AudioProfile:")) {
                                int profileIdx = Integer.parseInt(data.substring(data.indexOf(":") + 1));
                                applyAudioProfile(profileManager.getProfiles(ProfileType.AUDIO).get(profileIdx),
                                        currentTrackIdx);
                                dtde.dropComplete(true);
                                return;
                            }
                        }
                    } catch (Exception e) {
                    }
                    dtde.rejectDrop();
                }
            });

            chbEditAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleAudio(cbAudio.getSelectedIndex());
                }
            });

            chbEnableAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = rbNoEnableAudio[curCbAudio].isEnabled();

                    rbNoEnableAudio[curCbAudio].setEnabled(!state);
                    rbYesEnableAudio[curCbAudio].setEnabled(!state);
                }
            });

            chbDefaultAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = rbNoDefAudio[curCbAudio].isEnabled();

                    rbNoDefAudio[curCbAudio].setEnabled(!state);
                    rbYesDefAudio[curCbAudio].setEnabled(!state);
                }
            });

            chbForcedAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = rbNoForcedAudio[curCbAudio].isEnabled();

                    rbNoForcedAudio[curCbAudio].setEnabled(!state);
                    rbYesForcedAudio[curCbAudio].setEnabled(!state);
                }
            });

            chbNameAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = chbNumbAudio[curCbAudio].isEnabled();

                    chbNumbAudio[curCbAudio].setEnabled(!state);
                    txtNameAudio[curCbAudio].setEnabled(!state);

                    if (chbNumbAudio[curCbAudio].isSelected()) {
                        lblNumbStartAudio[curCbAudio].setEnabled(!state);
                        txtNumbStartAudio[curCbAudio].setEnabled(!state);
                        lblNumbPadAudio[curCbAudio].setEnabled(!state);
                        txtNumbPadAudio[curCbAudio].setEnabled(!state);
                        lblNumbExplainAudio[curCbAudio].setEnabled(!state);
                    }
                }
            });

            chbNumbAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = txtNumbStartAudio[curCbAudio].isEnabled();

                    lblNumbStartAudio[curCbAudio].setEnabled(!state);
                    txtNumbStartAudio[curCbAudio].setEnabled(!state);
                    lblNumbPadAudio[curCbAudio].setEnabled(!state);
                    txtNumbPadAudio[curCbAudio].setEnabled(!state);
                    lblNumbExplainAudio[curCbAudio].setEnabled(!state);
                }
            });

            txtNumbStartAudio[nAudio].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbStartAudio[curCbAudio].getText()) < 0) {
                            txtNumbStartAudio[curCbAudio].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbStartAudio[curCbAudio].setText("1");
                    }
                }
            });

            txtNumbPadAudio[nAudio].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbPadAudio[curCbAudio].getText()) < 0) {
                            txtNumbPadAudio[curCbAudio].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbPadAudio[curCbAudio].setText("1");
                    }
                }
            });

            chbLangAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = cbLangAudio[curCbAudio].isEnabled();

                    cbLangAudio[curCbAudio].setEnabled(!state);
                }
            });

            chbExtraCmdAudio[nAudio].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbAudio = cbAudio.getSelectedIndex();
                    boolean state = txtExtraCmdAudio[curCbAudio].isEnabled();

                    txtExtraCmdAudio[curCbAudio].setEnabled(!state);
                }
            });

            cbAudio.addItem(LanguageManager.getString("track.audio.title") + (nAudio + 1));
            nAudio++;
        }
    }

    private void addSubtitleTrack() {
        if (nSubtitle < MAX_STREAMS) {
            subPnlSubtitle[nSubtitle] = new JPanel();
            lyrdPnlSubtitle.add(subPnlSubtitle[nSubtitle], "subPnlSubtitle[" + nSubtitle + "]");
            GridBagLayout gbl_subPnlSubtitle = new GridBagLayout();
            gbl_subPnlSubtitle.columnWidths = new int[] { 140, 0, 0 };
            gbl_subPnlSubtitle.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            gbl_subPnlSubtitle.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
            gbl_subPnlSubtitle.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            subPnlSubtitle[nSubtitle].setLayout(gbl_subPnlSubtitle);

            chbEditSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.edit"));
            GridBagConstraints gbc_chbEditSubtitle = new GridBagConstraints();
            gbc_chbEditSubtitle.insets = new Insets(0, 0, 10, 5);
            gbc_chbEditSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbEditSubtitle.gridx = 0;
            gbc_chbEditSubtitle.gridy = 0;
            subPnlSubtitle[nSubtitle].add(chbEditSubtitle[nSubtitle], gbc_chbEditSubtitle);

            chbEnableSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.enable"));
            chbEnableSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbEnableSubtitle = new GridBagConstraints();
            gbc_chbEnableSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbEnableSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbEnableSubtitle.gridx = 0;
            gbc_chbEnableSubtitle.gridy = 1;
            subPnlSubtitle[nSubtitle].add(chbEnableSubtitle[nSubtitle], gbc_chbEnableSubtitle);

            JPanel pnlEnableControlsSubtitle = new JPanel();
            FlowLayout fl_pnlEnableControlsSubtitle = (FlowLayout) pnlEnableControlsSubtitle.getLayout();
            fl_pnlEnableControlsSubtitle.setAlignment(FlowLayout.LEFT);
            fl_pnlEnableControlsSubtitle.setVgap(0);
            GridBagConstraints gbc_pnlEnableControlsSubtitle = new GridBagConstraints();
            gbc_pnlEnableControlsSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_pnlEnableControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlEnableControlsSubtitle.gridx = 1;
            gbc_pnlEnableControlsSubtitle.gridy = 1;
            subPnlSubtitle[nSubtitle].add(pnlEnableControlsSubtitle, gbc_pnlEnableControlsSubtitle);

            rbYesEnableSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesEnableSubtitle[nSubtitle].setEnabled(false);
            rbYesEnableSubtitle[nSubtitle].setSelected(true);
            pnlEnableControlsSubtitle.add(rbYesEnableSubtitle[nSubtitle]);

            rbNoEnableSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoEnableSubtitle[nSubtitle].setEnabled(false);
            pnlEnableControlsSubtitle.add(rbNoEnableSubtitle[nSubtitle]);

            bgRbEnableSubtitle[nSubtitle] = new ButtonGroup();
            bgRbEnableSubtitle[nSubtitle].add(rbYesEnableSubtitle[nSubtitle]);
            bgRbEnableSubtitle[nSubtitle].add(rbNoEnableSubtitle[nSubtitle]);

            chbDefaultSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.default"));
            chbDefaultSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbDefaultSubtitle = new GridBagConstraints();
            gbc_chbDefaultSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbDefaultSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbDefaultSubtitle.gridx = 0;
            gbc_chbDefaultSubtitle.gridy = 2;
            subPnlSubtitle[nSubtitle].add(chbDefaultSubtitle[nSubtitle], gbc_chbDefaultSubtitle);

            JPanel pnlDefControlsSubtitle = new JPanel();
            FlowLayout fl_pnlDefControlsSubtitle = (FlowLayout) pnlDefControlsSubtitle.getLayout();
            fl_pnlDefControlsSubtitle.setAlignment(FlowLayout.LEFT);
            fl_pnlDefControlsSubtitle.setVgap(0);
            GridBagConstraints gbc_pnlDefControlsSubtitle = new GridBagConstraints();
            gbc_pnlDefControlsSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_pnlDefControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlDefControlsSubtitle.gridx = 1;
            gbc_pnlDefControlsSubtitle.gridy = 2;
            subPnlSubtitle[nSubtitle].add(pnlDefControlsSubtitle, gbc_pnlDefControlsSubtitle);

            rbYesDefSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesDefSubtitle[nSubtitle].setEnabled(false);
            rbYesDefSubtitle[nSubtitle].setSelected(true);
            pnlDefControlsSubtitle.add(rbYesDefSubtitle[nSubtitle]);

            rbNoDefSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoDefSubtitle[nSubtitle].setEnabled(false);
            pnlDefControlsSubtitle.add(rbNoDefSubtitle[nSubtitle]);

            bgRbDefSubtitle[nSubtitle] = new ButtonGroup();
            bgRbDefSubtitle[nSubtitle].add(rbYesDefSubtitle[nSubtitle]);
            bgRbDefSubtitle[nSubtitle].add(rbNoDefSubtitle[nSubtitle]);

            chbForcedSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.forced"));
            chbForcedSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbForcedSubtitle = new GridBagConstraints();
            gbc_chbForcedSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbForcedSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbForcedSubtitle.gridx = 0;
            gbc_chbForcedSubtitle.gridy = 3;
            subPnlSubtitle[nSubtitle].add(chbForcedSubtitle[nSubtitle], gbc_chbForcedSubtitle);

            JPanel pnlForControlsSubtitle = new JPanel();
            FlowLayout fl_pnlForControlsSubtitle = (FlowLayout) pnlForControlsSubtitle.getLayout();
            fl_pnlForControlsSubtitle.setAlignment(FlowLayout.LEFT);
            fl_pnlForControlsSubtitle.setVgap(0);
            GridBagConstraints gbc_pnlForControlsSubtitle = new GridBagConstraints();
            gbc_pnlForControlsSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_pnlForControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlForControlsSubtitle.gridx = 1;
            gbc_pnlForControlsSubtitle.gridy = 3;
            subPnlSubtitle[nSubtitle].add(pnlForControlsSubtitle, gbc_pnlForControlsSubtitle);

            rbYesForcedSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.yes"));
            rbYesForcedSubtitle[nSubtitle].setEnabled(false);
            rbYesForcedSubtitle[nSubtitle].setSelected(true);
            pnlForControlsSubtitle.add(rbYesForcedSubtitle[nSubtitle]);

            rbNoForcedSubtitle[nSubtitle] = new JRadioButton(LanguageManager.getString("common.no"));
            rbNoForcedSubtitle[nSubtitle].setEnabled(false);
            pnlForControlsSubtitle.add(rbNoForcedSubtitle[nSubtitle]);

            bgRbForcedSubtitle[nSubtitle] = new ButtonGroup();
            bgRbForcedSubtitle[nSubtitle].add(rbYesForcedSubtitle[nSubtitle]);
            bgRbForcedSubtitle[nSubtitle].add(rbNoForcedSubtitle[nSubtitle]);

            chbNameSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.name"));
            chbNameSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbNameSubtitle = new GridBagConstraints();
            gbc_chbNameSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbNameSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbNameSubtitle.gridx = 0;
            gbc_chbNameSubtitle.gridy = 4;
            subPnlSubtitle[nSubtitle].add(chbNameSubtitle[nSubtitle], gbc_chbNameSubtitle);

            txtNameSubtitle[nSubtitle] = new JTextField();
            txtNameSubtitle[nSubtitle].setEnabled(false);
            txtNameSubtitle[nSubtitle].setColumns(10);
            GridBagConstraints gbc_txtNameSubtitle = new GridBagConstraints();
            gbc_txtNameSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_txtNameSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtNameSubtitle.gridx = 1;
            gbc_txtNameSubtitle.gridy = 4;
            subPnlSubtitle[nSubtitle].add(txtNameSubtitle[nSubtitle], gbc_txtNameSubtitle);

            JPanel pnlNumbControlsSubtitle = new JPanel();
            FlowLayout fl_pnlNumbControlsSubtitle = (FlowLayout) pnlNumbControlsSubtitle.getLayout();
            fl_pnlNumbControlsSubtitle.setAlignment(FlowLayout.LEFT);
            fl_pnlNumbControlsSubtitle.setVgap(0);
            GridBagConstraints gbc_pnlNumbControlsSubtitle = new GridBagConstraints();
            gbc_pnlNumbControlsSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_pnlNumbControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_pnlNumbControlsSubtitle.gridx = 1;
            gbc_pnlNumbControlsSubtitle.gridy = 5;
            subPnlSubtitle[nSubtitle].add(pnlNumbControlsSubtitle, gbc_pnlNumbControlsSubtitle);

            chbNumbSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.numbering"));
            chbNumbSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbNumbSubtitle = new GridBagConstraints();
            gbc_chbNumbSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbNumbSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbNumbSubtitle.gridx = 0;
            gbc_chbNumbSubtitle.gridy = 5;
            subPnlSubtitle[nSubtitle].add(chbNumbSubtitle[nSubtitle], gbc_chbNumbSubtitle);

            lblNumbStartSubtitle[nSubtitle] = new JLabel(LanguageManager.getString("track.numbering.start"));
            lblNumbStartSubtitle[nSubtitle].setEnabled(false);
            pnlNumbControlsSubtitle.add(lblNumbStartSubtitle[nSubtitle]);

            txtNumbStartSubtitle[nSubtitle] = new JTextField();
            txtNumbStartSubtitle[nSubtitle].setText("1");
            txtNumbStartSubtitle[nSubtitle].setEnabled(false);
            txtNumbStartSubtitle[nSubtitle].setColumns(3);
            pnlNumbControlsSubtitle.add(txtNumbStartSubtitle[nSubtitle]);

            lblNumbPadSubtitle[nSubtitle] = new JLabel(LanguageManager.getString("track.numbering.padding"));
            lblNumbPadSubtitle[nSubtitle].setEnabled(false);
            pnlNumbControlsSubtitle.add(lblNumbPadSubtitle[nSubtitle]);

            txtNumbPadSubtitle[nSubtitle] = new JTextField();
            txtNumbPadSubtitle[nSubtitle].setText("1");
            txtNumbPadSubtitle[nSubtitle].setEnabled(false);
            txtNumbPadSubtitle[nSubtitle].setColumns(3);
            pnlNumbControlsSubtitle.add(txtNumbPadSubtitle[nSubtitle]);

            lblNumbExplainSubtitle[nSubtitle] = new JLabel(
                    "<html>" + LanguageManager.getString("track.numbering.explain") + "</html>");
            lblNumbExplainSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_lblNumbExplainSubtitle = new GridBagConstraints();
            gbc_lblNumbExplainSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_lblNumbExplainSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblNumbExplainSubtitle.gridx = 1;
            gbc_lblNumbExplainSubtitle.gridy = 6;
            subPnlSubtitle[nSubtitle].add(lblNumbExplainSubtitle[nSubtitle], gbc_lblNumbExplainSubtitle);

            chbLangSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.language"));
            chbLangSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbLangSubtitle = new GridBagConstraints();
            gbc_chbLangSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbLangSubtitle.insets = new Insets(0, 0, 5, 5);
            gbc_chbLangSubtitle.gridx = 0;
            gbc_chbLangSubtitle.gridy = 7;
            subPnlSubtitle[nSubtitle].add(chbLangSubtitle[nSubtitle], gbc_chbLangSubtitle);

            cbLangSubtitle[nSubtitle] = new JComboBox<String>();
            cbLangSubtitle[nSubtitle].setEnabled(false);
            cbLangSubtitle[nSubtitle].setModel(new DefaultComboBoxModel<String>(mkvStrings.getLangNames()));
            cbLangSubtitle[nSubtitle].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
            GridBagConstraints gbc_cbLangSubtitle = new GridBagConstraints();
            gbc_cbLangSubtitle.insets = new Insets(0, 0, 5, 0);
            gbc_cbLangSubtitle.anchor = GridBagConstraints.WEST;
            gbc_cbLangSubtitle.gridx = 1;
            gbc_cbLangSubtitle.gridy = 7;
            subPnlSubtitle[nSubtitle].add(cbLangSubtitle[nSubtitle], gbc_cbLangSubtitle);

            chbExtraCmdSubtitle[nSubtitle] = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
            chbExtraCmdSubtitle[nSubtitle].setEnabled(false);
            GridBagConstraints gbc_chbExtraCmdSubtitle = new GridBagConstraints();
            gbc_chbExtraCmdSubtitle.anchor = GridBagConstraints.WEST;
            gbc_chbExtraCmdSubtitle.gridx = 0;
            gbc_chbExtraCmdSubtitle.gridy = 8;
            subPnlSubtitle[nSubtitle].add(chbExtraCmdSubtitle[nSubtitle], gbc_chbExtraCmdSubtitle);

            txtExtraCmdSubtitle[nSubtitle] = new JTextField();
            txtExtraCmdSubtitle[nSubtitle].setEnabled(false);
            txtExtraCmdSubtitle[nSubtitle].setColumns(10);
            GridBagConstraints gbc_txtExtraCmdSubtitle = new GridBagConstraints();
            gbc_txtExtraCmdSubtitle.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtExtraCmdSubtitle.gridx = 1;
            gbc_txtExtraCmdSubtitle.gridy = 8;
            subPnlSubtitle[nSubtitle].add(txtExtraCmdSubtitle[nSubtitle], gbc_txtExtraCmdSubtitle);

            /* Start of mouse events for right-click menu */

            Utils.addRCMenuMouseListener(txtNameSubtitle[nSubtitle]);
            Utils.addRCMenuMouseListener(txtNumbStartSubtitle[nSubtitle]);
            Utils.addRCMenuMouseListener(txtNumbPadSubtitle[nSubtitle]);
            Utils.addRCMenuMouseListener(txtExtraCmdSubtitle[nSubtitle]);

            /* End of mouse events for right-click menu */

            chbEditSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean isEdit = chbEditSubtitle[curCbSubtitle].isSelected();

                    chbEnableSubtitle[curCbSubtitle].setEnabled(isEdit);
                    chbDefaultSubtitle[curCbSubtitle].setEnabled(isEdit);
                    chbForcedSubtitle[curCbSubtitle].setEnabled(isEdit);
                    chbNameSubtitle[curCbSubtitle].setEnabled(isEdit);
                    chbLangSubtitle[curCbSubtitle].setEnabled(isEdit);
                    chbExtraCmdSubtitle[curCbSubtitle].setEnabled(isEdit);

                    if (txtNameSubtitle[curCbSubtitle].isEnabled() || chbNameSubtitle[curCbSubtitle].isSelected()) {
                        txtNameSubtitle[curCbSubtitle]
                                .setEnabled(isEdit && chbNameSubtitle[curCbSubtitle].isSelected());
                        chbNumbSubtitle[curCbSubtitle]
                                .setEnabled(isEdit && chbNameSubtitle[curCbSubtitle].isSelected());

                        if (chbNumbSubtitle[curCbSubtitle].isSelected()) {
                            boolean isNumb = isEdit && chbNameSubtitle[curCbSubtitle].isSelected();
                            lblNumbStartSubtitle[curCbSubtitle].setEnabled(isNumb);
                            txtNumbStartSubtitle[curCbSubtitle].setEnabled(isNumb);
                            lblNumbPadSubtitle[curCbSubtitle].setEnabled(isNumb);
                            txtNumbPadSubtitle[curCbSubtitle].setEnabled(isNumb);
                            lblNumbExplainSubtitle[curCbSubtitle].setEnabled(isNumb);
                        }
                    }

                    if (rbNoEnableSubtitle[curCbSubtitle].isEnabled()
                            || chbEnableSubtitle[curCbSubtitle].isSelected()) {
                        boolean isEnable = isEdit && chbEnableSubtitle[curCbSubtitle].isSelected();
                        rbNoEnableSubtitle[curCbSubtitle].setEnabled(isEnable);
                        rbYesEnableSubtitle[curCbSubtitle].setEnabled(isEnable);
                    }

                    if (rbNoDefSubtitle[curCbSubtitle].isEnabled() || chbDefaultSubtitle[curCbSubtitle].isSelected()) {
                        boolean isDef = isEdit && chbDefaultSubtitle[curCbSubtitle].isSelected();
                        rbNoDefSubtitle[curCbSubtitle].setEnabled(isDef);
                        rbYesDefSubtitle[curCbSubtitle].setEnabled(isDef);
                    }

                    if (rbNoForcedSubtitle[curCbSubtitle].isEnabled()
                            || chbForcedSubtitle[curCbSubtitle].isSelected()) {
                        boolean isForced = isEdit && chbForcedSubtitle[curCbSubtitle].isSelected();
                        rbNoForcedSubtitle[curCbSubtitle].setEnabled(isForced);
                        rbYesForcedSubtitle[curCbSubtitle].setEnabled(isForced);
                    }

                    if (cbLangSubtitle[curCbSubtitle].isEnabled() || chbLangSubtitle[curCbSubtitle].isSelected()) {
                        cbLangSubtitle[curCbSubtitle].setEnabled(isEdit && chbLangSubtitle[curCbSubtitle].isSelected());
                    }

                    if (txtExtraCmdSubtitle[curCbSubtitle].isEnabled()
                            || chbExtraCmdSubtitle[curCbSubtitle].isSelected()) {
                        boolean isExtra = isEdit && chbExtraCmdSubtitle[curCbSubtitle].isSelected();
                        // chbExtraCmdSubtitle is already linked to isEdit above
                        txtExtraCmdSubtitle[curCbSubtitle].setEnabled(isExtra);
                    }
                }
            });

            chbEnableSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = rbNoEnableSubtitle[curCbSubtitle].isEnabled();

                    rbNoEnableSubtitle[curCbSubtitle].setEnabled(!state);
                    rbYesEnableSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            chbDefaultSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = rbNoDefSubtitle[curCbSubtitle].isEnabled();

                    rbNoDefSubtitle[curCbSubtitle].setEnabled(!state);
                    rbYesDefSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            chbForcedSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = rbNoForcedSubtitle[curCbSubtitle].isEnabled();

                    rbNoForcedSubtitle[curCbSubtitle].setEnabled(!state);
                    rbYesForcedSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            chbNameSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = chbNumbSubtitle[curCbSubtitle].isEnabled();

                    chbNumbSubtitle[curCbSubtitle].setEnabled(!state);
                    txtNameSubtitle[curCbSubtitle].setEnabled(!state);

                    if (chbNumbSubtitle[curCbSubtitle].isSelected()) {
                        lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
                        txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
                        lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
                        txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
                        lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
                    }
                }
            });

            chbNumbSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = txtNumbStartSubtitle[curCbSubtitle].isEnabled();

                    lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
                    txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
                    lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
                    txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
                    lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            txtNumbStartSubtitle[nSubtitle].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbStartSubtitle[curCbSubtitle].getText()) < 0) {
                            txtNumbStartSubtitle[curCbSubtitle].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbStartSubtitle[curCbSubtitle].setText("1");
                    }
                }
            });

            txtNumbPadSubtitle[nSubtitle].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();

                    try {
                        if (Integer.parseInt(txtNumbPadSubtitle[curCbSubtitle].getText()) < 0) {
                            txtNumbPadSubtitle[curCbSubtitle].setText("1");
                        }
                    } catch (NumberFormatException e1) {
                        txtNumbPadSubtitle[curCbSubtitle].setText("1");
                    }
                }
            });

            chbLangSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = cbLangSubtitle[curCbSubtitle].isEnabled();

                    cbLangSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            chbExtraCmdSubtitle[nSubtitle].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbSubtitle = cbSubtitle.getSelectedIndex();
                    boolean state = txtExtraCmdSubtitle[curCbSubtitle].isEnabled();

                    txtExtraCmdSubtitle[curCbSubtitle].setEnabled(!state);
                }
            });

            cbSubtitle.addItem(LanguageManager.getString("track.subtitle.title") + (nSubtitle + 1));
        }

        nSubtitle++;
    }

    /* End of track addition methods */

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

    private void setCmdLineVideo() {
        cmdLineVideo = new String[modelFiles.size()];
        cmdLineVideoOpt = new String[modelFiles.size()];
        String[] tmpCmdLineVideo = new String[nVideo];
        String[] tmpCmdLineVideoOpt = new String[nVideo];
        int[] numStartVideo = new int[nVideo];
        int[] numPadVideo = new int[nVideo];

        for (int i = 0; i < modelFiles.size(); i++) {
            int editCount = 0;
            cmdLineVideo[i] = "";
            cmdLineVideoOpt[i] = "";

            for (int j = 0; j < nVideo; j++) {
                if (chbEditVideo[j].isSelected()) {
                    numStartVideo[j] = Integer.parseInt(txtNumbStartVideo[j].getText());
                    numPadVideo[j] = Integer.parseInt(txtNumbPadVideo[j].getText());

                    tmpCmdLineVideo[j] = "";
                    tmpCmdLineVideoOpt[j] = "";

                    if (chbEditVideo[j].isSelected()) {
                        tmpCmdLineVideo[j] += " --edit track:v" + (j + 1);
                        tmpCmdLineVideoOpt[j] += " --edit track:v" + (j + 1);
                    }

                    if (chbEnableVideo[j].isSelected()) {
                        tmpCmdLineVideo[j] += " --set flag-enabled=";
                        tmpCmdLineVideoOpt[j] += " --set flag-enabled=";

                        if (rbYesEnableVideo[j].isSelected()) {
                            tmpCmdLineVideo[j] += "1";
                            tmpCmdLineVideoOpt[j] += "1";
                        } else {
                            tmpCmdLineVideo[j] += "0";
                            tmpCmdLineVideoOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbDefaultVideo[j].isSelected()) {
                        tmpCmdLineVideo[j] += " --set flag-default=";
                        tmpCmdLineVideoOpt[j] += " --set flag-default=";

                        if (rbYesDefVideo[j].isSelected()) {
                            tmpCmdLineVideo[j] += "1";
                            tmpCmdLineVideoOpt[j] += "1";
                        } else {
                            tmpCmdLineVideo[j] += "0";
                            tmpCmdLineVideoOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbForcedVideo[j].isSelected()) {
                        tmpCmdLineVideo[j] += " --set flag-forced=";
                        tmpCmdLineVideoOpt[j] += " --set flag-forced=";

                        if (rbYesForcedVideo[j].isSelected()) {
                            tmpCmdLineVideo[j] += "1";
                            tmpCmdLineVideoOpt[j] += "1";
                        } else {
                            tmpCmdLineVideo[j] += "0";
                            tmpCmdLineVideoOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbNameVideo[j].isSelected()) {
                        tmpCmdLineVideo[j] += " --set name=\"" + Utils.escapeQuotes(txtNameVideo[j].getText()) + "\"";
                        tmpCmdLineVideoOpt[j] += " --set name=\"" + Utils.escapeName(txtNameVideo[j].getText()) + "\"";
                        editCount++;
                    }

                    if (chbLangVideo[j].isSelected()) {
                        String curLangCode = mkvStrings.getLangCodeList().get(cbLangVideo[j].getSelectedIndex());
                        tmpCmdLineVideo[j] += " --set language=\"" + curLangCode + "\"";
                        tmpCmdLineVideoOpt[j] += " --set language=\"" + curLangCode + "\"";
                        editCount++;
                    }

                    if (chbExtraCmdVideo[j].isSelected() && !txtExtraCmdVideo[j].getText().trim().isEmpty()) {
                        tmpCmdLineVideo[j] += " " + txtExtraCmdVideo[j].getText();
                        tmpCmdLineVideoOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdVideo[j].getText());
                        editCount++;
                    }

                    if (editCount == 0) {
                        tmpCmdLineVideo[j] = "";
                        tmpCmdLineVideoOpt[j] = "";
                    }
                } else {
                    tmpCmdLineVideo[j] = "";
                    tmpCmdLineVideoOpt[j] = "";
                }
            }
        }

        for (int i = 0; i < nVideo; i++) {
            for (int j = 0; j < modelFiles.size(); j++) {
                String tmpText = tmpCmdLineVideo[i];
                String tmpText2 = tmpCmdLineVideoOpt[i];

                if (chbNumbVideo[i].isSelected() && chbEditVideo[i].isSelected()) {
                    tmpText = tmpText.replace("{num}", Utils.padNumber(numPadVideo[i], numStartVideo[i]));
                    tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadVideo[i], numStartVideo[i]));
                    numStartVideo[i]++;
                }

                tmpText = tmpText.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));
                tmpText2 = tmpText2.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));

                cmdLineVideo[j] += tmpText;
                cmdLineVideoOpt[j] += tmpText2;
            }
        }
    }

    private void setCmdLineAudio() {
        cmdLineAudio = new String[modelFiles.size()];
        cmdLineAudioOpt = new String[modelFiles.size()];
        String[] tmpCmdLineAudio = new String[nAudio];
        String[] tmpCmdLineAudioOpt = new String[nAudio];
        int[] numStartAudio = new int[nAudio];
        int[] numPadAudio = new int[nAudio];

        for (int i = 0; i < modelFiles.size(); i++) {
            int editCount = 0;
            cmdLineAudio[i] = "";
            cmdLineAudioOpt[i] = "";

            for (int j = 0; j < nAudio; j++) {
                if (chbEditAudio[j].isSelected()) {
                    numStartAudio[j] = Integer.parseInt(txtNumbStartAudio[j].getText());
                    numPadAudio[j] = Integer.parseInt(txtNumbPadAudio[j].getText());

                    tmpCmdLineAudio[j] = "";
                    tmpCmdLineAudioOpt[j] = "";

                    if (chbEditAudio[j].isSelected()) {
                        tmpCmdLineAudio[j] += " --edit track:a" + (j + 1);
                        tmpCmdLineAudioOpt[j] += " --edit track:a" + (j + 1);
                    }

                    if (chbEnableAudio[j].isSelected()) {
                        tmpCmdLineAudio[j] += " --set flag-enabled=";
                        tmpCmdLineAudioOpt[j] += " --set flag-enabled=";

                        if (rbYesEnableAudio[j].isSelected()) {
                            tmpCmdLineAudio[j] += "1";
                            tmpCmdLineAudioOpt[j] += "1";
                        } else {
                            tmpCmdLineAudio[j] += "0";
                            tmpCmdLineAudioOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbDefaultAudio[j].isSelected()) {
                        tmpCmdLineAudio[j] += " --set flag-default=";
                        tmpCmdLineAudioOpt[j] += " --set flag-default=";

                        if (rbYesDefAudio[j].isSelected()) {
                            tmpCmdLineAudio[j] += "1";
                            tmpCmdLineAudioOpt[j] += "1";
                        } else {
                            tmpCmdLineAudio[j] += "0";
                            tmpCmdLineAudioOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbForcedAudio[j].isSelected()) {
                        tmpCmdLineAudio[j] += " --set flag-forced=";
                        tmpCmdLineAudioOpt[j] += " --set flag-forced=";

                        if (rbYesForcedAudio[j].isSelected()) {
                            tmpCmdLineAudio[j] += "1";
                            tmpCmdLineAudioOpt[j] += "1";
                        } else {
                            tmpCmdLineAudio[j] += "0";
                            tmpCmdLineAudioOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbNameAudio[j].isSelected()) {
                        tmpCmdLineAudio[j] += " --set name=\"" + Utils.escapeQuotes(txtNameAudio[j].getText()) + "\"";
                        tmpCmdLineAudioOpt[j] += " --set name=\"" + Utils.escapeName(txtNameAudio[j].getText()) + "\"";
                        editCount++;
                    }

                    if (chbLangAudio[j].isSelected()) {
                        String curLangCode = mkvStrings.getLangCodeList().get(cbLangAudio[j].getSelectedIndex());
                        tmpCmdLineAudio[j] += " --set language=\"" + curLangCode + "\"";
                        tmpCmdLineAudioOpt[j] += " --set language=\"" + curLangCode + "\"";
                        editCount++;
                    }

                    if (chbExtraCmdAudio[j].isSelected() && !txtExtraCmdAudio[j].getText().trim().isEmpty()) {
                        tmpCmdLineAudio[j] += " " + txtExtraCmdAudio[j].getText();
                        tmpCmdLineAudioOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdAudio[j].getText());
                        editCount++;
                    }

                    if (editCount == 0) {
                        tmpCmdLineAudio[j] = "";
                        tmpCmdLineAudioOpt[j] = "";
                    }
                } else {
                    tmpCmdLineAudio[j] = "";
                    tmpCmdLineAudioOpt[j] = "";
                }
            }
        }

        for (int i = 0; i < nAudio; i++) {
            for (int j = 0; j < modelFiles.size(); j++) {
                String tmpText = tmpCmdLineAudio[i];
                String tmpText2 = tmpCmdLineAudioOpt[i];

                if (chbNumbAudio[i].isSelected() && chbEditAudio[i].isSelected()) {
                    tmpText = tmpText.replace("{num}", Utils.padNumber(numPadAudio[i], numStartAudio[i]));
                    tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadAudio[i], numStartAudio[i]));
                    numStartAudio[i]++;
                }

                tmpText = tmpText.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));
                tmpText2 = tmpText2.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));

                cmdLineAudio[j] += tmpText;
                cmdLineAudioOpt[j] += tmpText2;
            }
        }
    }

    private void setCmdLineSubtitle() {
        cmdLineSubtitle = new String[modelFiles.size()];
        cmdLineSubtitleOpt = new String[modelFiles.size()];
        String[] tmpCmdLineSubtitle = new String[nSubtitle];
        String[] tmpCmdLineSubtitleOpt = new String[nSubtitle];
        int[] numStartSubtitle = new int[nSubtitle];
        int[] numPadSubtitle = new int[nSubtitle];

        for (int i = 0; i < modelFiles.size(); i++) {
            int editCount = 0;
            cmdLineSubtitle[i] = "";
            cmdLineSubtitleOpt[i] = "";

            for (int j = 0; j < nSubtitle; j++) {
                if (chbEditSubtitle[j].isSelected()) {
                    numStartSubtitle[j] = Integer.parseInt(txtNumbStartSubtitle[j].getText());
                    numPadSubtitle[j] = Integer.parseInt(txtNumbPadSubtitle[j].getText());

                    tmpCmdLineSubtitle[j] = "";
                    tmpCmdLineSubtitleOpt[j] = "";

                    if (chbEditSubtitle[j].isSelected()) {
                        tmpCmdLineSubtitle[j] += " --edit track:s" + (j + 1);
                        tmpCmdLineSubtitleOpt[j] += " --edit track:s" + (j + 1);
                    }

                    if (chbEnableSubtitle[j].isSelected()) {
                        tmpCmdLineSubtitle[j] += " --set flag-enabled=";
                        tmpCmdLineSubtitleOpt[j] += " --set flag-enabled=";

                        if (rbYesEnableSubtitle[j].isSelected()) {
                            tmpCmdLineSubtitle[j] += "1";
                            tmpCmdLineSubtitleOpt[j] += "1";
                        } else {
                            tmpCmdLineSubtitle[j] += "0";
                            tmpCmdLineSubtitleOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbDefaultSubtitle[j].isSelected()) {
                        tmpCmdLineSubtitle[j] += " --set flag-default=";
                        tmpCmdLineSubtitleOpt[j] += " --set flag-default=";

                        if (rbYesDefSubtitle[j].isSelected()) {
                            tmpCmdLineSubtitle[j] += "1";
                            tmpCmdLineSubtitleOpt[j] += "1";
                        } else {
                            tmpCmdLineSubtitle[j] += "0";
                            tmpCmdLineSubtitleOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbForcedSubtitle[j].isSelected()) {
                        tmpCmdLineSubtitle[j] += " --set flag-forced=";
                        tmpCmdLineSubtitleOpt[j] += " --set flag-forced=";

                        if (rbYesForcedSubtitle[j].isSelected()) {
                            tmpCmdLineSubtitle[j] += "1";
                            tmpCmdLineSubtitleOpt[j] += "1";
                        } else {
                            tmpCmdLineSubtitle[j] += "0";
                            tmpCmdLineSubtitleOpt[j] += "0";
                        }

                        editCount++;
                    }

                    if (chbNameSubtitle[j].isSelected()) {
                        tmpCmdLineSubtitle[j] += " --set name=\"" + Utils.escapeQuotes(txtNameSubtitle[j].getText())
                                + "\"";
                        tmpCmdLineSubtitleOpt[j] += " --set name=\"" + Utils.escapeName(txtNameSubtitle[j].getText())
                                + "\"";
                        editCount++;
                    }

                    if (chbLangSubtitle[j].isSelected()) {
                        String curLangCode = mkvStrings.getLangCodeList().get(cbLangSubtitle[j].getSelectedIndex());
                        tmpCmdLineSubtitle[j] += " --set language=\"" + curLangCode + "\"";
                        tmpCmdLineSubtitleOpt[j] += " --set language=\"" + curLangCode + "\"";
                        editCount++;
                    }

                    if (chbExtraCmdSubtitle[j].isSelected() && !txtExtraCmdSubtitle[j].getText().trim().isEmpty()) {
                        tmpCmdLineSubtitle[j] += " " + txtExtraCmdSubtitle[j].getText();
                        tmpCmdLineSubtitleOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdSubtitle[j].getText());
                        editCount++;
                    }

                    if (editCount == 0) {
                        tmpCmdLineSubtitle[j] = "";
                        tmpCmdLineSubtitleOpt[j] = "";
                    }
                } else {
                    tmpCmdLineSubtitle[j] = "";
                    tmpCmdLineSubtitleOpt[j] = "";
                }
            }
        }

        for (int i = 0; i < nSubtitle; i++) {
            for (int j = 0; j < modelFiles.size(); j++) {
                String tmpText = tmpCmdLineSubtitle[i];
                String tmpText2 = tmpCmdLineSubtitleOpt[i];

                if (chbNumbSubtitle[i].isSelected() && chbEditSubtitle[i].isSelected()) {
                    tmpText = tmpText.replace("{num}", Utils.padNumber(numPadSubtitle[i], numStartSubtitle[i]));
                    tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadSubtitle[i], numStartSubtitle[i]));
                    numStartSubtitle[i]++;
                }

                tmpText = tmpText.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));
                tmpText2 = tmpText2.replace("{file_name}", Utils.getFileNameWithoutExt(modelFiles.get(j)));

                cmdLineSubtitle[j] += tmpText;
                cmdLineSubtitleOpt[j] += tmpText2;
            }
        }
    }

    private void setCmdLineAttachmentsAdd() {
        cmdLineAttachmentsAdd = "";
        cmdLineAttachmentsAddOpt = "";

        for (int i = 0; i < modelAttachmentsAdd.getRowCount(); i++) {
            String file = modelAttachmentsAdd.getValueAt(i, 0).toString();
            String name = modelAttachmentsAdd.getValueAt(i, 1).toString();
            String desc = modelAttachmentsAdd.getValueAt(i, 2).toString();
            String mime = modelAttachmentsAdd.getValueAt(i, 3).toString();

            if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
                if (!name.isEmpty()) {
                    cmdLineAttachmentsAdd += " --attachment-name \"" + name + "\"";
                    cmdLineAttachmentsAddOpt += " --attachment-name \"" + Utils.escapeName(name) + "\"";
                }

                if (!desc.isEmpty()) {
                    cmdLineAttachmentsAdd += " --attachment-description \"" + desc + "\"";
                    cmdLineAttachmentsAddOpt += " --attachment-description \"" + Utils.escapeName(desc) + "\"";
                }

                if (!mime.isEmpty()) {
                    cmdLineAttachmentsAdd += " --attachment-mime-type \"" + mime + "\"";
                    cmdLineAttachmentsAddOpt += " --attachment-mime-type \"" + Utils.escapeName(mime) + "\"";
                }
            }

            cmdLineAttachmentsAdd += " --add-attachment \"" + file + "\"";
            cmdLineAttachmentsAddOpt += " --add-attachment \"" + Utils.escapeName(file) + "\"";
        }
    }

    private void setCmdLineAttachmentsReplace() {
        cmdLineAttachmentsReplace = "";
        cmdLineAttachmentsReplaceOpt = "";

        for (int i = 0; i < modelAttachmentsReplace.getRowCount(); i++) {
            String type = modelAttachmentsReplace.getValueAt(i, 0).toString();
            String orig = modelAttachmentsReplace.getValueAt(i, 1).toString();
            String replace = modelAttachmentsReplace.getValueAt(i, 2).toString();
            String name = modelAttachmentsReplace.getValueAt(i, 3).toString();
            String desc = modelAttachmentsReplace.getValueAt(i, 4).toString();
            String mime = modelAttachmentsReplace.getValueAt(i, 5).toString();

            if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
                if (!name.isEmpty()) {
                    cmdLineAttachmentsReplace += " --attachment-name \"" + name + "\"";
                    cmdLineAttachmentsReplaceOpt += " --attachment-name \"" + Utils.escapeName(name) + "\"";
                }

                if (!desc.isEmpty()) {
                    cmdLineAttachmentsReplace += " --attachment-description \"" + desc + "\"";
                    cmdLineAttachmentsReplaceOpt += " --attachment-description \"" + Utils.escapeName(desc) + "\"";
                }

                if (!mime.isEmpty()) {
                    cmdLineAttachmentsReplace += " --attachment-mime-type \"" + mime + "\"";
                    cmdLineAttachmentsReplaceOpt += " --attachment-mime-type \"" + Utils.escapeName(mime) + "\"";
                }

            }

            if (type.equals(rbAttachReplaceName.getText())) {
                cmdLineAttachmentsReplace += " --replace-attachment \"name:" + orig + ":" + replace + "\"";
                cmdLineAttachmentsReplaceOpt += " --replace-attachment \"name:" + Utils.escapeName(orig) + ":"
                        + Utils.escapeName(replace) + "\"";
            } else if (type.equals(rbAttachReplaceID.getText())) {
                cmdLineAttachmentsReplace += " --replace-attachment \"" + orig + ":" + replace + "\"";
                cmdLineAttachmentsReplaceOpt += " --replace-attachment \"" + orig + ":" + Utils.escapeName(replace)
                        + "\"";
            } else {
                cmdLineAttachmentsReplace += " --replace-attachment \"mime-type:" + orig + ":" + replace + "\"";
                cmdLineAttachmentsReplaceOpt += " --replace-attachment \"mime-type:" + Utils.escapeName(orig) + ":"
                        + Utils.escapeName(replace) + "\"";
            }
        }
    }

    private void setCmdLineAttachmentsDelete() {
        cmdLineAttachmentsDelete = "";
        cmdLineAttachmentsDeleteOpt = "";

        for (int i = 0; i < modelAttachmentsDelete.getRowCount(); i++) {
            String type = modelAttachmentsDelete.getValueAt(i, 0).toString();
            String value = modelAttachmentsDelete.getValueAt(i, 1).toString();

            if (type.equals(rbAttachDeleteName.getText())) {
                cmdLineAttachmentsDelete += " --delete-attachment \"name:" + value + "\"";
                cmdLineAttachmentsDeleteOpt += " --delete-attachment \"name:" + Utils.escapeName(value) + "\"";
            } else if (type.equals(rbAttachDeleteID.getText())) {
                cmdLineAttachmentsDelete += " --delete-attachment \"" + value + "\"";
                cmdLineAttachmentsDeleteOpt += " --delete-attachment \"" + value + "\"";
            } else {
                cmdLineAttachmentsDelete += " --delete-attachment \"mime-type:" + value + "\"";
                cmdLineAttachmentsDeleteOpt += " --delete-attachment \"mime-type:" + Utils.escapeName(value) + "\"";
            }
        }
    }

    private void setCmdLine() {
        setCmdLineGeneral();
        setCmdLineVideo();
        setCmdLineAudio();
        setCmdLineSubtitle();
        setCmdLineAttachmentsAdd();
        setCmdLineAttachmentsReplace();
        setCmdLineAttachmentsDelete();

        cmdLineBatch = new ArrayList<String>();
        cmdLineBatchOpt = new ArrayList<String>();

        String cmdTemp = cmdLineGeneral[0] + cmdLineAttachmentsDelete + cmdLineAttachmentsAdd
                + cmdLineAttachmentsReplace + cmdLineVideo[0] + cmdLineAudio[0] + cmdLineSubtitle[0];

        if (!cmdTemp.isEmpty()) {
            for (int i = 0; i < modelFiles.getSize(); i++) {
                String cmdLineAll = cmdLineGeneral[i] + cmdLineAttachmentsDelete + cmdLineAttachmentsAdd
                        + cmdLineAttachmentsReplace + cmdLineVideo[i] + cmdLineAudio[i] + cmdLineSubtitle[i];

                String cmdLineAllOpt = cmdLineGeneralOpt[i] + cmdLineAttachmentsDeleteOpt + cmdLineAttachmentsAddOpt
                        + cmdLineAttachmentsReplaceOpt + cmdLineVideoOpt[i] + cmdLineAudioOpt[i]
                        + cmdLineSubtitleOpt[i];

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
                txtOutput.setText("");
                pnlTabs.setSelectedIndex(pnlTabs.getTabCount() - 1);
                pnlTabs.setEnabled(false);
                btnProcessFiles.setEnabled(false);
                btnGenerateCmdLine.setEnabled(false);

                for (int i = 0; i < cmdLineBatch.size(); i++) {
                    try {
                        File optFile = new File("options.json");
                        PrintWriter optFilePW = new PrintWriter(optFile, "UTF-8");
                        String[] optFileContents = Commandline.translateCommandline(cmdLineBatchOpt.get(i));
                        int optFileMaxLines = optFileContents.length - 1;

                        if (!optFile.exists()) {
                            optFile.createNewFile();
                        }

                        optFilePW.println("[");
                        int curLine = 0;
                        for (String content : optFileContents) {
                            content = Utils.fixEscapedQuotes(content);

                            optFilePW.print("  \"" + content + "\"");
                            if (curLine != optFileMaxLines)
                                optFilePW.print(",");
                            optFilePW.println();
                            curLine++;
                        }
                        optFilePW.println("]");
                        optFilePW.flush();
                        optFilePW.close();

                        ProcessBuilder pb = new ProcessBuilder(txtMkvPropExe.getText(), "@options.json");
                        pb.redirectErrorStream(true);

                        optFilePW.flush();
                        optFilePW.close();

                        pb.command(txtMkvPropExe.getText(), "@options.json");
                        pb.redirectErrorStream(true);

                        txtOutput.append("File: " + modelFiles.get(i) + "\n");
                        txtOutput.append("Command line: " + cmdLineBatch.get(i) + "\n\n");

                        proc = pb.start();

                        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), txtOutput);
                        outputGobbler.start();

                        proc.waitFor();

                        optFile.delete();

                        if (i < cmdLineBatch.size() - 1) {
                            txtOutput.append("--------------\n\n");
                        }

                        Thread.sleep(10);
                    } catch (IOException e) {
                    } catch (InterruptedException e) {
                        break;
                    }
                }

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
        worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    pb.command(exe);
                    pb.redirectErrorStream(true);
                    proc = pb.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    proc.waitFor();
                    in.close();

                    exeFound = true;
                } catch (IOException e) {
                    exeFound = false;
                } catch (InterruptedException e) {
                    exeFound = false;
                }

                return null;
            }
        };

        worker.execute();
        while (!worker.isDone()) {
        }

        return exeFound;
    }

    /* End of command line methods */

    /* Start of INI configuration file methods */

    private void readIniFile() {
        if (!iniFile.exists()) {
            try {
                iniFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Utils.isWindows()) {
                String exePath = getMkvPropExeDefaullt();
                if (exePath != null) {
                    saveIniFile(new File(exePath));
                    txtMkvPropExe.setText(exePath);
                    chbMkvPropExeDef.setSelected(false);
                    chbMkvPropExeDef.setEnabled(true);
                }
            } else {
                defaultIniFile();
            }
        }

        try {
            Ini ini = new Ini(iniFile);
            profileManager = new ProfileManager(ini);

            String exePath = ini.get("General", "mkvpropedit");

            if (exePath != null) {
                if (exePath.equals("mkvpropedit")) {
                    chbMkvPropExeDef.setSelected(true);
                    chbMkvPropExeDef.setEnabled(false);
                } else {
                    txtMkvPropExe.setText(exePath);
                    chbMkvPropExeDef.setSelected(false);
                    chbMkvPropExeDef.setEnabled(true);
                }
            }
        } catch (InvalidFileFormatException e) {
        } catch (IOException e) {
        }

        loadProfilesToModel();
    }

    private void loadProfilesToModel() {
        if (profileManager == null)
            return;
        loadProfileModel(modelAudioProfiles, ProfileType.AUDIO);
        loadProfileModel(modelVideoProfiles, ProfileType.VIDEO);
        loadProfileModel(modelSubtitleProfiles, ProfileType.SUBTITLE);
    }

    private void loadProfileModel(DefaultListModel<TrackProfile> model, ProfileType type) {
        if (model != null) {
            model.clear();
            for (TrackProfile p : profileManager.getProfiles(type)) {
                model.addElement(p);
            }
        }
    }

    // ... (INI methods skipped, keeping them as they are not selected in this range
    // usually unless I select them)
    // Wait, the selection started at 4786 which is INSIDE readIniFile?
    // No, 4786 is loadProfilesToModel.
    // I need to be careful with range. 4786 is loadProfilesToModel.
    // 4796 is saveIniFile.
    // I should only replace specific methods.

    // Let's replace loadProfilesToModel first.

    private void saveIniFile(File exeFile) {
        Ini ini = null;

        txtMkvPropExe.setText(exeFile.toString());
        chbMkvPropExeDef.setSelected(false);
        chbMkvPropExeDef.setEnabled(true);

        try {
            if (!iniFile.exists()) {
                iniFile.createNewFile();
            }

            ini = new Ini(iniFile);
            ini.put("General", "mkvpropedit", exeFile.toString());
            ini.store();
        } catch (InvalidFileFormatException e1) {
        } catch (IOException e1) {
        }
    }

    private void defaultIniFile() {
        Ini ini = null;

        try {
            if (!iniFile.exists()) {
                iniFile.createNewFile();
            }

            ini = new Ini(iniFile);

            ini.put("General", "mkvpropedit", "mkvpropedit");

            ini.store();
        } catch (InvalidFileFormatException e1) {
        } catch (IOException e1) {
        }
    }

    private String getMkvPropExeDefaullt() {
        String sysDrive = System.getenv("SystemDrive");
        String exePaths[] = new String[] { sysDrive + "\\Program Files (x86)\\MKVToolNix",
                sysDrive + "\\Program Files\\MKVToolNix" };

        for (int i = 0; i < exePaths.length; i++) {
            File tmpExe = new File(exePaths[i] + "\\mkvpropedit.exe");

            if (tmpExe.exists()) {
                return tmpExe.toString();
            }
        }

        return null;
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

    private void applyAudioProfile(TrackProfile p, int trackIdx) {
        if (p == null || trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        chbEditAudio[trackIdx].setSelected(true);
        // Force refresh state for controls
        boolean alreadyEnabled = chbDefaultAudio[trackIdx].isEnabled();
        if (!alreadyEnabled) {
            toggleAudio(trackIdx);
        }

        chbEnableAudio[trackIdx].setSelected(p.isUseEnableTrack());
        if (p.isEnableTrack())
            rbYesEnableAudio[trackIdx].setSelected(true);
        else
            rbNoEnableAudio[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isEnable = chbEnableAudio[trackIdx].isSelected();
        rbYesEnableAudio[trackIdx].setEnabled(isEnable);
        rbNoEnableAudio[trackIdx].setEnabled(isEnable);

        chbDefaultAudio[trackIdx].setSelected(p.isUseDefaultTrack());
        if (p.isDefaultTrack())
            rbYesDefAudio[trackIdx].setSelected(true);
        else
            rbNoDefAudio[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isDef = chbDefaultAudio[trackIdx].isSelected();
        rbYesDefAudio[trackIdx].setEnabled(isDef);
        rbNoDefAudio[trackIdx].setEnabled(isDef);

        chbForcedAudio[trackIdx].setSelected(p.isUseForcedTrack());
        if (p.isForcedTrack())
            rbYesForcedAudio[trackIdx].setSelected(true);
        else
            rbNoForcedAudio[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isForced = chbForcedAudio[trackIdx].isSelected();
        rbYesForcedAudio[trackIdx].setEnabled(isForced);
        rbNoForcedAudio[trackIdx].setEnabled(isForced);

        chbNameAudio[trackIdx].setSelected(p.isUseName());
        txtNameAudio[trackIdx].setText(p.getTrackName() == null ? "" : p.getTrackName());
        txtNameAudio[trackIdx].setEnabled(p.isUseName());
        chbNumbAudio[trackIdx].setEnabled(p.isUseName());

        if (p.getLanguage() != null && !p.getLanguage().isEmpty()) {
            cbLangAudio[trackIdx].setSelectedItem(p.getLanguage());
        }
        chbLangAudio[trackIdx].setSelected(p.isUseLanguage());
        cbLangAudio[trackIdx].setEnabled(p.isUseLanguage());
    }

    private void applyVideoProfile(TrackProfile p, int trackIdx) {
        if (p == null || trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        chbEditVideo[trackIdx].setSelected(true);
        boolean alreadyEnabled = chbDefaultVideo[trackIdx].isEnabled();
        if (!alreadyEnabled) {
            toggleVideo(trackIdx);
        }

        chbEnableVideo[trackIdx].setSelected(p.isUseEnableTrack());
        if (p.isEnableTrack())
            rbYesEnableVideo[trackIdx].setSelected(true);
        else
            rbNoEnableVideo[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isEnable = chbEnableVideo[trackIdx].isSelected();
        rbYesEnableVideo[trackIdx].setEnabled(isEnable);
        rbNoEnableVideo[trackIdx].setEnabled(isEnable);

        chbDefaultVideo[trackIdx].setSelected(p.isUseDefaultTrack());
        if (p.isDefaultTrack())
            rbYesDefVideo[trackIdx].setSelected(true);
        else
            rbNoDefVideo[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isDef = chbDefaultVideo[trackIdx].isSelected();
        rbYesDefVideo[trackIdx].setEnabled(isDef);
        rbNoDefVideo[trackIdx].setEnabled(isDef);

        chbForcedVideo[trackIdx].setSelected(p.isUseForcedTrack());
        if (p.isForcedTrack())
            rbYesForcedVideo[trackIdx].setSelected(true);
        else
            rbNoForcedVideo[trackIdx].setSelected(true);

        // Trigger enable state update
        boolean isForced = chbForcedVideo[trackIdx].isSelected();
        rbYesForcedVideo[trackIdx].setEnabled(isForced);
        rbNoForcedVideo[trackIdx].setEnabled(isForced);

        chbNameVideo[trackIdx].setSelected(p.isUseName());
        txtNameVideo[trackIdx].setText(p.getTrackName() == null ? "" : p.getTrackName());
        txtNameVideo[trackIdx].setEnabled(p.isUseName());
        chbNumbVideo[trackIdx].setEnabled(p.isUseName());
        // Handling nested enable state for numbering if needed, but simple enable is
        // sufficient for now

        if (p.getLanguage() != null && !p.getLanguage().isEmpty()) {
            cbLangVideo[trackIdx].setSelectedItem(p.getLanguage());
        }
        chbLangVideo[trackIdx].setSelected(p.isUseLanguage());
        cbLangVideo[trackIdx].setEnabled(p.isUseLanguage());
    }

    private void applySubtitleProfile(TrackProfile p, int trackIdx) {
        if (p == null || trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        chbEditSubtitle[trackIdx].setSelected(true);
        // Force refresh state for controls
        // Using direct enable check as toggleSubtitle might not be standard logic but
        // consistent with others
        boolean alreadyEnabled = chbDefaultSubtitle[trackIdx].isEnabled();
        // Assuming toggleSubtitle works or controls are enabled by listener
        if (!alreadyEnabled) {
            // Simulate toggle since we can't find method easily or trigger listener
            // Actually, chbEditSubtitle has an action listener that calls toggleSubtitle
            // (presumably)
            // Let's just manually enable the top level if needed, but firing the button
            // click might be safer if we knew it works
            // For consistency with other methods, we assume a similar toggle exists or
            // manual enablement
            // Let's rely on manual setEnabled for sub-components if toggle is missing
            // BUT, the existing code called toggleAudio/Video.
            // I'll assume toggleSubtitle exists or I will just set enabled states directly
            // below.

            // Update: chbEditSubtitle listeners are usually added in init.
            // If toggleSubtitle is not available, we should enable components manually.
            // However, for safety in this refactor, I will copy the pattern.

            // Manually enabling core checkboxes to be safe
            chbEnableSubtitle[trackIdx].setEnabled(true);
            chbDefaultSubtitle[trackIdx].setEnabled(true);
            chbForcedSubtitle[trackIdx].setEnabled(true);
            chbNameSubtitle[trackIdx].setEnabled(true);
            chbLangSubtitle[trackIdx].setEnabled(true);
        }

        chbEnableSubtitle[trackIdx].setSelected(p.isUseEnableTrack());
        if (p.isEnableTrack())
            rbYesEnableSubtitle[trackIdx].setSelected(true);
        else
            rbNoEnableSubtitle[trackIdx].setSelected(true);

        boolean isEnable = chbEnableSubtitle[trackIdx].isSelected();
        rbYesEnableSubtitle[trackIdx].setEnabled(isEnable);
        rbNoEnableSubtitle[trackIdx].setEnabled(isEnable);

        chbDefaultSubtitle[trackIdx].setSelected(p.isUseDefaultTrack());
        if (p.isDefaultTrack())
            rbYesDefSubtitle[trackIdx].setSelected(true);
        else
            rbNoDefSubtitle[trackIdx].setSelected(true);

        boolean isDef = chbDefaultSubtitle[trackIdx].isSelected();
        rbYesDefSubtitle[trackIdx].setEnabled(isDef);
        rbNoDefSubtitle[trackIdx].setEnabled(isDef);

        chbForcedSubtitle[trackIdx].setSelected(p.isUseForcedTrack());
        if (p.isForcedTrack())
            rbYesForcedSubtitle[trackIdx].setSelected(true);
        else
            rbNoForcedSubtitle[trackIdx].setSelected(true);

        boolean isForced = chbForcedSubtitle[trackIdx].isSelected();
        rbYesForcedSubtitle[trackIdx].setEnabled(isForced);
        rbNoForcedSubtitle[trackIdx].setEnabled(isForced);

        chbNameSubtitle[trackIdx].setSelected(p.isUseName());
        txtNameSubtitle[trackIdx].setText(p.getTrackName() == null ? "" : p.getTrackName());
        txtNameSubtitle[trackIdx].setEnabled(p.isUseName());
        chbNumbSubtitle[trackIdx].setEnabled(p.isUseName());

        if (p.getLanguage() != null && !p.getLanguage().isEmpty()) {
            cbLangSubtitle[trackIdx].setSelectedItem(p.getLanguage());
        }
        chbLangSubtitle[trackIdx].setSelected(p.isUseLanguage());
        cbLangSubtitle[trackIdx].setEnabled(p.isUseLanguage());
    }

    private void toggleAudio(int trackIdx) {
        if (trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        boolean isEdit = chbEditAudio[trackIdx].isSelected();

        chbEnableAudio[trackIdx].setEnabled(isEdit);
        chbDefaultAudio[trackIdx].setEnabled(isEdit);
        chbForcedAudio[trackIdx].setEnabled(isEdit);
        chbNameAudio[trackIdx].setEnabled(isEdit);
        chbLangAudio[trackIdx].setEnabled(isEdit);
        chbExtraCmdAudio[trackIdx].setEnabled(isEdit);

        if (txtNameAudio[trackIdx].isEnabled() || chbNameAudio[trackIdx].isSelected()) {
            txtNameAudio[trackIdx].setEnabled(isEdit && chbNameAudio[trackIdx].isSelected());
            chbNumbAudio[trackIdx].setEnabled(isEdit && chbNameAudio[trackIdx].isSelected());

            if (chbNumbAudio[trackIdx].isSelected()) {
                boolean isNumb = isEdit && chbNameAudio[trackIdx].isSelected();
                lblNumbStartAudio[trackIdx].setEnabled(isNumb);
                txtNumbStartAudio[trackIdx].setEnabled(isNumb);
                lblNumbPadAudio[trackIdx].setEnabled(isNumb);
                txtNumbPadAudio[trackIdx].setEnabled(isNumb);
                lblNumbExplainAudio[trackIdx].setEnabled(isNumb);
            }
        }

        if (rbNoEnableAudio[trackIdx].isEnabled() || chbEnableAudio[trackIdx].isSelected()) {
            boolean isEnable = isEdit && chbEnableAudio[trackIdx].isSelected();
            rbNoEnableAudio[trackIdx].setEnabled(isEnable);
            rbYesEnableAudio[trackIdx].setEnabled(isEnable);
        }

        if (rbNoDefAudio[trackIdx].isEnabled() || chbDefaultAudio[trackIdx].isSelected()) {
            boolean isDef = isEdit && chbDefaultAudio[trackIdx].isSelected();
            rbNoDefAudio[trackIdx].setEnabled(isDef);
            rbYesDefAudio[trackIdx].setEnabled(isDef);
        }

        if (rbNoForcedAudio[trackIdx].isEnabled() || chbForcedAudio[trackIdx].isSelected()) {
            boolean isForced = isEdit && chbForcedAudio[trackIdx].isSelected();
            rbNoForcedAudio[trackIdx].setEnabled(isForced);
            rbYesForcedAudio[trackIdx].setEnabled(isForced);
        }

        if (cbLangAudio[trackIdx].isEnabled() || chbLangAudio[trackIdx].isSelected()) {
            cbLangAudio[trackIdx].setEnabled(isEdit && chbLangAudio[trackIdx].isSelected());
        }

        if (txtExtraCmdAudio[trackIdx].isEnabled() || chbExtraCmdAudio[trackIdx].isSelected()) {
            boolean isExtra = isEdit && chbExtraCmdAudio[trackIdx].isSelected();
            // chbExtraCmdAudio is already linked to isEdit above
            txtExtraCmdAudio[trackIdx].setEnabled(isExtra);
        }
    }

    private void toggleVideo(int trackIdx) {
        if (trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        boolean isEdit = chbEditVideo[trackIdx].isSelected();

        chbEnableVideo[trackIdx].setEnabled(isEdit);
        chbDefaultVideo[trackIdx].setEnabled(isEdit);
        chbForcedVideo[trackIdx].setEnabled(isEdit);
        chbNameVideo[trackIdx].setEnabled(isEdit);
        chbLangVideo[trackIdx].setEnabled(isEdit);
        chbExtraCmdVideo[trackIdx].setEnabled(isEdit);

        if (txtNameVideo[trackIdx].isEnabled() || chbNameVideo[trackIdx].isSelected()) {
            txtNameVideo[trackIdx].setEnabled(isEdit && chbNameVideo[trackIdx].isSelected());
            chbNumbVideo[trackIdx].setEnabled(isEdit && chbNameVideo[trackIdx].isSelected());

            if (chbNumbVideo[trackIdx].isSelected()) {
                boolean isNumb = isEdit && chbNameVideo[trackIdx].isSelected();
                lblNumbStartVideo[trackIdx].setEnabled(isNumb);
                txtNumbStartVideo[trackIdx].setEnabled(isNumb);
                lblNumbPadVideo[trackIdx].setEnabled(isNumb);
                txtNumbPadVideo[trackIdx].setEnabled(isNumb);
                lblNumbExplainVideo[trackIdx].setEnabled(isNumb);
            }
        }

        if (rbNoEnableVideo[trackIdx].isEnabled() || chbEnableVideo[trackIdx].isSelected()) {
            boolean isEnable = isEdit && chbEnableVideo[trackIdx].isSelected();
            rbNoEnableVideo[trackIdx].setEnabled(isEnable);
            rbYesEnableVideo[trackIdx].setEnabled(isEnable);
        }

        if (rbNoDefVideo[trackIdx].isEnabled() || chbDefaultVideo[trackIdx].isSelected()) {
            boolean isDef = isEdit && chbDefaultVideo[trackIdx].isSelected();
            rbNoDefVideo[trackIdx].setEnabled(isDef);
            rbYesDefVideo[trackIdx].setEnabled(isDef);
        }

        if (rbNoForcedVideo[trackIdx].isEnabled() || chbForcedVideo[trackIdx].isSelected()) {
            boolean isForced = isEdit && chbForcedVideo[trackIdx].isSelected();
            rbNoForcedVideo[trackIdx].setEnabled(isForced);
            rbYesForcedVideo[trackIdx].setEnabled(isForced);
        }

        if (cbLangVideo[trackIdx].isEnabled() || chbLangVideo[trackIdx].isSelected()) {
            cbLangVideo[trackIdx].setEnabled(isEdit && chbLangVideo[trackIdx].isSelected());
        }

        if (txtExtraCmdVideo[trackIdx].isEnabled() || chbExtraCmdVideo[trackIdx].isSelected()) {
            boolean isExtra = isEdit && chbExtraCmdVideo[trackIdx].isSelected();
            // chbExtraCmdVideo is already linked to isEdit above
            txtExtraCmdVideo[trackIdx].setEnabled(isExtra);
        }
    }

    private JPanel createProfilePanel(ProfileType type, JList<TrackProfile> list, DefaultListModel<TrackProfile> model,
            java.util.function.Supplier<Integer> getSelectedTrackIndex,
            java.util.function.BiConsumer<TrackProfile, Integer> updateFromUI,
            java.util.function.BiConsumer<TrackProfile, Integer> applyToUI) {
        JPanel pnl = new JPanel();
        pnl.setBorder(new javax.swing.border.TitledBorder(null, LanguageManager.getString("profile.panel.title"),
                javax.swing.border.TitledBorder.LEADING,
                javax.swing.border.TitledBorder.TOP, null, null));
        pnl.setLayout(new BorderLayout(0, 0));
        pnl.setPreferredSize(new Dimension(180, 0));

        list.setModel(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDragEnabled(true);
        list.setDropMode(javax.swing.DropMode.INSERT);
        list.setTransferHandler(new javax.swing.TransferHandler() {
            private static final long serialVersionUID = 1L;
            private int draggedIndex = -1;

            @Override
            public int getSourceActions(javax.swing.JComponent c) {
                return MOVE;
            }

            @Override
            protected java.awt.datatransfer.Transferable createTransferable(javax.swing.JComponent c) {
                draggedIndex = list.getSelectedIndex();
                if (draggedIndex != -1) {
                    return new java.awt.datatransfer.StringSelection(String.valueOf(draggedIndex));
                }
                return null;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dl.getIndex();

                if (draggedIndex < 0 || draggedIndex >= model.getSize()) {
                    return false;
                }

                if (dropIndex == draggedIndex || dropIndex == draggedIndex + 1) {
                    return false; // No movement needed
                }

                TrackProfile draggedProfile = model.get(draggedIndex);
                model.remove(draggedIndex);

                // Adjust drop index if we removed an element before it
                if (dropIndex > draggedIndex) {
                    dropIndex--;
                }

                model.add(dropIndex, draggedProfile);
                list.setSelectedIndex(dropIndex);

                // Update the profile manager order
                profileManager.reorderProfiles(type, model);

                return true;
            }

            @Override
            protected void exportDone(javax.swing.JComponent source, java.awt.datatransfer.Transferable data,
                    int action) {
                draggedIndex = -1;
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = list.getSelectedIndex();
                    if (idx != -1) {
                        int trackIdx = getSelectedTrackIndex.get();
                        if (trackIdx != -1) {
                            applyToUI.accept(model.get(idx), trackIdx);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(list);
        pnl.add(sp, BorderLayout.CENTER);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        pnl.add(pnlControls, BorderLayout.SOUTH);

        JButton btnAdd = new JButton("");
        btnAdd.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
        btnAdd.setToolTipText(LanguageManager.getString("profile.add.tooltip"));
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(frmJMkvpropedit,
                        LanguageManager.getString("profile.add.prompt"));
                if (name != null && !name.trim().isEmpty()) {
                    int idx = getSelectedTrackIndex.get();
                    if (idx < 0) {
                        JOptionPane.showMessageDialog(frmJMkvpropedit,
                                LanguageManager.getString("profile.error.select.track"));
                        return;
                    }

                    TrackProfile p = new TrackProfile();
                    p.setName(name);
                    updateFromUI.accept(p, idx);

                    profileManager.addProfile(type, p);
                    model.addElement(p);
                }
            }
        });
        pnlControls.add(btnAdd);

        JButton btnUpdate = new JButton(LanguageManager.getString("profile.btn.update"));
        btnUpdate.setToolTipText(LanguageManager.getString("profile.update.tooltip"));
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    int trackIdx = getSelectedTrackIndex.get();
                    if (trackIdx < 0) {
                        JOptionPane.showMessageDialog(frmJMkvpropedit,
                                LanguageManager.getString("profile.error.select.track"));
                        return;
                    }

                    int response = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("profile.update.confirm"),
                            LanguageManager.getString("profile.update.title"),
                            JOptionPane.YES_NO_OPTION);

                    if (response == JOptionPane.YES_OPTION) {
                        TrackProfile p = model.get(idx);
                        updateFromUI.accept(p, trackIdx);

                        profileManager.saveProfiles();
                        list.repaint();
                    }
                }
            }
        });
        pnlControls.add(btnUpdate);

        JButton btnRename = new JButton(LanguageManager.getString("profile.btn.rename"));
        btnRename.setToolTipText(LanguageManager.getString("profile.rename.tooltip"));
        btnRename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    TrackProfile p = model.get(idx);
                    String newName = JOptionPane.showInputDialog(frmJMkvpropedit,
                            LanguageManager.getString("profile.rename.prompt"),
                            p.getName());
                    if (newName != null && !newName.trim().isEmpty()) {
                        p.setName(newName);
                        profileManager.saveProfiles();
                        list.repaint();
                    }
                }
            }
        });
        pnlControls.add(btnRename);

        JButton btnRemove = new JButton("");
        btnRemove.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
        btnRemove.setToolTipText(LanguageManager.getString("profile.remove.tooltip"));
        btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    profileManager.removeProfile(type, idx);
                    model.remove(idx);
                }
            }
        });
        pnlControls.add(btnRemove);

        return pnl;
    }

    @SuppressWarnings("deprecation")
    private void loadLanguage() {
        if (iniFile.exists()) {
            try {
                Ini ini = new Ini(iniFile);
                String lang = ini.get("General", "language");
                if (lang != null && !lang.isEmpty()) {
                    LanguageManager.setLocale(new Locale(lang));
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            e.printStackTrace();
        }
    }

}
