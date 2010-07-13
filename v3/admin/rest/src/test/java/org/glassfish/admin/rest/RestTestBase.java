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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import java.io.ByteArrayInputStream;
import java.io.File;
import org.junit.BeforeClass;

import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.glassfish.admin.rest.clientutils.MarshallingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RestTestBase {
    public static final String BASE_URL = "http://localhost:4848/management/domain";
    public static final String RESPONSE_TYPE = "application/xml";

    protected static Client client;

    public RestTestBase() {
    }

    @BeforeClass
    public static void setup() {
        if (client == null) {
            client = Client.create();
        }

        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "admin".toCharArray());
            }
        });
    }

    protected static String generateRandomString() {
        SecureRandom random = new SecureRandom();

        return new BigInteger(130, random).toString(16);
    }

    protected boolean isSuccess(ClientResponse response) {
        int status = response.getStatus();
        return ((status == 200) || (status == 201));
    }

    protected ClientResponse get(String address) {
        return client.resource(address).accept(RESPONSE_TYPE).get(ClientResponse.class);
    }

    protected ClientResponse options(String address) {
        return client.resource(address).accept(RESPONSE_TYPE).options(ClientResponse.class);
    }

    protected ClientResponse post(String address, Map<String, String> payload) {
        return client.resource(address).post(ClientResponse.class, buildMultivaluedMap(payload));
    }

    protected ClientResponse post(String address) {
        return client.resource(address).post(ClientResponse.class);
    }

    protected ClientResponse put(String address, Map<String, String> payload) {
        return client.resource(address).put(ClientResponse.class, buildMultivaluedMap(payload));
    }

    protected ClientResponse put(String address) {
        return client.resource(address).put(ClientResponse.class);
    }

    protected ClientResponse postWithUpload(String address, Map<String, Object> payload) {
        FormDataMultiPart form = new FormDataMultiPart();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if ((entry.getValue() instanceof File)) {
                form.getBodyParts().add((new FileDataBodyPart(entry.getKey(), (File)entry.getValue())));
            } else {
                form.field(entry.getKey(), entry.getValue(), MediaType.TEXT_PLAIN_TYPE);
            }
        }
        return client.resource(address).type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
    }

    protected ClientResponse create(String address, Map<String, String> payload) {
        return post(address, payload);
    }

    protected ClientResponse update(String address, Map<String, String> payload) {
        // For now... :(
        return create(address, payload);
        //return client.resource(address).put(ClientResponse.class, buildMultivalueMap(payload));
    }

    protected ClientResponse delete(String address) {
        return delete(address, new HashMap<String, String>());
    }

    protected ClientResponse delete(String address, Map<String, String> payload) {
        return client.resource(address).queryParams(buildMultivaluedMap(payload)).delete(ClientResponse.class);
    }

    /**
     * This method will parse the provided XML document and return a map of the attributes and values on the root element
     *
     * @param xml
     * @return
     */
    protected Map<String, String> getEntityValues(ClientResponse response) {
        Map<String, String> map = new HashMap<String, String>();

        String xml = response.getEntity(String.class);
        if ((xml != null) && !xml.isEmpty()) {
            try {
                Document doc = getDocument(xml);
                Element root = doc.getDocumentElement();
                NamedNodeMap nnm = root.getAttributes();

                for (int i = 0; i < nnm.getLength(); i++) {
                    Node attr = nnm.item(i);
                    String name = attr.getNodeName();
                    String value = attr.getNodeValue();
                    map.put(name, value);
                }
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return map;
    }

    protected List<String> getCommandResults(ClientResponse response) {
        String xml = response.getEntity(String.class);
        List<String> results = new ArrayList<String>();
        Document document = getDocument(xml);

        Element root = document.getDocumentElement();
        NodeList nl = root.getElementsByTagName("message-part");
        if (nl.getLength() > 0) {
            Node child;
            for (int i = 0; i < nl.getLength(); i++) {
                child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    results.add(((Element) child).getAttribute("message"));
                }
            }
        }

        return results;
    }

    public Document getDocument(String input) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(input.getBytes()));
            return doc;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<Map<String, String>> getProperties(ClientResponse response) {
        return MarshallingUtils.getPropertiesFromXml(response.getEntity(String.class));
    }

    private MultivaluedMap buildMultivaluedMap(Map<String, String> payload) {
        if (payload instanceof MultivaluedMap) {
            return (MultivaluedMap)payload;
        }
        MultivaluedMap formData = new MultivaluedMapImpl();
        if (payload != null) {
            for (final Map.Entry<String, String> entry : payload.entrySet()) {
                formData.add(entry.getKey(), entry.getValue());
            }
        }
        return formData;
    }

    protected void checkStatusForSuccess(ClientResponse cr) {
        int status = cr.getStatus();
        if ((status < 200) || (status > 299)) {
            throw new RuntimeException(cr.toString());
        }
    }
}