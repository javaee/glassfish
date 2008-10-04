/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl;

import org.glassfish.flashlight.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * @author Harpreet Singh
 */

@Service
@Scoped(Singleton.class)
public class MonitoringRuntimeDataRegistryImpl 
        implements MonitoringRuntimeDataRegistry {
    
    
    protected Map<String, TreeNode> children =
            new ConcurrentHashMap<String, TreeNode>();


    public MonitoringRuntimeDataRegistryImpl (){      
    }
    
    public void add(String name, TreeNode node) {
        if (name != null )
             children.put(name, node);
        else {
            throw new RuntimeException ("MonitoringRuntimeDataRegistry does not take null keys");
        }
    }

    public void remove(String name) {
        if (name != null)
            children.remove(name);
    }

    public TreeNode get (String name) {
        TreeNode node = (name != null)? children.get(name): null;
        return node;
    }
}
