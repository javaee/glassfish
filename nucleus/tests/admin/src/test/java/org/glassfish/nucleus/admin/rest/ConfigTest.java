/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin.rest;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class ConfigTest extends RestTestBase {

    public static final String BASE_CONFIGS_URL = "/domain/configs";

    @Test
    public void testConfigCopy() {
        String configName = "config-" + generateRandomString();
        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("id", "default-config");
        formData.add("id", configName);
        createAndVerifyConfig(configName, formData);
        deleteAndVerifyConfig(configName);
    }

    @Test
    public void duplicateCopyShouldFail() {
        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("id", "default-config");
        formData.add("id", "server-config");

        Response response = post(BASE_CONFIGS_URL + "/copy-config", formData);
        assertFalse(isSuccess(response));
    }

    public void createAndVerifyConfig(String configName, MultivaluedMap configData) {
        Response response = post(BASE_CONFIGS_URL + "/copy-config", configData);
        checkStatusForSuccess(response);

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        checkStatusForSuccess(response);
    }

    public void deleteAndVerifyConfig(String configName) {
        Response response = post(BASE_CONFIGS_URL + "/config/" + configName + "/delete-config");
        checkStatusForSuccess(response);

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        assertFalse(isSuccess(response));
    }
}
