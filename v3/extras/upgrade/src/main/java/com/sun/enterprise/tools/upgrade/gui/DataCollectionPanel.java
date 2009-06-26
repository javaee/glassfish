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

/**
 *
 * @author  prakash
 */
import com.sun.enterprise.tools.upgrade.gui.util.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.common.Credentials;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DataCollectionPanel extends JPanel {
	
	private JLabel contentLabel;
	private JLabel sourceDirLabel;
	private JLabel destDirLabel;
	private JTextField sourceDirTextField;
	private JTextField destDirTextField;
	private JButton sourceDirectoryBrowseButton;
	private JButton destDirBrowseButton;
	private JPanel containerPanel;
	private JTextField adminUserTextField;
	private JPasswordField adminPWTextField;
	private JPasswordField masterPWTextField;
	private JFileChooser fileChooser = null;
	private java.util.Vector dialogListeners = new java.util.Vector();
	
	//private utility fields
	private CommonInfoModel commonInfoModel = CommonInfoModel.getInstance();
	private StringManager stringManager = StringManager.getManager(DataCollectionPanel.class);
	
	/**
	 * Data Collection Panel Constructor
	 */
	public DataCollectionPanel(CommonInfoModel coInfoModel) {
		this.commonInfoModel = coInfoModel;
		initialize();
	}
	
	public DataCollectionPanel() {
		initialize();
	}
	
	/**
	 * Method to get source directory from GUI panel
	 */
	public String getSourceDirectoryPath(){
		return this.sourceDirTextField.getText();
	}
	
	/**
	 * Method to get destination directory from GUI panel
	 */
	public String getDestinationDirectoryPath(){
		return this.destDirTextField.getText();
	}
	
	/**
	 * Method to get admin user name from GUI panel
	 */
	public String getAdminUserName(){
		return this.adminUserTextField.getText();
	}
	
	/**
	 * Method to get admin password from GUI panel
	 */
	public String getAdminPassword(){
		return new String(this.adminPWTextField.getPassword());
	}
	
	/**
	 * Method to get master password from GUI panel
	 */
	public String getMasterPassword(){
		return new String(this.masterPWTextField.getPassword());
	}
	
	public void addDialogListener(DialogListener listener){
		this.dialogListeners.addElement(listener);
	}
	
	public void removeDialogListener(DialogListener listener){
		this.dialogListeners.removeElement(listener);
	}
	
	/**
	 * Method to initialize the components and set action listeners.
	 * Also to set the values in the different text boxes.
	 */
	private void initialize(){
		initComponents();
		this.setLayout(new BorderLayout());
        HeaderPanel headerPanel = new HeaderPanel(
            stringManager.getString("upgrade.gui.detailspanel.headerPanel"));
        headerPanel.setInsets(new Insets(12, 10, 12, 10));
        add(headerPanel, BorderLayout.NORTH);
        add(containerPanel, BorderLayout.CENTER);

		//Add action listener for source browse button
		sourceDirectoryBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sourceActionPerformed(evt);
			}
		});
		
		//Add action listenener for target browse button
		destDirBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				destActionPerformed(evt);
			}
		});
		
		//Set file selection mode
		getfileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		KeyAdapter keyAdapter = new KeyAdapter(){
            @Override
			public void keyReleased(KeyEvent ke){
				processDialogEvent();
			}
		};
				
		//Set values in the source and target text boxes
		sourceDirTextField.addKeyListener(keyAdapter);
		destDirTextField.addKeyListener(keyAdapter);
		String sourceDir=commonInfoModel.getSource().getInstallDir();
		String targetDir = commonInfoModel.getTarget().getInstallDir();
		if(sourceDir != null && !(sourceDir.equals("")))
			sourceDirTextField.setText(sourceDir);
		if(targetDir != null && !(targetDir.equals("")))
			destDirTextField.setText(targetDir);
		
		//Set values in the adminuser and password text boxes
		Credentials tmpC = commonInfoModel.getSource().getDomainCredentials();
		String adminUserName = tmpC.getAdminUserName();
		String adminPassword = tmpC.getAdminPassword();
		adminUserTextField.addKeyListener(keyAdapter);
		adminPWTextField.addKeyListener(keyAdapter);	
		
		if(adminUserName != null && !(adminUserName.equals("")))
			adminUserTextField.setText(adminUserName);
		if(adminPassword != null && !(adminPassword.equals("")))
			adminPWTextField.setText(adminPassword);
		
		//Set value in the master password text box
			String masterPassword = tmpC.getMasterPassword();
			masterPWTextField.addKeyListener(keyAdapter);			
				
			if(masterPassword != null && !(masterPassword.equals(""))
				&& !(masterPassword.equals(CLIConstants.DEFAULT_MASTER_PASSWORD)))
				masterPWTextField.setText(masterPassword);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		GridBagConstraints gridBagConstraints;
		contentLabel = new JLabel();
		sourceDirLabel = new JLabel();
		sourceDirTextField = new JTextField();
		sourceDirectoryBrowseButton = new JButton();
		destDirLabel = new JLabel();
		destDirTextField = new JTextField();
		destDirBrowseButton = new JButton();
		containerPanel = new JPanel();
		
		containerPanel.setLayout(new GridBagLayout());
		
		contentLabel.setText(stringManager.getString("upgrade.gui.detailspanel.contentLable"));
        contentLabel.setForeground(Color.BLUE);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 5, 0);
		gridBagConstraints.weightx = 1.0;
		containerPanel.add(contentLabel, gridBagConstraints);
		
		sourceDirLabel.setText(stringManager.getString("upgrade.gui.detailspanel.sourceDirLabel"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 10, 3, 10);
		gridBagConstraints.weightx = 1.0;
		containerPanel.add(sourceDirLabel, gridBagConstraints);
		
		sourceDirTextField.setToolTipText(stringManager.getString("upgrade.gui.detailspanel.sourceHelpLabel"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 10, 5);
		gridBagConstraints.weightx = 1.0;
		containerPanel.add(sourceDirTextField, gridBagConstraints);
		
		sourceDirectoryBrowseButton.setText(stringManager.getString("upgrade.gui.detailspanel.browseButtonText"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.insets = new Insets(0, 5, 10, 10);
		containerPanel.add(sourceDirectoryBrowseButton, gridBagConstraints);
		
		destDirLabel.setText(stringManager.getString("upgrade.gui.detailspanel.targetDirLabel"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 3, 10);
		gridBagConstraints.weightx = 1.0;
		containerPanel.add(destDirLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 10, 5);
		gridBagConstraints.weightx = 1.0;
		containerPanel.add(destDirTextField, gridBagConstraints);
		
		destDirBrowseButton.setText(stringManager.getString("upgrade.gui.detailspanel.browseButtonText"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.insets = new Insets(0, 5, 10, 5);
		containerPanel.add(destDirBrowseButton, gridBagConstraints);
	
		JPanel adminUserPwPanel = getAdminUserPasswordPanel();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 6;
		gridBagConstraints.gridheight = 1; gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 0, 3, 10);
		gridBagConstraints.weightx = 1.0; gridBagConstraints.weighty = 0.0;
		containerPanel.add(adminUserPwPanel, gridBagConstraints);	
	}
	
	private JPanel getAdminUserPasswordPanel(){
		JPanel panel = new JPanel();
		GridBagConstraints gridBagConstraints;
		JLabel admiUserLabel = new JLabel();
		JLabel admiPWLabel = new JLabel();
		adminUserTextField = new JTextField();
		adminPWTextField = new JPasswordField();
		
		panel.setLayout(new GridBagLayout());
		
		admiUserLabel.setText(stringManager.getString("upgrade.gui.detailspanel.adminUserLabel"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 10, 3, 20);
		gridBagConstraints.weightx = 1.0;
		panel.add(admiUserLabel, gridBagConstraints);
		
		admiPWLabel.setText(stringManager.getString("upgrade.gui.detailspanel.adminPWLabel"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1; gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 20, 3, 10);
		gridBagConstraints.weightx = 1.0;
		panel.add(admiPWLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 5, 20);
		gridBagConstraints.weightx = 1.0;
		panel.add(adminUserTextField, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1; gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 20, 5, 10);
		gridBagConstraints.weightx = 1.0;
		panel.add(adminPWTextField, gridBagConstraints);
		
        JLabel masterPWLabel = new JLabel();
        masterPWTextField = new JPasswordField();
        masterPWLabel.setText(stringManager.getString(
            "upgrade.gui.detailspanel.masterPWLabel"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(10, 10, 3, 5);
        gridBagConstraints.weightx = 1.0;
        panel.add(masterPWLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 10, 5);
        gridBagConstraints.weightx = 1.0;
        panel.add(masterPWTextField, gridBagConstraints);

        JPanel dummyPanel = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 20, 10, 5);
        gridBagConstraints.weightx = 1.0;
        panel.add(dummyPanel, gridBagConstraints);
		
		return panel;
	}
	
	private void sourceActionPerformed(ActionEvent evt){
		getfileChooser().setCurrentDirectory(new java.io.File(sourceDirTextField.getText()));
		int returnedValue = getfileChooser().showOpenDialog(this);
		if(returnedValue == JFileChooser.APPROVE_OPTION){
			sourceDirTextField.setText(String.valueOf(getfileChooser().getSelectedFile()));
			this.processDialogEvent();
		}
	}
	
	private void destActionPerformed(ActionEvent evt){
		getfileChooser().setCurrentDirectory(new java.io.File(destDirTextField.getText()));
		int returnedValue = getfileChooser().showOpenDialog(this);
		if(returnedValue == JFileChooser.APPROVE_OPTION){
			destDirTextField.setText(String.valueOf(getfileChooser().getSelectedFile()));
			this.processDialogEvent();
		}
	}
	
	private JFileChooser getfileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setName("fileChooser");
			fileChooser.setBounds(668, 49, 500, 300);
			fileChooser.setDialogTitle(stringManager.getString(
                "upgrade.gui.detailspanel.fileChooseTitle"));
			
		}
		return fileChooser;
	}
	
	private void processDialogEvent(){
        DialogEvent de = new DialogEvent(this, DialogEvent.CHANGE_ACTION);
        for (int i = 0; i < this.dialogListeners.size(); i++) {
            ((DialogListener) dialogListeners.elementAt(i)).dialogProcessed(de);
        }
	}
	
}
