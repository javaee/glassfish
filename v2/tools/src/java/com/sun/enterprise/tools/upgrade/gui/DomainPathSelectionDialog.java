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

/*
 * DomainPathSelectionDialog.java
 *
 * Created on September 17, 2004, 11:54 AM
 */

package com.sun.enterprise.tools.upgrade.gui;

/**
 *
 * @author  prakash
 */

import com.sun.enterprise.tools.upgrade.gui.util.*;
import java.util.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import javax.swing.*;
import java.util.*;
import java.awt.Insets;
import java.awt.GridBagConstraints;

public class DomainPathSelectionDialog extends javax.swing.JDialog {
    
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");
    private DomainPathTableModel domainPathTableModel= null;
    private JTable domainPathTable = null;
    private JScrollPane domainPathTableScrollPane = null;
    private JPanel domainPathsPanel = null;
    private JPanel addRemoveButtonsPanel = null;
    private JButton addDomainPathButton = null;
    private JButton removeDomainPathButton = null;
    
    private JFileChooser fileChooser = null;
    
    public static final int OK_ACTION = 2;
    public static final int CANCEL_ACTION = 1;
    public static final int HELP_ACTION = 3;
    public int USER_ACTION = 1;
    
    private java.util.Vector dialogListeners = new java.util.Vector();
    
    /** Creates a new instance of DomainPathSelectionDialog */
    public DomainPathSelectionDialog() {
        this.setTitle(stringManager.getString("upgrade.gui.domainPanel.dialogTitle"));
        this.initComponents();
        super.setModal(true);
    }
    
