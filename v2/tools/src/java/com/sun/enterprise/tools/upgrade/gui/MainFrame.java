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
    private CertificatePanel certificatePanel = null;
    private ClusterDetailsPanel clusterDetailsPanel = null;

    //other private utility fields
    private CommonInfoModel commonInfoModel;    
    private DomainPathSelectionDialog domainPathSelectionDialog;
    private Vector dialogListeners = new Vector();
    private StringManager stringManager = StringManager.getManager(LogService.UPGRADE_GUI_LOGGER);
    private static Logger logger = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();

    //static fields in GUI panel
    private static final int DETAILS_COLLECTION_PANEL = 1;
    private static final int CERTIFICATE_PANEL = 2;
    private static final int PROGRESS_PANEL = 3;
    private static final int CLUSTER_DETAILS_PANEL = 4;
    
    //fields that indicate the state of the upgrade tool
    private static int UPGRADE_STARTED = 1;
    private static int UPGRADE_FINISHED = 2;
    private static int DATA_COLLECTION_PENDING = 3;
    private static int CRTIFICATION_PENDING = 4;
    private static int CLUSTER_DETAILS_PENDING = 5;

    EventHandler eventHandler = new EventHandler();

    /**
     * MainFrame constructor
     */
    public MainFrame(CommonInfoModel coInfoModel) {
        super.setTitle(stringManager.getString(
                "upgrade.gui.mainframe.titleMessage", 
                coInfoModel.getTargetVersion()));
        this.commonInfoModel = coInfoModel;
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
            //imagePanel.setImage(java.awt.Toolkit.getDefaultToolkit().createImage("C:\\Sources\\ASUTws\\tools\\src\\java\\com\\sun\\enterprise\\tools\\upgrade\\gui\\Appserv_upgrade_wizard.gif"));
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
            MainFrame wiz = new MainFrame(new CommonInfoModel());
            wiz.show();
        }catch(java.lang.Throwable th){
            logger.severe(th.getMessage());
        }
    }
    
    public void processBackAction() {
        if(currentNavigationPanel == PROGRESS_PANEL){
            this.currentNavigationPanel = CERTIFICATE_PANEL ;
            CSH.setHelpIDString(gethelpButton(),"WIZARD_CERT");
        }else if(currentNavigationPanel == CERTIFICATE_PANEL){
            this.currentNavigationPanel = DETAILS_COLLECTION_PANEL ;
            CSH.setHelpIDString(gethelpButton(),"WIZARD_FIRST");
        }else if(currentNavigationPanel == CLUSTER_DETAILS_PANEL){
            if(commonInfoModel.getCertificateConversionFlag()){
                this.currentNavigationPanel = CERTIFICATE_PANEL;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_CERT");
            }else{
                this.currentNavigationPanel = DETAILS_COLLECTION_PANEL ;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_FIRST");
            }
        }else if(currentNavigationPanel == DETAILS_COLLECTION_PANEL){
            //
        }
        this.setCurrentNavigationPanel();
    }
    
    public void processHelpAction() {
        /*DialogEvent de = new DialogEvent(this, DialogEvent.HELP_ACTION);
        if(currentNavigationPanel == PROGRESS_PANEL){
            Utils.getHelpBroker().setCurrentID("top");
        }
        else if(currentNavigationPanel == CERTIFICATE_PANEL){
            Utils.getHelpBroker().setCurrentID("WIZARD_CERT");
        }
        else if(currentNavigationPanel == DETAILS_COLLECTION_PANEL){
            Utils.getHelpBroker().setCurrentID("WIZARD_FIRST");
        }
        / *
        for(int i=0 ; i<this.dialogListeners.size(); i++){
            ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
        }*/
    }
    
    public void processNextAction() {
        DialogEvent de = null;
        if(getnextButton().getActionCommand().equals("finish")){
            de = new DialogEvent(this, DialogEvent.FINISH_ACTION);
            this.dispose();
        }else if(currentNavigationPanel == DETAILS_COLLECTION_PANEL){
            //Validate inputs
            if(!this.validateInputs()) 
                return;
            
            //Set inputs in commonInfoModel
            String sourceDirPath = dataCollectionPanel.getSourceDirectoryPath();			
            String targetDirPath = dataCollectionPanel.getDestinationDirectoryPath();			
            String userName = dataCollectionPanel.getAdminUserName();
            String adminPasswd = dataCollectionPanel.getAdminPassword();
            String masterPwd = null;			
            boolean isUpgradeCertSelected = dataCollectionPanel.isUpgradeCertificatesSelected();
			
            commonInfoModel.setSourceInstallDir(sourceDirPath);
            commonInfoModel.setTargetDomainRoot(targetDirPath);
            commonInfoModel.setAdminUserName(userName);
            commonInfoModel.setAdminPassword(adminPasswd);
            commonInfoModel.setCertificateConversionFlag(isUpgradeCertSelected);
            
			// cr6585938  Both EE and PE require a masterpassword
			// EE's is provided by the user.  PE's is an internal
			// default value.
            if(!UpgradeConstants.EDITION_PE.equals(commonInfoModel.getSourceEdition())){
                //- The user must provide the master password 
				masterPwd = dataCollectionPanel.getMasterPassword(); 			
                commonInfoModel.setMasterPassword(masterPwd);
            } else {
				masterPwd = commonInfoModel.getDefaultMasterPassword();
				commonInfoModel.setMasterPassword(masterPwd);
			}

            //Build domain mapping for source
            commonInfoModel.enlistDomainsFromSource();
            
            //Validate admin credentials
            if(!UpgradeUtils.getUpgradeUtils(commonInfoModel).
                    validateUserDetails(userName,adminPasswd,masterPwd)){
                javax.swing.JOptionPane.showMessageDialog(this, 
                        stringManager.getString("upgrade.gui.mainframe.invalidUserDetailsMsg"),
                        stringManager.getString("upgrade.gui.mainframe.invalidUserDetailsTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;               
            }
            String VersionAndEdition = commonInfoModel.getSourceVersionAndEdition();
            //START - MIGHT REMOVE
            if(isUpgradeCertSelected){
                commonInfoModel.setCertificateConversionFlag(true);
                this.currentNavigationPanel = CERTIFICATE_PANEL;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_CERT");
            }else if(VersionAndEdition != null && VersionAndEdition.equals(UpgradeConstants.VERSION_AS7X_EE)){
                this.currentNavigationPanel = CLUSTER_DETAILS_PANEL;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_CLUSTER");
            }//END _ MIGHT REMOVE
            else{
                de = new DialogEvent(this, DialogEvent.UPGRADE_ACTION);
                this.currentNavigationPanel = PROGRESS_PANEL ;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_RESULT");
            }
            this.setCurrentNavigationPanel();
            
        } 
        
        //MIGHT KNOCK OFF
        else if(currentNavigationPanel == CERTIFICATE_PANEL){
            if(commonInfoModel.getSourceVersionAndEdition().equals(UpgradeConstants.VERSION_AS7X_EE)){
                this.currentNavigationPanel = CLUSTER_DETAILS_PANEL ;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_CLUSTER");
            }else{
                de = new DialogEvent(this, DialogEvent.UPGRADE_ACTION);
                this.currentNavigationPanel = PROGRESS_PANEL ;
                CSH.setHelpIDString(gethelpButton(),"WIZARD_RESULT");
            }
            this.setCurrentNavigationPanel();
        } //MIGHT KNOCK OFF
        else if(currentNavigationPanel == CLUSTER_DETAILS_PANEL){
            // gather data from cluster details panel and save it in common info.
            Vector clusterFileNames = this.clusterDetailsPanel.getClusterFilesTableModel().getClusterFiles();
            // Do error check here.  Check to see whether the entered list of files are valid or not.
            if((clusterFileNames != null) && (!clusterFileNames.isEmpty())){
                if(!commonInfoModel.processClinstnceConfFiles(clusterFileNames)){
                    javax.swing.JOptionPane.showMessageDialog(this, stringManager.getString("upgrade.gui.mainframe.clusterListInvalidMsg"),
                                                          stringManager.getString("upgrade.gui.mainframe.lusterListInvalidTitle"),
                                                          javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            de = new DialogEvent(this, DialogEvent.UPGRADE_ACTION);
            this.currentNavigationPanel = PROGRESS_PANEL ;
            this.setCurrentNavigationPanel();
            CSH.setHelpIDString(gethelpButton(),"WIZARD_RESULT");
        }else if(currentNavigationPanel == PROGRESS_PANEL){
            // should be a Finish button now.  Close the upgrade tool
            de = new DialogEvent(this, DialogEvent.FINISH_ACTION);
            this.dispose();
        }
        if(de != null){
            new UpgradeActionThread(dialogListeners, de).start();
        }
    }

    public void centerDialog(javax.swing.JDialog dialog) {
        java.awt.Rectangle bounds = this.getBounds();
        java.awt.Rectangle dialogLocation = new java.awt.Rectangle();
        dialogLocation.x = bounds.x+((bounds.width-dialog.getWidth())/2);
        dialogLocation.y = bounds.y+((bounds.height-dialog.getHeight())/2);
        dialogLocation.setSize(dialog.getWidth(),dialog.getHeight());
        dialog.setBounds(dialogLocation);
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
        }else if(this.CERTIFICATE_PANEL == currentNavigationPanel){
            getnavigationPanel().add(getCertificatePanel(), constraintsPanel);
            this.setFrameNavigationState(this.CRTIFICATION_PENDING);
            ((CertificatePanel)getCertificatePanel()).reInitializeAddDomainDialog();
        }else if(this.DETAILS_COLLECTION_PANEL == currentNavigationPanel){
            getnavigationPanel().add(getDataCollectionPanel(), constraintsPanel);
            this.setFrameNavigationState(this.DATA_COLLECTION_PENDING);
        }else if(this.CLUSTER_DETAILS_PANEL == currentNavigationPanel){
            getnavigationPanel().add(getClusterDetailsPanel(), constraintsPanel);
            this.setFrameNavigationState(this.CLUSTER_DETAILS_PENDING);
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
            if (e.getSource() == MainFrame.this.gethelpButton())
                processHelpAction();
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
        // START CR 6348866
        commonInfoModel.recover();       
        // END CR 6348866
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
    
    private java.util.List getUserDefinedSourceDomains(CommonInfoModel commonInfoModel){
        int returnedOption = javax.swing.JOptionPane.showConfirmDialog(this, stringManager.getString("upgrade.gui.mainframe.multiplDomainsMsg"),
						      stringManager.getString("upgrade.gui.mainframe.multiplDomainsTitle"),
						      javax.swing.JOptionPane.YES_NO_OPTION,javax.swing.JOptionPane.QUESTION_MESSAGE);
        if(returnedOption == javax.swing.JOptionPane.NO_OPTION)
            return null;
        // Bring up the dialog here.
        this.getDomainPathSelectionDialog().show();
        // The dialog is modal untill it is closed.
        if(this.getDomainPathSelectionDialog().USER_ACTION == DomainPathSelectionDialog.CANCEL_ACTION){
            return null;
        }else{
            return this.getDomainPathSelectionDialog().getDomainPathTableModel().getDomainPaths();
        }
    }
    
    private DomainPathSelectionDialog getDomainPathSelectionDialog(){
        if(this.domainPathSelectionDialog == null){
            domainPathSelectionDialog = new DomainPathSelectionDialog();
            centerDialog(domainPathSelectionDialog);
            domainPathSelectionDialog.addDialogListener(new DialogListener(){
                public void dialogProcessed(DialogEvent evt){
                    
                }
            });
        }
        return domainPathSelectionDialog;
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
        }else if(this.CRTIFICATION_PENDING == state){
            getnextButton().setEnabled(true);
            getbackButton().setEnabled(true);
        }else if(this.CLUSTER_DETAILS_PENDING == state){
            getnextButton().setEnabled(true);
            getbackButton().setEnabled(true);
         }else{
            // Data collection panel is shown at this point
            getcancelButton().setEnabled(true);
            this.setNextButtonStateForDataCollectionPanel();
            getnextButton().setActionCommand("next");
            getbackButton().setEnabled(false);
        }
    }
    
    private void setNextButtonStateForDataCollectionPanel(){
        String sourcePath = this.dataCollectionPanel.getSourceDirectoryPath();
        String destPath = this.dataCollectionPanel.getDestinationDirectoryPath();
        String adminUserName = this.dataCollectionPanel.getAdminUserName();
        String adminPassword = this.dataCollectionPanel.getAdminPassword();

        //Set source in commonInfoModel for further processing
        if(UpgradeUtils.getUpgradeUtils(commonInfoModel).isValidSourcePath(sourcePath)) {
            commonInfoModel.clearSourceAndTargetVersions();
            commonInfoModel.setSourceInstallDir(sourcePath);
        }

        //Enable/Disable Next button at any point after inputs
        if(UpgradeConstants.EDITION_PE.equals(commonInfoModel.getSourceEdition())){
            //Admin credential changes. Added for CR 6454007
            if((sourcePath == null) || (destPath == null) || 
                    (adminUserName == null) || (adminPassword == null) || 
                    ("".equals(adminUserName)) || ("".equals(adminPassword)) || 
                    ("".equals(sourcePath)) || ("".equals(destPath))){
                getnextButton().setEnabled(false);
            }else{
                getnextButton().setEnabled(true);
            }
        }else{
            //Admin credential changes. Added for CR 6454007
            String masterPassword = this.dataCollectionPanel.getMasterPassword();

            if((sourcePath == null) || (destPath == null) || 
                    (adminUserName == null) || (adminPassword == null) || 
                    (masterPassword == null) || 
                    ("".equals(adminUserName)) || ("".equals(adminPassword)) || 
                    ("".equals(sourcePath)) || ("".equals(destPath)) || 
                    ("".equals(masterPassword))){
                getnextButton().setEnabled(false);
            }else{
                getnextButton().setEnabled(true);
            }
        }
    }
    
    private void setNextButtonStateForClusterDetailsPanel(){
        if(this.clusterDetailsPanel.getClusterFilesTableModel().getClusterFiles().size() == 0){
            getnextButton().setEnabled(false);
        }else{
            getnextButton().setEnabled(true);
        }
    }
    
    /**
     * Method to validate source and target directory inputs on the GUI panel
     * Also checks whether the upgrade path is supported or not.
     */
    private boolean validateInputs() {
        
        //Collect inputs from the GUI panel
        String sourceDir = dataCollectionPanel.getSourceDirectoryPath();
        String destDir = dataCollectionPanel.getDestinationDirectoryPath();
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(commonInfoModel);            
        
        //Validate the source directory input
        if(!upgrUtils.isValidSourcePath(sourceDir)){
            // pop up error message
            javax.swing.JOptionPane.showMessageDialog(this, 
                    stringManager.getString("upgrade.gui.mainframe.invalidSourceMsg"), 
                    stringManager.getString("upgrade.gui.mainframe.invalidSourceTitle"), 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Validate the target directory input
        // START CR 6461833 
        if(!upgrUtils.isValidTargetPath(destDir)){
            // pop up error message
            javax.swing.JOptionPane.showMessageDialog(this, 
                    stringManager.getString("upgrade.gui.mainframe.invalidTargetMsg"),
                    stringManager.getString("upgrade.gui.mainframe.invalidTargetTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // END CR 6461833
        
        //Inputs are valid. Set values in commonInfoModel
        String targetInstallDir = System.getProperty("com.sun.aas.installRoot");
        commonInfoModel.setSourceInstallDir(sourceDir);
        commonInfoModel.setTargetInstallDir(targetInstallDir);
        
        //Check if the upgrade path is supported
        if(!commonInfoModel.isUpgradeSupported()){
            //Fix for CR 6376765
            javax.swing.JOptionPane.showMessageDialog(this, 
                    stringManager.getString("upgrade.gui.mainframe.versionNotSupportedMsg"),
                    stringManager.getString("upgrade.gui.mainframe.versionNotSupportedTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            commonInfoModel.clearSourceAndTargetVersions();
            return false;
        }//start CR 6429014
        else {
            //7X specific code - MIGHT KNOCK OFF    
            String src = (String) sourceDir;
            String targ = (String) targetInstallDir;
            if(commonInfoModel.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
                if(targ != null && !targ.equals("") && src!= null && !src.equals("")) {
                    if(targ.equals(src)) {
                        //Fix for CR 6376765
                        javax.swing.JOptionPane.showMessageDialog(this,
                                stringManager.getString("upgrade.gui.mainframe.versionNotSupportedMsg"),
                                stringManager.getString("upgrade.gui.mainframe.versionNotSupportedTitle"),
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        //End - Fix 6376765
                        commonInfoModel.clearSourceAndTargetVersions();
                        return false;
                    }
                }
            }
            //End - 7X
        }
        //end CR 6429014

        commonInfoModel.clearSourceAndTargetVersions();
        return true;
    }
    
    private javax.swing.JPanel getDataCollectionPanel() {
        if (dataCollectionPanel == null) {
            dataCollectionPanel = new DataCollectionPanel(commonInfoModel);
        }
        return dataCollectionPanel;
    }
    
    private javax.swing.JPanel getClusterDetailsPanel() {
        if (clusterDetailsPanel == null) {
            clusterDetailsPanel = new ClusterDetailsPanel();
            clusterDetailsPanel.addDialogListener(new DialogListener(){
            public void dialogProcessed(DialogEvent evt){
                //setNextButtonStateForClusterDetailsPanel();
            }
        });
        }
        return clusterDetailsPanel;
    }
    
    private javax.swing.JPanel getCertificatePanel() {
        if (certificatePanel == null) {
            certificatePanel = new CertificatePanel(this.commonInfoModel, this);
        }
        return certificatePanel;
    }

}
