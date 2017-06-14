/*
 * DottedNameInfo.java
 *
 * Created on April 21, 2006, 1:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author kravtch
 */
public class DottedNameInfo
{
    String _name;
    String _parent;
    
    
    /** Creates a new instance of DottedNameInfo */
    public DottedNameInfo(String name, String parentName)
    {
        _name = name;
        _parent = parentName;
    }
    
    public String toString()
    {
       if(_name.startsWith(_parent+"."))
           return _name.substring(_parent.length()+1);
       return _name;
    }
}
