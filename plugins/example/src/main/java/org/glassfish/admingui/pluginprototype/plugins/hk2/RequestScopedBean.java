/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.pluginprototype.plugins.hk2;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author anilam
 */
@ManagedBean(eager = true)
@RequestScoped
public class RequestScopedBean {

    private String text = " ";
    private List<Application> applications;

    public RequestScopedBean() {
        applications = new ArrayList<Application>();
        applications.add(new Application("console-demo", false, "web"));
        applications.add(new Application("plugin-demo", false, "web"));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Application> getApplications() {
        return applications;
    }
}
