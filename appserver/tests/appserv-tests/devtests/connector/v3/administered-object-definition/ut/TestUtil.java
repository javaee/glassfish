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

package com.sun.s1asdev.aod;

import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import org.glassfish.internal.api.Globals;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static junit.framework.Assert.*;

public class TestUtil {

    public static void compareAODD(Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs, 
            Set<ResourceDescriptor> actualAODDs) throws Exception{
        
        for(ResourceDescriptor descriptor : actualAODDs){
            AdministeredObjectDefinitionDescriptor actualDesc = (AdministeredObjectDefinitionDescriptor)descriptor;
            assertNotNull("the name of administered object cannot be null.", actualDesc.getName());
            
            AdministeredObjectDefinitionDescriptor expectedDesc = expectedAODDs.get(actualDesc.getName());
            assertNotNull("The AOD of the name ["+actualDesc.getName()+"] is not expected.", expectedDesc);
            
            assertEquals("Fail to verify interface-type of the AODD:"+actualDesc.getName(),
                    expectedDesc.getInterfaceName(), actualDesc.getInterfaceName());

            assertEquals("Fail to verify class-name of the AODD:"+actualDesc.getName(),
                    expectedDesc.getClassName(), actualDesc.getClassName());
            
            assertEquals("Fail to verify description of the AODD:"+actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());
            
            assertEquals("Fail to verify resourceAdapter of the AODD:"+actualDesc.getName(),
                    expectedDesc.getResourceAdapter(), actualDesc.getResourceAdapter());
            
            Properties expectedProps = expectedDesc.getProperties();
            Properties actualProps = actualDesc.getProperties();
            
            for(Object name : actualProps.keySet()){
                assertEquals("Fail to verify property ("+name+") of the AODD:"+actualDesc.getName(),
                        expectedProps.get(name), actualProps.get(name));
            }
            
            assertEquals("Fail to verify size of properties of the AODD:"+actualDesc.getName(),
                    expectedProps.size(), actualProps.size());
            
            expectedAODDs.remove(actualDesc.getName());
        }
        if(expectedAODDs.size()>0){
            StringBuilder sb = new StringBuilder();
            for(String name : expectedAODDs.keySet()){
                sb.append("  "+name+"\n");
            }
            fail("Still has expected "+ expectedAODDs.size()+" CRDs: \n"+sb.toString());
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
