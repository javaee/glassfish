/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EjbContainerTest extends BaseSeleniumTestClass {
    private static final String TAB_EJB_SETTINGS = "Enterprise Java Beans (EJB)";
    private static final String TAB_MDB_SETTINGS = "MDB Default Pool Settings";
    private static final String TAB_EJB_TIMER_SERVICE = "The EJB timer service enables you";

    @Test
    public void testEjbSettings() {
        final String minSize = Integer.toString(generateRandomNumber(64));
        final String maxSize = Integer.toString(generateRandomNumber(64));
        final String poolResize = Integer.toString(generateRandomNumber(64));
        final String timeout = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configurations:server-config:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);

        selenium.click("form1:propertySheet:generalPropertySection:commitOptionProp:optC");
        selenium.type("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize", minSize);
        selenium.type("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize", maxSize);
        selenium.type("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize", poolResize);
        selenium.type("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout", timeout);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbSettingsTab", TAB_EJB_SETTINGS);
        assertEquals(true, selenium.isChecked("form1:propertySheet:generalPropertySection:commitOptionProp:optC"));
        assertEquals(minSize, selenium.getValue("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize"));
        assertEquals(maxSize, selenium.getValue("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize"));
        assertEquals(poolResize, selenium.getValue("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize"));
        assertEquals(timeout, selenium.getValue("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testMdbSettings() {
        final String minSize = Integer.toString(generateRandomNumber(64));
        final String maxSize = Integer.toString(generateRandomNumber(64));
        final String poolResize = Integer.toString(generateRandomNumber(64));
        final String timeout = Integer.toString(generateRandomNumber(600));

        clickAndWait("treeForm:tree:configurations:server-config:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);

        selenium.type("form1:propertySheet:propertySectionTextField:MinSizeProp:MinSize", minSize);
        selenium.type("form1:propertySheet:propertySectionTextField:MaxSizeProp:MaxSize", maxSize);
        selenium.type("form1:propertySheet:propertySectionTextField:PoolResizeProp:PoolResize", poolResize);
        selenium.type("form1:propertySheet:propertySectionTextField:TimeoutProp:Timeout", timeout);
        int count = addTableRow("form1:basicTable", "form1:basicTable:topActionsGroup1:addSharedTableButton");

        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("treeForm:tree:configurations:server-config:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);

        assertEquals(minSize, selenium.getValue("form1:propertySheet:propertySectionTextField:MinSizeProp:MinSize"));
        assertEquals(maxSize, selenium.getValue("form1:propertySheet:propertySectionTextField:MaxSizeProp:MaxSize"));
        assertEquals(poolResize, selenium.getValue("form1:propertySheet:propertySectionTextField:PoolResizeProp:PoolResize"));
        assertEquals(timeout, selenium.getValue("form1:propertySheet:propertySectionTextField:TimeoutProp:Timeout"));
        assertTableRowCount("form1:basicTable", count);
    }

    @Test
    public void testEjbTimerService() {
        final String minDelivery = Integer.toString(generateRandomNumber(5000));
        final String maxRedelivery = Integer.toString(generateRandomNumber(10));
        final String redeliveryInterval = Integer.toString(generateRandomNumber(20000));
        final String timerDatasource = "jndi/" + generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbTimerTab", TAB_EJB_TIMER_SERVICE);

        selenium.type("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery", minDelivery);
        selenium.type("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery", maxRedelivery);
        selenium.type("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr", redeliveryInterval);
        selenium.type("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource", timerDatasource);
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbTimerTab", TAB_EJB_TIMER_SERVICE);

        assertEquals(minDelivery, selenium.getValue("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery"));
        assertEquals(maxRedelivery, selenium.getValue("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery"));
        assertEquals(redeliveryInterval, selenium.getValue("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr"));
        assertEquals(timerDatasource, selenium.getValue("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource"));

        // Clean up after ourselves, just because... :)
        selenium.type("form1:propertySheet:propertySectionTextField:MinDeliveryProp:MinDelivery", "1000");
        selenium.type("form1:propertySheet:propertySectionTextField:MaxRedeliveryProp:MaxRedelivery", "1");
        selenium.type("form1:propertySheet:propertySectionTextField:RedeliveryIntrProp:RedeliveryIntr", "5000");
        selenium.type("form1:propertySheet:propertySectionTextField:TimerDatasourceProp:TimerDatasource", "");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
    }

    //Test that the default button in EJB Settings will fill in the default value when pressed.
    @Test
    public void testEjbSettingsDefault() {

        //Go to EJB Settings page, enter some random value
        clickAndWait("treeForm:tree:configurations:server-config:ejbContainer:ejbContainer_link", TAB_EJB_SETTINGS);
        selenium.click("form1:propertySheet:generalPropertySection:commitOptionProp:optC");
        selenium.type("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize", "2");
        selenium.type("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize", "34");
        selenium.type("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize", "10");
        selenium.type("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout", "666");
        selenium.type("form1:propertySheet:cacheSettingSection:MaxCacheProp:MaxCache", "520");
        selenium.type("form1:propertySheet:cacheSettingSection:CacheResizeProp:CacheResize", "36");
        selenium.type("form1:propertySheet:cacheSettingSection:RemTimoutProp:RemTimout", "5454");
        selenium.select("form1:propertySheet:cacheSettingSection:RemPolicyProp:RemPolicy", "label=First In First Out (fifo)");
        selenium.type("form1:propertySheet:cacheSettingSection:CacheIdleProp:CacheIdle", "666");

        //Save this, goto another tab and back
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);
        clickAndWait("form1:ejbContainerTabs:mdbSettingsTab", TAB_MDB_SETTINGS);
        clickAndWait("form1:ejbContainerTabs:ejbSettingsTab", TAB_EJB_SETTINGS);

        //Now, click the default button and ensure all the default values are filled in
        //The default value should match whats specified in the config bean.
        //Save and come back to the page to assert.

        //Location should not be changed by the default button
        String location = selenium.getValue("form1:propertySheet:generalPropertySection:SessionStoreProp:SessionStore");

        //We are testing that default button fills in the correct default value, not testing if the Save button works.
        //no need to click Save for this test.
        clickAndWaitForButtonEnabled("form1:propertyContentPage:loadDefaultsButton");

        assertEquals(location, selenium.getValue("form1:propertySheet:generalPropertySection:SessionStoreProp:SessionStore"));
        
        assertEquals(true, selenium.isChecked("form1:propertySheet:generalPropertySection:commitOptionProp:optB"));
        assertEquals("0", selenium.getValue("form1:propertySheet:poolSettingSection:MinSizeProp:MinSize"));
        assertEquals("32", selenium.getValue("form1:propertySheet:poolSettingSection:MaxSizeProp:MaxSize"));
        assertEquals("8", selenium.getValue("form1:propertySheet:poolSettingSection:PoolResizeProp:PoolResize"));
        assertEquals("600", selenium.getValue("form1:propertySheet:poolSettingSection:TimeoutProp:Timeout"));
        assertEquals("512", selenium.getValue("form1:propertySheet:cacheSettingSection:MaxCacheProp:MaxCache"));
        assertEquals("32", selenium.getValue("form1:propertySheet:cacheSettingSection:CacheResizeProp:CacheResize"));
        assertEquals("5400", selenium.getValue("form1:propertySheet:cacheSettingSection:RemTimoutProp:RemTimout"));
        assertEquals("nru", selenium.getValue("form1:propertySheet:cacheSettingSection:RemPolicyProp:RemPolicy"));
        assertEquals("600", selenium.getValue("form1:propertySheet:cacheSettingSection:CacheIdleProp:CacheIdle"));

        //will be nice to have the default value back for the server.
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

    }
}
