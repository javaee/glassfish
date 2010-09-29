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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class BackupTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_SERVER = "General Information";
    private static final String TRIGGER_SCHEDULES = "Schedules";
    private static final String TRIGGER_NEW_SCHEDULE = "Create new Schedule";
    private static final String TRIGGER_BACKUP_CONFIGS = "Backup Configurations";
    private static final String TRIGGER_NEW_BACKUP_CONFIG = "Create New Backup Configuration";

    @Test
    public void testSchedules() {

        String testSchedule = generateRandomString();

        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_SERVER);
        clickAndWait("propertyForm:serverInstTabs:dasrecoveryTab", TRIGGER_SCHEDULES);
        clickAndWait("propertyForm:schedulesTable:topActionsGroup1:newButton", TRIGGER_NEW_SCHEDULE);
        //create the schedule
        selenium.type("form:propertySheet:propertSectionTextField:nameNew:name", testSchedule);
        clickAndWait("form:propertyContentPage:topButtons:newButton", TRIGGER_SCHEDULES);
        assertTrue(selenium.isTextPresent(testSchedule));

        //deleteRow("propertyForm:schedulesTable:topActionsGroup1:button1", "propertyForm:schedulesTable", testSchedule);

    }

    @Test
    public void testBackupConfigs() {

        String testBackupConfig = generateRandomString();

        clickAndWait("treeForm:tree:applicationServer:applicationServer_link", TRIGGER_SERVER);
        clickAndWait("propertyForm:serverInstTabs:dasrecoveryTab", TRIGGER_SCHEDULES);
        assertTrue(selenium.isTextPresent("Schedules"));
        clickAndWait("propertyForm:serverInstTabs:dasrecoveryTab:backupConfigsTab", TRIGGER_BACKUP_CONFIGS);
        assertTrue(selenium.isTextPresent(TRIGGER_BACKUP_CONFIGS));
    }

    
}

