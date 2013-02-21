/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
                    expectedDesc.getClassName(), actualDesc.getClassName());
            
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
