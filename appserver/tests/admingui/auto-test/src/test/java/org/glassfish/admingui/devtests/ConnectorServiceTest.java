/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.devtests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author: jeremy_lv
 * To change this template use File | Settings | File Templates.
 */
public class ConnectorServiceTest extends BaseSeleniumTestClass {

    @Test
    public void testConnectorService() {
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:connectorService:connectorService_link");

        String policy = "derived";
        if (getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy", "value").equals(policy)) {
            policy = "global";
        }
        final String timeout = Integer.toString(generateRandomNumber(120));

        setFieldValue("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout", timeout);
        Select select = new Select(driver.findElement(By.id("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy")));
        select.selectByVisibleText(policy);
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton");
        assertTrue(isElementSaveSuccessful("label_sun4","New values successfully saved."));
        
        gotoDasPage();
        clickAndWait("treeForm:tree:configurations:server-config:connectorService:connectorService_link");
        assertEquals(timeout, getValue("propertyForm:propertySheet:propertSectionTextField:timeout:tiimeout", "value"));
        assertEquals(policy, getValue("propertyForm:propertySheet:propertSectionTextField:ClassLoadingPolicy:ClassLoadingPolicy", "value"));
    }
}
