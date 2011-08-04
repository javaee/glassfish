/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jdlee
 */
public class NavigationNode implements Serializable {

    private String label;
    private String link;
    private String icon;
    private List<NavigationNode> children;

    public NavigationNode(String label) {
        this(label, (String) null, (String) null);
    }

    public NavigationNode(String label, String icon) {
        this(label, icon, (String) null);
    }

    public NavigationNode(String label, String icon, String link) {
        this.label = label;
        this.icon = icon;
        this.link = link;
    }

    public NavigationNode(String label, List<NavigationNode> children) {
        this(label, null, (List<NavigationNode>) null);
        this.children = children;
    }

    public NavigationNode(String label, String icon, List<NavigationNode> children) {
        this(label, icon, (String) null);
        this.children = children;
    }

    public NavigationNode(String label, String icon, String link, List<NavigationNode> children) {
        this(label, icon, link);
        this.children = children;

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setChildren(List<NavigationNode> nodes) {
        children = nodes;
    }

    public List<NavigationNode> getChildren() {
        return (children == null) ? null : Collections.unmodifiableList(children);
    }
}
