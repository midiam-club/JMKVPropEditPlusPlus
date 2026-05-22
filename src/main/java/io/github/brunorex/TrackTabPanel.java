package io.github.brunorex;

import io.github.brunorex.profiles.ProfileManager;
import io.github.brunorex.profiles.ProfileManager.ProfileType;
import io.github.brunorex.profiles.TrackProfile;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Encapsulates a complete track-type tab (Video, Audio, or Subtitle).
 * Replaces the triplicated video/audio/subtitle tab logic in JMkvpropedit.
 *
 * Each instance manages:
 * - A JComboBox track selector
 * - Add / Remove buttons
 * - A CardLayout pane containing one panel per track
 * - A profile sidebar with drag-and-drop reordering
 * - Command-line generation for all tracks of this type
 */
public class TrackTabPanel extends JPanel {

    private static final int MAX_STREAMS = 200;

    private final JFrame parentFrame;
    private final String trackTitleBase;
    private final String trackPrefix;
    private final ProfileType profileType;
    private final ProfileManager profileManager;
    private final MkvStrings mkvStrings;

    private final JComboBox<String> trackSelector;
    private final JButton btnAdd;
    private final JButton btnRemove;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final JPanel profilePanel;

    private final DefaultListModel<TrackProfile> profileModel;
    private final JList<TrackProfile> profileList;

    private final List<TrackControls> tracks = new ArrayList<>();

    /**
     * Creates a TrackTabPanel.
     *
     * @param parentFrame      The parent JFrame for dialogs
     * @param trackTitleBase   Localised base name, e.g. "Video Track "
     * @param trackPrefix      mkvpropedit prefix: "v", "a", or "s"
     * @param profileType      VIDEO, AUDIO, or SUBTITLE
     * @param profileManager   The shared profile manager
     */
    public TrackTabPanel(JFrame parentFrame,
                         String trackTitleBase,
                         String trackPrefix,
                         ProfileType profileType,
                         ProfileManager profileManager) {
        this.parentFrame = parentFrame;
        this.trackTitleBase = trackTitleBase;
        this.trackPrefix = trackPrefix;
        this.profileType = profileType;
        this.profileManager = profileManager;
        this.mkvStrings = new MkvStrings();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[] { 500, 200, 0 };
        gbl.rowHeights = new int[] { 30, 283, 0 };
        gbl.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
        gbl.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        setLayout(gbl);

        // --- Top controls: selector + add/remove ---
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        GridBagConstraints gbcControls = new GridBagConstraints();
        gbcControls.insets = new Insets(0, 0, 5, 0);
        gbcControls.fill = GridBagConstraints.BOTH;
        gbcControls.gridx = 0;
        gbcControls.gridy = 0;
        add(pnlControls, gbcControls);

        trackSelector = new JComboBox<>();
        trackSelector.setPreferredSize(new Dimension(150, trackSelector.getPreferredSize().height));
        pnlControls.add(trackSelector);

        btnAdd = createIconButton("/res/list-add.png");
        btnAdd.setMargin(new Insets(0, 5, 0, 5));
        pnlControls.add(btnAdd);

        btnRemove = createIconButton("/res/list-remove.png");
        btnRemove.setEnabled(false);
        pnlControls.add(btnRemove);

        // --- Card layout panel for tracks ---
        cardPanel = new JPanel();
        cardLayout = new CardLayout(0, 0);
        cardPanel.setLayout(cardLayout);
        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.fill = GridBagConstraints.BOTH;
        gbcCard.gridx = 0;
        gbcCard.gridy = 1;
        add(cardPanel, gbcCard);

        // --- Profile sidebar ---
        profileModel = new DefaultListModel<>();
        profileList = new JList<>(profileModel);
        profilePanel = createProfilePanel();
        GridBagConstraints gbcProfile = new GridBagConstraints();
        gbcProfile.gridheight = 2;
        gbcProfile.fill = GridBagConstraints.BOTH;
        gbcProfile.weightx = 0.0;
        gbcProfile.weighty = 1.0;
        gbcProfile.gridx = 1;
        gbcProfile.gridy = 0;
        gbcProfile.insets = new Insets(0, 5, 0, 0);
        add(profilePanel, gbcProfile);

        // --- Event wiring ---
        trackSelector.addActionListener(e -> {
            int idx = trackSelector.getSelectedIndex();
            if (idx >= 0) {
                cardLayout.show(cardPanel, cardName(idx));
            }
        });

        btnAdd.addActionListener(e -> {
            addTrack();
            trackSelector.setSelectedIndex(trackSelector.getItemCount() - 1);
            if (trackSelector.getItemCount() == MAX_STREAMS) {
                btnAdd.setEnabled(false);
            }
            if (!btnRemove.isEnabled()) {
                btnRemove.setEnabled(true);
            }
        });

        btnRemove.addActionListener(e -> {
            if (trackSelector.getItemCount() > 0) {
                int response = JOptionPane.showConfirmDialog(parentFrame,
                        LanguageManager.getString("delete.track.confirm"),
                        LanguageManager.getString("delete.track.title"),
                        JOptionPane.YES_NO_OPTION);

                if (response != JOptionPane.YES_OPTION) {
                    return;
                }

                if (trackSelector.getItemCount() == 1) {
                    // Clear the last track instead of removing it
                    tracks.get(0).clearAndDisable();
                } else {
                    int idx = tracks.size() - 1;
                    trackSelector.removeItemAt(idx);
                    cardPanel.remove(idx);
                    tracks.remove(idx);

                    if (trackSelector.getItemCount() < MAX_STREAMS && !btnAdd.isEnabled()) {
                        btnAdd.setEnabled(true);
                    }
                }
            }
        });

        // Audio-specific: drag-and-drop profile application
        if (profileType == ProfileType.AUDIO) {
            enableAudioProfileDnD();
        }
    }

