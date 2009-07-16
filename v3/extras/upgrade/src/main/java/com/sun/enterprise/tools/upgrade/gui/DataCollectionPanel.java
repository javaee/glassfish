/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.tools.upgrade.gui;

import com.sun.enterprise.tools.upgrade.common.CLIConstants;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.common.Credentials;
import com.sun.enterprise.util.i18n.StringManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * JPanel used to collect information from the user needed
 * to perform the upgrade. After the main panel has been initialized,
 * it will call into this panel to check the state of the required
 * text fields.
 *
 * @author Bobby Bissett
 */
public class DataCollectionPanel extends javax.swing.JPanel {

    private static final StringManager stringManager =
        StringManager.getManager(MainFrame.class);
    private static final CommonInfoModel commonInfoModel =
        CommonInfoModel.getInstance();
    
    // putting this in one place in case formatting needs to change
    private static final String REQUIRED_FIELD_CHAR =
        stringManager.getString("upgrade.gui.detailspanel.requiredField.char");
    private static final String SOURCE_LABEL_TEXT = String.format(
        "%s %s", REQUIRED_FIELD_CHAR,
        stringManager.getString("upgrade.gui.detailspanel.sourceDirLabel"));
    private static final String TARGET_LABEL_TEXT = String.format(
        "%s %s", REQUIRED_FIELD_CHAR,
        stringManager.getString("upgrade.gui.detailspanel.targetDirLabel"));
    private static final String REQUIRED_TEXT_LABEL = String.format(
        "(%s %s)", REQUIRED_FIELD_CHAR,
        stringManager.getString("upgrade.gui.detailspanel.requiredField.text"));
    private final MainFrame mainFrame;
    private final JFileChooser chooser;

    /**
     * Creates new form DataCollectionPanel. Instance of MainPanel is
     * passed in so that this panel can set the state of the 'next'
     * button to continue.
     */
    public DataCollectionPanel(MainFrame mainFrame) {
        initComponents();

        this.mainFrame = mainFrame;

        // listen for key presses to enable "next" button
        KeyAdapter dirTextKeyAdapter = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent ke) {
                processDirTextKeyReleased();
            }
        };
        sourceTextField.addKeyListener(dirTextKeyAdapter);
        targetTextField.addKeyListener(dirTextKeyAdapter);

        // set initial values for source/target text fields
        String tempStr = commonInfoModel.getSource().getInstallDir();
        if (tempStr != null) {
            sourceTextField.setText(tempStr);
        }
        tempStr = commonInfoModel.getTarget().getInstallDir();
        if (tempStr != null) {
            targetTextField.setText(tempStr);
        }

        // set initial values for credentials
        // todo: why do we check for default only in master pw case?
        Credentials cred = commonInfoModel.getSource().getDomainCredentials();
        if (cred.getAdminUserName() != null) {
            adminUserTextField.setText(cred.getAdminUserName());
        }
        if (cred.getAdminPassword() != null) {
            adminPWField.setText(cred.getAdminPassword());
        }
        String masPW = cred.getMasterPassword();
        if (masPW != null &&
            !CLIConstants.DEFAULT_MASTER_PASSWORD.equals(masPW)) {
            masterPWField.setText(cred.getMasterPassword());
        }

        // add listeners for source/target browse buttons
        sourceBrowseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                sourceBrowseButtonActionPerformed();
            }
        });
        targetBrowseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                targetBrowseButtonActionPerformed();
            }
        });

        // create file chooser for source and target directories
        chooser = new JFileChooser();
