/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.crd;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.internal.api.Globals;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import com.sun.enterprise.deployment.ConnectorResourceDefinitionDescriptor;

public class TestUtil {

    public static void compareCRDD(Map<String,ConnectorResourceDefinitionDescriptor> expectedCRDDs, 
            Set<ConnectorResourceDefinitionDescriptor> actualCRDDs) throws Exception{
        
        for(ConnectorResourceDefinitionDescriptor actualDesc : actualCRDDs){
            assertNotNull("the name of connector resource cannot be null.", actualDesc.getName());
            
            ConnectorResourceDefinitionDescriptor expectedDesc = expectedCRDDs.get(actualDesc.getName());
            assertNotNull("The CRD of the name ["+actualDesc.getName()+"] is not expected.", expectedDesc);
            
            assertEquals("Fail to verify class-name of the CRDD:"+actualDesc.getName(),
                    expectedDesc.getClassName(), actualDesc.getClassName());
            
            assertEquals("Fail to verify description of the CRDD:"+actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());
            
            Properties expectedProps = expectedDesc.getProperties();
            Properties actualProps = actualDesc.getProperties();
            
            for(Object name : actualProps.keySet()){
                assertEquals("Fail to verify property ("+name+") of the CRDD:"+actualDesc.getName(),
                        expectedProps.get(name), actualProps.get(name));
            }
            
            assertEquals("Fail to verify size of properties of the CRDD:"+actualDesc.getName(),
                    expectedProps.size(), actualProps.size());
            
            expectedCRDDs.remove(actualDesc.getName());
        }
        if(expectedCRDDs.size()>0){
            StringBuilder sb = new StringBuilder();
            for(String name : expectedCRDDs.keySet()){
                sb.append("  "+name+"\n");
            }
            fail("Still has expected "+ expectedCRDDs.size()+" CRDs: \n"+sb.toString());
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
