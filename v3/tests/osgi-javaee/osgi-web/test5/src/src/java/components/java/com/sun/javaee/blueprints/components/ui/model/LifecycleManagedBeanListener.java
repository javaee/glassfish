/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: LifecycleManagedBeanListener.java,v 1.2 2005/09/22 00:11:29 inder Exp $ */

package com.sun.javaee.blueprints.components.ui.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 *
 * @author edburns
 */
public class LifecycleManagedBeanListener implements ServletRequestListener, HttpSessionListener, ServletContextListener {
    
    /**
     * Creates a new instance of LifecycleManagedBeanListener 
     */
    public LifecycleManagedBeanListener() {
    }
    
    // 
    // Methods from ServletRequestListener
    //
    
    public void requestDestroyed(ServletRequestEvent sre) {
        callPreDestroyMethods((List<Object []>) sre.getServletRequest().getAttribute("components.model"));

    }
    
    public void requestInitialized(ServletRequestEvent sre) {
        
    }
    
    //
    // Methods from HttpSessionListener
    //
    
    public void sessionCreated(HttpSessionEvent se) {
        
    }
    public void sessionDestroyed(HttpSessionEvent se) {
        callPreDestroyMethods((List<Object []>) se.getSession().getAttribute("components.model"));
        
    }
    
    //
    // Methods from ServletContextListener
    //
    
    public void contextDestroyed(ServletContextEvent sce) {
        callPreDestroyMethods((List<Object []>) sce.getServletContext().getAttribute("components.model"));        
    }
    public void contextInitialized(ServletContextEvent sce) {
        
    }
    
    private void callPreDestroyMethods(List<Object []> destroyMethods) {
        if (null == destroyMethods) {
            return;
        }
        for (Object [] destroyMethod : destroyMethods) {
            try {
                ((Method)destroyMethod[0]).invoke(destroyMethod[1]);
                
            } catch (IllegalAccessException accessE) {
                
            } catch (IllegalArgumentException argumentE) {
                
            } catch (InvocationTargetException ite) {
                
            }
        }
        
    }
    
}
