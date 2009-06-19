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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.v3.server;

import java.util.Properties;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.ServerLifecycleException;

/**
 *  LifecycleListenerImpl is a dummy implementation for the LifecycleListener interface.
 *  This implementaion stubs out various lifecycle interface methods.
 */
public class LifecycleListenerImpl implements LifecycleListener {

    /** receive a server lifecycle event 
     *  @param event associated event
     *  @throws <code>ServerLifecycleException</code> for exceptional condition.
     *
     *  Configure this module as a lifecycle-module in server.xml:
     *
     *  <applications>
     *    <lifecycle-module name="test" 
     *               class-name="com.sun.appserv.server.LifecycleListenerImpl" 
                     is-failure-fatal="false">
     *      <property name="foo" value="fooval"/>
     *    </lifecycle-module>
     *  </applications>
     *
     *  Set<code>is-failure-fatal</code>in server.xml to <code>true</code> for 
     *  fatal conditions.
     */
    public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {
        LifecycleEventContext ctx = event.getLifecycleEventContext();

        ctx.log("got event" + event.getEventType() + " event data: " + event.getData());

        Properties props;

        if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: INIT_EVENT");

            props = (Properties) event.getData();

            // handle INIT_EVENT
            return;
        }

        if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: STARTUP_EVENT");

            // handle STARTUP_EVENT
            return;
        }

        if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
            System.out.println("LifecycleListener: SHUTDOWN_EVENT");

            // handle SHUTDOWN_EVENT
            return;
        }

        if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: TERMINATE_EVENT");

            // handle TERMINATION_EVENT
            return;
        }
    }
}
