/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class MultibyteCharacterTest extends RestTestBase {
    public static final String URL_CREATE_INSTANCE = "/domain/create-instance";

    public static final String serverName = "i1듈2";

    public static final String URL_MB_CHAR_SERVER = "http://localhost:4848/management/domain/servers/server/i1%EB%93%882";

    @Test
    public void serverWithMultibyteCharName() {
        Map<String, String> newServer = new HashMap<String, String>() {
            {
                put("id", serverName);
                put("node", "localhost-domain1");
            }
        };

        post(URL_MB_CHAR_SERVER + "/delete-instance");

        ClientResponse response = post(URL_CREATE_INSTANCE, newServer);
        checkStatusForSuccess(response);

        response = get("/domain/servers/server");
        checkStatusForSuccess(response);

        Map<String, String> childResources = getChildResources(response);
        assertTrue(childResources.containsKey(serverName));
        assertEquals(URL_MB_CHAR_SERVER, childResources.get(serverName));

        response = get(URL_MB_CHAR_SERVER);
        Map<String, String> entity = getEntityValues(response);
        assertEquals(serverName, entity.get("name"));
        assertEquals(serverName + "-config", entity.get("configRef"));

        response = post(URL_MB_CHAR_SERVER + "/delete-instance");
        checkStatusForSuccess(response);
    }

    @Test
    public void encode() {
        try {
            URI uri = new URI(
                    "http",
                    null,
                    "localhost",
                    4848,
                    "/management/domain/servers/server/i1듈2",
                    null, 
                    null);
            System.out.println(uri.toASCIIString());
            System.out.println(URLEncoder.encode("http://localhost:4848/management/domain/servers/server/"+serverName, "UTF-8"));
        } catch (Exception ex) {
            Logger.getLogger(MultibyteCharacterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}