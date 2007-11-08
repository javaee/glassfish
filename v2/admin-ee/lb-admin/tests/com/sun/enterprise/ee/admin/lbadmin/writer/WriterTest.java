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
package com.sun.enterprise.ee.admin.lbadmin.writer;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.List;
import java.io.File;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Domain;

import com.sun.enterprise.ee.admin.lbadmin.reader.impl.LoadbalancerReaderImpl;
import com.sun.enterprise.ee.admin.lbadmin.transform.LoadbalancerVisitor;
import com.sun.enterprise.ee.admin.lbadmin.beans.Loadbalancer;

import org.netbeans.modules.schema2beans.Schema2BeansException;

public class WriterTest extends TestCase {

    public WriterTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testLBWithTwoServers() {         
        try {
            ConfigContext ctx =  null;
            try {
                ctx = ConfigFactory.createConfigContext(inputXml); 
            } catch (ConfigException ce) {
                ce.printStackTrace();
            }
            LbConfigWriter lbw = new LbConfigWriter(ctx, testLbName3,
                            "." );
            lbw.write();
            assertTrue( new File(".",lbFile).exists());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testLBWithServer() {         
        try {
            ConfigContext ctx =  null;
            try {
                ctx = ConfigFactory.createConfigContext(inputXml); 
            } catch (ConfigException ce) {
                ce.printStackTrace();
            }
            LbConfigWriter lbw = new LbConfigWriter(ctx, testLbName2,
                            destPath );
            lbw.write();
            assertTrue( new File(destPath).exists());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testLBWithCluster() {         
        try {
            ConfigContext ctx =  null;
            try {
                ctx = ConfigFactory.createConfigContext(inputXml); 
            } catch (ConfigException ce) {
                ce.printStackTrace();
            }
            LbConfigWriter lbw = new LbConfigWriter(ctx, testLbName,
                            defaultLocation );
            lbw.write();
            assertTrue( new File(defaultLocation, lbFile).exists());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(WriterTest.class);
    }

    // --- PRIVATE VARS -------

    String testLbName = "lb1";
    String testLbName2 = "lb2";
    String testLbName3 = "lb3";
    String defaultLocation = System.getProperty("java.io.tmpdir");
    String destPath = System.getProperty("java.io.tmpdir") + File.separator + 
                            "test-loadbalancer.xml";
    String inputXml = "tests/com/sun/enterprise/ee/admin/lbadmin/test-domain.xml";
    String lbFile = "loadbalancer.xml";

}
