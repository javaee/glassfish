/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree;

import java.util.Collection;

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

    public boolean hasChildNodes ();
    /**
     * 
     * @param oldChild
     * @return oldChild
     */
    public void removeChild (TreeNode oldChild);

/**
 * 
 *    Returns an immutable view of the children
     * 
     * @return Enumeration<TreeNode>
     
    public Enumeration<TreeNode> getChildNodesImmutable();
**/
        
    /**
     * Returns a mutable view of the children
     * @return Collection<TreeNode>
     */
    public Collection<TreeNode> getChildNodes (); 
    
    public TreeNode getNode (String completeName);
    
    /*
     * XXX
    public void traverseTree (TreeNode node);
    */
}
