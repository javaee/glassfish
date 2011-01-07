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
import static org.junit.Assert.assertTrue;

public class JavaMessageServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_GENERAL_INFORMATION = "i18n.instance.GeneralTitle";
    private static final String TRIGGER_JMS_SERVICE = "i18njms.jms.PageHelp";
    private static final String TRIGGER_JMS_HOSTS = "i18njms.jmsHosts.ListPageHelp";
    private static final String TRIGGER_NEW_JMS_HOST = "i18njms.newJmsHost.NewJmsHost";
    private static final String TRIGGER_JMS_PHYSICAL_DESTINATIONS = "i18njms.jmsPhysDestinations.pageHelp";
    private static final String TRIGGER_NEW_JMS_PHYSICAL_DESTINATION = "i18njms.jmsPhysDestinations.newPageTitle";
    private static final String TRIGGER_EDIT_JMS_PHYSICAL_DESTINATION = "i18njms.jmsPhysDestinations.editPageTitle";
    private static final String TRIGGER_FLUSH = "i18njms.jmsPhysDestinations.purged";

    @Test
    public void testJmsService() {
        final String timeout = Integer.toString(generateRandomNumber(90));
        final String interval = Integer.toString(generateRandomNumber(10));
        final String attempts = Integer.toString(generateRandomNumber(10));

        clickAndWait("treeForm:tree:configurations:server-config:jmsConfiguration:jmsConfiguration_link", TRIGGER_JMS_SERVICE);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:timeoutProp:Timeout", timeout);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:intervalProp:Interval", interval);
        selenium.type("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:attemptsProp:Attempts", attempts);
        selenium.select("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:behaviorProp:Behavior", "label=priority");

        int count = addTableRow("propertyForm:propertyContentPage:basicTable", "propertyForm:propertyContentPage:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("propertyForm:propertyContentPage:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("propertyForm:propertyContentPage:basicTable:rowGroup1:0:col3:col1St", "value");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", TRIGGER_NEW_VALUES_SAVED);

        clickAndWait("treeForm:tree:configurations:server-config:jmsConfiguration:jmsHosts:jmsHosts_link", TRIGGER_JMS_HOSTS);
        clickAndWait("treeForm:tree:configurations:server-config:jmsConfiguration:jmsConfiguration_link", TRIGGER_JMS_SERVICE);

        assertEquals(timeout, selenium.getValue("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:timeoutProp:Timeout"));
        assertEquals(interval, selenium.getValue("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:intervalProp:Interval"));
        assertEquals(attempts, selenium.getValue("propertyForm:propertyContentPage:propertySheet:propertSectionTextField:attemptsProp:Attempts"));
        assertTableRowCount("propertyForm:propertyContentPage:basicTable", count);
    }

    @Test
    public void testJmsHosts() {
        String hostText = "host"+generateRandomString();
        String host = "somemachine"+generateRandomNumber(1000);
        String port = Integer.toString(generateRandomNumber(32768));

        clickAndWait("treeForm:tree:configurations:server-config:jmsConfiguration:jmsHosts:jmsHosts_link", TRIGGER_JMS_HOSTS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_JMS_HOST);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:JmsHostTextProp:JmsHostText", hostText);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:HostProp:Host", host);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:PortProp:Port", port);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:AdminUserProp:AdminUser", "admin");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:newPasswordProp:NewPassword", "admin");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:confirmPasswordProp:ConfirmPassword", "admin");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NEW_VALUES_SAVED);
        clickAndWait(this.getLinkIdByLinkText("propertyForm:configs", hostText), "Edit JMS Host");
        assertTrue(selenium.isTextPresent(hostText));
        assertEquals(host, selenium.getValue("propertyForm:propertySheet:propertSectionTextField:HostProp:Host"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JMS_HOSTS);
        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", hostText, "col0", "colName");
    }

    @Test
    public void testJmsPhysicalDestinations() {
        final String name = "dest"+generateRandomString();
        final String maxUnconsumed = Integer.toString(generateRandomNumber(100));
        final String maxMessageSize = Integer.toString(generateRandomNumber(100));
        final String maxTotalMemory = Integer.toString(generateRandomNumber(100));
        final String maxProducers = Integer.toString(generateRandomNumber(500));
        final String consumerFlowLimit = Integer.toString(generateRandomNumber(5000));

        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_GENERAL_INFORMATION);
        clickAndWait("propertyForm:serverInstTabs:jmsPhysDest", TRIGGER_JMS_PHYSICAL_DESTINATIONS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_JMS_PHYSICAL_DESTINATION);

        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:NameTextProp:NameText", name);
        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:maxNumMsgsProp:maxNumMsgs", maxUnconsumed);
        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:maxBytesPerMsgProp:maxBytesPerMsg", maxMessageSize);
        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:maxTotalMsgBytesProp:maxTotalMsgBytes", maxTotalMemory);
        selectDropdownOption("jmsPhysDestForm:propertySheet:propertSectionTextField:limitBehaviorProp:Type", "i18njms.jmsPhysDestinations.REMOVE_LOW_PRIORITY");
        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:maxNumProducersProp:maxNumProducers", maxProducers);
        selenium.type("jmsPhysDestForm:propertySheet:propertSectionTextField:consumerFlowLimitProp:consumerFlowLimit", consumerFlowLimit);
        selectDropdownOption("jmsPhysDestForm:propertySheet:propertSectionTextField:useDmqProp:useDmq", "i18n.common.false");
        selectDropdownOption("jmsPhysDestForm:propertySheet:propertSectionTextField:validateSchemaProp:validateXMLSchemaEnabled", "i18n.common.true");
        clickAndWait("jmsPhysDestForm:propertyContentPage:topButtons:newButton", TRIGGER_JMS_PHYSICAL_DESTINATIONS);

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", name), TRIGGER_EDIT_JMS_PHYSICAL_DESTINATION);

        assertTrue(selenium.isTextPresent(name));
        assertEquals(maxUnconsumed, selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:maxNumMsgsProp:maxNumMsgs"));
        assertEquals(maxMessageSize, selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:maxBytesPerMsgProp:maxBytesPerMsg"));
        assertEquals(maxTotalMemory, selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:maxTotalMsgBytesProp:maxTotalMsgBytes"));
        
        // TODO: These options do not seem to be be supported by the backend. Passing these props to the CLI does not affect its value. Disabling for now.
        // FIXME
//        assertEquals("REMOVE_LOW_PRIORITY", selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:limitBehaviorProp:Type"));
//        assertEquals(maxProducers, selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:maxNumProducersProp:maxNumProducers"));
//        assertEquals("false", selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:useDmqProp:useDmq"));

        assertEquals(consumerFlowLimit, selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:consumerFlowLimitProp:consumerFlowLimit"));
        assertEquals("true", selenium.getValue("jmsPhysDestForm:propertySheet:propertSectionTextField:validateSchemaProp:validateXMLSchemaEnabled"));
        clickAndWait("jmsPhysDestForm:propertyContentPage:topButtons:cancelButton", TRIGGER_JMS_PHYSICAL_DESTINATIONS);

        this.selectTableRowByValue("propertyForm:configs", name);
        clickAndWait("propertyForm:configs:topActionsGroup1:flushButton", TRIGGER_FLUSH);
        this.selectTableRowByValue("propertyForm:configs", name); // Deselect row. This is ugly, but will have to stay this way for now

        deleteRow("propertyForm:configs:topActionsGroup1:deleteButton", "propertyForm:configs", name);
    }
}
