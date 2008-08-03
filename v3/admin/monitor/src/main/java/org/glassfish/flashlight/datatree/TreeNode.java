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


    /**
     * 
     * @return String name of TreeNode. Set earlier by a call to setName
     */
    public String getName ();
    public void setName (String name);
    // TBD getValue should take varargs
    /**
     * @return <p>
     * <ul> 
     * <li> Leaf nodes: Return value that this node has: </li>
     * <ul> 
     *  <li> Set up by a call to setValue.</li>
     *  <li> getValue() of
     * {@link org.glassfish.flashlight.statistics Default Statistics data types}
     * </ul>
     * <li> For non-leaf nodes, the call is equivalent to {@link #getChildNodes() } 
     * </ul>
     */
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
     * @return complete dotted name to this node
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
     * 
     * @return Collection<TreeNode> collection of children
     */
    public Collection<TreeNode> getChildNodes (); 
    
    /**
     * 
     * @param complete dotted name to the node
     * @return TreeNode uniquely identified tree node. Null if no matching tree node.
     */
    
    public TreeNode getNode (String completeName);
      
    /**
     * Performs a depth first traversal of the tree.
     * @return List<TreeNode> lists all nodes under the current sub tree.
     */

    public List<TreeNode> traverse ();
    /**
     * 
     * Returns all nodes that match the given regex pattern. <p>
     * <b>*</b> is interpreted as regex <b>.*</b>
     * @param regex
     * @return
     */
    public List<TreeNode> getNodes (String regex);    
}
