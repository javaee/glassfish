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

package com.sun.enterprise.ee.nodeagent.mbeans;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.ee.admin.proxy.MBeanServerProxy;
import com.sun.enterprise.ee.nodeagent.mbeans.mbeanapi.NodeAgentsConfigMBean;
import com.sun.enterprise.ee.nodeagent.NodeAgent;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

public class NodeAgentConfigMBeanTest extends TestCase {
   
    public NodeAgentConfigMBeanTest(String name) {
        super(name);        
    }       

    private static NodeAgentsConfigMBean _proxy = null;
    
    private synchronized NodeAgentsConfigMBean getProxy()        
    {
        if (_proxy == null) {
            try {
                MBeanServerConnectionInfo connInfo = new MBeanServerConnectionInfo(
                    "localhost", "8686", "jmxmp", "admin", "admin123");
                ObjectName objname  = new ObjectName(IAdminConstants.DAS_NODECONTROLLER_MBEAN_NAME);            
                _proxy = (NodeAgentsConfigMBean)MBeanServerProxy.getMBeanServerProxy(
                    NodeAgentsConfigMBean.class, objname, connInfo);
                //force a connection to the mbean server
                InvocationHandler ih = Proxy.getInvocationHandler(_proxy);
                ((MBeanServerProxy)ih).connect();                
            } catch (Exception ex) {                
                _proxy = null;
                System.out.println("!!!!FATAL ERROR: Could not connect to DAS");                
                System.out.println("!!!!FATAL ERROR: NO tests were run");                
                System.out.println("!!!!FATAL ERROR: " + ex.toString());
                System.exit(1);                
            }
        }
        return _proxy;
    }
    
    public void testUnbindNodeAgent() {                        
        String failure = null;
        NodeAgentsConfigMBean proxy = getProxy();
        try {                                                
            proxy.unbindNodeAgent("foo");            
            failure = "unbindNodeAgent: unbinding a non-existent agent succeeded";    
        } catch (Exception ex) {}
        if (failure != null) {
            fail(failure);
        }
    }

    public void testBindNodeAgent() {
        String failure = null;
        NodeAgentsConfigMBean proxy = getProxy();
        try {                                                
            proxy.bindNodeAgent("localhost", "1234", "admin", "admin123", "agent1");            
            proxy.unbindNodeAgent("agent1");     
        } catch (Exception ex) {
            failure = "bindNodeAgent: binding failed " + ex.toString();    
        }
        if (failure != null) {
            fail(failure);
        }
    }
    
    /*
    public static TestSuite suite() {
        //To run all tests
        return new TestSuite(NodeAgentConfigMBeanTest.class);
        //To run a subset of the tests
        TestSuite suite = new TestSuite();
        suite.addTest(new NodeAgentConfigMBeanTest("test1"));       
        suite.addTest(new NodeAgentConfigMBeanTest("test2"));       
        return suite;
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(NodeAgentConfigMBeanTest.suite());
    }
    */

    public static void main(String args[]) {
        junit.textui.TestRunner.run(NodeAgentConfigMBeanTest.class);
    }
}
