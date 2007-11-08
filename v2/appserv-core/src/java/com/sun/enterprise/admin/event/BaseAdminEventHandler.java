/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.admin.event;

import com.sun.enterprise.v3.server.Globals;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * This class calls appropriates listener method for the event with a specific
 * action code
 *
 * @author Satish Viswanatham
 */
 public class BaseAdminEventHandler {

    /**
     * Constructor
     */
     public BaseAdminEventHandler(AdminEvent event) {
        _event = event;
     }

     /**
      * This method processes the event.
      */
      public int processEvent() {
        EventListenerRegistry registry = Globals.getGlobals().getDefaultHabitat().getByType(EventListenerRegistry.class);
        if (registry==null) {
            return 0;
        }
        List list = registry.getListeners(
                            _event.getType());
                if (list != null && !list.isEmpty()) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                AdminEventListener listener =
                        (AdminEventListener)iter.next();
                invokeAdminEventListener(listener, _event);
            }
        }
        return ((list != null) ? list.size() : 0);
      }

    private void invokeNotification(AdminEventListener ae, String methodName,
                          AdminEvent e) throws Exception
    {
        invokeNotification(ae, methodName, e, ((Object)e).getClass());
    }
    
    private void invokeNotification(AdminEventListener ae, String methodName,
                          AdminEvent e, Class eventParamClass) throws Exception
    {
        Method meth = null;
        meth = ae.getClass().getMethod( methodName, new Class[]{eventParamClass});
        try {
            meth.invoke(ae, new Object[]{e});
        } catch (Exception ex) {
            Throwable t = ex.getCause();
            if (t != null) {
                throw (Exception)t;
            }
            else {
                throw ex;
            }
        } 
    }

    private void invokeAdminEventListener(
            AdminEventListener listener,
            AdminEvent e) {
        
        EventListenerRegistry registry = Globals.getGlobals().getDefaultHabitat().getByType(EventListenerRegistry.class);
        AdminEventResult result = AdminEventResult.getAdminEventResult(e);

        try {
           invokeNotification(listener, GENERIC_PROCESS_EVENT_METHOD, e);
           return;
        }catch (Throwable t) {
            if( !(t instanceof NoSuchMethodException))
                registry.handleListenerError(e, t, result);
        }
        
        try {
           invokeNotification(listener, GENERIC_PROCESS_EVENT_METHOD, e, AdminEvent.class);
           return;
        }catch (Throwable t) {
            if( !(t instanceof NoSuchMethodException))
                registry.handleListenerError(e, t, result);
        }
    
        try {
            ElementChangeEvent event = (ElementChangeEvent)e;
            int code = event.getActionType();
            if ( code == ElementChangeEvent.ACTION_ELEMENT_CREATE ) {
                invokeNotification(listener,ACTION_CREATE_METHOD, e);
            } else if ( code == ElementChangeEvent.ACTION_ELEMENT_UPDATE) {
                invokeNotification(listener,ACTION_UPDATE_METHOD, e);
            } else if (code == ElementChangeEvent.ACTION_ELEMENT_DELETE) {
                invokeNotification(listener,ACTION_DELETE_METHOD, e);
            } else {
                throw new RuntimeException("not valid error code");
            }
        } catch (Throwable t) {
            registry.handleListenerError(e, t, result);
        }
    }

    private AdminEvent _event;

    // Method to be called in the listener for action of type 
    //  ACTION_UPDATE_METHOD
    private static String ACTION_UPDATE_METHOD = "handleUpdate";

    // Method to be called in the listener for action of type 
    //  ACTION_DELTE_METHOD
    private static String ACTION_DELETE_METHOD = "handleDelete";

    // Method to be called in the listener for action of type 
    //  ACTION_CREATE_METHOD
    private static String ACTION_CREATE_METHOD = "handleCreate";

    // Generic method to be called in the listener
    private static String GENERIC_PROCESS_EVENT_METHOD = "processEvent";
 }
