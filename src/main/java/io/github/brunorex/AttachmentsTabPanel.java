package io.github.brunorex;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentsTabPanel extends JTabbedPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentsTabPanel.class);

    private static final String[] COLUMNS_ATTACHMENTS_ADD = { "File", "Name", "Description", "MIME Type" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_ADD = { 0.35, 0.20, 0.25, 0.20 };

    private static final String[] COLUMNS_ATTACHMENTS_REPLACE = { "Type", "Original Value", "Replacement", "Name",
            "Description", "MIME Type" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_REPLACE = { 0.15, 0.15, 0.20, 0.20, 0.15, 0.15 };

    private static final String[] COLUMNS_ATTACHMENTS_DELETE = { "Type", "Value" };
    private static final double[] COLUMN_SIZES_ATTACHMENTS_DELETE = { 0.40, 0.60 };

    private final JFrame parentFrame;
    private final JFileChooser chooser;

    private DefaultTableModel modelAttachmentsAdd = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_ADD) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel modelAttachmentsReplace = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_REPLACE) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel modelAttachmentsDelete = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_DELETE) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable tblAttachAdd;
    private JTable tblAttachReplace;
    private JTable tblAttachDelete;

    private JTextField txtAttachAddFile;
    private JTextField txtAttachAddName;
    private JTextField txtAttachAddDesc;
    private JComboBox<String> cbAttachAddMime;
    private JButton btnAttachAddAdd;
    private JButton btnAttachAddEdit;
    private JButton btnAttachAddRemove;
    private JButton btnAttachAddCancel;

    private ButtonGroup bgAttachReplaceType = new ButtonGroup();
    private JRadioButton rbAttachReplaceName;
    private JRadioButton rbAttachReplaceID;
    private JRadioButton rbAttachReplaceMime;
    private JTextField txtAttachReplaceOrig;
    private JComboBox<String> cbAttachReplaceOrig;
    private JTextField txtAttachReplaceNew;
    private JButton btnAttachReplaceNewBrowse;
    private JTextField txtAttachReplaceName;
    private JTextField txtAttachReplaceDesc;
    private JComboBox<String> cbAttachReplaceMime;
    private JButton btnAttachReplaceAdd;
    private JButton btnAttachReplaceEdit;
    private JButton btnAttachReplaceRemove;
    private JButton btnAttachReplaceCancel;

    private ButtonGroup bgAttachDeleteType = new ButtonGroup();
    private JRadioButton rbAttachDeleteName;
    private JRadioButton rbAttachDeleteID;
    private JRadioButton rbAttachDeleteMime;
    private JTextField txtAttachDeleteValue;
    private JComboBox<String> cbAttachDeleteValue;
    private JButton btnAttachDeleteAdd;
    private JButton btnAttachDeleteEdit;
    private JButton btnAttachDeleteRemove;
    private JButton btnAttachDeleteCancel;

    private String cmdLineAttachmentsAdd;
    private String cmdLineAttachmentsAddOpt;
    private String cmdLineAttachmentsReplace;
    private String cmdLineAttachmentsReplaceOpt;
    private String cmdLineAttachmentsDelete;
    private String cmdLineAttachmentsDeleteOpt;

    public AttachmentsTabPanel(JFrame parentFrame, JFileChooser chooser, MkvStrings mkvStrings) {
        super(JTabbedPane.TOP);
        this.parentFrame = parentFrame;
        this.chooser = chooser;

        buildAddTab(mkvStrings);
        buildReplaceTab(mkvStrings);
        buildDeleteTab(mkvStrings);
        setupListeners(mkvStrings);
    }

    public JTable getTableAdd() {
        return tblAttachAdd;
    }

    public JTable getTableReplace() {
        return tblAttachReplace;
    }

    public JTable getTableDelete() {
        return tblAttachDelete;
    }

    public double[] getColumnSizesAdd() {
        return COLUMN_SIZES_ATTACHMENTS_ADD;
    }

    public double[] getColumnSizesReplace() {
        return COLUMN_SIZES_ATTACHMENTS_REPLACE;
    }

    public double[] getColumnSizesDelete() {
        return COLUMN_SIZES_ATTACHMENTS_DELETE;
    }

    public String getCommandLineAdd() {
        return cmdLineAttachmentsAdd;
    }

    public String getCommandLineAddOpt() {
        return cmdLineAttachmentsAddOpt;
    }

    public String getCommandLineReplace() {
        return cmdLineAttachmentsReplace;
    }

    public String getCommandLineReplaceOpt() {
        return cmdLineAttachmentsReplaceOpt;
    }

    public String getCommandLineDelete() {
        return cmdLineAttachmentsDelete;
    }

    public String getCommandLineDeleteOpt() {
        return cmdLineAttachmentsDeleteOpt;
    }

    public void updateCommandLines() {
        setCmdLineAttachmentsAdd();
        setCmdLineAttachmentsReplace();
        setCmdLineAttachmentsDelete();
    }

    private void buildAddTab(MkvStrings mkvStrings) {
        JPanel pnlAttachAdd = new JPanel();
        addTab(LanguageManager.getString("attachments.tab.add"), null, pnlAttachAdd, null);
        pnlAttachAdd.setLayout(new BorderLayout(0, 0));

        JScrollPane spAttachAdd = new JScrollPane();
        pnlAttachAdd.add(spAttachAdd, BorderLayout.CENTER);

        tblAttachAdd = new JTable();
        tblAttachAdd.setShowGrid(false);
        tblAttachAdd.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachAdd.setModel(modelAttachmentsAdd);
        tblAttachAdd.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachAdd.setAutoscrolls(false);
        tblAttachAdd.setFillsViewportHeight(true);
        spAttachAdd.setViewportView(tblAttachAdd);

        JPanel pnlAttachAddControls = new JPanel();
        pnlAttachAddControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachAdd.add(pnlAttachAddControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachAddControls = new GridBagLayout();
        gbl_pnlAttachAddControls.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachAddControls.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
        gbl_pnlAttachAddControls.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachAddControls.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
        pnlAttachAddControls.setLayout(gbl_pnlAttachAddControls);

        JLabel lblAttachAddFile = new JLabel(LanguageManager.getString("attachments.file"));
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

        JButton btnBrowseAttachAddFile = new JButton(LanguageManager.getString("button.browse"));
        GridBagConstraints gbc_btnBrowseAttachAddFile = new GridBagConstraints();
        gbc_btnBrowseAttachAddFile.insets = new Insets(0, 0, 5, 0);
        gbc_btnBrowseAttachAddFile.gridx = 2;
        gbc_btnBrowseAttachAddFile.gridy = 0;
        pnlAttachAddControls.add(btnBrowseAttachAddFile, gbc_btnBrowseAttachAddFile);

        JLabel lblAttachAddName = new JLabel(LanguageManager.getString("attachments.name"));
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

        JLabel lblAttachAddDesc = new JLabel(LanguageManager.getString("attachments.description"));
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

        JLabel lblAttachAddMime = new JLabel(LanguageManager.getString("attachments.mime"));
        GridBagConstraints gbc_lblAttachAddMime = new GridBagConstraints();
        gbc_lblAttachAddMime.anchor = GridBagConstraints.EAST;
        gbc_lblAttachAddMime.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachAddMime.gridx = 0;
        gbc_lblAttachAddMime.gridy = 3;
        pnlAttachAddControls.add(lblAttachAddMime, gbc_lblAttachAddMime);

        cbAttachAddMime = new JComboBox<>();
        cbAttachAddMime.setModel(new DefaultComboBoxModel<>(mkvStrings.getMimeTypes()));
        GridBagConstraints gbc_cbAttachAddMime = new GridBagConstraints();
        gbc_cbAttachAddMime.insets = new Insets(0, 0, 5, 5);
        gbc_cbAttachAddMime.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbAttachAddMime.gridx = 1;
        gbc_cbAttachAddMime.gridy = 3;
        pnlAttachAddControls.add(cbAttachAddMime, gbc_cbAttachAddMime);

        JPanel pnlAttachAddControlsBottom = new JPanel();
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

        btnBrowseAttachAddFile.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.attachment"));
            chooser.setMultiSelectionEnabled(false);
            chooser.resetChoosableFileFilters();
            chooser.setAcceptAllFileFilterUsed(true);

            int open = chooser.showOpenDialog(parentFrame);

            if (open == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();

                if (f.exists()) {
                    try {
                        txtAttachAddFile.setText(f.getCanonicalPath());
                    } catch (IOException e1) {
                        LOGGER.error("Error resolving attachment add file path", e1);
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

        btnAttachAddAdd.addActionListener(e -> {
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
        });

        btnAttachAddEdit.addActionListener(e -> {
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
        });

        btnAttachAddRemove.addActionListener(e -> {
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
        });

        btnAttachAddCancel.addActionListener(e -> {
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
        });
    }

    private void buildReplaceTab(MkvStrings mkvStrings) {
        JPanel pnlAttachReplace = new JPanel();
        addTab(LanguageManager.getString("attachments.tab.replace"), null, pnlAttachReplace, null);
        pnlAttachReplace.setLayout(new BorderLayout(0, 0));

        JScrollPane spAttachReplace = new JScrollPane();
        pnlAttachReplace.add(spAttachReplace, BorderLayout.CENTER);

        tblAttachReplace = new JTable();
        tblAttachReplace.setShowGrid(false);
        tblAttachReplace.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachReplace.setModel(modelAttachmentsReplace);
        tblAttachReplace.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachReplace.setAutoscrolls(false);
        tblAttachReplace.setFillsViewportHeight(true);
        spAttachReplace.setViewportView(tblAttachReplace);

        JPanel pnlAttachReplaceControls = new JPanel();
        pnlAttachReplaceControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachReplace.add(pnlAttachReplaceControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachReplaceControls = new GridBagLayout();
        gbl_pnlAttachReplaceControls.columnWidths = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachReplaceControls.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_pnlAttachReplaceControls.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gbl_pnlAttachReplaceControls.rowWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        pnlAttachReplaceControls.setLayout(gbl_pnlAttachReplaceControls);

        JLabel lblAttachReplaceType = new JLabel(LanguageManager.getString("attachments.type"));
        GridBagConstraints gbc_lblAttachReplaceType = new GridBagConstraints();
        gbc_lblAttachReplaceType.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceType.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceType.gridx = 0;
        gbc_lblAttachReplaceType.gridy = 0;
        pnlAttachReplaceControls.add(lblAttachReplaceType, gbc_lblAttachReplaceType);

        JPanel pnlAttachReplaceType = new JPanel();
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

        JLabel lblAttachReplaceOrig = new JLabel(LanguageManager.getString("attachments.original.value"));
        GridBagConstraints gbc_lblAttachReplaceOrig = new GridBagConstraints();
        gbc_lblAttachReplaceOrig.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceOrig.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceOrig.gridx = 0;
        gbc_lblAttachReplaceOrig.gridy = 1;
        pnlAttachReplaceControls.add(lblAttachReplaceOrig, gbc_lblAttachReplaceOrig);

        JPanel pnlAttachReplaceOrig = new JPanel();
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

        cbAttachReplaceOrig = new JComboBox<>();
        List<String> mimeList = mkvStrings.getMimeTypeList();
        mimeList.remove(0);
        cbAttachReplaceOrig.setModel(new DefaultComboBoxModel<>(mimeList.toArray(new String[0])));
        cbAttachReplaceOrig.setVisible(false);
        pnlAttachReplaceOrig.add(cbAttachReplaceOrig, "cbAttachReplaceOrig");

        JLabel lblAttachReplaceNew = new JLabel(LanguageManager.getString("attachments.replacement"));
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

        JLabel lblAttachReplaceName = new JLabel(LanguageManager.getString("attachments.name"));
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

        JLabel lblAttachReplaceDesc = new JLabel(LanguageManager.getString("attachments.description"));
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

        JLabel lblAttachReplaceMime = new JLabel(LanguageManager.getString("attachments.mime"));
        GridBagConstraints gbc_lblAttachReplaceMime = new GridBagConstraints();
        gbc_lblAttachReplaceMime.anchor = GridBagConstraints.WEST;
        gbc_lblAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachReplaceMime.gridx = 0;
        gbc_lblAttachReplaceMime.gridy = 5;
        pnlAttachReplaceControls.add(lblAttachReplaceMime, gbc_lblAttachReplaceMime);

        cbAttachReplaceMime = new JComboBox<>();
        cbAttachReplaceMime.setModel(new DefaultComboBoxModel<>(mkvStrings.getMimeTypes()));
        GridBagConstraints gbc_cbAttachReplaceMime = new GridBagConstraints();
        gbc_cbAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
        gbc_cbAttachReplaceMime.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbAttachReplaceMime.gridx = 1;
        gbc_cbAttachReplaceMime.gridy = 5;
        pnlAttachReplaceControls.add(cbAttachReplaceMime, gbc_cbAttachReplaceMime);

        JPanel pnlAttachReplaceControlsBottom = new JPanel();
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
    }

    private void buildDeleteTab(MkvStrings mkvStrings) {
        JPanel pnlAttachDelete = new JPanel();
        addTab(LanguageManager.getString("attachments.tab.delete"), null, pnlAttachDelete, null);
        pnlAttachDelete.setLayout(new BorderLayout(0, 0));

        JScrollPane spAttachDelete = new JScrollPane();
        pnlAttachDelete.add(spAttachDelete, BorderLayout.CENTER);

        tblAttachDelete = new JTable();
        tblAttachDelete.setShowGrid(false);
        tblAttachDelete.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAttachDelete.setModel(modelAttachmentsDelete);
        tblAttachDelete.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAttachDelete.setAutoscrolls(false);
        tblAttachDelete.setFillsViewportHeight(true);
        spAttachDelete.setViewportView(tblAttachDelete);

        JPanel pnlAttachDeleteControls = new JPanel();
        pnlAttachDeleteControls.setBorder(new EmptyBorder(5, 5, 5, 5));
        pnlAttachDelete.add(pnlAttachDeleteControls, BorderLayout.SOUTH);
        GridBagLayout gbl_pnlAttachDeleteControls = new GridBagLayout();
        gbl_pnlAttachDeleteControls.columnWidths = new int[] { 0, 0, 0 };
        gbl_pnlAttachDeleteControls.rowHeights = new int[] { 0, 0, 0, 0 };
        gbl_pnlAttachDeleteControls.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_pnlAttachDeleteControls.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
        pnlAttachDeleteControls.setLayout(gbl_pnlAttachDeleteControls);

        JLabel lblAttachDeleteType = new JLabel(LanguageManager.getString("attachments.type"));
        GridBagConstraints gbc_lblAttachDeleteType = new GridBagConstraints();
        gbc_lblAttachDeleteType.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachDeleteType.gridx = 0;
        gbc_lblAttachDeleteType.gridy = 0;
        pnlAttachDeleteControls.add(lblAttachDeleteType, gbc_lblAttachDeleteType);

        JPanel pnlAttachDeleteType = new JPanel();
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

        JLabel lblAttachDeleteValue = new JLabel(LanguageManager.getString("attachments.original.value"));
        GridBagConstraints gbc_lblAttachDeleteValue = new GridBagConstraints();
        gbc_lblAttachDeleteValue.anchor = GridBagConstraints.EAST;
        gbc_lblAttachDeleteValue.insets = new Insets(0, 0, 5, 5);
        gbc_lblAttachDeleteValue.gridx = 0;
        gbc_lblAttachDeleteValue.gridy = 1;
        pnlAttachDeleteControls.add(lblAttachDeleteValue, gbc_lblAttachDeleteValue);

        JPanel pnlAttachDeleteValue = new JPanel();
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

        List<String> mimeList = mkvStrings.getMimeTypeList();
        mimeList.remove(0);
        cbAttachDeleteValue = new JComboBox<>();
        cbAttachDeleteValue.setVisible(false);
        cbAttachDeleteValue.setModel(new DefaultComboBoxModel<>(mimeList.toArray(new String[0])));
        pnlAttachDeleteValue.add(cbAttachDeleteValue, "cbAttachDeleteValue");

        JPanel pnlAttachDeleteControlsBottom = new JPanel();
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
    }

    private void setupListeners(MkvStrings mkvStrings) {
        rbAttachReplaceName.addActionListener(e -> {
            cbAttachReplaceOrig.setVisible(false);
            txtAttachReplaceOrig.setVisible(true);
            txtAttachReplaceOrig.setText("");
        });

        rbAttachReplaceID.addActionListener(e -> {
            cbAttachReplaceOrig.setVisible(false);
            txtAttachReplaceOrig.setVisible(true);
            txtAttachReplaceOrig.setText("1");
        });

        rbAttachReplaceMime.addActionListener(e -> {
            txtAttachReplaceOrig.setVisible(false);
            cbAttachReplaceOrig.setVisible(true);
            cbAttachReplaceOrig.setSelectedIndex(0);
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

        btnAttachReplaceNewBrowse.addActionListener(e -> {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.attachment"));
            chooser.setMultiSelectionEnabled(false);
            chooser.resetChoosableFileFilters();
            chooser.setAcceptAllFileFilterUsed(true);

            int open = chooser.showOpenDialog(parentFrame);

            if (open == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();

                if (f.exists()) {
                    try {
                        txtAttachReplaceNew.setText(f.getCanonicalPath());
                    } catch (IOException e1) {
                        LOGGER.error("Error resolving attachment replacement file path", e1);
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
                        cbAttachReplaceOrig.setSelectedItem(orig);
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

        btnAttachReplaceAdd.addActionListener(e -> {
            String type;
            String orig;

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
        });

        btnAttachReplaceEdit.addActionListener(e -> {
            String type;
            String orig;

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
        });

        btnAttachReplaceRemove.addActionListener(e -> {
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
        });

        btnAttachReplaceCancel.addActionListener(e -> {
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

        rbAttachDeleteName.addActionListener(e -> {
            cbAttachDeleteValue.setVisible(false);
            txtAttachDeleteValue.setVisible(true);
            txtAttachDeleteValue.setText("");
        });

        rbAttachDeleteID.addActionListener(e -> {
            cbAttachDeleteValue.setVisible(false);
            txtAttachDeleteValue.setVisible(true);
            txtAttachDeleteValue.setText("1");
        });

        rbAttachDeleteMime.addActionListener(e -> {
            txtAttachDeleteValue.setVisible(false);
            cbAttachDeleteValue.setVisible(true);
            cbAttachDeleteValue.setSelectedIndex(0);
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

        btnAttachDeleteAdd.addActionListener(e -> {
            String type;
            String value;

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
        });

        btnAttachDeleteEdit.addActionListener(e -> {
            String type;
            String value;

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
        });

        btnAttachDeleteRemove.addActionListener(e -> {
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
        });

        btnAttachDeleteCancel.addActionListener(e -> {
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
        });
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
}
