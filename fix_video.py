import re, sys
p = 'd:/Mis proyectos/jmkvpropedit/src/io/github/brunorex/JMkvpropedit.java'
c = open(p, 'r', encoding='utf-8').read()
clean = r'''    private void addVideoTrack() {
        if (nVideo < MAX_STREAMS) {
            subPnlVideo[nVideo] = new JPanel();
            lyrdPnlVideo.add(subPnlVideo[nVideo], "subPnlVideo[" + nVideo + "]");
            GridBagLayout gbl_subPnlVideo = new GridBagLayout();
            gbl_subPnlVideo.columnWidths = new int[] { 0, 0, 0 };
            gbl_subPnlVideo.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            gbl_subPnlVideo.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
            gbl_subPnlVideo.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            subPnlVideo[nVideo].setLayout(gbl_subPnlVideo);

            chbEditVideo[nVideo] = new JCheckBox("Edit this track:");
            GridBagConstraints gbc_chbEditVideo = new GridBagConstraints();
            gbc_chbEditVideo.insets = new Insets(0, 0, 10, 5);
            gbc_chbEditVideo.anchor = GridBagConstraints.WEST;
            gbc_chbEditVideo.gridx = 0;
            gbc_chbEditVideo.gridy = 0;
            subPnlVideo[nVideo].add(chbEditVideo[nVideo], gbc_chbEditVideo);

            chbEnableVideo[nVideo] = new JCheckBox("Enable track:");
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

            rbYesEnableVideo[nVideo] = new JRadioButton("Yes");
            rbYesEnableVideo[nVideo].setEnabled(false);
            rbYesEnableVideo[nVideo].setSelected(true);
            pnlEnableControlsVideo.add(rbYesEnableVideo[nVideo]);

            rbNoEnableVideo[nVideo] = new JRadioButton("No");
            rbNoEnableVideo[nVideo].setEnabled(false);
            pnlEnableControlsVideo.add(rbNoEnableVideo[nVideo]);

            bgRbEnableVideo[nVideo] = new ButtonGroup();
            bgRbEnableVideo[nVideo].add(rbYesEnableVideo[nVideo]);
            bgRbEnableVideo[nVideo].add(rbNoEnableVideo[nVideo]);

            chbDefaultVideo[nVideo] = new JCheckBox("Default track:");
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

            rbYesDefVideo[nVideo] = new JRadioButton("Yes");
            rbYesDefVideo[nVideo].setEnabled(false);
            rbYesDefVideo[nVideo].setSelected(true);
            pnlDefControlsVideo.add(rbYesDefVideo[nVideo]);

            rbNoDefVideo[nVideo] = new JRadioButton("No");
            rbNoDefVideo[nVideo].setEnabled(false);
            pnlDefControlsVideo.add(rbNoDefVideo[nVideo]);

            bgRbDefVideo[nVideo] = new ButtonGroup();
            bgRbDefVideo[nVideo].add(rbYesDefVideo[nVideo]);
            bgRbDefVideo[nVideo].add(rbNoDefVideo[nVideo]);

            chbForcedVideo[nVideo] = new JCheckBox("Forced track:");
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

            rbYesForcedVideo[nVideo] = new JRadioButton("Yes");
            rbYesForcedVideo[nVideo].setEnabled(false);
            rbYesForcedVideo[nVideo].setSelected(true);
            pnlForControlsVideo.add(rbYesForcedVideo[nVideo]);

            rbNoForcedVideo[nVideo] = new JRadioButton("No");
            rbNoForcedVideo[nVideo].setEnabled(false);
            pnlForControlsVideo.add(rbNoForcedVideo[nVideo]);

            bgRbForcedVideo[nVideo] = new ButtonGroup();
            bgRbForcedVideo[nVideo].add(rbYesForcedVideo[nVideo]);
            bgRbForcedVideo[nVideo].add(rbNoForcedVideo[nVideo]);

            chbNameVideo[nVideo] = new JCheckBox("Track name:");
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

            chbNumbVideo[nVideo] = new JCheckBox("Numbering:");
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

            lblNumbStartVideo[nVideo] = new JLabel("Start:");
            lblNumbStartVideo[nVideo].setEnabled(false);
            pnlNumbControlsVideo.add(lblNumbStartVideo[nVideo]);

            txtNumbStartVideo[nVideo] = new JTextField();
            txtNumbStartVideo[nVideo].setText("1");
            txtNumbStartVideo[nVideo].setEnabled(false);
            txtNumbStartVideo[nVideo].setColumns(3);
            pnlNumbControlsVideo.add(txtNumbStartVideo[nVideo]);

            lblNumbPadVideo[nVideo] = new JLabel("Padding:");
            lblNumbPadVideo[nVideo].setEnabled(false);
            pnlNumbControlsVideo.add(lblNumbPadVideo[nVideo]);

            txtNumbPadVideo[nVideo] = new JTextField();
            txtNumbPadVideo[nVideo].setText("1");
            txtNumbPadVideo[nVideo].setEnabled(false);
            txtNumbPadVideo[nVideo].setColumns(3);
            pnlNumbControlsVideo.add(txtNumbPadVideo[nVideo]);

            lblNumbExplainVideo[nVideo] = new JLabel("use {num} in the name");
            lblNumbExplainVideo[nVideo].setEnabled(false);
            pnlNumbControlsVideo.add(lblNumbExplainVideo[nVideo]);

            chbLangVideo[nVideo] = new JCheckBox("Language:");
            chbLangVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbLangVideo = new GridBagConstraints();
            gbc_chbLangVideo.insets = new Insets(0, 0, 5, 5);
            gbc_chbLangVideo.anchor = GridBagConstraints.WEST;
            gbc_chbLangVideo.gridx = 0;
            gbc_chbLangVideo.gridy = 6;
            subPnlVideo[nVideo].add(chbLangVideo[nVideo], gbc_chbLangVideo);

            cbLangVideo[nVideo] = new JComboBox<String>();
            cbLangVideo[nVideo].setModel(new DefaultComboBoxModel<String>(mkvStrings.getLanguageList()));
            cbLangVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_cbLangVideo = new GridBagConstraints();
            gbc_cbLangVideo.insets = new Insets(0, 0, 5, 0);
            gbc_cbLangVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_cbLangVideo.gridx = 1;
            gbc_cbLangVideo.gridy = 6;
            subPnlVideo[nVideo].add(cbLangVideo[nVideo], gbc_cbLangVideo);

            chbExtraCmdVideo[nVideo] = new JCheckBox("Extra command line:");
            chbExtraCmdVideo[nVideo].setEnabled(false);
            GridBagConstraints gbc_chbExtraCmdVideo = new GridBagConstraints();
            gbc_chbExtraCmdVideo.insets = new Insets(0, 0, 0, 5);
            gbc_chbExtraCmdVideo.anchor = GridBagConstraints.WEST;
            gbc_chbExtraCmdVideo.gridx = 0;
            gbc_chbExtraCmdVideo.gridy = 7;
            subPnlVideo[nVideo].add(chbExtraCmdVideo[nVideo], gbc_chbExtraCmdVideo);

            txtExtraCmdVideo[nVideo] = new JTextField();
            txtExtraCmdVideo[nVideo].setEnabled(false);
            txtExtraCmdVideo[nVideo].setColumns(10);
            GridBagConstraints gbc_txtExtraCmdVideo = new GridBagConstraints();
            gbc_txtExtraCmdVideo.fill = GridBagConstraints.HORIZONTAL;
            gbc_txtExtraCmdVideo.gridx = 1;
            gbc_txtExtraCmdVideo.gridy = 7;
            subPnlVideo[nVideo].add(txtExtraCmdVideo[nVideo], gbc_txtExtraCmdVideo);

            chbEditVideo[nVideo].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int curCbVideo = cbVideo.getSelectedIndex();
                    boolean state = chbDefaultVideo[curCbVideo].isEnabled();

                    chbEnableVideo[curCbVideo].setEnabled(!state);
                    chbDefaultVideo[curCbVideo].setEnabled(!state);
                    chbForcedVideo[curCbVideo].setEnabled(!state);
                    chbNameVideo[curCbVideo].setEnabled(!state);
                    chbLangVideo[curCbVideo].setEnabled(!state);
                    chbExtraCmdVideo[curCbVideo].setEnabled(!state);

                    if (txtNameVideo[curCbVideo].isEnabled() || chbNameVideo[curCbVideo].isSelected()) {
                        txtNameVideo[curCbVideo].setEnabled(!state);
                        chbNumbVideo[curCbVideo].setEnabled(!state);

                        if (chbNumbVideo[curCbVideo].isSelected()) {
                            lblNumbStartVideo[curCbVideo].setEnabled(!state);
                            txtNumbStartVideo[curCbVideo].setEnabled(!state);
                            lblNumbPadVideo[curCbVideo].setEnabled(!state);
                            txtNumbPadVideo[curCbVideo].setEnabled(!state);
                            lblNumbExplainVideo[curCbVideo].setEnabled(!state);
                        }
                    }

                    if (rbNoEnableVideo[curCbVideo].isEnabled() || chbEnableVideo[curCbVideo].isSelected()) {
                        rbNoEnableVideo[curCbVideo].setEnabled(!state);
                        rbYesEnableVideo[curCbVideo].setEnabled(!state);
                    }

                    if (rbNoDefVideo[curCbVideo].isEnabled() || chbDefaultVideo[curCbVideo].isSelected()) {
                        rbNoDefVideo[curCbVideo].setEnabled(!state);
                        rbYesDefVideo[curCbVideo].setEnabled(!state);
                    }

                    if (rbNoForcedVideo[curCbVideo].isEnabled() || chbForcedVideo[curCbVideo].isSelected()) {
                        rbNoForcedVideo[curCbVideo].setEnabled(!state);
                        rbYesForcedVideo[curCbVideo].setEnabled(!state);
                    }

                    if (cbLangVideo[curCbVideo].isEnabled() || chbLangVideo[curCbVideo].isSelected()) {
                        cbLangVideo[curCbVideo].setEnabled(!state);
                    }

                    if (txtExtraCmdVideo[curCbVideo].isEnabled() || chbExtraCmdVideo[curCbVideo].isSelected()) {
                        chbExtraCmdVideo[curCbVideo].setEnabled(!state);
                        txtExtraCmdVideo[curCbVideo].setEnabled(!state);
                    }
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

            cbVideo.addItem("Video Track " + (nVideo + 1));
            nVideo++;
        }
    }
'''
res = re.sub(r'(?s)private void addVideoTrack\(\) \{.*?cbVideo\.addItem\(\"Video Track \" \+ \(nVideo \+ 1\)\);\s*nVideo\+\+;\s*\}\s*\}', clean, c)
open(p, 'w', encoding='utf-8').write(res)
