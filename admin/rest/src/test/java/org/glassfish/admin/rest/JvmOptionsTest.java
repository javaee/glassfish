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

package org.glassfish.admin.rest;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;

import org.glassfish.admin.rest.clientutils.MarshallingUtils;

import java.util.List;
import java.util.Map;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class JvmOptionsTest extends RestTestBase {
    protected static final String URL_SERVER_JVM_OPTIONS = "/domain/configs/config/server-config/java-config/jvm-options";
    protected static final String URL_DEFAULT_JVM_OPTIONS = "/domain/configs/config/default-config/java-config/jvm-options";

    @Test
    public void getJvmOptions() {
        ClientResponse response = get(URL_SERVER_JVM_OPTIONS);
        assertTrue(isSuccess(response));
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(response.getEntity(String.class));
        List<String> jvmOptions = (List<String>)((Map)responseMap.get("extraProperties")).get("leafList");
        assertTrue(jvmOptions.size() > 0);
    }

    @Test
    public void createAndDeleteOptions() {
        final String optionName = "-Doption" + generateRandomString();
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(optionName, "someValue");
        }};

//        Map<String, String> payload = buildJvmOptionsPayload(newOptions);
        ClientResponse response = post(URL_SERVER_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_SERVER_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName+"=someValue"));

        response = delete(URL_SERVER_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_SERVER_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(optionName+"=someValue"));
    }

    @Test
    public void createAndDeleteOptionsWithoutValues() {
        final String option1Name = "-Doption" + generateRandomString();
        final String option2Name = "-Doption" + generateRandomString();
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(option1Name, "");
            put(option2Name, "");
        }};

        ClientResponse response = post(URL_SERVER_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_SERVER_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(option1Name));
        assertFalse(jvmOptions.contains(option1Name+"="));
        assertTrue(jvmOptions.contains(option2Name));
        assertFalse(jvmOptions.contains(option2Name+"="));

        response = delete(URL_SERVER_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_SERVER_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(option1Name));
        assertFalse(jvmOptions.contains(option2Name));
    }

    @Test
    public void testIsolatedOptionsCreationOnNewConfig() {
        ConfigTest configTest = new ConfigTest();
        final String optionName = "-Doption" + generateRandomString();
        final String configName = "config-" + generateRandomString();
        final String URL_NEW_CONFIG_JVM_OPTIONS = "/domain/configs/config/" + configName + "/java-config/jvm-options";

        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(optionName, "");
        }};
        MultivaluedMap formData = new MultivaluedMapImpl() {{
            add("id", "default-config");
            add("id", configName);
        }};
        configTest.setup();
        configTest.createAndVerifyConfig(configName, formData);

        // Test new config to make sure option is there
        ClientResponse response = post(URL_NEW_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_NEW_CONFIG_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName));

        // Test server-config to make sure the options are NOT there
        response = get(URL_SERVER_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(optionName));

        configTest.deleteAndVerifyConfig(configName);
    }

    protected List<String> getJvmOptions(ClientResponse response) {
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(response.getEntity(String.class));
        List<String> jvmOptions = (List<String>)((Map)responseMap.get("extraProperties")).get("leafList");

        return jvmOptions;
    }
}
