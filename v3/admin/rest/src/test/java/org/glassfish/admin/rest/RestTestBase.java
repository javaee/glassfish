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

    public static final String BASE_URL = "http://localhost:4848/management";

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

    protected String get(String address) {
        return client.resource(address).accept(RESPONSE_TYPE).get(String.class);
    }

    protected ClientResponse post(String address, Map<String, String> payload) {
        WebResource webResource = client.resource(address);

        MultivaluedMap formData = new MultivaluedMapImpl();
        for (final Map.Entry<String, String> entry : payload.entrySet()) {
            formData.putSingle(entry.getKey(), entry.getValue());
        }
        ClientResponse cr = webResource
//                .accept(RESPONSE_TYPE)
                .post(ClientResponse.class, formData);
        return cr;
    }

    protected ClientResponse delete(String address, Map<String, String> payload) {
        WebResource webResource = client.resource(address);
        ClientResponse cr = webResource.queryParams(buildMultivalueMap(payload)).delete(ClientResponse.class);
        return cr;
    }

    protected String put1(String address, Map<String, String> payload, String responseType) {
        WebResource webResource = client.resource(address);

        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.putAll(payload);
        ClientResponse cr = webResource.type("application/x-www-form-urlencoded").accept(responseType).put(ClientResponse.class, formData);
        return cr.toString();
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
        for (final Map.Entry<String, String> entry : payload.entrySet()) {
            formData.putSingle(entry.getKey(), entry.getValue());
        }
        return formData;
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
