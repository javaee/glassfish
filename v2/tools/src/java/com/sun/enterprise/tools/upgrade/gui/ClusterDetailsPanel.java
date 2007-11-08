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
 * ClusterDetailsPanel.java
 *
 * Created on May 27, 2004, 5:22 PM
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

public class ClusterDetailsPanel extends javax.swing.JPanel {
    
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");
    private ClusterFilesTableModel clusterFilesTableModel= null;
    private JTable clusterFilesTable = null;
    private JScrollPane clusterFilesTableScrollPane = null;
    private JPanel clusterFilesPanel = null;
    private JPanel addRemoveEditHomeInterfacePanel = null;
    private JButton addClusterFileButton = null;
    private JButton removeClusterFileButton = null;
    
    private JFileChooser fileChooser = null;
    
    private java.util.Vector dialogListeners = new java.util.Vector();

    /** Creates a new instance of ClusterDetailsPanel */
    public ClusterDetailsPanel() {
        this.initComponents();
    }
    private void initComponents(){
        this.setLayout(new java.awt.GridBagLayout());
        
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx = 0; constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = java.awt.GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new java.awt.Insets(0, 0, 0, 0);
        this.add(getClusterFilesPanel(), constraints);
        
        getClusterFilesTableScrollPane().setViewportView(getClusterFilesTable());
        
        getClusterFilesTable().setModel(this.getClusterFilesTableModel());
        getClusterFilesTable().createDefaultColumnsFromModel();
        
    }
    private javax.swing.JPanel getClusterFilesPanel() {
	if (clusterFilesPanel == null) {
            clusterFilesPanel = new javax.swing.JPanel();
            clusterFilesPanel.setName("clusterFilesPanel");
            clusterFilesPanel.setLayout(new java.awt.GridBagLayout());
                        
            JLabel tableTitleLabel = new JLabel();
            tableTitleLabel.setText(stringManager.getString("upgrade.gui.clusterPanel.tableTitleName"));
            java.awt.GridBagConstraints labelConstraints = new java.awt.GridBagConstraints();
            labelConstraints.gridx = 0; labelConstraints.gridy = 0;
            labelConstraints.gridwidth = 3;
            labelConstraints.fill = java.awt.GridBagConstraints.NONE;
            labelConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
            clusterFilesPanel.add(tableTitleLabel, labelConstraints);
            
            java.awt.GridBagConstraints constraintsClusterScrollPane1 = new java.awt.GridBagConstraints();
            constraintsClusterScrollPane1.gridx = 0; constraintsClusterScrollPane1.gridy = 1;
            constraintsClusterScrollPane1.gridwidth = 2;
            constraintsClusterScrollPane1.fill = java.awt.GridBagConstraints.BOTH;
            constraintsClusterScrollPane1.weightx = 1.0;
            constraintsClusterScrollPane1.weighty = 1.0;
            constraintsClusterScrollPane1.insets = new java.awt.Insets(5, 10, 5, 0);
            clusterFilesPanel.add(getClusterFilesTableScrollPane(), constraintsClusterScrollPane1);
            
            java.awt.GridBagConstraints constraintsaddRemoveEditHomeInterfacePanel1 = new java.awt.GridBagConstraints();
            constraintsaddRemoveEditHomeInterfacePanel1.gridx = 2; constraintsaddRemoveEditHomeInterfacePanel1.gridy = 1;
            constraintsaddRemoveEditHomeInterfacePanel1.fill = java.awt.GridBagConstraints.VERTICAL;
            constraintsaddRemoveEditHomeInterfacePanel1.weighty = 1.0;
            constraintsaddRemoveEditHomeInterfacePanel1.insets = new java.awt.Insets(5, 0, 5, 0);
            clusterFilesPanel.add(getaddRemoveEditHomeInterfacePanel(), constraintsaddRemoveEditHomeInterfacePanel1);
        }
	return clusterFilesPanel;
    }
    private javax.swing.JTable getClusterFilesTable() {
        if (clusterFilesTable == null) {
            clusterFilesTable = new javax.swing.JTable();
            clusterFilesTable.setName("clusterFilesTable");
            getClusterFilesTableScrollPane().setColumnHeaderView(clusterFilesTable.getTableHeader());
            getClusterFilesTableScrollPane().getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
            clusterFilesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            clusterFilesTable.setAutoCreateColumnsFromModel(true);
            clusterFilesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            clusterFilesTable.setAutoCreateColumnsFromModel(true);
            
            ListSelectionModel rowSM = clusterFilesTable.getSelectionModel();
            rowSM.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    enableDisableRemoveButton(clusterFilesTable,getremoveClusterFileButton());
                }
            });
            final DefaultCellEditor editor = (DefaultCellEditor)clusterFilesTable.getDefaultEditor(String.class);
            editor.getComponent().addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyReleased(java.awt.event.KeyEvent ke){
                    try{
                        getClusterFilesTableModel().setValueAt(((JTextField)editor.getComponent()).getText().trim(),clusterFilesTable.getEditingRow(),0);
                    }catch(Exception e){
                    }
                }
                
            });
            
        }
        return clusterFilesTable;
    }
    private javax.swing.JScrollPane getClusterFilesTableScrollPane() {
        if (clusterFilesTableScrollPane == null) {
            clusterFilesTableScrollPane = new javax.swing.JScrollPane();
            clusterFilesTableScrollPane.setName("getClusterFilesTable");
            clusterFilesTableScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            clusterFilesTableScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            clusterFilesTableScrollPane.setMaximumSize(new java.awt.Dimension(21, 20));
            clusterFilesTableScrollPane.setPreferredSize(new java.awt.Dimension(21, 20));
            clusterFilesTableScrollPane.setMinimumSize(new java.awt.Dimension(21, 20));
            clusterFilesTableScrollPane.setViewportView(getClusterFilesTable());
        }
        return clusterFilesTableScrollPane;
    }
    private javax.swing.JPanel getaddRemoveEditHomeInterfacePanel() {
        if (addRemoveEditHomeInterfacePanel == null) {
            addRemoveEditHomeInterfacePanel = new javax.swing.JPanel();
            addRemoveEditHomeInterfacePanel.setName("addRemoveEditHomeInterfacePanel");
            addRemoveEditHomeInterfacePanel.setLayout(new java.awt.GridBagLayout());
            
            java.awt.GridBagConstraints constraintsaddClusterFileButton = new java.awt.GridBagConstraints();
            constraintsaddClusterFileButton.gridx = 0; constraintsaddClusterFileButton.gridy = 0;
            constraintsaddClusterFileButton.insets = new java.awt.Insets(10, 5, 5, 5);
            addRemoveEditHomeInterfacePanel.add(getaddClusterFileButton(), constraintsaddClusterFileButton);
            
            java.awt.GridBagConstraints constraintsremoveClusterFileButton = new java.awt.GridBagConstraints();
            constraintsremoveClusterFileButton.gridx = 0; constraintsremoveClusterFileButton.gridy = 1;
            constraintsremoveClusterFileButton.insets = new java.awt.Insets(5, 5, 5, 5);
            addRemoveEditHomeInterfacePanel.add(getremoveClusterFileButton(), constraintsremoveClusterFileButton);
        }
        return addRemoveEditHomeInterfacePanel;
    }
    private javax.swing.JButton getaddClusterFileButton() {
        if (addClusterFileButton == null) {
            addClusterFileButton = new javax.swing.JButton();
            addClusterFileButton.setName("addClusterFileButton");
            addClusterFileButton.setText(stringManager.getString("upgrade.gui.clusterPanel.addButtonText"));
            addClusterFileButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    addClusterFileAction();
                }
            });
        }
        return addClusterFileButton;
    }
    private javax.swing.JButton getremoveClusterFileButton() {
        if (removeClusterFileButton == null) {
            removeClusterFileButton = new javax.swing.JButton();
            removeClusterFileButton.setName("removeClusterFileButton");
            removeClusterFileButton.setText(stringManager.getString("upgrade.gui.clusterPanel.deleteButtonText"));
            removeClusterFileButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    removeClusterFileAction();
                }
            });
        }
        return removeClusterFileButton;
    }
    public ClusterFilesTableModel getClusterFilesTableModel(){
	if(this.clusterFilesTableModel == null){
	    clusterFilesTableModel = new ClusterFilesTableModel(this);
	}
	return clusterFilesTableModel;
    }
    public void addClusterFileAction() {
        int returnedValue = getfileChooser().showOpenDialog(this);
        if(returnedValue == javax.swing.JFileChooser.APPROVE_OPTION){
            getClusterFilesTableModel().addClusterFile(String.valueOf(getfileChooser().getSelectedFile()));	            
        }	
    }
    private javax.swing.JFileChooser getfileChooser() {
        if (fileChooser == null) {
            fileChooser = new javax.swing.JFileChooser();
            fileChooser.setName("fileChooser");
            fileChooser.setBounds(668, 49, 500, 300);
            fileChooser.setDialogTitle(stringManager.getString("upgrade.gui.certpanel.chooseFile"));            
        }
        return fileChooser;
    }
    public void removeClusterFileAction() {
	
	int rows[] = getClusterFilesTable().getSelectedRows();
	String[] rowPropNames = new String[rows.length];
	int option = 
	    javax.swing.JOptionPane.showConfirmDialog(
						      this, 
						      stringManager.getString("upgrade.gui.clusterPanel.removeConfirmMsg"),
                                                      stringManager.getString("upgrade.gui.clusterPanel.removeConfirmTitle"),
						      javax.swing.JOptionPane.YES_NO_OPTION, 
						      javax.swing.JOptionPane.QUESTION_MESSAGE); 
	if (option == javax.swing.JOptionPane.NO_OPTION) {
	    return;
	}

	for (int i = 0; i < rows.length; i++) {
	    rowPropNames[i] = 
		(String)getClusterFilesTableModel().getClusterFile(rows[i]); 
	}
	for (int i = 0; i < rows.length; i++) {
	    getClusterFilesTableModel().removeClusterFile(rowPropNames[i]);
	}
	// Un select items rows in table.
	getClusterFilesTable().clearSelection();
	enableDisableRemoveButton(getClusterFilesTable(), getremoveClusterFileButton()); 	
    }

    class ClusterFilesTableModel extends javax.swing.table.AbstractTableModel {
        private Vector clusterFiles = new Vector(0);
	private String columnNames[] = null;
        private ClusterDetailsPanel clusterDetailsPanel;
        
        private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");
	public ClusterFilesTableModel(ClusterDetailsPanel cdp) {
	    super();
            this.clusterDetailsPanel = cdp;
            columnNames = new String[]{stringManager.getString("upgrade.gui.clusterPanel.tableColumnName")};
	}
	public int getColumnCount() {
	    return columnNames.length;
	}
	public java.lang.String getColumnName(int column) {
	    return columnNames[column];
	}
        public int getRowCount() {
	    return clusterFiles.size();
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
		if(row < clusterFiles.size())
		    clusterFiles.setElementAt(value,row);
	    }
	}
	public Class getColumnClass(int c) {
	    return getValueAt(0, c).getClass();
	}
	public Object getValueAt(int row, int col) {
	    //System.out.println("in getValueAt row="+row+" col="+col);
	    String rowEle = (String) clusterFiles.elementAt(row);
	    String ret = "";
	    switch (col) {
	    case 0:
		// Column 0 is the filename
		ret = rowEle;
		break;
				
	    }
	    return ret;
			
	}
	public Vector getClusterFiles(){
	    return this.clusterFiles;
	}
	public void setClusterFiles(Vector v){
	    this.clusterFiles=v;
	}
	public void addClusterFile(String ele){
	    if(ele != null){
		if(getClusterFile(ele) == null)
		    this.clusterFiles.addElement(ele);
	    }
	    fireTableDataChanged();	
            clusterDetailsPanel.processDialogEvent();
	}
	public void removeClusterFile(String ele){
	    if(ele != null){
		this.clusterFiles.removeElement(ele);
	    }
	    fireTableDataChanged();	
            clusterDetailsPanel.processDialogEvent();
	}		
	public String getClusterFile(int rowNo){
	    if(rowNo < clusterFiles.size()){
		return (String)clusterFiles.elementAt(rowNo);
	    }
	    else return null;
	}
	public String getClusterFile(String propName){
	    String ele1 = null;
	    boolean found = false;
	    if(propName != null){
		for(int i=0; i<clusterFiles.size(); i++){
		    ele1 = (String)clusterFiles.elementAt(i);
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
	    //no rows are selected
	    // Need to remove disable remove and edit buttons.
	    removeButton.setEnabled(false);
		
	} else {
	    //int selectedRow = lsm.getMinSelectionIndex();
	    if (table.getSelectedRowCount() > 1) {
		//editButton.setEnabled(false);
	    } else {
		removeButton.setEnabled(true);
		//editButton.setEnabled(true);
	    }

	    //selectedRow is selected
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
