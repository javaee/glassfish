/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/client/ProxyFactoryTest.java,v 1.5 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.client;

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.base.NotificationServiceMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ProxyFactory;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;


/**
 */
public final class ProxyFactoryTest
        extends AMXTestBase {
    public ProxyFactoryTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }


    /**
     Verify that when an MBean is removed, the ProxyFactory
     detects this, and removes any proxy from its cache.
     */
    public void
    testProxyFactoryDetectsMBeanRemoved()
            throws InstanceNotFoundException {
        // use the NotificationServiceMgr as a convenient way of making
        // an MBean (a NotificationService) come and go.
        final NotificationServiceMgr mgr = getDomainRoot().getNotificationServiceMgr();
        final NotificationService ns = mgr.createNotificationService("UserData", 10);
        final ObjectName nsObjectName = Util.getObjectName(ns);
        assert (ns.getUserData().equals("UserData"));

        final ProxyFactory factory = getProxyFactory();
        final NotificationService proxy =
                factory.getProxy(nsObjectName, NotificationService.class, false);
        assert (proxy == ns) : "proxies differ: " + ns + "\n" + proxy;

        mgr.removeNotificationService(ns.getName());

        int iterations = 0;
        long sleepMillis = 10;
        while (factory.getProxy(nsObjectName, NotificationService.class, false) != null) {
            mySleep(sleepMillis);
            if (sleepMillis >= 400) {
                trace("testProxyFactoryDetectsMBeanRemoved: waiting for proxy to be removed");
            }
            sleepMillis *= 2;
        }
    }
}









