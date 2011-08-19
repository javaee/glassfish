/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jdlee
 */
@ManagedBean
@SessionScoped
public class MyBean {

    private String text = "blargh!";

    public MyBean() {
        System.out.println("ARGH!");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void updateVisibility(AjaxBehaviorEvent event) {
        System.out.println(event);
    }

    public String dropListener() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) fc.getExternalContext().getRequest();
        HttpSession mySession = myRequest.getSession();
        myRequest.getParameter("triggerId");
        System.out.println("hello");

        return null;
    }
}
