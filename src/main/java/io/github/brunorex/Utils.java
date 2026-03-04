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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;

import java.util.Locale;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public class Utils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private static final String ESCAPED_QUOTES_PLACEHOLDER = "####escaped__quotes#####";

    /* Start of OS detection functions */

    public static boolean isWindows() {
        return OS_NAME.startsWith("windows");
    }

    public static boolean isMac() {
        return OS_NAME.startsWith("mac");
    }

    public static boolean isLinux() {
        return OS_NAME.startsWith("linux");
    }

    /* End of OS detection functions */

    /* Start of escaping functions */

    public static String escapeName(String name) {
        if (name != null && !name.isEmpty()) {
            return name.replace("\"", ESCAPED_QUOTES_PLACEHOLDER)
                    .replace("\\", "\\\\");
        }
        return name == null ? "" : name;
    }

    public static String escapeQuotes(String text) {
        return text == null ? "" : text.replace("\"", "\\\"");
    }

    public static String escapeBackslashes(String text) {
        return text == null ? "" : text.replace("\\", "\\\\");
    }

    public static String fixEscapedQuotes(String text) {
        if (text != null && !text.isEmpty()) {
            return text.replace(ESCAPED_QUOTES_PLACEHOLDER, "\\\"");
        }
        return text == null ? "" : text;
    }

    /* End of escaping functions */

    /* Start of right-click menu code */

    private static void showRCMenu(JTextComponent text, MouseEvent e) {
        int selStart = text.getSelectionStart();
        int selEnd = text.getSelectionEnd();

        JPopupMenu rightClickMenu = new JPopupMenu();

        JMenuItem copyMenuItem = new JMenuItem(text.getActionMap().get(DefaultEditorKit.copyAction));
        JMenuItem cutMenuItem = new JMenuItem(text.getActionMap().get(DefaultEditorKit.cutAction));
        JMenuItem pasteMenuItem = new JMenuItem(text.getActionMap().get(DefaultEditorKit.pasteAction));
        JMenuItem selectAllMenuItem = new JMenuItem(text.getActionMap().get(DefaultEditorKit.selectAllAction));

        // Ensure actions have text
        if (copyMenuItem.getText() == null || copyMenuItem.getText().isEmpty())
            copyMenuItem.setText("Copy");
        if (cutMenuItem.getText() == null || cutMenuItem.getText().isEmpty())
            cutMenuItem.setText("Cut");
        if (pasteMenuItem.getText() == null || pasteMenuItem.getText().isEmpty())
            pasteMenuItem.setText("Paste");
        if (selectAllMenuItem.getText() == null || selectAllMenuItem.getText().isEmpty())
            selectAllMenuItem.setText("Select All");

        rightClickMenu.add(copyMenuItem);
        rightClickMenu.add(cutMenuItem);
        rightClickMenu.add(pasteMenuItem);
        rightClickMenu.addSeparator();
        rightClickMenu.add(selectAllMenuItem);

        boolean hasText = text.getText() != null && !text.getText().isEmpty();
        boolean hasSelection = selStart != selEnd;

        if (!hasText) {
            copyMenuItem.setEnabled(false);
            selectAllMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
        }

        if (!hasSelection) {
            copyMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
        }

        // Disable "Select All" if everything is already selected
        if (hasText && (selEnd - selStart) == text.getText().length()) {
            selectAllMenuItem.setEnabled(false);
        }

        if (!text.isEditable()) {
            cutMenuItem.setEnabled(false);
            pasteMenuItem.setEnabled(false);
        }

        rightClickMenu.show(text, e.getX(), e.getY());
    }

    public static void addRCMenuMouseListener(final JTextComponent text) {
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isMetaDown() && text.isEnabled()) {
                    text.requestFocus();
                    showRCMenu(text, e);
                }
            }
        });
    }

    /* End of right-click menu code */

    public static String padNumber(int pad, int number) {
        if (pad > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pad; i++)
                sb.append('0');
            DecimalFormat formatter = new DecimalFormat(sb.toString());
            return formatter.format(number);
        }
        return String.valueOf(number);
    }

    public static int getDotIndex(String file) {
        if (file == null)
            return -1;
        int dotIndex = file.lastIndexOf(".");
        return dotIndex != -1 ? dotIndex : file.length();
    }

    public static int getSeparatorIndex(String file) {
        if (file == null)
            return 0;
        int sepIndex = file.lastIndexOf(File.separator);
        return sepIndex != -1 ? sepIndex + 1 : 0;
    }

    public static String getFileNameWithoutExt(String file) {
        if (file == null)
            return "";
        int start = getSeparatorIndex(file);
        int end = getDotIndex(file);
        // Fix for when dot appears in path but not in filename (no extension)
        if (end < start)
            end = file.length();
        return file.substring(start, end);
    }

    public static String getPathWithoutExt(String file) {
        if (file == null)
            return "";
        int end = getDotIndex(file);
        // Fix for when dot appears in path but not in filename (no extension)
        // If end < separator index, it means the last dot was in a directory name
        if (end < getSeparatorIndex(file))
            end = file.length();

        return file.substring(0, end);
    }

    /**
     * http://niravjavadeveloper.blogspot.com/2011/05/resize-jtable-columns.html
     */
    public static void adjustColumnPreferredWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxwidth = 0;

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer rend = table.getCellRenderer(row, col);
                Object value = table.getValueAt(row, col);

                Component comp = rend.getTableCellRendererComponent(table, value, false, false, row, col);
                maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
            }

            TableColumn column = columnModel.getColumn(col);
            column.setPreferredWidth(maxwidth + 10); // Added padding
        }
    }
}
