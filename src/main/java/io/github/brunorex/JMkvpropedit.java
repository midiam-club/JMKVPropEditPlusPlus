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

    private static final String VERSION_NUMBER = "v2.3.0";
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

    // IMP-01: Dynamic track controls lists (parallel to fixed arrays during
    // migration)
    private final List<TrackControls> videoTrackControls = new ArrayList<>();
    private final List<TrackControls> audioTrackControls = new ArrayList<>();
    private final List<TrackControls> subtitleTrackControls = new ArrayList<>();

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

    // Unified track component sets (Fase 3d)
    private static class TrackComponentSet {
        final JPanel[] subPanels;
        final JCheckBox[] chbEdit;
        final JCheckBox[] chbEnable;
        final JRadioButton[] rbYesEnable;
        final JRadioButton[] rbNoEnable;
        final ButtonGroup[] bgRbEnable;
        final JCheckBox[] chbDefault;
        final JRadioButton[] rbYesDef;
        final JRadioButton[] rbNoDef;
        final ButtonGroup[] bgRbDef;
        final JCheckBox[] chbForced;
        final JRadioButton[] rbYesForced;
        final JRadioButton[] rbNoForced;
        final ButtonGroup[] bgRbForced;
        final JCheckBox[] chbName;
        final JTextField[] txtName;
        final JCheckBox[] chbNumb;
        final JLabel[] lblNumbStart;
        final JTextField[] txtNumbStart;
        final JLabel[] lblNumbPad;
        final JTextField[] txtNumbPad;
        final JLabel[] lblNumbExplain;
        final JCheckBox[] chbLang;
        final JComboBox<String>[] cbLang;
        final JCheckBox[] chbExtraCmd;
        final JTextField[] txtExtraCmd;

        TrackComponentSet(JPanel[] subPanels, JCheckBox[] chbEdit, JCheckBox[] chbEnable,
                JRadioButton[] rbYesEnable, JRadioButton[] rbNoEnable, ButtonGroup[] bgRbEnable,
                JCheckBox[] chbDefault, JRadioButton[] rbYesDef, JRadioButton[] rbNoDef, ButtonGroup[] bgRbDef,
                JCheckBox[] chbForced, JRadioButton[] rbYesForced, JRadioButton[] rbNoForced, ButtonGroup[] bgRbForced,
                JCheckBox[] chbName, JTextField[] txtName,
                JCheckBox[] chbNumb, JLabel[] lblNumbStart, JTextField[] txtNumbStart,
                JLabel[] lblNumbPad, JTextField[] txtNumbPad, JLabel[] lblNumbExplain,
                JCheckBox[] chbLang, JComboBox<String>[] cbLang,
                JCheckBox[] chbExtraCmd, JTextField[] txtExtraCmd) {
            this.subPanels = subPanels;
            this.chbEdit = chbEdit;
            this.chbEnable = chbEnable;
            this.rbYesEnable = rbYesEnable;
            this.rbNoEnable = rbNoEnable;
            this.bgRbEnable = bgRbEnable;
            this.chbDefault = chbDefault;
            this.rbYesDef = rbYesDef;
            this.rbNoDef = rbNoDef;
            this.bgRbDef = bgRbDef;
            this.chbForced = chbForced;
            this.rbYesForced = rbYesForced;
            this.rbNoForced = rbNoForced;
            this.bgRbForced = bgRbForced;
            this.chbName = chbName;
            this.txtName = txtName;
            this.chbNumb = chbNumb;
            this.lblNumbStart = lblNumbStart;
            this.txtNumbStart = txtNumbStart;
            this.lblNumbPad = lblNumbPad;
            this.txtNumbPad = txtNumbPad;
            this.lblNumbExplain = lblNumbExplain;
            this.chbLang = chbLang;
            this.cbLang = cbLang;
            this.chbExtraCmd = chbExtraCmd;
            this.txtExtraCmd = txtExtraCmd;
        }
    }

    private final TrackComponentSet VIDEO_COMPONENTS = new TrackComponentSet(
            subPnlVideo, chbEditVideo, chbEnableVideo, rbYesEnableVideo, rbNoEnableVideo, bgRbEnableVideo,
            chbDefaultVideo, rbYesDefVideo, rbNoDefVideo, bgRbDefVideo,
            chbForcedVideo, rbYesForcedVideo, rbNoForcedVideo, bgRbForcedVideo,
            chbNameVideo, txtNameVideo,
            chbNumbVideo, lblNumbStartVideo, txtNumbStartVideo,
            lblNumbPadVideo, txtNumbPadVideo, lblNumbExplainVideo,
            chbLangVideo, cbLangVideo,
            chbExtraCmdVideo, txtExtraCmdVideo);

    private final TrackComponentSet AUDIO_COMPONENTS = new TrackComponentSet(
            subPnlAudio, chbEditAudio, chbEnableAudio, rbYesEnableAudio, rbNoEnableAudio, bgRbEnableAudio,
            chbDefaultAudio, rbYesDefAudio, rbNoDefAudio, bgRbDefAudio,
            chbForcedAudio, rbYesForcedAudio, rbNoForcedAudio, bgRbForcedAudio,
            chbNameAudio, txtNameAudio,
            chbNumbAudio, lblNumbStartAudio, txtNumbStartAudio,
            lblNumbPadAudio, txtNumbPadAudio, lblNumbExplainAudio,
            chbLangAudio, cbLangAudio,
            chbExtraCmdAudio, txtExtraCmdAudio);

    private final TrackComponentSet SUBTITLE_COMPONENTS = new TrackComponentSet(
            subPnlSubtitle, chbEditSubtitle, chbEnableSubtitle, rbYesEnableSubtitle, rbNoEnableSubtitle, bgRbEnableSubtitle,
            chbDefaultSubtitle, rbYesDefSubtitle, rbNoDefSubtitle, bgRbDefSubtitle,
            chbForcedSubtitle, rbYesForcedSubtitle, rbNoForcedSubtitle, bgRbForcedSubtitle,
            chbNameSubtitle, txtNameSubtitle,
            chbNumbSubtitle, lblNumbStartSubtitle, txtNumbStartSubtitle,
            lblNumbPadSubtitle, txtNumbPadSubtitle, lblNumbExplainSubtitle,
            chbLangSubtitle, cbLangSubtitle,
            chbExtraCmdSubtitle, txtExtraCmdSubtitle);

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

        modelAudioProfiles = new DefaultListModel<>();
        listAudioProfiles = new JList<>(modelAudioProfiles);

        modelVideoProfiles = new DefaultListModel<>();
        listVideoProfiles = new JList<>(modelVideoProfiles);

        modelSubtitleProfiles = new DefaultListModel<>();
        listSubtitleProfiles = new JList<>(modelSubtitleProfiles);

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

                    if (cbVideo.getItemCount() == 1) {
                        // Only one track remains: clear its options and disable editing
                        chbEditVideo[0].setSelected(false);
                        chbEnableVideo[0].setSelected(false);
                        chbDefaultVideo[0].setSelected(false);
                        chbForcedVideo[0].setSelected(false);
                        chbNameVideo[0].setSelected(false);
                        chbNumbVideo[0].setSelected(false);
                        chbLangVideo[0].setSelected(false);
                        chbExtraCmdVideo[0].setSelected(false);
                        txtNameVideo[0].setText("");
                        txtNumbStartVideo[0].setText("1");
                        txtNumbPadVideo[0].setText("1");
                        txtExtraCmdVideo[0].setText("");
                        rbYesEnableVideo[0].setSelected(true);
                        rbYesDefVideo[0].setSelected(true);
                        rbYesForcedVideo[0].setSelected(true);
                        cbLangVideo[0].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
                        toggleVideo(0);
                    } else {
                        int idx = cbVideo.getItemCount() - 1;
                        cbVideo.removeItemAt(idx);
                        lyrdPnlVideo.remove(idx);
                        nVideo--;
                        if (!videoTrackControls.isEmpty())
                            videoTrackControls.remove(videoTrackControls.size() - 1);

                        if (cbVideo.getItemCount() < MAX_STREAMS && !btnAddVideo.isEnabled()) {
                            btnAddVideo.setEnabled(true);
                        }

                        // System.gc() removed — IMP-02: JVM handles GC optimally
                    }
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

                    if (cbAudio.getItemCount() == 1) {
                        // Only one track remains: clear its options and disable editing
                        chbEditAudio[0].setSelected(false);
                        chbEnableAudio[0].setSelected(false);
                        chbDefaultAudio[0].setSelected(false);
                        chbForcedAudio[0].setSelected(false);
                        chbNameAudio[0].setSelected(false);
                        chbNumbAudio[0].setSelected(false);
                        chbLangAudio[0].setSelected(false);
                        chbExtraCmdAudio[0].setSelected(false);
                        txtNameAudio[0].setText("");
                        txtNumbStartAudio[0].setText("1");
                        txtNumbPadAudio[0].setText("1");
                        txtExtraCmdAudio[0].setText("");
                        rbYesEnableAudio[0].setSelected(true);
                        rbYesDefAudio[0].setSelected(true);
                        rbYesForcedAudio[0].setSelected(true);
                        cbLangAudio[0].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
                        toggleAudio(0);
                    } else {
                        int idx = cbAudio.getItemCount() - 1;
                        cbAudio.removeItemAt(idx);
                        lyrdPnlAudio.remove(idx);
                        nAudio--;
                        if (!audioTrackControls.isEmpty())
                            audioTrackControls.remove(audioTrackControls.size() - 1);

                        if (cbAudio.getItemCount() < MAX_STREAMS && !btnAddAudio.isEnabled()) {
                            btnAddAudio.setEnabled(true);
                        }

                        // System.gc() removed — IMP-02: JVM handles GC optimally
                    }
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

                    if (cbSubtitle.getItemCount() == 1) {
                        // Only one track remains: clear its options and disable editing
                        chbEditSubtitle[0].setSelected(false);
                        chbEnableSubtitle[0].setSelected(false);
                        chbDefaultSubtitle[0].setSelected(false);
                        chbForcedSubtitle[0].setSelected(false);
                        chbNameSubtitle[0].setSelected(false);
                        chbNumbSubtitle[0].setSelected(false);
                        chbLangSubtitle[0].setSelected(false);
                        chbExtraCmdSubtitle[0].setSelected(false);
                        txtNameSubtitle[0].setText("");
                        txtNumbStartSubtitle[0].setText("1");
                        txtNumbPadSubtitle[0].setText("1");
                        txtExtraCmdSubtitle[0].setText("");
                        rbYesEnableSubtitle[0].setSelected(true);
                        rbYesDefSubtitle[0].setSelected(true);
                        rbYesForcedSubtitle[0].setSelected(true);
                        cbLangSubtitle[0].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
                        // Disable all sub-controls by simulating toggle off
                        chbEnableSubtitle[0].setEnabled(false);
                        chbDefaultSubtitle[0].setEnabled(false);
                        chbForcedSubtitle[0].setEnabled(false);
                        chbNameSubtitle[0].setEnabled(false);
                        chbNumbSubtitle[0].setEnabled(false);
                        chbLangSubtitle[0].setEnabled(false);
                        chbExtraCmdSubtitle[0].setEnabled(false);
                        txtNameSubtitle[0].setEnabled(false);
                        txtNumbStartSubtitle[0].setEnabled(false);
                        txtNumbPadSubtitle[0].setEnabled(false);
                        txtExtraCmdSubtitle[0].setEnabled(false);
                        rbYesEnableSubtitle[0].setEnabled(false);
                        rbNoEnableSubtitle[0].setEnabled(false);
                        rbYesDefSubtitle[0].setEnabled(false);
                        rbNoDefSubtitle[0].setEnabled(false);
                        rbYesForcedSubtitle[0].setEnabled(false);
                        rbNoForcedSubtitle[0].setEnabled(false);
                        cbLangSubtitle[0].setEnabled(false);
                    } else {
                        int idx = cbSubtitle.getItemCount() - 1;
                        cbSubtitle.removeItemAt(idx);
                        lyrdPnlSubtitle.remove(idx);
                        nSubtitle--;
                        if (!subtitleTrackControls.isEmpty())
                            subtitleTrackControls.remove(subtitleTrackControls.size() - 1);

                        if (cbSubtitle.getItemCount() < MAX_STREAMS && !btnAddSubtitle.isEnabled()) {
                            btnAddSubtitle.setEnabled(true);
                        }

                        // System.gc() removed — IMP-02: JVM handles GC optimally
                    }
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
                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.attachment"));
                getChooser().setMultiSelectionEnabled(false);
                getChooser().resetChoosableFileFilters();
                getChooser().setAcceptAllFileFilterUsed(true);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    File f = getChooser().getSelectedFile();

                    if (f.exists()) {
                        try {
                            txtAttachAddFile.setText(f.getCanonicalPath());
                        } catch (IOException e1) {
                            LOGGER.error("Error resolving attachment add file path", e1);
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
                getChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
                getChooser().setDialogTitle(LanguageManager.getString("getChooser().title.attachment"));
                getChooser().setMultiSelectionEnabled(false);
                getChooser().resetChoosableFileFilters();
                getChooser().setAcceptAllFileFilterUsed(true);

                int open = getChooser().showOpenDialog(frmJMkvpropedit);

                if (open == JFileChooser.APPROVE_OPTION) {
                    File f = getChooser().getSelectedFile();

                    if (f.exists()) {
                        try {
                            txtAttachReplaceNew.setText(f.getCanonicalPath());
                        } catch (IOException e1) {
                            LOGGER.error("Error resolving attachment replacement file path", e1);
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

    private void addTrack(int index, TrackComponentSet c, JComboBox<String> combo,
            JPanel layeredPane, String panelPrefix, String trackTitleBase,
            TrackControls.TrackType trackType, List<TrackControls> controlsList) {
        c.subPanels[index] = new JPanel();
        layeredPane.add(c.subPanels[index], panelPrefix + "[" + index + "]");
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[] { 140, 0, 0 };
        gbl.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        c.subPanels[index].setLayout(gbl);

        c.chbEdit[index] = new JCheckBox(LanguageManager.getString("track.edit"));
        GridBagConstraints gbcEdit = new GridBagConstraints();
        gbcEdit.insets = new Insets(0, 0, 10, 5);
        gbcEdit.anchor = GridBagConstraints.WEST;
        gbcEdit.gridx = 0;
        gbcEdit.gridy = 0;
        c.subPanels[index].add(c.chbEdit[index], gbcEdit);

        c.chbEnable[index] = new JCheckBox(LanguageManager.getString("track.enable"));
        c.chbEnable[index].setEnabled(false);
        GridBagConstraints gbcEnable = new GridBagConstraints();
        gbcEnable.insets = new Insets(0, 0, 5, 5);
        gbcEnable.anchor = GridBagConstraints.WEST;
        gbcEnable.gridx = 0;
        gbcEnable.gridy = 1;
        c.subPanels[index].add(c.chbEnable[index], gbcEnable);

        JPanel pnlEnable = new JPanel();
        FlowLayout flEnable = (FlowLayout) pnlEnable.getLayout();
        flEnable.setAlignment(FlowLayout.LEFT);
        flEnable.setVgap(0);
        GridBagConstraints gbcPnlEnable = new GridBagConstraints();
        gbcPnlEnable.insets = new Insets(0, 0, 5, 0);
        gbcPnlEnable.fill = GridBagConstraints.HORIZONTAL;
        gbcPnlEnable.gridx = 1;
        gbcPnlEnable.gridy = 1;
        c.subPanels[index].add(pnlEnable, gbcPnlEnable);

        c.rbYesEnable[index] = new JRadioButton(LanguageManager.getString("common.yes"));
        c.rbYesEnable[index].setEnabled(false);
        c.rbYesEnable[index].setSelected(true);
        pnlEnable.add(c.rbYesEnable[index]);

        c.rbNoEnable[index] = new JRadioButton(LanguageManager.getString("common.no"));
        c.rbNoEnable[index].setEnabled(false);
        pnlEnable.add(c.rbNoEnable[index]);

        c.bgRbEnable[index] = new ButtonGroup();
        c.bgRbEnable[index].add(c.rbYesEnable[index]);
        c.bgRbEnable[index].add(c.rbNoEnable[index]);

        c.chbDefault[index] = new JCheckBox(LanguageManager.getString("track.default"));
        c.chbDefault[index].setEnabled(false);
        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(0, 0, 5, 5);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.gridx = 0;
        gbcDefault.gridy = 2;
        c.subPanels[index].add(c.chbDefault[index], gbcDefault);

        JPanel pnlDef = new JPanel();
        FlowLayout flDef = (FlowLayout) pnlDef.getLayout();
        flDef.setAlignment(FlowLayout.LEFT);
        flDef.setVgap(0);
        GridBagConstraints gbcPnlDef = new GridBagConstraints();
        gbcPnlDef.insets = new Insets(0, 0, 5, 0);
        gbcPnlDef.fill = GridBagConstraints.HORIZONTAL;
        gbcPnlDef.gridx = 1;
        gbcPnlDef.gridy = 2;
        c.subPanels[index].add(pnlDef, gbcPnlDef);

        c.rbYesDef[index] = new JRadioButton(LanguageManager.getString("common.yes"));
        c.rbYesDef[index].setEnabled(false);
        c.rbYesDef[index].setSelected(true);
        pnlDef.add(c.rbYesDef[index]);

        c.rbNoDef[index] = new JRadioButton(LanguageManager.getString("common.no"));
        c.rbNoDef[index].setEnabled(false);
        pnlDef.add(c.rbNoDef[index]);

        c.bgRbDef[index] = new ButtonGroup();
        c.bgRbDef[index].add(c.rbYesDef[index]);
        c.bgRbDef[index].add(c.rbNoDef[index]);

        c.chbForced[index] = new JCheckBox(LanguageManager.getString("track.forced"));
        c.chbForced[index].setEnabled(false);
        GridBagConstraints gbcForced = new GridBagConstraints();
        gbcForced.insets = new Insets(0, 0, 5, 5);
        gbcForced.anchor = GridBagConstraints.WEST;
        gbcForced.gridx = 0;
        gbcForced.gridy = 3;
        c.subPanels[index].add(c.chbForced[index], gbcForced);

        JPanel pnlForced = new JPanel();
        FlowLayout flForced = (FlowLayout) pnlForced.getLayout();
        flForced.setAlignment(FlowLayout.LEFT);
        flForced.setVgap(0);
        GridBagConstraints gbcPnlForced = new GridBagConstraints();
        gbcPnlForced.insets = new Insets(0, 0, 5, 0);
        gbcPnlForced.fill = GridBagConstraints.HORIZONTAL;
        gbcPnlForced.gridx = 1;
        gbcPnlForced.gridy = 3;
        c.subPanels[index].add(pnlForced, gbcPnlForced);

        c.rbYesForced[index] = new JRadioButton(LanguageManager.getString("common.yes"));
        c.rbYesForced[index].setEnabled(false);
        c.rbYesForced[index].setSelected(true);
        pnlForced.add(c.rbYesForced[index]);

        c.rbNoForced[index] = new JRadioButton(LanguageManager.getString("common.no"));
        c.rbNoForced[index].setEnabled(false);
        pnlForced.add(c.rbNoForced[index]);

        c.bgRbForced[index] = new ButtonGroup();
        c.bgRbForced[index].add(c.rbYesForced[index]);
        c.bgRbForced[index].add(c.rbNoForced[index]);

        c.chbName[index] = new JCheckBox(LanguageManager.getString("track.name"));
        c.chbName[index].setEnabled(false);
        GridBagConstraints gbcName = new GridBagConstraints();
        gbcName.insets = new Insets(0, 0, 5, 5);
        gbcName.anchor = GridBagConstraints.WEST;
        gbcName.gridx = 0;
        gbcName.gridy = 4;
        c.subPanels[index].add(c.chbName[index], gbcName);

        c.txtName[index] = new JTextField();
        c.txtName[index].setEnabled(false);
        c.txtName[index].setColumns(10);
        GridBagConstraints gbcTxtName = new GridBagConstraints();
        gbcTxtName.insets = new Insets(0, 0, 5, 0);
        gbcTxtName.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtName.gridx = 1;
        gbcTxtName.gridy = 4;
        c.subPanels[index].add(c.txtName[index], gbcTxtName);

        c.chbNumb[index] = new JCheckBox(LanguageManager.getString("track.numbering"));
        c.chbNumb[index].setEnabled(false);
        GridBagConstraints gbcNumb = new GridBagConstraints();
        gbcNumb.insets = new Insets(0, 0, 5, 5);
        gbcNumb.anchor = GridBagConstraints.WEST;
        gbcNumb.gridx = 0;
        gbcNumb.gridy = 5;
        c.subPanels[index].add(c.chbNumb[index], gbcNumb);

        JPanel pnlNumb = new JPanel();
        FlowLayout flNumb = (FlowLayout) pnlNumb.getLayout();
        flNumb.setAlignment(FlowLayout.LEFT);
        flNumb.setVgap(0);
        GridBagConstraints gbcPnlNumb = new GridBagConstraints();
        gbcPnlNumb.insets = new Insets(0, 0, 5, 0);
        gbcPnlNumb.fill = GridBagConstraints.HORIZONTAL;
        gbcPnlNumb.gridx = 1;
        gbcPnlNumb.gridy = 5;
        c.subPanels[index].add(pnlNumb, gbcPnlNumb);

        c.lblNumbStart[index] = new JLabel(LanguageManager.getString("track.numbering.start"));
        c.lblNumbStart[index].setEnabled(false);
        pnlNumb.add(c.lblNumbStart[index]);

        c.txtNumbStart[index] = new JTextField();
        c.txtNumbStart[index].setText("1");
        c.txtNumbStart[index].setEnabled(false);
        c.txtNumbStart[index].setColumns(3);
        pnlNumb.add(c.txtNumbStart[index]);

        c.lblNumbPad[index] = new JLabel(LanguageManager.getString("track.numbering.padding"));
        c.lblNumbPad[index].setEnabled(false);
        pnlNumb.add(c.lblNumbPad[index]);

        c.txtNumbPad[index] = new JTextField();
        c.txtNumbPad[index].setText("1");
        c.txtNumbPad[index].setEnabled(false);
        c.txtNumbPad[index].setColumns(3);
        pnlNumb.add(c.txtNumbPad[index]);

        c.lblNumbExplain[index] = new JLabel(
                "<html>" + LanguageManager.getString("track.numbering.explain") + "</html>");
        c.lblNumbExplain[index].setEnabled(false);
        GridBagConstraints gbcExplain = new GridBagConstraints();
        gbcExplain.insets = new Insets(0, 0, 5, 0);
        gbcExplain.fill = GridBagConstraints.HORIZONTAL;
        gbcExplain.gridx = 1;
        gbcExplain.gridy = 6;
        c.subPanels[index].add(c.lblNumbExplain[index], gbcExplain);

        c.chbLang[index] = new JCheckBox(LanguageManager.getString("track.language"));
        c.chbLang[index].setEnabled(false);
        GridBagConstraints gbcLang = new GridBagConstraints();
        gbcLang.insets = new Insets(0, 0, 5, 5);
        gbcLang.anchor = GridBagConstraints.WEST;
        gbcLang.gridx = 0;
        gbcLang.gridy = 7;
        c.subPanels[index].add(c.chbLang[index], gbcLang);

        c.cbLang[index] = new JComboBox<String>();
        c.cbLang[index]
                .setModel(new DefaultComboBoxModel<String>(mkvStrings.getLangNameList().toArray(new String[0])));
        c.cbLang[index].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
        c.cbLang[index].setEnabled(false);
        GridBagConstraints gbcCbLang = new GridBagConstraints();
        gbcCbLang.insets = new Insets(0, 0, 5, 0);
        gbcCbLang.fill = GridBagConstraints.HORIZONTAL;
        gbcCbLang.gridx = 1;
        gbcCbLang.gridy = 7;
        c.subPanels[index].add(c.cbLang[index], gbcCbLang);

        c.chbExtraCmd[index] = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
        c.chbExtraCmd[index].setEnabled(false);
        GridBagConstraints gbcExtra = new GridBagConstraints();
        gbcExtra.insets = new Insets(0, 0, 0, 5);
        gbcExtra.anchor = GridBagConstraints.WEST;
        gbcExtra.gridx = 0;
        gbcExtra.gridy = 8;
        c.subPanels[index].add(c.chbExtraCmd[index], gbcExtra);

        c.txtExtraCmd[index] = new JTextField();
        c.txtExtraCmd[index].setEnabled(false);
        c.txtExtraCmd[index].setColumns(10);
        GridBagConstraints gbcTxtExtra = new GridBagConstraints();
        gbcTxtExtra.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtExtra.gridx = 1;
        gbcTxtExtra.gridy = 8;
        c.subPanels[index].add(c.txtExtraCmd[index], gbcTxtExtra);

        c.chbEdit[index].addActionListener(e -> toggleTrack(combo.getSelectedIndex(), c));

        c.chbEnable[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.rbNoEnable[selected].isEnabled();
            c.rbNoEnable[selected].setEnabled(!state);
            c.rbYesEnable[selected].setEnabled(!state);
        });

        c.chbDefault[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.rbNoDef[selected].isEnabled();
            c.rbNoDef[selected].setEnabled(!state);
            c.rbYesDef[selected].setEnabled(!state);
        });

        c.chbForced[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.rbNoForced[selected].isEnabled();
            c.rbNoForced[selected].setEnabled(!state);
            c.rbYesForced[selected].setEnabled(!state);
        });

        c.chbName[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.chbNumb[selected].isEnabled();
            c.chbNumb[selected].setEnabled(!state);
            c.txtName[selected].setEnabled(!state);

            if (c.chbNumb[selected].isSelected()) {
                c.lblNumbStart[selected].setEnabled(!state);
                c.txtNumbStart[selected].setEnabled(!state);
                c.lblNumbPad[selected].setEnabled(!state);
                c.txtNumbPad[selected].setEnabled(!state);
                c.lblNumbExplain[selected].setEnabled(!state);
            }
        });

        c.chbNumb[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.txtNumbStart[selected].isEnabled();
            c.lblNumbStart[selected].setEnabled(!state);
            c.txtNumbStart[selected].setEnabled(!state);
            c.lblNumbPad[selected].setEnabled(!state);
            c.txtNumbPad[selected].setEnabled(!state);
            c.lblNumbExplain[selected].setEnabled(!state);
        });

        c.txtNumbStart[index].addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                int selected = combo.getSelectedIndex();
                try {
                    if (Integer.parseInt(c.txtNumbStart[selected].getText()) < 0) {
                        c.txtNumbStart[selected].setText("1");
                    }
                } catch (NumberFormatException e1) {
                    c.txtNumbStart[selected].setText("1");
                }
            }
        });

        c.txtNumbPad[index].addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                int selected = combo.getSelectedIndex();
                try {
                    if (Integer.parseInt(c.txtNumbPad[selected].getText()) < 0) {
                        c.txtNumbPad[selected].setText("1");
                    }
                } catch (NumberFormatException e1) {
                    c.txtNumbPad[selected].setText("1");
                }
            }
        });

        c.chbLang[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.cbLang[selected].isEnabled();
            c.cbLang[selected].setEnabled(!state);
        });

        c.chbExtraCmd[index].addActionListener(e -> {
            int selected = combo.getSelectedIndex();
            boolean state = c.txtExtraCmd[selected].isEnabled();
            c.txtExtraCmd[selected].setEnabled(!state);
        });

        combo.addItem(trackTitleBase + (index + 1));

        controlsList.add(new TrackControls(
                trackType, c.subPanels[index],
                c.chbEdit[index], c.chbEnable[index],
                c.rbYesEnable[index], c.rbNoEnable[index], c.bgRbEnable[index],
                c.chbDefault[index], c.rbYesDef[index], c.rbNoDef[index], c.bgRbDef[index],
                c.chbForced[index], c.rbYesForced[index], c.rbNoForced[index], c.bgRbForced[index],
                c.chbName[index], c.txtName[index],
                c.chbNumb[index], c.lblNumbStart[index], c.txtNumbStart[index],
                c.lblNumbPad[index], c.txtNumbPad[index], c.lblNumbExplain[index],
                c.chbLang[index], c.cbLang[index],
                c.chbExtraCmd[index], c.txtExtraCmd[index]));
    }

    private void addVideoTrack() {
        if (nVideo < MAX_STREAMS) {
            addTrack(nVideo, VIDEO_COMPONENTS, cbVideo, lyrdPnlVideo, "subPnlVideo",
                    LanguageManager.getString("track.video.title"),
                    TrackControls.TrackType.VIDEO, videoTrackControls);
            nVideo++;
        }
    }

    private void addAudioTrack() {
        if (nAudio < MAX_STREAMS) {
            addTrack(nAudio, AUDIO_COMPONENTS, cbAudio, lyrdPnlAudio, "subPnlAudio",
                    LanguageManager.getString("track.audio.title"),
                    TrackControls.TrackType.AUDIO, audioTrackControls);

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

            nAudio++;
        }
    }

    private void addSubtitleTrack() {
        if (nSubtitle < MAX_STREAMS) {
            addTrack(nSubtitle, SUBTITLE_COMPONENTS, cbSubtitle, lyrdPnlSubtitle, "subPnlSubtitle",
                    LanguageManager.getString("track.subtitle.title"),
                    TrackControls.TrackType.SUBTITLE, subtitleTrackControls);
            nSubtitle++;
        }
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
        StringBuilder sbAdd = new StringBuilder();
        StringBuilder sbAddOpt = new StringBuilder();

        for (int i = 0; i < modelAttachmentsAdd.getRowCount(); i++) {
            String file = modelAttachmentsAdd.getValueAt(i, 0).toString();
            String name = modelAttachmentsAdd.getValueAt(i, 1).toString();
            String desc = modelAttachmentsAdd.getValueAt(i, 2).toString();
            String mime = modelAttachmentsAdd.getValueAt(i, 3).toString();

            if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
                if (!name.isEmpty()) {
                    sbAdd.append(" --attachment-name \"").append(name).append("\"");
                    sbAddOpt.append(" --attachment-name \"").append(Utils.escapeName(name)).append("\"");
                }

                if (!desc.isEmpty()) {
                    sbAdd.append(" --attachment-description \"").append(desc).append("\"");
                    sbAddOpt.append(" --attachment-description \"").append(Utils.escapeName(desc)).append("\"");
                }

                if (!mime.isEmpty()) {
                    sbAdd.append(" --attachment-mime-type \"").append(mime).append("\"");
                    sbAddOpt.append(" --attachment-mime-type \"").append(Utils.escapeName(mime)).append("\"");
                }
            }

            sbAdd.append(" --add-attachment \"").append(file).append("\"");
            sbAddOpt.append(" --add-attachment \"").append(Utils.escapeName(file)).append("\"");
        }

        cmdLineAttachmentsAdd = sbAdd.toString();
        cmdLineAttachmentsAddOpt = sbAddOpt.toString();
    }

    private void setCmdLineAttachmentsReplace() {
        StringBuilder sbReplace = new StringBuilder();
        StringBuilder sbReplaceOpt = new StringBuilder();

        for (int i = 0; i < modelAttachmentsReplace.getRowCount(); i++) {
            String type = modelAttachmentsReplace.getValueAt(i, 0).toString();
            String orig = modelAttachmentsReplace.getValueAt(i, 1).toString();
            String replace = modelAttachmentsReplace.getValueAt(i, 2).toString();
            String name = modelAttachmentsReplace.getValueAt(i, 3).toString();
            String desc = modelAttachmentsReplace.getValueAt(i, 4).toString();
            String mime = modelAttachmentsReplace.getValueAt(i, 5).toString();

            if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
                if (!name.isEmpty()) {
                    sbReplace.append(" --attachment-name \"").append(name).append("\"");
                    sbReplaceOpt.append(" --attachment-name \"").append(Utils.escapeName(name)).append("\"");
                }

                if (!desc.isEmpty()) {
                    sbReplace.append(" --attachment-description \"").append(desc).append("\"");
                    sbReplaceOpt.append(" --attachment-description \"").append(Utils.escapeName(desc)).append("\"");
                }

                if (!mime.isEmpty()) {
                    sbReplace.append(" --attachment-mime-type \"").append(mime).append("\"");
                    sbReplaceOpt.append(" --attachment-mime-type \"").append(Utils.escapeName(mime)).append("\"");
                }
            }

            if (type.equals(rbAttachReplaceName.getText())) {
                sbReplace.append(" --replace-attachment \"name:").append(orig).append(":").append(replace).append("\"");
                sbReplaceOpt.append(" --replace-attachment \"name:").append(Utils.escapeName(orig)).append(":")
                        .append(Utils.escapeName(replace)).append("\"");
            } else if (type.equals(rbAttachReplaceID.getText())) {
                sbReplace.append(" --replace-attachment \"").append(orig).append(":").append(replace).append("\"");
                sbReplaceOpt.append(" --replace-attachment \"").append(orig).append(":")
                        .append(Utils.escapeName(replace)).append("\"");
            } else {
                sbReplace.append(" --replace-attachment \"mime-type:").append(orig).append(":").append(replace)
                        .append("\"");
                sbReplaceOpt.append(" --replace-attachment \"mime-type:").append(Utils.escapeName(orig)).append(":")
                        .append(Utils.escapeName(replace)).append("\"");
            }
        }

        cmdLineAttachmentsReplace = sbReplace.toString();
        cmdLineAttachmentsReplaceOpt = sbReplaceOpt.toString();
    }

    private void setCmdLineAttachmentsDelete() {
        StringBuilder sbDelete = new StringBuilder();
        StringBuilder sbDeleteOpt = new StringBuilder();

        for (int i = 0; i < modelAttachmentsDelete.getRowCount(); i++) {
            String type = modelAttachmentsDelete.getValueAt(i, 0).toString();
            String value = modelAttachmentsDelete.getValueAt(i, 1).toString();

            if (type.equals(rbAttachDeleteName.getText())) {
                sbDelete.append(" --delete-attachment \"name:").append(value).append("\"");
                sbDeleteOpt.append(" --delete-attachment \"name:").append(Utils.escapeName(value)).append("\"");
            } else if (type.equals(rbAttachDeleteID.getText())) {
                sbDelete.append(" --delete-attachment \"").append(value).append("\"");
                sbDeleteOpt.append(" --delete-attachment \"").append(value).append("\"");
            } else {
                sbDelete.append(" --delete-attachment \"mime-type:").append(value).append("\"");
                sbDeleteOpt.append(" --delete-attachment \"mime-type:").append(Utils.escapeName(value)).append("\"");
            }
        }

        cmdLineAttachmentsDelete = sbDelete.toString();
        cmdLineAttachmentsDeleteOpt = sbDeleteOpt.toString();
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
            for (int j = 0; j < nVideo; j++) {
                if (chbExtraCmdVideo[j].isSelected()) {
                    InputValidator.validateSafeExtraCommand(txtExtraCmdVideo[j].getText());
                }
            }
            for (int j = 0; j < nAudio; j++) {
                if (chbExtraCmdAudio[j].isSelected()) {
                    InputValidator.validateSafeExtraCommand(txtExtraCmdAudio[j].getText());
                }
            }
            for (int j = 0; j < nSubtitle; j++) {
                if (chbExtraCmdSubtitle[j].isSelected()) {
                    InputValidator.validateSafeExtraCommand(txtExtraCmdSubtitle[j].getText());
                }
            }
        } catch (MkvPropeditException e) {
            JOptionPane.showMessageDialog(frmJMkvpropedit, e.getMessage(),
                    LanguageManager.getString("error.title.security"), JOptionPane.ERROR_MESSAGE);
            return;
        }

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
        boolean alreadyEnabled = chbDefaultSubtitle[trackIdx].isEnabled();
        if (!alreadyEnabled) {
            toggleSubtitle(trackIdx);
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

    private void toggleTrack(int trackIdx, TrackComponentSet c) {
        if (trackIdx < 0 || trackIdx >= MAX_STREAMS)
            return;

        boolean isEdit = c.chbEdit[trackIdx].isSelected();

        c.chbEnable[trackIdx].setEnabled(isEdit);
        c.chbDefault[trackIdx].setEnabled(isEdit);
        c.chbForced[trackIdx].setEnabled(isEdit);
        c.chbName[trackIdx].setEnabled(isEdit);
        c.chbLang[trackIdx].setEnabled(isEdit);
        c.chbExtraCmd[trackIdx].setEnabled(isEdit);

        if (c.txtName[trackIdx].isEnabled() || c.chbName[trackIdx].isSelected()) {
            c.txtName[trackIdx].setEnabled(isEdit && c.chbName[trackIdx].isSelected());
            c.chbNumb[trackIdx].setEnabled(isEdit && c.chbName[trackIdx].isSelected());

            if (c.chbNumb[trackIdx].isSelected()) {
                boolean isNumb = isEdit && c.chbName[trackIdx].isSelected();
                c.lblNumbStart[trackIdx].setEnabled(isNumb);
                c.txtNumbStart[trackIdx].setEnabled(isNumb);
                c.lblNumbPad[trackIdx].setEnabled(isNumb);
                c.txtNumbPad[trackIdx].setEnabled(isNumb);
                c.lblNumbExplain[trackIdx].setEnabled(isNumb);
            }
        }

        if (c.rbNoEnable[trackIdx].isEnabled() || c.chbEnable[trackIdx].isSelected()) {
            boolean isEnable = isEdit && c.chbEnable[trackIdx].isSelected();
            c.rbNoEnable[trackIdx].setEnabled(isEnable);
            c.rbYesEnable[trackIdx].setEnabled(isEnable);
        }

        if (c.rbNoDef[trackIdx].isEnabled() || c.chbDefault[trackIdx].isSelected()) {
            boolean isDef = isEdit && c.chbDefault[trackIdx].isSelected();
            c.rbNoDef[trackIdx].setEnabled(isDef);
            c.rbYesDef[trackIdx].setEnabled(isDef);
        }

        if (c.rbNoForced[trackIdx].isEnabled() || c.chbForced[trackIdx].isSelected()) {
            boolean isForced = isEdit && c.chbForced[trackIdx].isSelected();
            c.rbNoForced[trackIdx].setEnabled(isForced);
            c.rbYesForced[trackIdx].setEnabled(isForced);
        }

        if (c.cbLang[trackIdx].isEnabled() || c.chbLang[trackIdx].isSelected()) {
            c.cbLang[trackIdx].setEnabled(isEdit && c.chbLang[trackIdx].isSelected());
        }

        if (c.txtExtraCmd[trackIdx].isEnabled() || c.chbExtraCmd[trackIdx].isSelected()) {
            boolean isExtra = isEdit && c.chbExtraCmd[trackIdx].isSelected();
            c.txtExtraCmd[trackIdx].setEnabled(isExtra);
        }
    }

    private void toggleAudio(int trackIdx) {
        toggleTrack(trackIdx, AUDIO_COMPONENTS);
    }

    private void toggleVideo(int trackIdx) {
        toggleTrack(trackIdx, VIDEO_COMPONENTS);
    }

    private void toggleSubtitle(int trackIdx) {
        toggleTrack(trackIdx, SUBTITLE_COMPONENTS);
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
                    int response = JOptionPane.showConfirmDialog(frmJMkvpropedit,
                            LanguageManager.getString("profile.delete.confirm"),
                            LanguageManager.getString("profile.delete.title"),
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        profileManager.removeProfile(type, idx);
                        model.remove(idx);
                    }
                }
            }
        });
        pnlControls.add(btnRemove);

        return pnl;
    }

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
