/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.ee.synchronization.cleaner;

import java.io.File;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Synchronization cleaner test.
 *
 * @author Nazrul Islam
 */
public class SynchronizationCleanerTest extends TestCase {
   
    public SynchronizationCleanerTest(String name) {
        super(name);        
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    /**
     * Tests the remove functionality of file handler.
     */
    public void test() {

        try {
            String iRoot = System.getProperty("com.sun.aas.instanceRoot");
            String appname = System.getProperty("appname");
            String appdir = iRoot + File.separator + "applications"
                + File.separator + "j2ee-apps" + File.separator + appname;

            System.out.println("Application dir: " + appdir);

            File app = new File(appdir);

            // file should be removed
            assertTrue( !app.exists() );
            System.out.println("Application dir is removed after restart.");

            String gendir = iRoot + File.separator + "generated"
              + "ejb" + File.separator + "j2ee-apps" + File.separator + appname;
            File gen = new File(gendir);
            assertTrue( !gen.exists() );
            System.out.println("Generated dir is removed after restart.");

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    /*
    public static TestSuite suite() {
        //To run all tests
        return new TestSuite(SynchronizationMementoTest.class);
        //To run a subset of the tests
        TestSuite suite = new TestSuite();
        suite.addTest(new SynchronizationMementoTest("testEmptySync"));       
        suite.addTest(new SynchronizationMementoTest("testEmptySync2"));       
        return suite;
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationCleanerTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationCleanerTest.class);
    }
}
