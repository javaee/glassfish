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

/*
 * ConfigHelperTest.java
 *
 * Created on June 10, 2005, 2:45 PM
 */

package com.sun.enterprise.admin.wsmgmt.registry;

/**
 *
 * @author Harpreet Singh
 */

import com.sun.enterprise.admin.wsmgmt.registry.*;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.wsmgmt.registry.ConfigHelper;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigHelperTest extends TestCase {
   
    public ConfigHelperTest(String name) {
        super(name);        
    }       

    public void testListRegistryLocations() throws ConfigException {                        
        try {
            String[] locations = ch_query.listRegistryLocations();
            System.out.println("Looking up Connection Jndi Name. Name" +
                    " should be "+POOL_NAME);
            
            if(locations.length == 1){
                System.out.println("Locations = "+ locations[0]);
                assertEquals("Looked up Registry Location ", POOL_NAME, 
                        locations[0]);
            } else{
                fail(" Returned wrong number of RegistryLocation");
            }
        } catch (ConfigException ce) {
            ce.printStackTrace();
            throw ce;
        }
    }

 
    protected void setUp() {
        try{
            System.out.println("ConfigHelperTest");
            ch_query = ConfigHelper.getInstanceToQueryRegistryLocations();
            ConfigContext ctx = ConfigFactory.createConfigContext(URL);
            ch_query.setConfigContext(ctx);            
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private final static String URL = 
       "tests/com/sun/enterprise/admin/wsmgmt/registry/connectionpool-domain.xml";
    private final static String POOL_NAME = "foojndi";
    
    private ConfigHelper ch_query = null;
    private ConfigHelper ch_add_delete = null;
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ConfigHelperTest.class);
    }
}
