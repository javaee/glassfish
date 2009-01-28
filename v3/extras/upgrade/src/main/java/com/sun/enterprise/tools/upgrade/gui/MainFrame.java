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

import java.util.*;
import javax.swing.*;
import java.io.*;
import com.sun.enterprise.tools.upgrade.gui.util.DialogEvent;
import com.sun.enterprise.tools.upgrade.gui.util.DialogListener;
import com.sun.enterprise.tools.upgrade.gui.util.Utils;
import java.util.*;
import com.sun.enterprise.tools.upgrade.common.*;
import java.util.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_source;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_target;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminuser;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_adminpassword;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_masterpassword;

import javax.help.*;
/**
 * Insert the type's description here.
 *
 * @author:  Prakash Aradhya
 */
public class MainFrame extends JFrame implements LogMessageListener, 
        UpgradeUpdateListener{

    //private fields in GUI panel
    private JButton backButton = null;
    private JPanel buttonsPanel = null;
    private JButton cancelButton = null;
    private JButton helpButton = null;
    private JPanel JDialogContentPane = null;
    private JButton nextButton = null;
    private JPanel navigationPanel = null;
    private int currentNavigationPanel = DETAILS_COLLECTION_PANEL;
    private ProgressPanel progressPanel = null;
    private DataCollectionPanel dataCollectionPanel = null;

    //other private utility fields
    private CommonInfoModel commonInfoModel = CommonInfoModel.getInstance();
    private Vector dialogListeners = new Vector();
    private StringManager stringManager = StringManager.getManager(MainFrame.class);
    private static Logger logger = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();

    //static fields in GUI panel
    private static final int DETAILS_COLLECTION_PANEL = 1;
    private static final int PROGRESS_PANEL = 3;
    
    //fields that indicate the state of the upgrade tool
    private static int UPGRADE_STARTED = 1;
    private static int UPGRADE_FINISHED = 2;
    private static int DATA_COLLECTION_PENDING = 3;

    EventHandler eventHandler = new EventHandler();

    /**
     * MainFrame constructor
     */
	public MainFrame() {
        super.setTitle(stringManager.getString("upgrade.gui.mainframe.titleMessage", 
                this.commonInfoModel.getTarget().getVersion()));
        initialize();
    }
    
    public void addDialogListener(DialogListener listener){
        this.dialogListeners.addElement(listener);
    }
    
    public void removeDialogListener(DialogListener listener){
        this.dialogListeners.removeElement(listener);
    }

    /**
     * Return the JDialogContentPane property value.
     * @return javax.swing.JPanel
     */
    public javax.swing.JPanel getJDialogContentPane() {
        if (JDialogContentPane == null) {
            JDialogContentPane = new javax.swing.JPanel();
            JDialogContentPane.setName("JDialogContentPane");
            JDialogContentPane.setLayout(new java.awt.GridBagLayout());

            java.awt.GridBagConstraints imageconstraintsPanel = new java.awt.GridBagConstraints();
            imageconstraintsPanel.gridx = 0; imageconstraintsPanel.gridy = 0;
            imageconstraintsPanel.fill = java.awt.GridBagConstraints.BOTH;
            imageconstraintsPanel.weightx = 0;
            imageconstraintsPanel.weighty = 0;
            ImagePanel imagePanel = new ImagePanel(new java.awt.Insets(5,10,5,10));
            java.awt.Image image = this.getUpgradeToolImage();
            imagePanel.setImage(image);
            getJDialogContentPane().add(imagePanel, imageconstraintsPanel);

            java.awt.GridBagConstraints constraintsnavigationPanel = new java.awt.GridBagConstraints();
            constraintsnavigationPanel.gridx = 1; constraintsnavigationPanel.gridy = 0;
            constraintsnavigationPanel.fill = java.awt.GridBagConstraints.BOTH;
            constraintsnavigationPanel.weightx = 1.0;
            constraintsnavigationPanel.weighty = 1.0;
            getJDialogContentPane().add(getnavigationPanel(), constraintsnavigationPanel);

            java.awt.GridBagConstraints constraintsSeparator = new java.awt.GridBagConstraints();
            constraintsSeparator.gridx = 0; constraintsSeparator.gridy = 1;
            constraintsSeparator.gridwidth = 2; constraintsSeparator.gridheight = 1;
            constraintsSeparator.fill = java.awt.GridBagConstraints.HORIZONTAL;
            constraintsSeparator.weightx = 1.0;
            constraintsSeparator.insets = new java.awt.Insets(5, 0, 0, 0);
            javax.swing.JSeparator separatorPanel = new javax.swing.JSeparator();
            getJDialogContentPane().add(separatorPanel, constraintsSeparator);

            java.awt.GridBagConstraints constraintsbuttonsPanel = new java.awt.GridBagConstraints();
            constraintsbuttonsPanel.gridx = 0; constraintsbuttonsPanel.gridy = 2;
            constraintsbuttonsPanel.gridwidth = 2; constraintsbuttonsPanel.gridheight = 1;
            constraintsbuttonsPanel.fill = java.awt.GridBagConstraints.HORIZONTAL;
            constraintsbuttonsPanel.weightx = 1.0;
            constraintsbuttonsPanel.insets = new java.awt.Insets(5, 0, 0, 0);
            getJDialogContentPane().add(getbuttonsPanel(), constraintsbuttonsPanel);
        }
        return JDialogContentPane;
    }

    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args) {
        // Insert code to start the application here.
        try{
            MainFrame wiz = new MainFrame();
			wiz.setVisible(true);
        }catch(java.lang.Throwable th){
            logger.severe(th.getMessage());
        }
    }
    
    public void processBackAction() {
		if(currentNavigationPanel == DETAILS_COLLECTION_PANEL){
			//
		}
		this.setCurrentNavigationPanel();
	}
    
	public void processNextAction() {
		DialogEvent de = null;
		if(getnextButton().getActionCommand().equals("finish")){
			de = new DialogEvent(this, DialogEvent.FINISH_ACTION);
			this.dispose();
		}else if(currentNavigationPanel == DETAILS_COLLECTION_PANEL){
			//Validate inputs
			if (this.processArguments()){
				printArguments();
				de = new DialogEvent(this, DialogEvent.UPGRADE_ACTION);
				this.currentNavigationPanel = PROGRESS_PANEL ;
				CSH.setHelpIDString(gethelpButton(),"WIZARD_RESULT");
				this.setCurrentNavigationPanel();
			}else{
				return;
			}			
		} else if(currentNavigationPanel == PROGRESS_PANEL){
			// should be a Finish button now.  Close the upgrade tool
			de = new DialogEvent(this, DialogEvent.FINISH_ACTION);
			this.dispose();
		}
		
		if(de != null){
			new UpgradeActionThread(dialogListeners, de).start();
		}
	}
	 
    public void setCurrentNavigationPanel() {
        getnavigationPanel().removeAll();
        this.getJDialogContentPane().invalidate();
        java.awt.GridBagConstraints constraintsPanel = new java.awt.GridBagConstraints();
        constraintsPanel.gridx = 0; constraintsPanel.gridy = 0;
        constraintsPanel.fill = java.awt.GridBagConstraints.BOTH;
        constraintsPanel.weightx = 1.0;
        constraintsPanel.weighty = 1.0;
        if(this.PROGRESS_PANEL == currentNavigationPanel){
            getnavigationPanel().add(getProgressPanel(), constraintsPanel);
            this.setFrameNavigationState(this.UPGRADE_STARTED);
        }else if(this.DETAILS_COLLECTION_PANEL == currentNavigationPanel){
            getnavigationPanel().add(getDataCollectionPanel(), constraintsPanel);
            this.setFrameNavigationState(this.DATA_COLLECTION_PENDING);
        }
        this.getJDialogContentPane().validate();
        this.repaint();
    }
    
    public void logMessageReceived(LogMessageEvent evt) {
        // retrive message and show it in Process panel.
        if(this.progressPanel != null){
            this.progressPanel.updateLog(evt);
        }
    }

    public void upgradeProcessUpdate(UpgradeUpdateEvent evt) {
        if(this.progressPanel != null){
            this.progressPanel.updateProgress(evt);
            if(evt.getProgressState() == 100){
                this.setFrameNavigationState(this.UPGRADE_FINISHED);
            }
        }
    }
    
    class UpgradeActionThread extends Thread{
        private Vector dialogListeners;
        private DialogEvent de;
        public UpgradeActionThread(Vector listeners, DialogEvent d){
            this.dialogListeners = listeners;
            this.de = d;
        }
        public void run(){
            for(int i=0 ; i<this.dialogListeners.size(); i++){
                ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
            }
        }
    }
    
    class EventHandler implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (e.getSource() == MainFrame.this.getcancelButton())
                processCancelAction();
            if (e.getSource() == MainFrame.this.getbackButton())
                processBackAction();
            if (e.getSource() == MainFrame.this.getnextButton())
                processNextAction();
        }
    }
    

    private void processCancelAction() {
        int option = javax.swing.JOptionPane.showConfirmDialog(this,
        stringManager.getString("upgrade.gui.mainframe.exitMessage"),
        stringManager.getString("upgrade.gui.mainframe.exitMessageTitle"),
        javax.swing.JOptionPane.YES_NO_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (option == javax.swing.JOptionPane.NO_OPTION) {
            return;
        }
        logger.info("Before Recover Call");
        commonInfoModel.recover();
        DialogEvent de = new DialogEvent(this, DialogEvent.CANCEL_ACTION);
        for(int i=0 ; i<this.dialogListeners.size(); i++){
            ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
        }
        this.dispose();
    }
    
    /**
     * Return the backButton property value.
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getbackButton() {
        if (backButton == null) {
            backButton = new javax.swing.JButton();
            backButton.setName("backButton");
            backButton.setText(stringManager.getString("upgrade.gui.mainframe.backbutton"));
        }
        return backButton;
    }

    /**
     * Return the JPanel1 property value.
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getProgressPanel() {
        if (progressPanel == null) {
            progressPanel = new ProgressPanel();
        }
        return progressPanel;
    }


    /**
     * Return the buttonsPanel property value.
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getbuttonsPanel() {
        if (buttonsPanel == null) {
            buttonsPanel = new javax.swing.JPanel();
            buttonsPanel.setName("buttonsPanel");
            buttonsPanel.setLayout(new java.awt.GridBagLayout());
            JPanel placeHolderPanel1 = new JPanel();
            JPanel placeHolderPanel2 = new JPanel();
            placeHolderPanel1.add(getbackButton());
            placeHolderPanel1.add(getnextButton());
            placeHolderPanel2.add(getcancelButton());
            placeHolderPanel2.add(gethelpButton());

            java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
            constraints.gridx = 0; constraints.gridy = 0;
            constraints.anchor = java.awt.GridBagConstraints.WEST;
            constraints.gridwidth = 1; constraints.gridheight = 1;
            constraints.weightx = 1.0; constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            constraints.insets = new java.awt.Insets(5, 10, 10, 20);
            getbuttonsPanel().add(placeHolderPanel1, constraints);

            constraints = new java.awt.GridBagConstraints();
            constraints.gridx = 1; constraints.gridy = 0;
            constraints.anchor = java.awt.GridBagConstraints.EAST;
            constraints.gridwidth = 1; constraints.gridheight = 1;
            constraints.weightx = 1.0; constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            constraints.insets = new java.awt.Insets(5, 10, 10, 20);
            getbuttonsPanel().add(placeHolderPanel2, constraints);
        }
        return buttonsPanel;
    }
    
    /**
     * Return the cancelButton property value.
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getcancelButton() {
        if (cancelButton == null) {
            cancelButton = new javax.swing.JButton();
            cancelButton.setName("cancelButton");
            cancelButton.setText(stringManager.getString("upgrade.gui.mainframe.cancelbutton"));
        }
        return cancelButton;
    }
    
    /**
     * Return the helpButton property value.
     * @return javax.swing.JButton
     */
    private javax.swing.JButton gethelpButton() {
        if (helpButton == null) {
            helpButton = new javax.swing.JButton();
            helpButton.setName("helpButton");
            helpButton.setText(stringManager.getString("upgrade.gui.mainframe.helpbutton"));
            if(Utils.getHelpBroker() != null)
                Utils.getHelpBroker().enableHelpOnButton(helpButton, "WIZARD_FIRST", null);
        }
        return helpButton;
    }

    private java.awt.Image getUpgradeToolImage(){
        java.net.URL imageURL = ClassLoader.getSystemClassLoader().getResource("com/sun/enterprise/tools/upgrade/gui/Appserv_upgrade_wizard.gif");
        return (imageURL != null)? java.awt.Toolkit.getDefaultToolkit().getImage(imageURL) : null;
    }
    
    /**
     * Return the navigationPanel property value.
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getnavigationPanel() {
        if (navigationPanel == null) {
            navigationPanel = new javax.swing.JPanel();
            navigationPanel.setName("navigationPanel");
            navigationPanel.setLayout(new java.awt.GridBagLayout());

            java.awt.GridBagConstraints constraintsbeanNameBeanTypePanel = new java.awt.GridBagConstraints();
            constraintsbeanNameBeanTypePanel.gridx = 0; constraintsbeanNameBeanTypePanel.gridy = 0;
            constraintsbeanNameBeanTypePanel.fill = java.awt.GridBagConstraints.BOTH;
            constraintsbeanNameBeanTypePanel.weightx = 1.0;
            constraintsbeanNameBeanTypePanel.weighty = 1.0;
            getnavigationPanel().add(getDataCollectionPanel(), constraintsbeanNameBeanTypePanel);
        }
        return navigationPanel;
    }
    
    /**
     * Return the nextButton property value.
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getnextButton() {
        if (nextButton == null) {
            nextButton = new javax.swing.JButton();
            nextButton.setName("nextButton");
            nextButton.setText(stringManager.getString("upgrade.gui.mainframe.nextbutton"));
            nextButton.setActionCommand("next");
			nextButton.setEnabled(false);
        }
        return nextButton;
    }

    /**
     * Initializes connections
     * @exception java.lang.Exception The exception description.
     */
    private void initConnections() {
        getcancelButton().addActionListener(eventHandler);
        getbackButton().addActionListener(eventHandler);
        getnextButton().addActionListener(eventHandler);
        gethelpButton().addActionListener(eventHandler);
        dataCollectionPanel.addDialogListener(new DialogListener(){
            public void dialogProcessed(DialogEvent evt){
                setNextButtonStateForDataCollectionPanel();
            }
        });
        this.addWindowListener(new java.awt.event.WindowAdapter(){
            public void windowClosing(java.awt.event.WindowEvent e){
                processCancelAction();
            }
        });
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        setName("MainFrame");
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(725, 545);
        setContentPane(getJDialogContentPane());
        initConnections();
        this.setFrameNavigationState(this.DATA_COLLECTION_PENDING);
        this.setBounds(new java.awt.Rectangle(100,100,this.getWidth(),this.getHeight()));
    }
    
    
    private void setFrameNavigationState(int state){
        if(this.UPGRADE_FINISHED == state){
            getcancelButton().setEnabled(false);
            getnextButton().setEnabled(true);
            getnextButton().setActionCommand("finish");
            getnextButton().setText(stringManager.getString("upgrade.gui.mainframe.finishbutton"));
            getbackButton().setEnabled(false);
        }else if(this.UPGRADE_STARTED == state){
            getnextButton().setEnabled(false);
            getbackButton().setEnabled(false);
         }else{
            // Data collection panel is shown at this point
            getcancelButton().setEnabled(true);
			//- settings via cmd-line checked for on startup
            this.setNextButtonStateForDataCollectionPanel();
            getnextButton().setActionCommand("next");
            getbackButton().setEnabled(false);
        }
    }
    
	private void setNextButtonStateForDataCollectionPanel(){
		if (this.dataCollectionPanel.getMasterPassword().length() > 0 &&
			this.dataCollectionPanel.getAdminPassword().length() > 0 &&
			this.dataCollectionPanel.getAdminUserName().length() > 0 &&
			this.dataCollectionPanel.getSourceDirectoryPath().length() > 0 &&
			this.dataCollectionPanel.getDestinationDirectoryPath().length() > 0 ){
			getnextButton().setEnabled(true);
		}else {
			getnextButton().setEnabled(false);
		}
	}
	
	/**
     * Method to validate source and target directory inputs on the GUI panel
     * Also checks whether the upgrade path is supported or not.
     */
	private boolean processArguments(){
		ARG_source s = new ARG_source();
		s.setRawParameters(dataCollectionPanel.getSourceDirectoryPath());
		if (s.isValidParameter()){
			s.exec();
		} else{
			// pop up error message
			javax.swing.JOptionPane.showMessageDialog(this,
				stringManager.getString("upgrade.gui.mainframe.invalidSourceMsg"),
				stringManager.getString("upgrade.gui.mainframe.invalidSourceTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		ARG_target t = new ARG_target();
		t.setRawParameters(dataCollectionPanel.getDestinationDirectoryPath());
		if (t.isValidParameter()){
			t.exec();
		} else{
			// pop up error message
			javax.swing.JOptionPane.showMessageDialog(this,
				stringManager.getString("upgrade.gui.mainframe.invalidTargetMsg"),
				stringManager.getString("upgrade.gui.mainframe.invalidTargetTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(!commonInfoModel.isUpgradeSupported()){
			javax.swing.JOptionPane.showMessageDialog(this,
				stringManager.getString("upgrade.gui.mainframe.versionNotSupportedMsg"),
				stringManager.getString("upgrade.gui.mainframe.versionNotSupportedTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//- check credentials
		String userName = dataCollectionPanel.getAdminUserName();
		String adminPasswd = dataCollectionPanel.getAdminPassword();
		String masterPwd = dataCollectionPanel.getMasterPassword();	
		if(UpgradeUtils.getUpgradeUtils(commonInfoModel).
			validateUserDetails(userName,adminPasswd,masterPwd)){
			ARG_adminuser u = new ARG_adminuser();
			u.setRawParameters(userName);
			u.exec();
			ARG_adminpassword pswd = new ARG_adminpassword();
			pswd.setRawParameters(adminPasswd);
			pswd.exec();
			ARG_masterpassword masterpswd = new ARG_masterpassword();
			masterpswd.setRawParameters(masterPwd);
			masterpswd.exec();
		} else {
			javax.swing.JOptionPane.showMessageDialog(this,
				stringManager.getString("upgrade.gui.mainframe.invalidUserDetailsMsg"),
				stringManager.getString("upgrade.gui.mainframe.invalidUserDetailsTitle"),
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
    
    private javax.swing.JPanel getDataCollectionPanel() {
        if (dataCollectionPanel == null) {
            dataCollectionPanel = new DataCollectionPanel(commonInfoModel);
        }
        return dataCollectionPanel;
    }
	
	/**
	 * Print user input but do not reveal the passwords.
	 */
	private void printArguments(){
		Credentials c = commonInfoModel.getSource().getDomainCredentials();
		logger.info(UpgradeConstants.ASUPGRADE + " -s " + 
			commonInfoModel.getSource().getInstallDir() +
			"\t -t " + commonInfoModel.getTarget().getInstallDir() +
			"\t -a " + c.getAdminUserName() + "\t -w " + 
			c.getAdminPassword().replaceAll(".","*") +  "\t -m " + 
			c.getMasterPassword().replaceAll(".","*"));
	}
}
