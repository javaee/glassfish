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
package com.sun.enterprise.ee.synchronization;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;

/**
 * Synchronization server director builder test.
 *
 * @author Nazrul Islam
 */
public class ServerDirectorTest extends TestCase {
   
    public ServerDirectorTest(String name) {
        super(name);        
    }

    protected void setUp() {
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    }

    protected void tearDown() {
    }

    /**
     * Returns config context for this test.
     */
    private ConfigContext getConfigContext() throws ConfigException {
        String configPath = "/tmp/director-test-domain.xml";
        ConfigContext ctx = ConfigFactory.createConfigContext(configPath);
        assertTrue(ctx != null);
        return ctx;
    }

    /**
     * When updateTS is true, the test sets an older last updated time stamp
     * of the attempted file.
     */
    public void testExclude() {
        System.out.println("\n-- ServerDirector.constructExcludes() Test --");

        try {
            ServerDirector director = 
                new ServerDirector(getConfigContext(), "server");

            List list = director.constructExcludes();
            System.out.println("Total excluded dirs: " + list.size());
            for (int i=0; i<list.size(); i++) {
                System.out.println("Exclude List " + i + " - "+ list.get(i));
            }
            assertTrue( isInList("ejb-stateful-persistenceApp", list));
            assertTrue( isInList("__ejb_container_timer_app", list));
            assertTrue( isInList("ejb_module_2", list));
            assertTrue( isInList("web_app", list));
            assertTrue( isInList("cciblackbox-tx", list));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public void test() {
        System.out.println("\n-- ServerDirector.construct() Test --");

        try {
            ServerDirector director = 
                new ServerDirector(getConfigContext(), "server");

            List list = director.construct();
            System.out.println("Total reqs: " + list.size());
            assertTrue( isInReqList("MEjbApp", list) );
            assertTrue( isInReqList("ejb_module_1", list) );
            assertTrue( isInReqList("adminapp", list) );
            assertTrue( isInReqList("admingui", list) );
            assertTrue( isInReqList("com_sun_web_ui", list) );
            assertTrue( isInReqList("rar2", list) );
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    /**
     * Returns true if name is part of the request list.
     *
     * @param  name  name of the app
     * @param  list  application synch request list
     * 
     * @return true if name is part of the request list
     */
    private boolean isInReqList(String name, List list) {
        List newList = new ArrayList();
        SynchronizationRequest sr = null;
        for (int i=0; i<list.size(); i++) {
            ApplicationSynchRequest ar = (ApplicationSynchRequest) list.get(i);
            sr = ar.getApplicationRequest();
            if (sr != null) {
                newList.add( sr.getMetaFileName() );
            }
            sr = ar.getEJBRequest();
            if (sr != null) {
                newList.add( sr.getMetaFileName() );
            }
            sr = ar.getPolicyRequest();
            if (sr != null) {
                newList.add( sr.getMetaFileName() );
            }
            sr = ar.getJSPRequest();
            if (sr != null) {
                newList.add( sr.getMetaFileName() );
            }
        }

        return isInList(name, newList);
    }

    /**
     * Returns true if name is part of the list.
     *
     * @param  name  application name
     * @param  list  path list
     *
     * @return   true if application id is part of the list
     */
    private boolean isInList(String name, List list) {
        boolean found = false;
        for (int i=0; i<list.size(); i++) {
            String l = (String) list.get(i);
            if (l.indexOf(name) >= 0) {
                found = true;
                break;
            }
        }

        return found;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ServerDirectorTest.class);
    }
}
