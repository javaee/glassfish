/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.cfd;

import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.internal.api.Globals;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static junit.framework.Assert.*;

public class TestUtil {

    public static void compareCFDD(Map<String,ConnectionFactoryDefinitionDescriptor> expectedCFDDs, 
            Set<ResourceDescriptor> actualCFDDs) throws Exception{
        
        for(Descriptor descriptor : actualCFDDs){
            ConnectionFactoryDefinitionDescriptor actualDesc = (ConnectionFactoryDefinitionDescriptor)descriptor;
            assertNotNull("the name of connector resource cannot be null.", actualDesc.getName());
            
            ConnectionFactoryDefinitionDescriptor expectedDesc = expectedCFDDs.get(actualDesc.getName());
            assertNotNull("The CFD of the name ["+actualDesc.getName()+"] is not expected.", expectedDesc);
            
            assertEquals("Fail to verify class-name of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getInterfaceName(), actualDesc.getInterfaceName());
            
            assertEquals("Fail to verify resource-adapter of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getResourceAdapter(), actualDesc.getResourceAdapter());
            
            assertEquals("Fail to verify transaction-support of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getTransactionSupport(), actualDesc.getTransactionSupport());
            
            assertEquals("Fail to verify max-pool-size of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getMaxPoolSize(), actualDesc.getMaxPoolSize());
            
            assertEquals("Fail to verify min-pool-size of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getMinPoolSize(), actualDesc.getMinPoolSize());
            
            assertEquals("Fail to verify description of the CFDD:"+actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());
            
            Properties expectedProps = expectedDesc.getProperties();
            Properties actualProps = actualDesc.getProperties();
            
            for(Object name : actualProps.keySet()){
                assertEquals("Fail to verify property ("+name+") of the CFDD:"+actualDesc.getName(),
                        expectedProps.get(name), actualProps.get(name));
            }
            
            assertEquals("Fail to verify size of properties of the CFDD:"+actualDesc.getName(),
                    expectedProps.size(), actualProps.size());
            
            expectedCFDDs.remove(actualDesc.getName());
        }
        if(expectedCFDDs.size()>0){
            StringBuilder sb = new StringBuilder();
            for(String name : expectedCFDDs.keySet()){
                sb.append("  "+name+"\n");
            }
            fail("Still has expected "+ expectedCFDDs.size()+" CFDs: \n"+sb.toString());
        }
    }

    public static void setupHK2() throws Exception{
        Globals.getStaticHabitat();
        assertNotNull("The global habitat is not initialized.", Globals.getDefaultHabitat());

    }
    public static Object getByType(Class clz) throws Exception{
        setupHK2();
        return Globals.getDefaultHabitat().getService(clz);
    }
}
