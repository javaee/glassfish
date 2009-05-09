/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.factory;
import java.lang.reflect.Method;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.MethodInvoker;
import org.glassfish.flashlight.datatree.impl.MethodInvokerImpl;
import org.glassfish.flashlight.datatree.impl.TreeNodeImpl;

/**
 *
 * @author Harpreet Singh
 */
public class TreeNodeFactory {
    
    public static TreeNode createTreeNode (String name, Object instance, 
            String category){
        TreeNode tn = new TreeNodeImpl (name, category);
        tn.setEnabled (true);
        return tn;
    }
    
    public static TreeNode createMethodInvoker (String name, Object instance, 
            String category, Method m){
           
        TreeNode tn = new MethodInvokerImpl();
        tn.setName(name);
        ((MethodInvoker)tn).setInstance (instance);
        ((MethodInvoker)tn).setMethod (m);
        tn.setCategory(category);
        tn.setEnabled(true);
        return tn;
    } 
    
}
