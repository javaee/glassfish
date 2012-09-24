/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.aod;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.internal.api.Globals;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;

public class TestUtil {

    public static void compareAODD(Map<String,AdministeredObjectDefinitionDescriptor> expectedAODDs, 
            Set<AdministeredObjectDefinitionDescriptor> actualAODDs) throws Exception{
        
        for(AdministeredObjectDefinitionDescriptor actualDesc : actualAODDs){
            assertNotNull("the name of administered object cannot be null.", actualDesc.getName());
            
            AdministeredObjectDefinitionDescriptor expectedDesc = expectedAODDs.get(actualDesc.getName());
            assertNotNull("The AOD of the name ["+actualDesc.getName()+"] is not expected.", expectedDesc);
            
            assertEquals("Fail to verify class-name of the AODD:"+actualDesc.getName(),
                    expectedDesc.getClassName(), actualDesc.getClassName());
            
            assertEquals("Fail to verify description of the AODD:"+actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());
            
            assertEquals("Fail to verify resourceAdapterName of the AODD:"+actualDesc.getName(),
                    expectedDesc.getResourceAdapterName(), actualDesc.getResourceAdapterName());
            
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
