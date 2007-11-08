/*
 * MyTableEditor.java
 *
 * Created on April 28, 2006, 5:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import javax.swing.JTextField;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;

/**
 *
 * @author kravtch
 */
public class MyTableEditor{
    
    /**
     * Creates a new instance of MyTableEditor
     */
    DefaultCellEditor textEditor  = new DefaultCellEditor(new JTextField());
    public MyTableEditor() {
    }
    
    /*public Component getTableCellEditorComponent(JTable table, Object value,
              boolean isSelected, int row, int column) {    
        return textEditor.getTableCellEditorComponent(
                       table, value, isSelected, row, column);
    }*/
}