    private void initComponents(){
        this.getContentPane().setLayout(new java.awt.GridBagLayout());
        javax.swing.JSeparator separatorPanel = new javax.swing.JSeparator();
        
        this.addComponetWithConstraints(getDomainPathsPanel(), this.getContentPane(), 0, 0,1,1, GridBagConstraints.BOTH, new Insets(10, 0, 0, 5), 1.0,1.0);
        this.addComponetWithConstraints(separatorPanel, this.getContentPane(), 0, 1,1,1, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 1.0,0.0);
        this.addComponetWithConstraints(getButtonsPanel(), this.getContentPane(), 0, 2,1,1, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 1.0,0.0);
        getDomainPathTableScrollPane().setViewportView(getDomainPathTable());
        
        getDomainPathTable().setModel(this.getDomainPathTableModel());
        getDomainPathTable().createDefaultColumnsFromModel();
        
        this.setSize(500, 350);
    }
    private javax.swing.JPanel getDomainPathsPanel() {
        if (domainPathsPanel == null) {
            domainPathsPanel = new javax.swing.JPanel();
            domainPathsPanel.setName("domainPathsPanel");
            domainPathsPanel.setLayout(new java.awt.GridBagLayout());
            
            JLabel tableTitleLabel = new JLabel();
            tableTitleLabel.setText(stringManager.getString("upgrade.gui.domainPanel.tableTitleName"));
            this.addComponetWithConstraints(tableTitleLabel, domainPathsPanel, 0, 0,3,1, GridBagConstraints.NONE, new Insets(10, 10, 5, 0), 0.0,0.0);
            this.addComponetWithConstraints(getDomainPathTableScrollPane(), domainPathsPanel, 0, 1,2,1, GridBagConstraints.BOTH, new Insets(5, 10, 5, 0), 1.0,1.0);
            this.addComponetWithConstraints(getaddRemoveButtonsPanel(), domainPathsPanel, 2, 1,1,1, GridBagConstraints.VERTICAL, new Insets(5, 0, 5, 0), 0.0,1.0);
        }
        return domainPathsPanel;
    }
    private javax.swing.JTable getDomainPathTable() {
        if (domainPathTable == null) {
            domainPathTable = new javax.swing.JTable();
            domainPathTable.setName("domainPathTable");
            getDomainPathTableScrollPane().setColumnHeaderView(domainPathTable.getTableHeader());
            getDomainPathTableScrollPane().getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
            domainPathTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            domainPathTable.setAutoCreateColumnsFromModel(true);
            domainPathTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            domainPathTable.setAutoCreateColumnsFromModel(true);
            
            ListSelectionModel rowSM = domainPathTable.getSelectionModel();
            rowSM.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    enableDisableRemoveButton(domainPathTable,getremoveDomainPathButton());
                }
            });
            final DefaultCellEditor editor = (DefaultCellEditor)domainPathTable.getDefaultEditor(String.class);
            editor.getComponent().addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyReleased(java.awt.event.KeyEvent ke){
                    try{
                        getDomainPathTableModel().setValueAt(((JTextField)editor.getComponent()).getText().trim(),domainPathTable.getEditingRow(),0);
                    }catch(Exception e){
                    }
                }
                
            });
            
        }
        return domainPathTable;
    }
    private javax.swing.JScrollPane getDomainPathTableScrollPane() {
        if (domainPathTableScrollPane == null) {
            domainPathTableScrollPane = new javax.swing.JScrollPane();
            domainPathTableScrollPane.setName("getDomainPathTable");
            domainPathTableScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            domainPathTableScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            domainPathTableScrollPane.setMaximumSize(new java.awt.Dimension(21, 20));
            domainPathTableScrollPane.setPreferredSize(new java.awt.Dimension(21, 20));
            domainPathTableScrollPane.setMinimumSize(new java.awt.Dimension(21, 20));
            domainPathTableScrollPane.setViewportView(getDomainPathTable());
        }
        return domainPathTableScrollPane;
    }
    private javax.swing.JPanel getaddRemoveButtonsPanel() {
        if (addRemoveButtonsPanel == null) {
            addRemoveButtonsPanel = new javax.swing.JPanel();
            addRemoveButtonsPanel.setName("addRemoveButtonsPanel");
            addRemoveButtonsPanel.setLayout(new java.awt.GridBagLayout());
            
            this.addComponetWithConstraints(getaddDomainPathButton(), addRemoveButtonsPanel, 0, 0,1,1, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0.0,0.0);
            this.addComponetWithConstraints(getremoveDomainPathButton(), addRemoveButtonsPanel, 0, 1,1,1, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0.0,0.0);
        }
        return addRemoveButtonsPanel;
    }
    private javax.swing.JButton getaddDomainPathButton() {
        if (addDomainPathButton == null) {
            addDomainPathButton = new javax.swing.JButton();
            addDomainPathButton.setName("addDomainPathButton");
            addDomainPathButton.setText(stringManager.getString("upgrade.gui.domainPanel.addButtonText"));
            addDomainPathButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addDomainPathAction();
                }
            });
        }
        return addDomainPathButton;
    }
    private javax.swing.JButton getremoveDomainPathButton() {
        if (removeDomainPathButton == null) {
            removeDomainPathButton = new javax.swing.JButton();
            removeDomainPathButton.setName("removeDomainPathButton");
            removeDomainPathButton.setText(stringManager.getString("upgrade.gui.domainPanel.deleteButtonText"));
            removeDomainPathButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeDomainPathAction();
                }
            });
        }
        return removeDomainPathButton;
    }
    private javax.swing.JPanel getButtonsPanel(){
        javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
        javax.swing.JButton okButton = new javax.swing.JButton();
        javax.swing.JButton cancelButton = new javax.swing.JButton();
        javax.swing.JButton helpButton = new javax.swing.JButton();
        buttonsPanel.setLayout(new java.awt.GridBagLayout());
        
        okButton.setText(stringManager.getString("upgrade.gui.adddomainpanel.okButtonText"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });
        this.addComponetWithConstraints(okButton, buttonsPanel, 1, 0, 1,1, GridBagConstraints.NONE, new Insets(5, 5, 10, 10), 0.0,0.0);
        
        cancelButton.setText(stringManager.getString("upgrade.gui.adddomainpanel.cancelButtonText"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });
        this.addComponetWithConstraints(cancelButton, buttonsPanel, 2, 0, 1,1, GridBagConstraints.NONE, new Insets(5, 5, 10, 10), 0.0,0.0);
        
        helpButton.setText(stringManager.getString("upgrade.gui.adddomainpanel.helpButtonText"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed();
            }
        });
        this.addComponetWithConstraints(helpButton, buttonsPanel, 3, 0, 1,1, GridBagConstraints.NONE, new Insets(5, 5, 10, 10), 0.0,0.0);
        this.addComponetWithConstraints(new JPanel(), buttonsPanel, 0, 0, 1,1, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 10, 10), 1.0,0.0);
        if(Utils.getHelpBroker() != null)
            Utils.getHelpBroker().enableHelpOnButton(helpButton, "ADD_DOMAIN_DIALOG", null);
        return buttonsPanel;
    }
    private void addComponetWithConstraints(JComponent compo, java.awt.Container parent, int gx, int gy, int gw, int gh, int fill,java.awt.Insets in,
    double wx, double wy){
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = gx; gridBagConstraints.gridy = gy;
        gridBagConstraints.gridwidth = gw; gridBagConstraints.gridheight = gh;
        gridBagConstraints.fill = fill;
        gridBagConstraints.insets = in;
        gridBagConstraints.weightx = wx;
        gridBagConstraints.weighty = wy;
        parent.add(compo, gridBagConstraints);
    }
    private void helpButtonActionPerformed() {
        this.USER_ACTION = HELP_ACTION;
        //this.dispose();
        /*DialogEvent de = new DialogEvent(this, DialogEvent.HELP_ACTION);
        for(int i=0 ; i<this.dialogListeners.size(); i++){
            ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
        }*/
    }
    
    private void cancelButtonActionPerformed() {
        this.USER_ACTION = CANCEL_ACTION;
        this.dispose();
        /*DialogEvent de = new DialogEvent(this, DialogEvent.CANCEL_ACTION);
        for(int i=0 ; i<this.dialogListeners.size(); i++){
            ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
        }*/
    }
    
    private void okButtonActionPerformed() {
        this.USER_ACTION = OK_ACTION;
        this.dispose();
    }
    public DomainPathTableModel getDomainPathTableModel(){
        if(this.domainPathTableModel == null){
            domainPathTableModel = new DomainPathTableModel(this);
        }
        return domainPathTableModel;
    }
    public void addDomainPathAction() {
        int returnedValue = getfileChooser().showOpenDialog(this);
        if(returnedValue == javax.swing.JFileChooser.APPROVE_OPTION){
            getDomainPathTableModel().addDomainPath(String.valueOf(getfileChooser().getSelectedFile()));
        }
    }
    private javax.swing.JFileChooser getfileChooser() {
        if (fileChooser == null) {
            fileChooser = new javax.swing.JFileChooser();
            fileChooser.setName("fileChooser");
            fileChooser.setBounds(668, 49, 500, 300);
            fileChooser.setDialogTitle(stringManager.getString("upgrade.gui.certpanel.chooseFile"));
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        }
        return fileChooser;
    }
    public void removeDomainPathAction() {
        
        int rows[] = getDomainPathTable().getSelectedRows();
        String[] rowPropNames = new String[rows.length];
        int option =
        javax.swing.JOptionPane.showConfirmDialog(
        this,
        stringManager.getString("upgrade.gui.domainPanel.removeConfirmMsg"),
        stringManager.getString("upgrade.gui.domainPanel.removeConfirmTitle"),
        javax.swing.JOptionPane.YES_NO_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (option == javax.swing.JOptionPane.NO_OPTION) {
            return;
        }
        
        for (int i = 0; i < rows.length; i++) {
            rowPropNames[i] =
            (String)getDomainPathTableModel().getDomainPath(rows[i]);
        }
        for (int i = 0; i < rows.length; i++) {
            getDomainPathTableModel().removeDomainPath(rowPropNames[i]);
        }
        // Un select items rows in table.
        getDomainPathTable().clearSelection();
        enableDisableRemoveButton(getDomainPathTable(), getremoveDomainPathButton());
    }
    
    class DomainPathTableModel extends javax.swing.table.AbstractTableModel {
        private Vector domainPaths = new Vector(0);
        private String columnNames[] = null;
        private DomainPathSelectionDialog domainPathSelectionDialog;
        
        private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");
        public DomainPathTableModel(DomainPathSelectionDialog cdp) {
            super();
            this.domainPathSelectionDialog = cdp;
            columnNames = new String[]{stringManager.getString("upgrade.gui.domainPanel.tableColumnName")};
        }
        public int getColumnCount() {
            return columnNames.length;
        }
        public java.lang.String getColumnName(int column) {
            return columnNames[column];
        }
        public int getRowCount() {
            return domainPaths.size();
        }
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col == 0) {
                return true;
            } else {
                return false;
            }
        }
        public void setValueAt(Object value, int row, int col) {
            
            if ( value instanceof String) {
                if(row < domainPaths.size())
                    domainPaths.setElementAt(value,row);
            }
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        public Object getValueAt(int row, int col) {
            String rowEle = (String) domainPaths.elementAt(row);
            String ret = "";
            switch (col) {
                case 0:
                    // Column 0 is the filename
                    ret = rowEle;
                    break;
                    
            }
            return ret;
            
        }
        public Vector getDomainPaths(){
            return this.domainPaths;
        }
        public void setDomainPaths(Vector v){
            this.domainPaths=v;
        }
        public void addDomainPath(String ele){
            if(ele != null){
                if(getDomainPath(ele) == null)
                    this.domainPaths.addElement(ele);
            }
            fireTableDataChanged();
            domainPathSelectionDialog.processDialogEvent();
        }
        public void removeDomainPath(String ele){
            if(ele != null){
                this.domainPaths.removeElement(ele);
            }
            fireTableDataChanged();
            domainPathSelectionDialog.processDialogEvent();
        }
        public String getDomainPath(int rowNo){
            if(rowNo < domainPaths.size()){
                return (String)domainPaths.elementAt(rowNo);
            }
            else return null;
        }
        public String getDomainPath(String propName){
            String ele1 = null;
            boolean found = false;
            if(propName != null){
                for(int i=0; i<domainPaths.size(); i++){
                    ele1 = (String)domainPaths.elementAt(i);
                    if(propName.equals(ele1)){
                        found = true;
                        break;
                    }
                }
            }
            if(found)
                return ele1;
            else
                return null;
        }
        
    }
    public void enableDisableRemoveButton(JTable table,JButton removeButton) {
        if (table.getSelectionModel().isSelectionEmpty()) {
            removeButton.setEnabled(false);
            
        } else {
            if (table.getSelectedRowCount() > 1) {
                //editButton.setEnabled(false);
            } else {
                removeButton.setEnabled(true);
            }
        }
    }
    public void addDialogListener(DialogListener listener){
        this.dialogListeners.addElement(listener);
    }
    public void removeDialogListener(DialogListener listener){
        this.dialogListeners.removeElement(listener);
    }
    private void processDialogEvent(){
        DialogEvent de = new DialogEvent(this, DialogEvent.CHANGE_ACTION);
        for(int i=0 ; i<this.dialogListeners.size(); i++){
            ((DialogListener)dialogListeners.elementAt(i)).dialogProcessed(de);
        }
    }
    
}
