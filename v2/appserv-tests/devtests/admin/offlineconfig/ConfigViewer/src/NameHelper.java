/*
 * NameHelper.java
 *
 * Created on April 21, 2006, 12:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import com.sun.enterprise.admin.config.OfflineConfigMgr;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import javax.management.AttributeList;
import javax.management.Attribute;

/**
 *
 * @author kravtch
 */
public class NameHelper
{
    OfflineConfigMgr _mgr;
    
    /** Creates a new instance of NameHelper */
    public NameHelper(String fileName) throws Exception
    {
       _mgr = new OfflineConfigMgr(fileName);
    }
    
    void fillDottedNamesTree(DefaultMutableTreeNode root) 
       throws Exception
    {
       fillAddNodeChildren(root, ""); 
    }
    
    void fillAddNodeChildren(DefaultMutableTreeNode parentNode, 
                    String parentDottedName)
          throws Exception
    {
        ArrayList childList = _mgr.getListDottedNames(parentDottedName);
        for (int i=0; i<childList.size(); i++)
        {
            String childName = (String)childList.get(i);
            DefaultMutableTreeNode childNode = 
                    new DefaultMutableTreeNode(new DottedNameInfo(childName,parentDottedName));
            fillAddNodeChildren(childNode, childName);
            parentNode.add(childNode);
        }
    }
    

    Object[][] getAttributesForNodeInPrintForm(DefaultMutableTreeNode node, boolean bProperties) throws Exception
    {
        if(_mgr==null)
            return null;
        if(!(node.getUserObject() instanceof DottedNameInfo))
            return null;
        DottedNameInfo info = (DottedNameInfo)node.getUserObject();
        String name = info._name; //_mgr.removeNamePrefixes(info._name);
        AttributeList list = _mgr.getAttributes(name+(bProperties?".property.*":".*"));
        if(list==null)
            return null;
        Object[][] arr = new Object[list.size()][2];
        int iOffset = name.length()+1;
        if(bProperties)
            iOffset += "property.".length();
            
        for (int i=0; i<list.size(); i++)
        {
            Attribute attr = (Attribute)list.get(i);
            String attrName = attr.getName();
            arr[i][0] = attrName.substring(iOffset);
            Object value = null;
            try {
                value = attr.getValue();
            } catch (Exception e) {
                value = e;
            }
            arr[i][1] = attr.getValue();
        }
        return arr;
    }
    void setValue(DefaultMutableTreeNode node, String name, Object value, boolean bProperties)
        throws Exception
    {
        DottedNameInfo info = (DottedNameInfo)node.getUserObject();
        setValue(info, name, value, bProperties);
    }
    
    void setValue(DottedNameInfo info, String name, Object value, boolean bProperties)
        throws Exception
    {
        String dottedName = info._name; //_mgr.removeNamePrefixes(info._name);
        name = name.replaceAll("\\.","\\\\.");
        _mgr.setAttribute(dottedName+  (bProperties?".property.":".") + name, value);
    }
}
