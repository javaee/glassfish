/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jdlee
 */
public class TreeNodeBean implements Serializable {

    private String label;
    private String link;
    private String icon;
    private List<TreeNodeBean> children;

    public TreeNodeBean(String label) {
        this(label, null, null);
    }
    public TreeNodeBean(String label, String icon) {
        this(label, icon, null);
    }
    public TreeNodeBean(String label, String icon, String link) {
        this.label = label;
        this.icon = icon;
        this.link = link;
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

    public void setChildren(List<TreeNodeBean> nodes) {
        children = nodes;
    }

    public List<TreeNodeBean> getChildren() {
        return (children == null) ? null : Collections.unmodifiableList(children);
    }
}
