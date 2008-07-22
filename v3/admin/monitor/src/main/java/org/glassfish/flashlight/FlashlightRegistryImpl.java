/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.flashlight.datatree.TreeNode;

/**
 * XXX Need to make this @Service
 * @author hsingh
 */
public class FlashlightRegistryImpl implements FlashlightRegistry {
    
    protected Map<String, TreeNode> children =
            new ConcurrentHashMap<String, TreeNode>();


    public void add(String name, TreeNode node) {
        if (name != null )
             children.put(name, node);
        else {
            throw new RuntimeException ("FlashlightMonitoringRegistry does not take null keys");
        }
    }

    public void remove(String name) {
        if (name != null)
            children.remove(name);
    }

    public TreeNode getNodeFromRegistry (String name) {
        TreeNode node = (name != null)? children.get(name): null;
        return node;
    }

}
