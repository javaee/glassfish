/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.sse.impl;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import java.util.*;

import org.glassfish.sse.api.*;

/**
 * Registers a context listener to get ServletContext
 *
 * Registers a servlet dynamically if there are ServerSentEventHandlers in
 * an application.
 *
 * @author Jitendra Kotamraju
 */
@HandlesTypes(ServerSentEvent.class)
public class ServerSentEventServletContainerInitializer implements ServletContainerInitializer {

    public void onStartup(Set<Class<?>> set, ServletContext ctx) throws ServletException {
        if (set == null || set.isEmpty()) {
            return;
        }

        // Check if there is already a servlet for server sent events
        Map<String, ? extends ServletRegistration> registrations = ctx.getServletRegistrations();
        for(ServletRegistration reg : registrations.values()) {
            if (reg.getClass().equals(ServerSentEventServlet.class)) {
                return;
            }
        }

        // Collect all the url patterns for server sent event handlers
        List<String> urlPatternList = new ArrayList<String>();

        for(Class<?> clazz : set) {
            if (ServerSentEventHandler.class.isAssignableFrom(clazz)) {
                ServerSentEvent handler = clazz.getAnnotation(ServerSentEvent.class);
                if (handler == null) {
                    throw new RuntimeException("ServerSentEventHandler Class "+clazz+" doesn't have WebHandler annotation");
                }
                urlPatternList.add(handler.value());
            }
        }

        // Register a servlet for all the url patterns of server sent event handler
        if (!urlPatternList.isEmpty()) {
            ServletRegistration.Dynamic reg = ctx.addServlet("sse servlet", ServerSentEventServlet.class);
            reg.setAsyncSupported(true);
            reg.addMapping(urlPatternList.toArray(new String[urlPatternList.size()]));
        }
    }

}
