/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.pluginprototype.plugins.hk2;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author jdlee
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class AppScopedBean {
    private String text = "This is some text from a plugin!";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}