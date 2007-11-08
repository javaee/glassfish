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
 * RoleMapElementEditor.java
 *
 * Created on March 19, 2002, 11:18 AM
 */

package com.sun.enterprise.tools.common.properties;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import java.beans.*;
import javax.swing.*;
import java.awt.*;

import java.util.ResourceBundle;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
/**
 *
 * @author  vkraemer
 * @version 
 */
public class RoleMapElementEditor extends PropertyEditorSupport {

    private RoleMapElement roleMap;
    private JTable table = null;
    private JTextField desc = null;
    private static ResourceBundle bundle =
        ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); //NOI18N
    static final java.util.ResourceBundle helpBundle = java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.HelpIDBundle"); // NOI18N
    
    /** Creates new RoleMapElementEditor */
    public RoleMapElementEditor() {
    }

    /** The String is not editable in the sheet.
     */
    public String getAsText() {
        return null;
    }
    
    /** Does this editor use painting to illuminate the property state in the 
     * property sheet
     */
    public boolean isPaintable() {
        return true;
    }
    
    /** return the UI components to edit this object */
    public Component getCustomEditor() {
        table = new JTable(new RoleMapElementTableModel(roleMap));
        table.setDefaultEditor(Object.class, c023cell);
        if (table.getModel().getRowCount() > 0)
            table.editCellAt(0,0);
        javax.swing.table.TableColumnModel tcm = table.getColumnModel();
        //
        // make the password field editor 'secret'...
        //
        javax.swing.table.TableColumn tc = tcm.getColumn(1);
        //PasswordRenderEdit pre1 = new PasswordRenderEdit();
        PasswordRender pre2 = new PasswordRender();
        tc.setCellEditor(c1cell);
        tc.setCellRenderer(pre2);
        
        //set principal editor
        javax.swing.table.TableColumn principalColumn = tcm.getColumn(3);
        principalColumn.setCellEditor(new PrincipalsEditor(null));
        principalColumn.setCellRenderer(new PrincipalsRenderer());
        JScrollPane sp = new JScrollPane(table);
        
        JPanel pane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        pane.setLayout(gridbag);
        GridBagConstraints  c = new GridBagConstraints();
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        pane.add(sp, c);

        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        JLabel descLabel = new JLabel(bundle.getString("LBL_DESCRIPTION"));   //NOI18N
/*        pane.add(descLabel, c);

        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
 */
        desc = new JTextField(68);
        desc.setText(roleMap.getRoleMapDescription());
        JPanel descPane = new JPanel();
        descLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        descPane.add(descLabel);
        descPane.add(desc);
        pane.add(descPane, c);
        org.openide.util.HelpCtx.setHelpIDString(pane, helpBundle.getString("role_map_editor")); //NOI18N
        return pane;
    }
    
    public Object getValue() {
        if (roleMap != null && desc != null)
            roleMap.setRoleMapDescription(desc.getText());
        return new RoleMapElement(roleMap);
    }
    
    public void paintValue(Graphics gfx, Rectangle box) {
        int num = roleMap.getLength();
        String s = "" + num + " "; //NOI18N
        
        String format = null; //bundle.getString
        if(num == 1)
            format = bundle.getString("PROP_TEXT_ONE_MAP_ELEMNT");//NOI18N
        else
            format = bundle.getString("PROP_TEXT_N_MAP_ELEMNT");//NOI18N

        s = java.text.MessageFormat.format(format, new Object[]  { s });
        
        java.awt.FontMetrics fm = gfx.getFontMetrics();
        
        //PropertySheetSettings pss = (PropertySheetSettings)	PropertySheetSettings.findObject(PropertySheetSettings.class, true);
        //gfx.setColor(pss.getValueColor());
        gfx.drawString(s, 4, (box.height - fm.getHeight()) / 2 + 1 + fm.getMaxAscent());
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(bundle.getString("ERR_SET_AS_TEXT_ILLEGAL")); //NOI18N
    }
    
    TableCellEditor c023cell = new javax.swing.DefaultCellEditor(new javax.swing.JTextField());
    TableCellEditor c1cell = new javax.swing.DefaultCellEditor(new javax.swing.JPasswordField());
    
    public void setValue(Object value) {
        Reporter.info("");  //NOI18N
        if (table != null) {
            TableCellEditor cell = table.getCellEditor();
            if (null != cell) {
                Reporter.info("There is a cell: " + cell);//NOI18N
                Reporter.info("  this is the value of the cell: " + cell.getCellEditorValue());//NOI18N
                int r = table.getEditingRow();
                int c = table.getEditingColumn();
                if (r > -1 && c > -1) {
                    table.setValueAt(cell.getCellEditorValue(), r, c);
                }
            }
        }
        if (value instanceof RoleMapElement) {
            roleMap = (RoleMapElement) value; //new RoleMapElement((RoleMapElement) value);
        }
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
}
