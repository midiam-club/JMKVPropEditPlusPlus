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

import javax.swing.*;

/**
 * Encapsulates all UI controls for a single track (Video, Audio, or Subtitle).
 * IMP-01: Replaces the fixed-size arrays of MAX_STREAMS (200) elements with
 * dynamically created, type-safe objects.
 *
 * This class is a data holder for the UI controls; layout and event wiring
 * remain in the parent class to minimize refactoring risk.
 */
public class TrackControls {

    /** The type of track this controls object represents. */
    public enum TrackType {
        VIDEO, AUDIO, SUBTITLE
    }

    public final TrackType type;
    public final JPanel panel;
    public final JCheckBox chbEdit;
    public final JCheckBox chbEnable;
    public final JRadioButton rbYesEnable;
    public final JRadioButton rbNoEnable;
    public final ButtonGroup bgRbEnable;
    public final JCheckBox chbDefault;
    public final JRadioButton rbYesDef;
    public final JRadioButton rbNoDef;
    public final ButtonGroup bgRbDef;
    public final JCheckBox chbForced;
    public final JRadioButton rbYesForced;
    public final JRadioButton rbNoForced;
    public final ButtonGroup bgRbForced;
    public final JCheckBox chbName;
    public final JTextField txtName;
    public final JCheckBox chbNumb;
    public final JLabel lblNumbStart;
    public final JTextField txtNumbStart;
    public final JLabel lblNumbPad;
    public final JTextField txtNumbPad;
    public final JLabel lblNumbExplain;
    public final JCheckBox chbLang;
    public final JComboBox<String> cbLang;
    public final JCheckBox chbExtraCmd;
    public final JTextField txtExtraCmd;

