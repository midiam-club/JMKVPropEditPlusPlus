package io.github.brunorex;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class InputTabPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JFrame parentFrame;
    private final JFileChooser chooser;
    private final MkvStrings mkvStrings;
    private final IOFileFilter matroskaFilter;
    private final FileFilter matroskaExtFilter;

    private final DefaultListModel<String> modelFiles = new DefaultListModel<>();
    private final JList<String> listFiles = new JList<>(modelFiles);

    public InputTabPanel(JFrame parentFrame, JFileChooser chooser, MkvStrings mkvStrings,
                         IOFileFilter matroskaFilter, FileFilter matroskaExtFilter) {
        this.parentFrame = parentFrame;
        this.chooser = chooser;
        this.mkvStrings = mkvStrings;
        this.matroskaFilter = matroskaFilter;
        this.matroskaExtFilter = matroskaExtFilter;

        setBorder(new EmptyBorder(10, 10, 10, 0));
        setLayout(new BorderLayout(0, 0));

        JScrollPane spFiles = new JScrollPane();
        spFiles.setViewportBorder(null);
        add(spFiles);
        spFiles.setViewportView(listFiles);

        JPanel pnlListToolbar = new JPanel();
        pnlListToolbar.setBorder(new EmptyBorder(0, 5, 0, 5));
        add(pnlListToolbar, BorderLayout.EAST);
        pnlListToolbar.setLayout(new BoxLayout(pnlListToolbar, BoxLayout.Y_AXIS));

        JButton btnAddFiles = createToolbarButton("/res/list-add.png", "Add files");
        pnlListToolbar.add(btnAddFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnAddFolder = createToolbarButton("/res/list-add-folder.png", "Add folder");
        pnlListToolbar.add(btnAddFolder);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnRemoveFiles = createToolbarButton("/res/list-remove.png", "Remove selected files");
        pnlListToolbar.add(btnRemoveFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnTopFiles = createToolbarButton("/res/go-top.png", "Move selected files to the top");
        pnlListToolbar.add(btnTopFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnUpFiles = createToolbarButton("/res/go-up.png", "Move selected files up");
        pnlListToolbar.add(btnUpFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnDownFiles = createToolbarButton("/res/go-down.png", "Move selected files down");
        pnlListToolbar.add(btnDownFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnBottomFiles = createToolbarButton("/res/go-bottom.png", "Move selected files to the bottom");
        pnlListToolbar.add(btnBottomFiles);
        pnlListToolbar.add(Box.createVerticalStrut(10));

        JButton btnClearFiles = createToolbarButton("/res/edit-clear.png", LanguageManager.getString("input.clear.tooltip"));
        pnlListToolbar.add(btnClearFiles);

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

        btnAddFiles.addActionListener((ActionEvent e) -> {
            File[] files = null;

            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.file"));
            chooser.setMultiSelectionEnabled(true);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.resetChoosableFileFilters();
            chooser.setFileFilter(matroskaExtFilter);

            int open = chooser.showOpenDialog(parentFrame);

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
        });

        btnAddFolder.addActionListener((ActionEvent e) -> {
            File folder = null;

            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle(LanguageManager.getString("getChooser().title.folder"));
            chooser.setAcceptAllFileFilterUsed(false);

            int open = chooser.showOpenDialog(parentFrame);

            if (open == JFileChooser.APPROVE_OPTION) {
                folder = chooser.getSelectedFile();
                addMkvFilesFromFolder(folder);
            }
        });

        btnRemoveFiles.addActionListener((ActionEvent e) -> {
            if (modelFiles.getSize() > 0) {
                while (listFiles.getSelectedIndex() != -1) {
                    int[] idx = listFiles.getSelectedIndices();
                    modelFiles.remove(idx[0]);
                }
            }
        });

        btnClearFiles.addActionListener((ActionEvent e) -> {
            modelFiles.removeAllElements();
        });

        btnTopFiles.addActionListener((ActionEvent e) -> {
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
        });

        btnUpFiles.addActionListener((ActionEvent e) -> {
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
        });

        btnDownFiles.addActionListener((ActionEvent e) -> {
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
        });

        btnBottomFiles.addActionListener((ActionEvent e) -> {
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
        });
    }

    public DefaultListModel<String> getModel() {
        return modelFiles;
    }

    public JComponent getListComponent() {
        return listFiles;
    }

    public void addFile(File file, boolean checkExtension) {
        try {
            if (!modelFiles.contains(file.getCanonicalPath()) && !checkExtension) {
                modelFiles.add(modelFiles.getSize(), file.getCanonicalPath());
            } else if (!modelFiles.contains(file.getCanonicalPath()) && matroskaExtFilter.accept(file)) {
                modelFiles.add(modelFiles.getSize(), file.getCanonicalPath());
            }
        } catch (IOException e) {
        }
    }

    public void addMkvFilesFromFolder(final File folder) {
        Runnable tmpWorker = new Runnable() {
            @Override
            public void run() {
                Iterator<File> mkvFiles = FileUtils.iterateFiles(folder, matroskaFilter, TrueFileFilter.INSTANCE);

                while (mkvFiles.hasNext()) {
                    addFile(mkvFiles.next(), false);
                }
            }
        };

        SwingUtilities.invokeLater(tmpWorker);
    }

    private static JButton createToolbarButton(String iconPath, String tooltip) {
        JButton btn = new JButton("");
        btn.setIcon(new ImageIcon(InputTabPanel.class.getResource(iconPath)));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setToolTipText(tooltip);
        return btn;
    }
}
