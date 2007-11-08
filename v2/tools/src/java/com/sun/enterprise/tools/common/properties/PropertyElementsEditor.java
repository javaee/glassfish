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
 * PropertyElementsEditor.java
 *
 * Created on March 13, 2002, 4:22 PM
 */

package com.sun.enterprise.tools.common.properties;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import java.beans.*;
import java.util.*;
import java.util.ResourceBundle;

//import com.sun.enterprise.tools.common.ui.GenericTable;

/**
 *
 * @author  vkraemer
 * @version 
 */
public class PropertyElementsEditor extends PropertyEditorSupport {
    
    private PropertyElements propElements;
    private static ResourceBundle bundle =
        ResourceBundle.getBundle("com.sun.enterprise.tools.common.properties.Bundle"); // NOI18N
    private JTable table = null;

    /** Creates new PropertyElementsEditor */
    public PropertyElementsEditor() {
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
        //System.out.println("PropElEd.getCustomEditor(" + propElements.toString() +") " + //NOI18N
        //propElements.hashCode());
        table = new JTable(new PropertyElementsTableModel(propElements));
        table.setDefaultEditor(Object.class, acell);
        if (table.getModel().getRowCount() > 0)
            table.editCellAt(0,0);
        //JTable tab = new JTable(propElements);
        JScrollPane sp = new JScrollPane(table);
        return sp;
    }
    
    public Object getValue() {
//        Reporter.info(propElements.toString() + ",   " + propElements.hashCode());  //NOI18N
        return new PropertyElements((PropertyElements) propElements);//*/ propElements;
    }
    
    public void paintValue(Graphics gfx, Rectangle box) {
        //System.out.println("PropElEd.paintValue(" + propElements.toString() +") " + //NOI18N
        //propElements.hashCode());
        int num = propElements.getLength();
        String s = "" + num + " "; // NOI18N
        
        String format = null;
        if(num == 1)
            format = bundle.getString("PROP_TEXT_ONE_PROPERTY");//NOI18N
        else
            format = bundle.getString("PROP_TEXT_N_PROPERTIES");//NOI18N

        s = java.text.MessageFormat.format(format, new Object[]  { s });
        
        java.awt.FontMetrics fm = gfx.getFontMetrics();
        
        gfx.drawString(s, 4, (box.height - fm.getHeight()) / 2 + 1 + fm.getMaxAscent());
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(bundle.getString("ERR_SET_AS_TEXT_ILLEGAL"));//NOI18N
    }
    
    public List isValueValid(PropertyElements value) {
        String propName = null;
        String propValue = null;
        Vector errors = new Vector();
        HashSet propNames = new HashSet();
        try {
            for (int row = 0; row < value.getLength(); row++) {
                if ((propName = (String)value.getAttributeDetail(row, 0)) == null || propName.trim().length() == 0 || //NOI18N
                   (propValue = (String)value.getAttributeDetail(row, 1)) == null || propValue.trim().length() == 0) { //NOI18N
                    String format = bundle.getString("ERR_InvalidEntry");  //NOI18N
                    errors.add(java.text.MessageFormat.format(format, new Object[] { new Integer(row + 1) }));
                }
                else if (propNames.contains(propName)) {
                    String format = bundle.getString("ERR_DuplicateProperty");  //NOI18N
                    errors.add(java.text.MessageFormat.format(format, new Object[] { propName, new Integer(row + 1) }));
                }
                else 
                    propNames.add(propName);
            }
        } catch (Exception ex) {
            errors.add(ex.getLocalizedMessage());
        }
        Reporter.info(new Integer(errors.size())); //NOI18N
        return errors;
    }
        
    public void setValue(Object value) {
//      Reporter.info(value.toString() + ",  " + value.hashCode());  //NOI18N
        List errors = null;
        if (table != null) {
            Reporter.info("table");  //NOI18N
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
            if (value instanceof PropertyElements) {
                errors = isValueValid((PropertyElements) value);
                if (!errors.isEmpty()) {
                    StringBuffer str = new StringBuffer(bundle.getString("ERR_Properties"));  //NOI18N
                    Iterator iter = errors.iterator();
                    while (iter.hasNext()) {
                        str.append("\n\t -" + (String)iter.next()); // NOI18N
                    }
                    JOptionPane.showMessageDialog(null, str.toString(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N 
                }
            }
        }
        if (value instanceof PropertyElements) {
            if (errors == null || errors.isEmpty()) {
                Reporter.info("");  //NOI18N
                propElements = (PropertyElements) value; //*/ new PropertyElements((PropertyElements) value);
            }
        }
        //firePropertyChange();
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    ////
    
    TableCellEditor acell = new javax.swing.DefaultCellEditor(new javax.swing.JTextField());
    
}
