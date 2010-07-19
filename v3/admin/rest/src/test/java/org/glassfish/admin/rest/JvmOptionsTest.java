/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import java.util.Map;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class JvmOptionsTest extends RestTestBase {
    protected static final String URL_JVM_OPTIONS = BASE_URL_DOMAIN + "/configs/config/server-config/java-config/jvm-options";

    @Test
    public void getJvmOptions() {
        ClientResponse response = get(URL_JVM_OPTIONS);
        assertTrue(isSuccess(response));
        Map<String, String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.size() > 0);
    }

    @Test
    public void createAndDeleteOptions() {
        final String optionName = "-Doption" + generateRandomString();
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(optionName, "someValue");
        }};

        Map<String, String> payload = buildJvmOptionsPayload(newOptions);
        ClientResponse response = post(URL_JVM_OPTIONS, payload);
        assertTrue(isSuccess(response));
        response = get(URL_JVM_OPTIONS);
        assertTrue(getJvmOptions(response).containsKey(optionName));

        response = delete(URL_JVM_OPTIONS, payload);
        assertTrue(isSuccess(response));
        response = get(URL_JVM_OPTIONS);
        assertFalse(getJvmOptions(response).containsKey(optionName));
    }

    protected Map<String, String> buildJvmOptionsPayload(Map<String, String> options) {
        Map<String, String> payload = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
        String sep = "";

        for (Map.Entry<String,String> entry : options.entrySet()) {
            sb.append(sep)
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        payload.put("id", sb.toString());

        return payload;
    }

    protected Map<String, String> getJvmOptions(ClientResponse response) {
        Map<String, String> options = new HashMap<String, String>();
        Document doc = this.getDocument(response.getEntity(String.class));

        NodeList list = doc.getElementsByTagName("JvmOption");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            String text = node.getTextContent();
            int equal = text.indexOf("=");
            String key;
            String value = "";
            if (equal > -1) {
                key = text.substring(0, equal);
                value = text.substring(equal+1);
            } else {
                key = text;
            }
            options.put(key, value);
        }


        return options;
    }
}
