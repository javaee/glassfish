/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.sample;

import java.util.ArrayList;
import java.util.List;
import org.glassfish.admingui.plugins.NavigationNode;
import org.glassfish.admingui.plugins.annotations.ConsolePlugin;
import org.glassfish.admingui.plugins.annotations.NavNodes;
import org.glassfish.admingui.plugins.annotations.ViewFragment;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * Only one of these is needed, strictly speaking, per plugin module. You can
 * use several if you want the separation of data, but it's not necessary.
 * @author jasonlee
 */
@Service
@Scoped(Singleton.class)
public class SamplePlugin implements ConsolePlugin {

    public int priority = 275;
    @ViewFragment(type = "tab")
    public static final String TAB = "/sample/tab.xhtml";
    
    @NavNodes(parent="root")
    public static final List<NavigationNode> navNodes = new ArrayList<NavigationNode>() {{
       add (new NavigationNode("Field Test 1", "/sample/icons/family-tree.jpg", "/sample/page1.xhtml",
               new ArrayList<NavigationNode>() {{ 
                   add (new NavigationNode("Field Test 1-1")); 
               }}
           )); 
    }};

    @NavNodes(parent = "root")
    public static List<NavigationNode> getNavNodes() {
        List<NavigationNode> nodes = new ArrayList<NavigationNode>();

        nodes.add(new NavigationNode("Test 1"));
        nodes.add(new NavigationNode("Test 2"));
        nodes.add(new NavigationNode("Test 3"));
        nodes.add(new NavigationNode("Test 4"));
        nodes.add(new NavigationNode("Test 5"));
        nodes.add(new NavigationNode("Test 6", new ArrayList<NavigationNode>() {{
            add(new NavigationNode("Test 6-1"));
            add(new NavigationNode("Test 6-2"));
            add(new NavigationNode("Test 6-3"));
            add(new NavigationNode("Test 6-4"));
            add(new NavigationNode("Test 6-5"));
        }}));


        return nodes;
    }
}