/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