    /**
     * Creates a TrackControls with all UI components initialized externally.
     * The caller (addVideoTrack, addAudioTrack, addSubtitleTrack) creates the
     * components, wires events, and then wraps them here for type-safe access.
     */
    public TrackControls(TrackType type,
            JPanel panel,
            JCheckBox chbEdit,
            JCheckBox chbEnable,
            JRadioButton rbYesEnable,
            JRadioButton rbNoEnable,
            ButtonGroup bgRbEnable,
            JCheckBox chbDefault,
            JRadioButton rbYesDef,
            JRadioButton rbNoDef,
            ButtonGroup bgRbDef,
            JCheckBox chbForced,
            JRadioButton rbYesForced,
            JRadioButton rbNoForced,
            ButtonGroup bgRbForced,
            JCheckBox chbName,
            JTextField txtName,
            JCheckBox chbNumb,
            JLabel lblNumbStart,
            JTextField txtNumbStart,
            JLabel lblNumbPad,
            JTextField txtNumbPad,
            JLabel lblNumbExplain,
            JCheckBox chbLang,
            JComboBox<String> cbLang,
            JCheckBox chbExtraCmd,
            JTextField txtExtraCmd) {
        this.type = type;
        this.panel = panel;
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

    /**
     * Resets all controls to their default disabled state.
     * Used when the last remaining track cannot be deleted (track protection).
     */
    public void clearAndDisable() {
        chbEdit.setSelected(false);
        chbEnable.setSelected(false);
        chbDefault.setSelected(false);
        chbForced.setSelected(false);
        chbName.setSelected(false);
        chbNumb.setSelected(false);
        chbLang.setSelected(false);
        chbExtraCmd.setSelected(false);
        txtName.setText("");
        txtExtraCmd.setText("");
        rbYesEnable.setSelected(true);
        rbYesDef.setSelected(true);
        rbYesForced.setSelected(true);

        setEditEnabled(false);
    }

    /**
     * Enables or disables all editable controls based on the edit checkbox state.
     */
    public void setEditEnabled(boolean enabled) {
        chbEnable.setEnabled(enabled);
        rbYesEnable.setEnabled(enabled && chbEnable.isSelected());
        rbNoEnable.setEnabled(enabled && chbEnable.isSelected());
        chbDefault.setEnabled(enabled);
        rbYesDef.setEnabled(enabled && chbDefault.isSelected());
        rbNoDef.setEnabled(enabled && chbDefault.isSelected());
        chbForced.setEnabled(enabled);
        rbYesForced.setEnabled(enabled && chbForced.isSelected());
        rbNoForced.setEnabled(enabled && chbForced.isSelected());
        chbName.setEnabled(enabled);
        txtName.setEnabled(enabled && chbName.isSelected());
        chbNumb.setEnabled(enabled);
        lblNumbStart.setEnabled(enabled && chbNumb.isSelected());
        txtNumbStart.setEnabled(enabled && chbNumb.isSelected());
        lblNumbPad.setEnabled(enabled && chbNumb.isSelected());
        txtNumbPad.setEnabled(enabled && chbNumb.isSelected());
        lblNumbExplain.setEnabled(enabled && chbNumb.isSelected());
        chbLang.setEnabled(enabled);
        cbLang.setEnabled(enabled && chbLang.isSelected());
        chbExtraCmd.setEnabled(enabled);
        txtExtraCmd.setEnabled(enabled && chbExtraCmd.isSelected());
    }

    /**
     * Builds the command line arguments for this track.
     * IMP-04: Part of the Command pattern — each TrackControls knows how to
     * build its own CLI arguments.
     *
     * @param trackIndex The 1-based index for the track prefix (v1, a1, s1, etc.)
     * @param fileIndex  The index of the current file being processed.
     * @param fileName   The filename without extension for {file_name}
     *                   substitution.
     * @return A two-element array: [0]=display command, [1]=options command.
     *         Both are empty strings if no edits are selected.
     */
    public String[] buildCommandLine(int trackIndex, int fileIndex, String fileName) {
        if (!chbEdit.isSelected()) {
            return new String[] { "", "" };
        }

        StringBuilder sbCmd = new StringBuilder();
        StringBuilder sbOpt = new StringBuilder();
        int editCount = 0;

        // Java 21: switch expression
        String prefix = switch (type) {
            case VIDEO -> "v";
            case AUDIO -> "a";
            case SUBTITLE -> "s";
        };

        sbCmd.append(" --edit track:").append(prefix).append(trackIndex);
        sbOpt.append(" --edit track:").append(prefix).append(trackIndex);

        if (chbEnable.isSelected()) {
            String val = rbYesEnable.isSelected() ? "1" : "0";
            sbCmd.append(" --set flag-enabled=").append(val);
            sbOpt.append(" --set flag-enabled=").append(val);
            editCount++;
        }

        if (chbDefault.isSelected()) {
            String val = rbYesDef.isSelected() ? "1" : "0";
            sbCmd.append(" --set flag-default=").append(val);
            sbOpt.append(" --set flag-default=").append(val);
            editCount++;
        }

        if (chbForced.isSelected()) {
            String val = rbYesForced.isSelected() ? "1" : "0";
            sbCmd.append(" --set flag-forced=").append(val);
            sbOpt.append(" --set flag-forced=").append(val);
            editCount++;
        }

        if (chbName.isSelected()) {
            sbCmd.append(" --set name=\"").append(Utils.escapeQuotes(txtName.getText())).append("\"");
            sbOpt.append(" --set name=\"").append(Utils.escapeName(txtName.getText())).append("\"");
            editCount++;
        }

        if (chbLang.isSelected()) {
            String curLangCode = new MkvStrings().getLangCodeList().get(cbLang.getSelectedIndex());
            sbCmd.append(" --set language=\"").append(curLangCode).append("\"");
            sbOpt.append(" --set language=\"").append(curLangCode).append("\"");
            editCount++;
        }

        if (chbExtraCmd.isSelected() && !txtExtraCmd.getText().trim().isEmpty()) {
            sbCmd.append(" ").append(txtExtraCmd.getText());
            sbOpt.append(" ").append(Utils.escapeBackslashes(txtExtraCmd.getText()));
            editCount++;
        }

        if (editCount == 0) {
            return new String[] { "", "" };
        }

        return new String[] { sbCmd.toString(), sbOpt.toString() };
    }
}
