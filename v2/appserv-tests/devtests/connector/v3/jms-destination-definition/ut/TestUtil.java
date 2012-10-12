/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.jmsdd;

import com.sun.enterprise.deployment.JMSDestinationDefinitionDescriptor;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.glassfish.internal.api.Globals;

public class TestUtil {

    public static void compareJMSDDD(Map<String, JMSDestinationDefinitionDescriptor> expectedJMSDDDs,
            Set<JMSDestinationDefinitionDescriptor> actualJMSDDDs) {

        for (JMSDestinationDefinitionDescriptor actualDesc : actualJMSDDDs) {
            assertNotNull("The JMSDestinationDefinitionDescriptor name cannot be null.", actualDesc.getName());

            JMSDestinationDefinitionDescriptor expectedDesc = expectedJMSDDDs.get(actualDesc.getName());
            assertNotNull("The JMSDestinationDefinitionDescriptor of the name [" + actualDesc.getName() + "] is not expected.", expectedDesc);

            assertEquals("Fail to verify description of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                    expectedDesc.getDescription(), actualDesc.getDescription());

            assertEquals("Fail to verify class-name of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                    expectedDesc.getClassName(), actualDesc.getClassName());

            assertEquals("Fail to verify resource-adapter-name of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                    expectedDesc.getResourceAdapterName(), actualDesc.getResourceAdapterName());

            assertEquals("Fail to verify destination-name of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                    expectedDesc.getDestinationName(), actualDesc.getDestinationName());

            Properties expectedProps = expectedDesc.getProperties();
            Properties actualProps = actualDesc.getProperties();

            for (Object name : actualProps.keySet()) {
                assertEquals("Fail to verify property (" + name + ") of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                        expectedProps.get(name), actualProps.get(name));
            }

            assertEquals("Fail to verify size of properties of the JMSDestinationDefinitionDescriptor:" + actualDesc.getName(),
                    expectedProps.size(), actualProps.size());

            expectedJMSDDDs.remove(actualDesc.getName());
        }

        if (expectedJMSDDDs.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String name : expectedJMSDDDs.keySet()) {
                sb.append("  " + name + "\n");
            }
            fail("Still has expected " + expectedJMSDDDs.size() + " JMSDestinationDefinitionDescriptors: \n" + sb.toString());
        }
    }

    public static void setupHK2() {
        Globals.getStaticHabitat();
        assertNotNull("The global habitat is not initialized.", Globals.getDefaultHabitat());
    }

    public static Object getByType(Class clz) {
        setupHK2();
        return Globals.getDefaultHabitat().getService(clz);
    }
}
