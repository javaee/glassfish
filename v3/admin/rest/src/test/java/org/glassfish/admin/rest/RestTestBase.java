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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

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

    protected String get(String address) {
        return client.resource(address).accept(RESPONSE_TYPE).get(String.class);
    }

    protected ClientResponse post(String address, Map<String, String> payload) {
        return client.resource(address).post(ClientResponse.class, buildMultivalueMap(payload));
    }

    protected ClientResponse create(String address, Map<String, String> payload) {
        return post(address, payload);
    }

    protected String read(String address) {
        return get(address);
    }

    protected ClientResponse update(String address, Map<String, String> payload) {
        // For now... :(
        return create(address, payload);
        //return client.resource(address).put(ClientResponse.class, buildMultivalueMap(payload));
    }

    protected ClientResponse delete(String address, Map<String, String> payload) {
        return client.resource(address).queryParams(buildMultivalueMap(payload)).delete(ClientResponse.class);
    }

    /**
     * This method will parse the provided XML document and return a map of the attributes and values on the root element
     *
     * @param xml
     * @return
     */
    protected Map<String, String> getEntityValues(String xml) {
        Map<String, String> map = new HashMap<String, String>();

        if ((xml != null) && !xml.isEmpty()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
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

    private MultivaluedMap buildMultivalueMap(Map<String, String> payload) {
        MultivaluedMap formData = new MultivaluedMapImpl();
        if (payload != null) {
            for (final Map.Entry<String, String> entry : payload.entrySet()) {
                formData.putSingle(entry.getKey(), entry.getValue());
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
/*
private HttpURLConnection getConnection(String address, String responseType) throws MalformedURLException, IOException {
URL url = new URL(address);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestProperty("Accept", responseType);
return conn;
}

protected String options1(String address, String responseType) {
StringBuilder sb = new StringBuilder();
try {
HttpURLConnection conn = getConnection(address, responseType);
conn.setDoOutput(true);
conn.setRequestMethod("OPTIONS");
BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String inputLine;

while ((inputLine = in.readLine()) != null) {
sb.append(inputLine);
}
in.close();
} catch (Exception ex) {
ex.printStackTrace();
}

return sb.toString();
}
 */
