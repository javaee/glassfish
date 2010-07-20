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

import java.util.List;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: May 26, 2010
 * Time: 2:28:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class JmsTest extends RestTestBase {
    static final String URL_ADMIN_OBJECT_RESOURCE = "/domain/resources/admin-object-resource";
    static final String URL_CONNECTOR_CONNECTION_POOL = "/domain/resources/connector-connection-pool";
    static final String URL_CONNECTOR_RESOURCE = "/domain/resources/connector-resource";
    static final String URL_JMS_HOST = "/domain/configs/config/server-config/jms-service/jms-host";
    static final String URL_CREATE_JMS_DEST = "/domain/configs/config/server-config/jms-service/create-jmsdest";
    static final String URL_DELETE_JMS_DEST = "/domain/configs/config/server-config/jms-service/delete-jmsdest";
    static final String URL_LIST_JMS_DEST = "/domain/configs/config/server-config/jms-service/list-jmsdest";
    static final String URL_FLUSH_JMS_DEST = "/domain/configs/config/server-config/jms-service/flush-jmsdest";
    static final String URL_PING_JMS = "/domain/configs/config/server-config/jms-service/jms-ping";

    @Test
    public void testJmsConnectionFactories() {
        final String poolName = "JmsConnectionFactory" + generateRandomString();
        Map<String, String> ccp_attrs = new HashMap<String, String>() {{
            put("name", poolName);
            put("connectiondefinition", "javax.jms.ConnectionFactory");
            put("raname", "jmsra");
        }};
        Map<String, String> cr_attrs = new HashMap<String, String>() {{
            put("id", poolName);
            put("poolname", poolName);
        }};

        // Create connection pool
        ClientResponse response = post(URL_CONNECTOR_CONNECTION_POOL, ccp_attrs);
        assertTrue(isSuccess(response));

        // Check connection pool creation
        Map<String, String> pool = getEntityValues(get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertFalse(pool.isEmpty());

        // Create connector resource
        response = post(URL_CONNECTOR_RESOURCE, cr_attrs);
        assertTrue(isSuccess(response));

        // Check connector resource
        Map<String, String> resource = getEntityValues(get(URL_CONNECTOR_RESOURCE + "/" + poolName));
        assertFalse(resource.isEmpty());

        // Edit and check ccp
        response = post(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, new HashMap<String, String>() {{
                put("description", poolName);
        }});
        assertTrue(isSuccess(response));

        pool = getEntityValues(get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertTrue(pool.get("description").equals(poolName));

        // Edit and check cr
        response = post(URL_CONNECTOR_RESOURCE + "/" + poolName, new HashMap<String, String>() {{
                put("description", poolName);
        }});
        assertTrue(isSuccess(response));

        resource = getEntityValues(get(URL_CONNECTOR_RESOURCE + "/" + poolName));
        assertTrue(pool.get("description").equals(poolName));

        // Delete objects
        response = delete(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, new HashMap<String, String>() {{
                put("cascade", "true");
        }});
        assertTrue(isSuccess(response));
    }

    @Test
    public void testJmsDestinationResources() {
        final String jndiName = "jndi/" + generateRandomString();
        String encodedJndiName = jndiName;
        try {
            encodedJndiName = URLEncoder.encode(jndiName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        Map<String, String> attrs = new HashMap<String, String>() {{
            put("id", jndiName);
            put("raname", "jmsra");
            put("restype", "javax.jms.Topic");
        }};

        ClientResponse response = post(URL_ADMIN_OBJECT_RESOURCE, attrs);
        assertTrue(isSuccess(response));

        Map<String, String> entity = getEntityValues(get(URL_ADMIN_OBJECT_RESOURCE + "/" + encodedJndiName));
        assertFalse(entity.isEmpty());

        response = delete(URL_ADMIN_OBJECT_RESOURCE + "/" + encodedJndiName);
        assertTrue(isSuccess(response));
    }

    @Test
    public void testJmsDest() {
        final String jmsDestName = "jmsDest" + generateRandomString();
        Map<String, String> newDest = new HashMap<String, String>() {{
            put("id", jmsDestName);
            put("destType", "topic");
        }};

        // Test Create
        ClientResponse response = post(URL_CREATE_JMS_DEST, newDest);
        // This command returns 200 instead of 201, for some reason.  Odd.
        assertTrue(isSuccess(response));

        // Test creation. There's no CLI for editing a JMS destination, so we query
        // the broker for the newly created destination to make sure it knows about it
        List<String> list = getCommandResults(get(URL_LIST_JMS_DEST));
        assertTrue(list.contains(jmsDestName));

        // Test deletion
        response = post(URL_DELETE_JMS_DEST, newDest); // You POST to commands
        assertTrue(isSuccess(response));
        list = getCommandResults(get(URL_LIST_JMS_DEST));
        assertFalse(list.contains(jmsDestName));
    }

    @Test
    public void testJmsPing() {
        List<String> results = getCommandResults(get(URL_PING_JMS));
        assertTrue(results.contains("JMS-ping command executed successfully"));
    }

    @Test
    public void testJmsFlush() {
        Map<String, String> payload = new HashMap<String, String>() {{
            put("id", "mq.sys.dmq");
            put("destType", "queue");
        }};

        ClientResponse response = post(URL_FLUSH_JMS_DEST, payload);
        assertTrue(isSuccess(response));
    }

    @Test
    public void testJmsHosts() {
        final String jmsHostName = "jmshost" + generateRandomString();
        Map<String, String> newHost = new HashMap<String, String>() {{
                put("id", jmsHostName);
                put("adminPassword", "admin");
                put("port", "7676");
                put("adminUserName", "admin");
                put("host", "localhost");
            }};

        // Test create
        ClientResponse response = post(URL_JMS_HOST, newHost);
        assertTrue(isSuccess(response));

        // Test edit
        Map<String, String> entity = getEntityValues(get(URL_JMS_HOST + "/" + jmsHostName));
        assertFalse(entity.isEmpty());
        assertEquals(jmsHostName, entity.get("name"));
        entity.put("port", "8686");
        response = post(URL_JMS_HOST + "/" + jmsHostName, entity);
        assertTrue(isSuccess(response));
        entity = getEntityValues(get(URL_JMS_HOST + "/" + jmsHostName));
        assertEquals("8686", entity.get("port"));

        // Test delete
        response = delete(URL_JMS_HOST + "/" + jmsHostName);
        assertTrue(isSuccess(response));
    }
}