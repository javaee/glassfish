/*
 * MyTableListener.java
 *
 * Created on April 25, 2006, 1:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import javax.swing.event.*;
import javax.swing.table.*;
/**author
 *
 * @author kravtch
 */
public class MyTableListener implements TableModelListener
{
 NodeViewFrame _frame;   
 boolean _bProperty;
    /** Creates a new instance of MyTableListener */
    public MyTableListener(NodeViewFrame frame, boolean bProperty)
    {
        _frame = frame;
        _bProperty = bProperty;
    }
    
    public void tableChanged(TableModelEvent e) {
        this.
        _frame.tableChanged(e, _bProperty);
    }
    
}
