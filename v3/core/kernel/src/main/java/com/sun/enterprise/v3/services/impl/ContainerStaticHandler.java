/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import java.io.IOException;
import java.lang.reflect.Method;

import com.sun.grizzly.standalone.StaticHandler;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.util.Interceptor;
import com.sun.grizzly.util.ThreadAttachment;
import com.sun.grizzly.util.WorkerThread;
import com.sun.grizzly.util.http.mapper.MappingData;

/**
 * Extends Grizzly StaticHandler.
 *
 * @author Shing Wai Chan
 */
public class ContainerStaticHandler extends StaticHandler {
    public ContainerStaticHandler() {
    }

    /**
     * Intercept the request and decide if we cache the static resource. If the
     * static resource is already cached, return it.
     */
    public int handle(Request req, int handlerCode) throws IOException {
        if (fileCache == null) {
            return Interceptor.CONTINUE;
        }

        WorkerThread workerThread = (WorkerThread)Thread.currentThread();
        ThreadAttachment attachment = workerThread.getAttachment();
        if (attachment != null) {
            Object mappingDataObj = attachment.getAttribute("mappingData");
            if (mappingDataObj != null && mappingDataObj instanceof MappingData) {
                MappingData mappingData = (MappingData)mappingDataObj;
                if (mappingData.wrapper != null &&
                        mappingData.wrapper.getClass().getName().equals(
                               "org.apache.catalina.core.StandardWrapper")) {

                    try {
                        Object wrapper = mappingData.wrapper;
                        Class clazz = wrapper.getClass();
                        Method getServletClassMethod = clazz.getMethod("getServletClass");
                        String servletClass =
                                (String)getServletClassMethod.invoke(wrapper);

                        if ("org.apache.catalina.servlets.DefaultServlet".equals(servletClass)) {
                            return super.handle(req, handlerCode);
                        }
                    } catch(Exception ex) {
                        IOException ioex = new IOException();
                        ioex.initCause(ex);
                        throw ioex;
                    }
                }
            }
        }

        return Interceptor.CONTINUE;   
    }
}
