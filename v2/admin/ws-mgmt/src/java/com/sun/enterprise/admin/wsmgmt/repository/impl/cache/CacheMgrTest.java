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
package com.sun.enterprise.admin.wsmgmt.repository.impl.cache;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * Test class for cache manager object. 
 *
 * @author Nazrul Islam
 * @since  J2SE 5.0
 */
class CacheMgrTest {

    public static void testSave() {
        CacheMgr mgr = CacheMgr.getInstance();
        List ejbModules = new ArrayList();
        List webModules = new ArrayList();

        ejbModules.add("ejb1.jar");
        webModules.add("web1.war");
        mgr.addJ2eeApplication("app1",  ejbModules, webModules);

        List ejbModules2 = new ArrayList();
        List webModules2 = new ArrayList();
        ejbModules2.add("ejb2.jar");
        webModules2.add("web2.war");
        ejbModules2.add("ejb3.jar");
        webModules2.add("web3.war");
        mgr.addJ2eeApplication("app2",  ejbModules2, webModules2);

        mgr.addEjbModule("ejb-module1");
        mgr.addWebModule("web-module1");

        mgr.save();
    }

    public static void testLoad() {
        CacheMgr mgr = CacheMgr.getInstance();

        System.out.println("J2EE APPLICATION:");
        Map apps = mgr.getJ2eeApplications();
        Collection values = apps.values();
        for (Iterator iter=values.iterator(); iter.hasNext();) {
            J2eeApplication app = (J2eeApplication) iter.next();
            System.out.println("\t"+app.getName()+"="+app.getPersistentValue());
        }

        System.out.println("EJB MODULES:");
        Map ejbModules = mgr.getEjbModules();
        Collection ejbValues = ejbModules.values();
        for (Iterator iter=ejbValues.iterator(); iter.hasNext();) {
            String ejbModule = (String) iter.next();
            System.out.println("\t"+ejbModule);
        }

        System.out.println("WEB MODULES:");
        Map webModules = mgr.getWebModules();
        Collection webValues = webModules.values();
        for (Iterator iter=webValues.iterator(); iter.hasNext();) {
            String webModule = (String) iter.next();
            System.out.println("\t"+webModule);
        }
    }

    public static void main(String[] args) {
        testSave();
        testLoad();
    }
}
