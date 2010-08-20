/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Apr 19, 2010
 * Time: 11:34:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class MonitoringTest extends BaseSeleniumTestClass {
    @Test
    public void testMonitoring() {
		selenium.click("treeForm:tree:configurations:server-config:monitor:monitor_link");
//		verifyTrue(selenium.isTextPresent("Enable monitoring for a component or service by selecting either LOW or HIGH. To use the Administration Console for monitoring, Monitoring Service and Monitoring MBeans must both be enabled."));
//		selenium.click("form1:propertySheet:propertSectionTextField:monServiceProp:sun_checkbox130");
//		selenium.click("form1:propertySheet:propertSectionTextField:monMbeansProp:sun_checkbox131");
//		selenium.select("form1:basicTable:rowGroup1:0:col3:level", "label=HIGH");
//		selenium.click("form1:title:topButtons:saveButton");
//		verifyTrue(selenium.isTextPresent("New values successfully saved."));
//		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:monServiceProp:sun_checkbox130"));
//		assertEquals("off", selenium.getValue("form1:propertySheet:propertSectionTextField:monMbeansProp:sun_checkbox131"));
//		verifyTrue(selenium.isElementPresent("form1:basicTable:rowGroup1:0:col3:level"));
    }
}
