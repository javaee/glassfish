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
 * RoleMapElementTableModel.java
 *
 * Created on March 18, 2002, 3:14 PM
 */

package com.sun.enterprise.tools.common.properties;

import java.util.*;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

/**
 *
 * @author  vkraemer
 * @version 
 */
public class RoleMapElementTableModel extends javax.swing.table.AbstractTableModel{

    RoleMapElement v = null;
    
    private static java.util.ResourceBundle bundle =
        java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); //NOI18N
    
    /** Creates new RoleMapElementTableModel */
    public RoleMapElementTableModel(RoleMapElement rm) {
        v = rm;
    }

    public java.lang.Object getValueAt(int param, int param1) {
        Object retVal = null; 
        if (param1 != 3)
            retVal = "";   // NOI18N
        if (param < v.getLength())
            retVal = v.getAttributeDetail(param, param1); //NOI18N
        return retVal;
        //        return ra.getAttributeValue("PropertyElement", param, intToAttribute(param1)); //NOI18N
    }
    
    public int getRowCount() {
        return v.getLength() + 1;
    }
    
    public int getColumnCount() {
        return v.getWidth();
    }
    
    
    public boolean isCellEditable(int row, int col) {
        return true;
    }
    
    public void setValueAt(Object val, int row, int col) {
        if (col == 3)
            Reporter.info("val size = " + ((Vector)val).size() + " row = " + row + " column = " + col);
        int pre = v.getLength();
        v.setAttributeDetail(val, row, col); //NOI18N
        if (v.getLength() < pre) {
            Reporter.info("fireTableRowsDeleted");   //NOI18N
//            fireTableStructureChanged();
            fireTableRowsDeleted(row, row);
        }
    }
    
    public String getColumnName(int col) {
        if (0 == col) 
            return bundle.getString("COL_HEADER_BE_USERNAME");
        if (1 == col)
            return bundle.getString("COL_HEADER_BE_PASSWORD");
        if (2 == col) 
            return bundle.getString("COL_HEADER_BE_CREDENTIAL");
        if (3 == col)
            return bundle.getString("COL_HEADER_CONTAINER_USERNAMES");
        throw new RuntimeException(bundle.getString("COL_HEADER_ERR_ERR_ERR"));
    }
    
    public static void main(String args[]) {
        RoleMapElement pe = new RoleMapElement(args);
        //if (null == args || 0 == args.length) {
        javax.swing.JTable tab = new javax.swing.JTable(new RoleMapElementTableModel(pe));
        javax.swing.table.TableColumnModel tcm = tab.getColumnModel();
        javax.swing.table.TableColumn tc = tcm.getColumn(1);
        //PasswordRenderEdit pre1 = new PasswordRenderEdit();
        PasswordRender pre2 = new PasswordRender();
        tc.setCellEditor(new javax.swing.DefaultCellEditor(new javax.swing.JPasswordField()));
        tc.setCellRenderer(pre2);
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(tab);
        javax.swing.JFrame f = new javax.swing.JFrame();
        f.addWindowListener(new CloseTestWindow(pe));
        f.getContentPane().add(sp);
        f.show();
    }
    
    static class CloseTestWindow extends java.awt.event.WindowAdapter {
        private RoleMapElement myRM;
        public CloseTestWindow(RoleMapElement rm) {
            myRM = rm;
        }
        public void windowClosing(java.awt.event.WindowEvent e) {
            System.out.println(myRM.dumpIt()); //NOI18N
            System.exit(0);
        }
    }
    
}
