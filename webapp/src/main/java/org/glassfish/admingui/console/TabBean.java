/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 *
 * @author jdlee
 */
@ManagedBean
@SessionScoped
// This is an ugly, ugly hack. Post-OOW, when we hopefully are using a better
// component set, this can be made "right".
public class TabBean {
    private String currentTab = "/demo/applications.jsf";

    private List<String> tabs = new ArrayList<String>() {{
        add("/demo/applications.xhtml");
        add("/demo/environments.xhtml");
        /*
        add("/demo/listServices.xhtml");
        add("/demo/listTemplates.xhtml");
         */
    }};
    
    public boolean isActiveTab(String tab) {
        tab = tab.replace(".jsf", ".xhtml");
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return viewId.equals(tab) && tabs.contains(tab);
    }

    public String getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }

    public void pageLoadListener(ComponentSystemEvent event) throws javax.faces.event.AbortProcessingException {
    }
}
