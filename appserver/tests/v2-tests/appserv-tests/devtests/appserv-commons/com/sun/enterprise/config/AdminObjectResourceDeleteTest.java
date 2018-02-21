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

import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import java.io.File;
import java.io.FileReader;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseProperty;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.xml.sax.InputSource;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.2 $
 */

public class AdminObjectResourceDeleteTest extends TestCase {
    public void testWithClonedContext() throws Exception {
        // Set up the domain.xml file - adding an admin object resource to it
        AdminObjectResource aor = new AdminObjectResource();
        aor.setJndiName("toby/aor");
        aor.setObjectType("user");
        aor.setResAdapter("generic-ra");
        aor.setResType("connector.MyAdminObject");
        final ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        final Resources r = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r);
        r.addAdminObjectResource(aor);
        assertEquals(aor, r.getAdminObjectResourceByJndiName("toby/aor"));
        configCtx.flush();

        // Construct the deleter:
        final String xpath=aor.getXPath();      
        assertEquals("/domain/resources/admin-object-resource[@jndi-name='toby/aor']", xpath);
        ConfigDelete configDelete=ConfigChangeFactory.createConfigDelete(xpath); 
        assertEquals("delete xpath=/domain/resources/admin-object-resource[@jndi-name='toby/aor']", ""+configDelete);
  
        final ConfigContext ctx2 = (ConfigContext)configCtx.clone();
        ctx2.updateFromConfigChange(configDelete);
        
          // Demonstrate that its no longer there for ctx2
        Resources r2 = (Resources) ServerBeansFactory.getDomainBean(ctx2).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        assertNull("Unexpected AdminObjectResource from ctx2", r2.getAdminObjectResourceByJndiName("toby/aor"));
 
        // If we don't do a flush()/refresh() we find that the AdminObject is still available
        // from the original context
        
        r2 = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        
        // WE DONT WANT THIS - THIS IS THE PROBLEM
        assertNotNull("Unexpected AdminObjectResource from configCtx", r2.getAdminObjectResourceByJndiName("toby/aor"));

        
        // The flush()/refresh() is necessary to get the changed clone to write to disc
        // and the original to read from disc, thus communicating the change between them
        ctx2.flush();
      configCtx.refresh(true);
        
          // and now its gone away!
        r2 = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        assertNull("Unexpected AdminObjectResource from configCtx", r2.getAdminObjectResourceByJndiName("toby/aor"));
    }

    public AdminObjectResourceDeleteTest(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
        Runtime.getRuntime().exec("cp domain.orig.xml domain.xml").waitFor();
    }

    protected void tearDown() throws Exception {
        //Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(AdminObjectResourceDeleteTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new AdminObjectResourceDeleteTest(args[i]));
        }
        return ts;
    }

        
}