    private JButton createIconButton(String iconPath) {
        JButton btn = new JButton("");
        btn.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private String cardName(int index) {
        return trackPrefix + "_track_" + index;
    }

    /**
     * Adds a new track to this tab.
     */
    public void addTrack() {
        if (tracks.size() >= MAX_STREAMS) {
            return;
        }

        int index = tracks.size();
        JPanel panel = new JPanel();
        cardPanel.add(panel, cardName(index));

        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[] { 140, 0, 0 };
        gbl.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        panel.setLayout(gbl);

        JCheckBox chbEdit = new JCheckBox(LanguageManager.getString("track.edit"));
        JCheckBox chbEnable = new JCheckBox(LanguageManager.getString("track.enable"));
        JRadioButton rbYesEnable = new JRadioButton(LanguageManager.getString("common.yes"));
        JRadioButton rbNoEnable = new JRadioButton(LanguageManager.getString("common.no"));
        ButtonGroup bgRbEnable = new ButtonGroup();
        JCheckBox chbDefault = new JCheckBox(LanguageManager.getString("track.default"));
        JRadioButton rbYesDef = new JRadioButton(LanguageManager.getString("common.yes"));
        JRadioButton rbNoDef = new JRadioButton(LanguageManager.getString("common.no"));
        ButtonGroup bgRbDef = new ButtonGroup();
        JCheckBox chbForced = new JCheckBox(LanguageManager.getString("track.forced"));
        JRadioButton rbYesForced = new JRadioButton(LanguageManager.getString("common.yes"));
        JRadioButton rbNoForced = new JRadioButton(LanguageManager.getString("common.no"));
        ButtonGroup bgRbForced = new ButtonGroup();
        JCheckBox chbName = new JCheckBox(LanguageManager.getString("track.name"));
        JTextField txtName = new JTextField();
        JCheckBox chbNumb = new JCheckBox(LanguageManager.getString("track.numbering"));
        JLabel lblNumbStart = new JLabel(LanguageManager.getString("track.numbering.start"));
        JTextField txtNumbStart = new JTextField("1");
        JLabel lblNumbPad = new JLabel(LanguageManager.getString("track.numbering.padding"));
        JTextField txtNumbPad = new JTextField("1");
        JLabel lblNumbExplain = new JLabel(
                "<html>" + LanguageManager.getString("track.numbering.explain") + "</html>");
        JCheckBox chbLang = new JCheckBox(LanguageManager.getString("track.language"));
        JComboBox<String> cbLang = new JComboBox<>(
                new DefaultComboBoxModel<>(mkvStrings.getLangNameList().toArray(new String[0])));
        cbLang.setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
        JCheckBox chbExtraCmd = new JCheckBox(LanguageManager.getString("track.extra.cmd"));
        JTextField txtExtraCmd = new JTextField();

        // --- Layout ---
        addGridBag(panel, chbEdit, 0, 0, new Insets(0, 0, 10, 5), GridBagConstraints.WEST);

        chbEnable.setEnabled(false);
        addGridBag(panel, chbEnable, 0, 1, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        JPanel pnlEnable = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rbYesEnable.setEnabled(false);
        rbYesEnable.setSelected(true);
        rbNoEnable.setEnabled(false);
        pnlEnable.add(rbYesEnable);
        pnlEnable.add(rbNoEnable);
        bgRbEnable.add(rbYesEnable);
        bgRbEnable.add(rbNoEnable);
        addGridBag(panel, pnlEnable, 1, 1, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbDefault.setEnabled(false);
        addGridBag(panel, chbDefault, 0, 2, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        JPanel pnlDef = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rbYesDef.setEnabled(false);
        rbYesDef.setSelected(true);
        rbNoDef.setEnabled(false);
        pnlDef.add(rbYesDef);
        pnlDef.add(rbNoDef);
        bgRbDef.add(rbYesDef);
        bgRbDef.add(rbNoDef);
        addGridBag(panel, pnlDef, 1, 2, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbForced.setEnabled(false);
        addGridBag(panel, chbForced, 0, 3, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        JPanel pnlForced = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rbYesForced.setEnabled(false);
        rbYesForced.setSelected(true);
        rbNoForced.setEnabled(false);
        pnlForced.add(rbYesForced);
        pnlForced.add(rbNoForced);
        bgRbForced.add(rbYesForced);
        bgRbForced.add(rbNoForced);
        addGridBag(panel, pnlForced, 1, 3, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbName.setEnabled(false);
        addGridBag(panel, chbName, 0, 4, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        txtName.setEnabled(false);
        txtName.setColumns(10);
        addGridBag(panel, txtName, 1, 4, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbNumb.setEnabled(false);
        addGridBag(panel, chbNumb, 0, 5, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        JPanel pnlNumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        lblNumbStart.setEnabled(false);
        txtNumbStart.setEnabled(false);
        txtNumbStart.setColumns(3);
        lblNumbPad.setEnabled(false);
        txtNumbPad.setEnabled(false);
        txtNumbPad.setColumns(3);
        pnlNumb.add(lblNumbStart);
        pnlNumb.add(txtNumbStart);
        pnlNumb.add(lblNumbPad);
        pnlNumb.add(txtNumbPad);
        addGridBag(panel, pnlNumb, 1, 5, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        lblNumbExplain.setEnabled(false);
        addGridBag(panel, lblNumbExplain, 1, 6, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbLang.setEnabled(false);
        addGridBag(panel, chbLang, 0, 7, new Insets(0, 0, 5, 5), GridBagConstraints.WEST);

        cbLang.setEnabled(false);
        addGridBag(panel, cbLang, 1, 7, new Insets(0, 0, 5, 0), GridBagConstraints.HORIZONTAL);

        chbExtraCmd.setEnabled(false);
        addGridBag(panel, chbExtraCmd, 0, 8, new Insets(0, 0, 0, 5), GridBagConstraints.WEST);

        txtExtraCmd.setEnabled(false);
        txtExtraCmd.setColumns(10);
        addGridBag(panel, txtExtraCmd, 1, 8, new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL);

        // --- Listeners ---
        chbEdit.addActionListener(e -> setTrackEditEnabled(index, chbEdit.isSelected()));

        chbEnable.addActionListener(e -> {
            boolean on = chbEnable.isSelected();
            rbYesEnable.setEnabled(on);
            rbNoEnable.setEnabled(on);
        });

        chbDefault.addActionListener(e -> {
            boolean on = chbDefault.isSelected();
            rbYesDef.setEnabled(on);
            rbNoDef.setEnabled(on);
        });

        chbForced.addActionListener(e -> {
            boolean on = chbForced.isSelected();
            rbYesForced.setEnabled(on);
            rbNoForced.setEnabled(on);
        });

        chbName.addActionListener(e -> {
            boolean on = chbName.isSelected();
            txtName.setEnabled(on);
            chbNumb.setEnabled(on);
            if (chbNumb.isSelected()) {
                boolean numOn = on;
                lblNumbStart.setEnabled(numOn);
                txtNumbStart.setEnabled(numOn);
                lblNumbPad.setEnabled(numOn);
                txtNumbPad.setEnabled(numOn);
                lblNumbExplain.setEnabled(numOn);
            }
        });

        chbNumb.addActionListener(e -> {
            boolean on = chbNumb.isSelected();
            lblNumbStart.setEnabled(on);
            txtNumbStart.setEnabled(on);
            lblNumbPad.setEnabled(on);
            txtNumbPad.setEnabled(on);
            lblNumbExplain.setEnabled(on);
        });

        txtNumbStart.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    if (Integer.parseInt(txtNumbStart.getText()) < 0) {
                        txtNumbStart.setText("1");
                    }
                } catch (NumberFormatException ex) {
                    txtNumbStart.setText("1");
                }
            }
        });

        txtNumbPad.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    if (Integer.parseInt(txtNumbPad.getText()) < 0) {
                        txtNumbPad.setText("1");
                    }
                } catch (NumberFormatException ex) {
                    txtNumbPad.setText("1");
                }
            }
        });

        chbLang.addActionListener(e -> cbLang.setEnabled(chbLang.isSelected()));
        chbExtraCmd.addActionListener(e -> txtExtraCmd.setEnabled(chbExtraCmd.isSelected()));

        // --- TrackControls wrapper ---
        TrackControls.TrackType tcType = switch (profileType) {
            case VIDEO -> TrackControls.TrackType.VIDEO;
            case AUDIO -> TrackControls.TrackType.AUDIO;
            case SUBTITLE -> TrackControls.TrackType.SUBTITLE;
        };

        TrackControls tc = new TrackControls(
                tcType, panel,
                chbEdit, chbEnable, rbYesEnable, rbNoEnable, bgRbEnable,
                chbDefault, rbYesDef, rbNoDef, bgRbDef,
                chbForced, rbYesForced, rbNoForced, bgRbForced,
                chbName, txtName,
                chbNumb, lblNumbStart, txtNumbStart, lblNumbPad, txtNumbPad, lblNumbExplain,
                chbLang, cbLang,
                chbExtraCmd, txtExtraCmd);

        tracks.add(tc);
        trackSelector.addItem(trackTitleBase + (index + 1));
    }

    private void addGridBag(JPanel parent, JComponent comp, int gridx, int gridy,
                            Insets insets, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.fill = fill;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        parent.add(comp, gbc);
    }

    private void setTrackEditEnabled(int trackIdx, boolean enabled) {
        if (trackIdx < 0 || trackIdx >= tracks.size()) return;
        tracks.get(trackIdx).setEditEnabled(enabled);
    }

    private void enableAudioProfileDnD() {
        // Apply drop target to the card panel so profiles can be dropped anywhere
        new DropTarget(cardPanel, new DropTargetListener() {
            @Override public void dragEnter(DropTargetDragEvent dtde) { }
            @Override public void dragOver(DropTargetDragEvent dtde) { }
            @Override public void dropActionChanged(DropTargetDragEvent dtde) { }
            @Override public void dragExit(DropTargetEvent dte) { }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    var tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
                        if (data.startsWith("AudioProfile:")) {
                            int profileIdx = Integer.parseInt(data.substring(data.indexOf(":") + 1));
                            int trackIdx = trackSelector.getSelectedIndex();
                            if (trackIdx >= 0) {
                                applyProfile(profileManager.getProfiles(ProfileType.AUDIO).get(profileIdx), trackIdx);
                            }
                            dtde.dropComplete(true);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
                dtde.rejectDrop();
            }
        });
    }

    // ------------------------------------------------------------------
    // Profile panel (mirrors createProfilePanel from JMkvpropedit)
    // ------------------------------------------------------------------
    private JPanel createProfilePanel() {
        JPanel pnl = new JPanel();
        pnl.setBorder(new TitledBorder(null, LanguageManager.getString("profile.panel.title"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pnl.setLayout(new BorderLayout(0, 0));
        pnl.setPreferredSize(new Dimension(180, 0));

        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setDragEnabled(true);
        profileList.setDropMode(javax.swing.DropMode.INSERT);
        profileList.setTransferHandler(new ProfileListTransferHandler(profileList, profileModel, profileType, profileManager));

        profileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = profileList.getSelectedIndex();
                    if (idx != -1) {
                        int trackIdx = trackSelector.getSelectedIndex();
                        if (trackIdx != -1) {
                            applyProfile(profileModel.get(idx), trackIdx);
                        }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(profileList);
        pnl.add(sp, BorderLayout.CENTER);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        pnl.add(pnlControls, BorderLayout.SOUTH);

        JButton btnAddProfile = createIconButton("/res/list-add.png");
        btnAddProfile.setToolTipText(LanguageManager.getString("profile.add.tooltip"));
        btnAddProfile.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(parentFrame,
                    LanguageManager.getString("profile.add.prompt"));
            if (name != null && !name.trim().isEmpty()) {
                int trackIdx = trackSelector.getSelectedIndex();
                if (trackIdx < 0) {
                    JOptionPane.showMessageDialog(parentFrame,
                            LanguageManager.getString("profile.error.select.track"));
                    return;
                }
                TrackProfile p = new TrackProfile();
                p.setName(name);
                updateProfileFromUI(p, trackIdx);
                profileManager.addProfile(profileType, p);
                profileModel.addElement(p);
            }
        });
        pnlControls.add(btnAddProfile);

        JButton btnUpdate = new JButton(LanguageManager.getString("profile.btn.update"));
        btnUpdate.setToolTipText(LanguageManager.getString("profile.update.tooltip"));
        btnUpdate.addActionListener(e -> {
            int idx = profileList.getSelectedIndex();
            if (idx != -1) {
                int trackIdx = trackSelector.getSelectedIndex();
                if (trackIdx < 0) {
                    JOptionPane.showMessageDialog(parentFrame,
                            LanguageManager.getString("profile.error.select.track"));
                    return;
                }
                int response = JOptionPane.showConfirmDialog(parentFrame,
                        LanguageManager.getString("profile.update.confirm"),
                        LanguageManager.getString("profile.update.title"),
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    TrackProfile p = profileModel.get(idx);
                    updateProfileFromUI(p, trackIdx);
                    profileManager.saveProfiles();
                    profileList.repaint();
                }
            }
        });
        pnlControls.add(btnUpdate);

        JButton btnRename = new JButton(LanguageManager.getString("profile.btn.rename"));
        btnRename.setToolTipText(LanguageManager.getString("profile.rename.tooltip"));
        btnRename.addActionListener(e -> {
            int idx = profileList.getSelectedIndex();
            if (idx != -1) {
                TrackProfile p = profileModel.get(idx);
                String newName = JOptionPane.showInputDialog(parentFrame,
                        LanguageManager.getString("profile.rename.prompt"), p.getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    p.setName(newName);
                    profileManager.saveProfiles();
                    profileList.repaint();
                }
            }
        });
        pnlControls.add(btnRename);

        JButton btnRemoveProfile = createIconButton("/res/list-remove.png");
        btnRemoveProfile.setToolTipText(LanguageManager.getString("profile.remove.tooltip"));
        btnRemoveProfile.addActionListener(e -> {
            int idx = profileList.getSelectedIndex();
            if (idx != -1) {
                int response = JOptionPane.showConfirmDialog(parentFrame,
                        LanguageManager.getString("profile.delete.confirm"),
                        LanguageManager.getString("profile.delete.title"),
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    profileManager.removeProfile(profileType, idx);
                    profileModel.remove(idx);
                }
            }
        });
        pnlControls.add(btnRemoveProfile);

        return pnl;
    }

    private void updateProfileFromUI(TrackProfile p, int trackIdx) {
        if (trackIdx < 0 || trackIdx >= tracks.size()) return;
        TrackControls c = tracks.get(trackIdx);
        p.setEnableTrack(c.rbYesEnable.isSelected());
        p.setUseEnableTrack(c.chbEnable.isSelected());
        p.setDefaultTrack(c.rbYesDef.isSelected());
        p.setUseDefaultTrack(c.chbDefault.isSelected());
        p.setForcedTrack(c.rbYesForced.isSelected());
        p.setUseForcedTrack(c.chbForced.isSelected());
        p.setTrackName(c.txtName.getText());
        p.setUseName(c.chbName.isSelected());
        p.setLanguage((String) c.cbLang.getSelectedItem());
        p.setUseLanguage(c.chbLang.isSelected());
    }

    /**
     * Applies a profile to the given track index.
     */
    public void applyProfile(TrackProfile p, int trackIdx) {
        if (p == null || trackIdx < 0 || trackIdx >= tracks.size()) return;
        TrackControls c = tracks.get(trackIdx);

        c.chbEdit.setSelected(true);
        c.setEditEnabled(true);

        c.chbEnable.setSelected(p.isUseEnableTrack());
        if (p.isEnableTrack()) c.rbYesEnable.setSelected(true);
        else c.rbNoEnable.setSelected(true);
        rbYesEnableSetEnabled(c, c.chbEnable.isSelected());

        c.chbDefault.setSelected(p.isUseDefaultTrack());
        if (p.isDefaultTrack()) c.rbYesDef.setSelected(true);
        else c.rbNoDef.setSelected(true);
        rbYesDefSetEnabled(c, c.chbDefault.isSelected());

        c.chbForced.setSelected(p.isUseForcedTrack());
        if (p.isForcedTrack()) c.rbYesForced.setSelected(true);
        else c.rbNoForced.setSelected(true);
        rbYesForcedSetEnabled(c, c.chbForced.isSelected());

        c.chbName.setSelected(p.isUseName());
        c.txtName.setText(p.getTrackName() == null ? "" : p.getTrackName());
        c.txtName.setEnabled(p.isUseName());
        c.chbNumb.setEnabled(p.isUseName());

        if (p.getLanguage() != null && !p.getLanguage().isEmpty()) {
            c.cbLang.setSelectedItem(p.getLanguage());
        }
        c.chbLang.setSelected(p.isUseLanguage());
        c.cbLang.setEnabled(p.isUseLanguage());
    }

    private void rbYesEnableSetEnabled(TrackControls c, boolean enabled) {
        c.rbYesEnable.setEnabled(enabled);
        c.rbNoEnable.setEnabled(enabled);
    }
    private void rbYesDefSetEnabled(TrackControls c, boolean enabled) {
        c.rbYesDef.setEnabled(enabled);
        c.rbNoDef.setEnabled(enabled);
    }
    private void rbYesForcedSetEnabled(TrackControls c, boolean enabled) {
        c.rbYesForced.setEnabled(enabled);
        c.rbNoForced.setEnabled(enabled);
    }

    // ------------------------------------------------------------------
    // Command-line generation
    // ------------------------------------------------------------------

    /**
     * Builds the command-line segments for all tracks of this type.
     *
     * @param fileCount number of files being processed
     * @param fileNames list of file names (without extension) for substitution
     * @return Two arrays: [0]=display commands, [1]=option commands.
     *         Each array has {@code fileCount} elements.
     */
    public String[][] buildCommandLines(int fileCount, List<String> fileNames) {
        String[] cmdLines = new String[fileCount];
        String[] optLines = new String[fileCount];
        for (int i = 0; i < fileCount; i++) {
            cmdLines[i] = "";
            optLines[i] = "";
        }

        // Per-track temporary command lines
        String[] tmpCmd = new String[tracks.size()];
        String[] tmpOpt = new String[tracks.size()];
        int[] numStart = new int[tracks.size()];
        int[] numPad = new int[tracks.size()];

        for (int trackIdx = 0; trackIdx < tracks.size(); trackIdx++) {
            TrackControls c = tracks.get(trackIdx);
            if (!c.chbEdit.isSelected()) {
                tmpCmd[trackIdx] = "";
                tmpOpt[trackIdx] = "";
                continue;
            }

            numStart[trackIdx] = Integer.parseInt(c.txtNumbStart.getText());
            numPad[trackIdx] = Integer.parseInt(c.txtNumbPad.getText());

            String[] result = c.buildCommandLine(trackIdx + 1, 0, "");
            tmpCmd[trackIdx] = result[0];
            tmpOpt[trackIdx] = result[1];
        }

        for (int trackIdx = 0; trackIdx < tracks.size(); trackIdx++) {
            TrackControls c = tracks.get(trackIdx);
            for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
                String text = tmpCmd[trackIdx];
                String textOpt = tmpOpt[trackIdx];

                if (text.isEmpty()) continue;

                if (c.chbNumb.isSelected() && c.chbEdit.isSelected()) {
                    String padded = Utils.padNumber(numPad[trackIdx], numStart[trackIdx]);
                    text = text.replace("{num}", padded);
                    textOpt = textOpt.replace("{num}", padded);
                    numStart[trackIdx]++;
                }

                text = text.replace("{file_name}", fileNames.get(fileIdx));
                textOpt = textOpt.replace("{file_name}", fileNames.get(fileIdx));

                cmdLines[fileIdx] += text;
                optLines[fileIdx] += textOpt;
            }
        }

        return new String[][] { cmdLines, optLines };
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public List<TrackControls> getTracks() {
        return tracks;
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public int getSelectedTrackIndex() {
        return trackSelector.getSelectedIndex();
    }

    public DefaultListModel<TrackProfile> getProfileModel() {
        return profileModel;
    }

    public JList<TrackProfile> getProfileList() {
        return profileList;
    }

    // ------------------------------------------------------------------
    // Inner class: profile list drag-and-drop transfer handler
    // ------------------------------------------------------------------

    private static class ProfileListTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;
        private final JList<TrackProfile> list;
        private final DefaultListModel<TrackProfile> model;
        private final ProfileType type;
        private final ProfileManager manager;
        private int draggedIndex = -1;

        ProfileListTransferHandler(JList<TrackProfile> list, DefaultListModel<TrackProfile> model,
                                   ProfileType type, ProfileManager manager) {
            this.list = list;
            this.model = model;
            this.type = type;
            this.manager = manager;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            draggedIndex = list.getSelectedIndex();
            if (draggedIndex != -1) {
                return new StringSelection(String.valueOf(draggedIndex));
            }
            return null;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            int dropIndex = dl.getIndex();
            if (draggedIndex < 0 || draggedIndex >= model.getSize()) return false;
            if (dropIndex == draggedIndex || dropIndex == draggedIndex + 1) return false;

            TrackProfile dragged = model.get(draggedIndex);
            model.remove(draggedIndex);
            if (dropIndex > draggedIndex) dropIndex--;
            model.add(dropIndex, dragged);
            list.setSelectedIndex(dropIndex);
            manager.reorderProfiles(type, model);
            return true;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            draggedIndex = -1;
        }
    }
}
