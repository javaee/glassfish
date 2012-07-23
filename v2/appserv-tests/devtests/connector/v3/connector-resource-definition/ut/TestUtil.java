/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.crd;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.sun.enterprise.deployment.ConnectorResourceDefinitionDescriptor;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.hk2.component.ExistingSingletonInhabitant;

public class TestUtil {
    private static Habitat habitat;

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
        }
        
        assertEquals("Fail to verify size of the CRDD.",  expectedCRDDs.size(), actualCRDDs.size());
    }

    public static void setupHabitat(){
        if ((habitat == null)) {
            // Bootstrap a hk2 environment.
            ModulesRegistry registry = new StaticModulesRegistry(Thread.currentThread().getContextClassLoader());
            habitat = registry.createHabitat("default");

            StartupContext startupContext = new StartupContext();
            habitat.add(new ExistingSingletonInhabitant(startupContext));

            habitat.addComponent(new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));
            Globals.setDefaultHabitat(habitat);
            
        }
    }
    public static <T> T getByType(Class<T> clz){
        setupHabitat();
        return habitat.getByType(clz);
    }
}