//        chooser.setBounds(668, 49, 500, 300);
        chooser.setDialogTitle(stringManager.getString(
            "upgrade.gui.detailspanel.fileChooseTitle"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel headerLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JLabel contentLabel = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sourceTextField = new javax.swing.JTextField();
        sourceBrowseButton = new javax.swing.JButton();
        javax.swing.JLabel targetLabel = new javax.swing.JLabel();
        targetTextField = new javax.swing.JTextField();
        targetBrowseButton = new javax.swing.JButton();
        javax.swing.JLabel adminUserLabel = new javax.swing.JLabel();
        adminUserTextField = new javax.swing.JTextField();
        javax.swing.JLabel adminPWLabel = new javax.swing.JLabel();
        adminPWField = new javax.swing.JPasswordField();
        javax.swing.JLabel masterPWLabel = new javax.swing.JLabel();
        masterPWField = new javax.swing.JPasswordField();
        javax.swing.JLabel reqTextLabel = new javax.swing.JLabel();

        headerLabel.setText(stringManager.getString("upgrade.gui.detailspanel.headerPanel"));

        contentLabel.setForeground(java.awt.Color.blue);
        contentLabel.setText(stringManager.getString("upgrade.gui.detailspanel.contentLabel"));

        sourceLabel.setLabelFor(sourceTextField);
        sourceLabel.setText(SOURCE_LABEL_TEXT);

        sourceTextField.setToolTipText(stringManager.getString("upgrade.gui.detailspanel.sourceHelpLabel"));

        sourceBrowseButton.setText(stringManager.getString("upgrade.gui.detailspanel.browseButtonText"));

        targetLabel.setLabelFor(targetTextField);
        targetLabel.setText(TARGET_LABEL_TEXT);

        targetBrowseButton.setText(stringManager.getString("upgrade.gui.detailspanel.browseButtonText"));

        adminUserLabel.setLabelFor(adminUserTextField);
        adminUserLabel.setText(stringManager.getString("upgrade.gui.detailspanel.adminUserLabel"));

        adminUserTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminUserTextFieldActionPerformed(evt);
            }
        });

        adminPWLabel.setLabelFor(adminPWField);
        adminPWLabel.setText(stringManager.getString("upgrade.gui.detailspanel.adminPWLabel"));

        masterPWLabel.setLabelFor(adminPWField);
        masterPWLabel.setText(stringManager.getString("upgrade.gui.detailspanel.masterPWLabel"));

        reqTextLabel.setText(REQUIRED_TEXT_LABEL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentLabel)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addComponent(headerLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceLabel)
                            .addComponent(targetLabel)
                            .addComponent(targetTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .addComponent(sourceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(targetBrowseButton)
                            .addComponent(sourceBrowseButton)))
                    .addComponent(masterPWLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(adminUserLabel)
                            .addComponent(adminUserTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(masterPWField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reqTextLabel)
                            .addComponent(adminPWLabel)
                            .addComponent(adminPWField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {sourceBrowseButton, targetBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(contentLabel)
                .addGap(18, 18, 18)
                .addComponent(sourceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(targetLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminUserLabel)
                    .addComponent(adminPWLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(adminPWField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(masterPWLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(masterPWField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reqTextLabel))
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void adminUserTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminUserTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adminUserTextFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField adminPWField;
    private javax.swing.JTextField adminUserTextField;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPasswordField masterPWField;
    private javax.swing.JButton sourceBrowseButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JTextField sourceTextField;
    private javax.swing.JButton targetBrowseButton;
    private javax.swing.JTextField targetTextField;
    // End of variables declaration//GEN-END:variables

    /*
     * After the main frame is initialized, it calls
     * this method so that the data collection panel
     * can check if the user can continue (e.g., click
     * the 'next' button) or not. This method simply
     * calls the processDirTextKeyReleased method,
     * an implementation detail.
     */
    void checkState() {
        processDirTextKeyReleased();
    }

    /*** Getters for data fields ***/
    public String getSourceDirPath() {
        return sourceTextField.getText();
    }
    
    public String getDestinationDirPath() {
        return targetTextField.getText();
    }

    public String getAdminUserName() {
        return adminUserTextField.getText();
    }

    public String getAdminPassword() {
        return adminPWField.getPassword().toString();
    }

    public String getMasterPassword() {
        return masterPWField.getPassword().toString();
    }
    /*** end getters ***/

    /*
     * Check required fields and update "next" button in main
     * frame. No need to check state of main frame since the fields
     * that fire this event are only present in data collection panel.
     */
    private void processDirTextKeyReleased() {
        if (sourceTextField.getText().isEmpty() ||
            targetTextField.getText().isEmpty()) {
            mainFrame.allowContinue(false);
        } else {
            mainFrame.allowContinue(true);
        }
    }

    /*
     * Show file chooser dialog and update status. Setting
     * current dir here to null in case it has already
     * been set by targetBrowseButtonActionPerformed.
     */
    private void sourceBrowseButtonActionPerformed() {
        chooser.setCurrentDirectory(null);
        int retVal = chooser.showOpenDialog(mainFrame);
        if (JFileChooser.APPROVE_OPTION == retVal) {
            sourceTextField.setText(chooser.getSelectedFile().getPath());
            checkState();
        }
    }

    /*
     * Show file chooser dialog and update status. This is the
     * same as sourceBrowseButtonActionPerformed, except that the
     * target directory text field has a default value so we will
     * use it as the starting directory in the dialog.
     */
    private void targetBrowseButtonActionPerformed() {
        chooser.setCurrentDirectory(new File(targetTextField.getText()));
        int retVal = chooser.showOpenDialog(mainFrame);
        if (JFileChooser.APPROVE_OPTION == retVal) {
            targetTextField.setText(chooser.getSelectedFile().getPath());
            checkState();
        }
    }
}
