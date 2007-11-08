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
 * PrincipalTableModel.java
 *
 * Created on June 27, 2002, 4:50 PM
 */

package com.sun.enterprise.tools.common.properties;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

/**
 * @author  shirleyc
 */
public class PrincipalTableModel extends javax.swing.table.AbstractTableModel{

    Vector principals = null;
    
    private static java.util.ResourceBundle bundle =
        java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); //NOI18N
    
    /** Creates new RoleMapElementTableModel */
    public PrincipalTableModel(Vector values) {
        if (values == null)
            this.principals = new Vector();
        else
            this.principals = (Vector)values.clone();
    }

    public java.lang.Object getValueAt(int row, int column) {
        if (row < principals.size())
            return ((String[])principals.elementAt(row))[column];
        else
            return "";    //NOI18N
    }
    
    public int getRowCount() {
        return principals.size() + 1;
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public Vector getPrincipals() {
        return principals;
    }
    
    public boolean isCellEditable(int row, int col) {
        return true;
    }
    
    public void setValueAt(Object val, int row, int col) {
//        System.out.println("val = " + (String)val + ", row = " + new Integer(row) + ", col = " + new Integer(col));
        
        int pre = principals.size();
        if (row >= pre) {
            String[] newPrincipal = new String[2];
            principals.add(newPrincipal);
        }
             
        if (!(val instanceof String)) {
            throw new IllegalArgumentException();
        } 
        String input = (String)val;
        if (col == 0 && (input == null || input.trim().length() == 0)) {
            Reporter.info("row has no value (" + input + ")");    //NOI18N
            principals.removeElementAt(row);
        } else {
            Reporter.info("(" + input.trim() + ")");   //NOI18N
            ((String[])principals.elementAt(row))[col] = input.trim();
        } 
        
        if (principals.size() < pre) {
//            System.out.println("fireTableStructureChanged");
            fireTableStructureChanged();
        }
    }
    
    public String getColumnName(int col) {
        if (0 == col) 
            return bundle.getString("COL_HEADER_PRINCIPAL");    //NOI18N
        if (1 == col)
            return bundle.getString("COL_HEADER_DESCRIPTION");   //NOI18N

        throw new RuntimeException(bundle.getString("COL_HEADER_ERR_ERR_ERR"));  //NOI18N
    }
    
    public static void main(String args[]) {
        String[] principal = {"user-name", "description"};  //NOI18N
        Vector principals = new Vector();
        principals.add(principal);
        javax.swing.JTable table = new javax.swing.JTable(new PrincipalTableModel(principals));
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(table);
/*        
        javax.swing.JFrame f = new javax.swing.JFrame();
        f.addWindowListener(new CloseTestWindow(principals));
        f.getContentPane().add(sp);
        f.show();
 */
        final JDialog d = new JDialog();
        d.setSize(200, 150);
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(sp, BorderLayout.CENTER);
        JButton okButton = new JButton("OK");  //NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                d.setVisible(false);
                d.dispose();
            }
        });
        JPanel buttonsPane = new JPanel();
        buttonsPane.add(okButton);
        d.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        d.setVisible(true);
    }
    
    static class CloseTestWindow extends java.awt.event.WindowAdapter {
 
        private Vector principals = null;
        
        public CloseTestWindow(Vector vec) {
            principals = vec;
        }
        
        public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
        }
    }
    
}
