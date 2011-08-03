package org.glassfish.admingui.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jasonlee
 */
public class ConsolePluginMetadata {
    private Map<String, List<String>> viewFragments = new HashMap<String, List<String>>();
    private Map<String, List<NavigationNode>> navigationNodes = new HashMap<String, List<NavigationNode>>();
    private String pluginPackage;
    private int priority = 500;

    public ConsolePluginMetadata(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(String pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

    public void addViewFragment(String type, String id) {
        List<String> list = viewFragments.get(type);
        if (list == null) {
            list = new ArrayList<String>();
            viewFragments.put(type, list);
        }

        list.add(id);
    }

    public List<String> getViewFragments(String type) {
        return viewFragments.get(type);
    }
    
    public List<NavigationNode> getNavigationNodes(String parent) {
        return navigationNodes.get(parent);
    }
    
    public void addNavigationNodes(String parent, List<NavigationNode> newNodes) {
        List<NavigationNode> nodes = getNavigationNodes(parent);
        if (nodes == null) {
            nodes = new ArrayList<NavigationNode>();
            navigationNodes.put(parent, nodes);
        }
        nodes.addAll(newNodes);
    }
}