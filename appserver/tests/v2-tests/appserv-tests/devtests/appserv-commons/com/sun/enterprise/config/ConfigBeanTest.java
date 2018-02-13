/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config;

import junit.framework.*;
import org.xml.sax.InputSource;
import java.io.FileReader;
import java.io.File;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.Domain;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.BaseProperty;
import org.netbeans.modules.schema2beans.Common;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.2 $
 */

public class ConfigBeanTest extends TestCase {
    public void testBaseBean() throws Exception {
        JmxConnector jc = new JmxConnector();
        assertEquals("", ""+jc);
    }

        // IN this test we're trying to find out how to determine if
        // we should return an xpath with a predicate. It would appear
        // that if an element has a bean property, and taht property
        // says its indexed, then the xpath should have a predicate.
    public void testBeanProperty() throws Exception {
        ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        configCtx.refresh();
        NodeAgents nas = (NodeAgents) configCtx.exactLookup("/domain/node-agents");
        assertNotNull("Null Node Agents by exactLookup(\"/domain/node-agents\")", nas);
        NodeAgent na = nas.getNodeAgentByName("na1");
        assertNotNull("Null Node Agent!", na);
        assertEquals("NodeAgent", na.name());
        assertEquals("node-agent", na.dtdName());
        BaseProperty bp = na.getProperty("jmx-connector");
        assertNotNull("bean property fetch returned null", bp);
        assertEquals("unexpected bean type", Common.TYPE_0_1, bp.getInstanceType());
        BeanProp na_bp = na.beanProp();
        assertNotNull("bean property is null", na_bp);
        assertTrue("Not indexed", na_bp.isIndexed());
        JmxConnector jc = new JmxConnector();
        jc.setName("jc1");
        na.setJmxConnector(jc);
        assertNotNull(jc.beanProp());
        assertTrue(!jc.beanProp().isIndexed());
        AdminService as = (AdminService) configCtx.exactLookup("/domain/configs/config[@name='server-config']/admin-service");
        jc = new JmxConnector();
        jc.setName("jc1");
        as.addJmxConnector(jc);
        assertNotNull(jc.beanProp());
        assertTrue(jc.beanProp().isIndexed());
        
    }
    
        
        // This test demonstrates that an element that has different
        // cardinality will have different xpath expressions
    public void testXPath() throws Exception {
        ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        configCtx.refresh();
        NodeAgents nas = (NodeAgents) configCtx.exactLookup("/domain/node-agents");
        assertNotNull("Null Node Agents by exactLookup(\"/domain/node-agents\")", nas);
        NodeAgent na = nas.getNodeAgentByName("na1");
        assertNotNull("Null Node Agent!", na);
        JmxConnector jc = new JmxConnector();
        jc.setName("jc1");
        na.setJmxConnector(jc);
        assertEquals("/domain/node-agents/node-agent[@name='na1']/jmx-connector", na.getJmxConnector().getXPath());
        assertEquals(jc, configCtx.exactLookup(na.getJmxConnector().getXPath()));
        AdminService as = (AdminService) configCtx.exactLookup("/domain/configs/config[@name='server-config']/admin-service");
        jc = new JmxConnector();
        jc.setName("jc1");
        as.addJmxConnector(jc);
        assertEquals("/domain/configs/config[@name='server-config']/admin-service/jmx-connector[@name='jc1']", as.getJmxConnectorByName("jc1").getXPath());
        

    }

    public ConfigBeanTest(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
        Runtime.getRuntime().exec("cp domain.orig.xml domain.xml").waitFor();
    }

    protected void tearDown() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(ConfigBeanTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ConfigBeanTest(args[i]));
        }
        return ts;
    }

        
}
