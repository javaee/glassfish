/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree;

import java.util.Collection;
import java.util.List;
import org.jvnet.hk2.annotations.Contract;

/**
 * 
 * @author Harpreet Singh
 */
public interface TreeNode {


    public String getName ();
    public void setName (String name);
    // TBD getValue should take varargs
    public Object getValue ();
    public void setValue (Object value);
   
    public String getCategory ();
    public void setCategory (String category);
    
    public boolean isEnabled ();
    public void setEnabled (boolean enabled);
    
    // Children utility methods
    public TreeNode addChild (TreeNode newChild);
    
    public void setParent (TreeNode parent);
    public TreeNode getParent ();
    
    /**
     * 
     * @return the complete dotted name to this node
     */
    public String getCompletePathName ();

    public boolean hasChildNodes ();
    /*
     * Removed it due to security issues. 
     * @param oldChild
     * @return oldChild
     */
    /*
    public void removeChild (TreeNode oldChild);
    */
    /**
     * Returns a mutable view of the children
     * @return Collection<TreeNode>
     */
    public Collection<TreeNode> getChildNodes (); 
    
    public TreeNode getNode (String completeName);
    
    public List<TreeNode> traverse ();
    
    public List<TreeNode> getNodes (String regex);    
}
