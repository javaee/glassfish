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
 * PrincipalsEditor.java
 *
 * Created on June 28, 2002, 5:25 PM
 */

package com.sun.enterprise.tools.common.properties;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

/**
 *
 * @author  shirleyc
 * @version 
 */
public class PrincipalsEditor implements TableCellEditor {
    
    private Vector model;
    private transient Vector listeners;
    private transient Vector originalValue;
    private JTable principalsTable;
    private JButton button;
    private JDialog d;
    private JFrame frame;
    
    private static java.util.ResourceBundle bundle =
        java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); //NOI18N
    static final java.util.ResourceBundle helpBundle = java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.HelpIDBundle"); // NOI18N
    
    public PrincipalsEditor(JFrame f) {
        listeners = new Vector();
        this.frame = f;
        
	// Create button that brings up the editor
	button = new JButton();
	button.setBackground(Color.white);
	button.setBorderPainted(false);
	button.setMargin(new Insets(0,0,0,0));
        // Set up the dialog that the button brings up
	// This will be called when OK button is selected on resulting Dialog
	button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (d == null)
                        createDialog();
                    d.setVisible(true);
                }
        });        
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, 
                                                 boolean isSelected,
                                                 int row, int column) {
        if (value == null)
            model = new Vector();
        else if (value instanceof String && ((String)value).length() == 0)
            model = new Vector();
        else if (value instanceof Vector)
            model = (Vector)value;
        else
            Reporter.error(value);
        if (principalsTable == null)
            createDialog();
        principalsTable.setModel(new PrincipalTableModel(model));
        originalValue = model;
     
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
//        return sp;   
        return button;
    }    
    
    public void createDialog() {
        JPanel pane = new JPanel();
        principalsTable = new JTable();        
        JScrollPane sp = new JScrollPane(principalsTable);        
        d = new JDialog(this.frame, bundle.getString("PRIN_TITLE"), true);    //NOI18N
        d.setSize(500, 300);
        d.getContentPane().setLayout(new BorderLayout());
        pane.setLayout(new BorderLayout());
//        d.getContentPane().add(sp, BorderLayout.CENTER);
        pane.add(sp, BorderLayout.CENTER);
        JButton okButton = new JButton(bundle.getString("OK_BUTTON_LABEL"));   //NOI18N
        JButton cancelButton = new JButton(bundle.getString("CANCEL_BUTTON_LABEL"));   //NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                stopCellEditing();
                d.setVisible(false);
                d.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                cancelCellEditing();
                d.setVisible(false);
                d.dispose();
            }
        });
        JPanel buttonsPane = new JPanel();
        buttonsPane.add(okButton);
        buttonsPane.add(cancelButton);
        org.openide.util.HelpCtx.setHelpIDString(pane, helpBundle.getString("role_map_principal_editor")); //NOI18N
 //       d.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        pane.add(buttonsPane, BorderLayout.SOUTH);
        d.getContentPane().add(pane, BorderLayout.CENTER);
 //       d.setLocationRelativeTo(this.frame);
    }
    
    public void addCellEditorListener(javax.swing.event.CellEditorListener cellEditorListener) {
        listeners.addElement(cellEditorListener);
    }
    
    public void cancelCellEditing() {
        fireEditingCanceled();
    }
    
    public Object getCellEditorValue() {
        return model;
    }
    
    public boolean isCellEditable(java.util.EventObject eventObject) {
        return true;
    }
    
    public void removeCellEditorListener(javax.swing.event.CellEditorListener cellEditorListener) {
        listeners.removeElement(cellEditorListener);
    }
    
    public boolean shouldSelectCell(java.util.EventObject eventObject) {
        return true;
    }
    
    public boolean stopCellEditing() {
        if (principalsTable != null) {
            TableCellEditor cell = principalsTable.getCellEditor();
            if (cell != null)
                cell.stopCellEditing();
        }
 
        model = ((PrincipalTableModel)principalsTable.getModel()).getPrincipals();
        fireEditingStopped();
        return true;
    }
    
    private void fireEditingCanceled() {
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ((CellEditorListener)listeners.elementAt(i)).editingCanceled(ce);
        }
    }
    
    private void fireEditingStopped() {
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ((CellEditorListener)listeners.elementAt(i)).editingStopped(ce);
        }
    }
}
