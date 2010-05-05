package org.glassfish.admin.rest;

import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class RestTestBase {
    public static final String BASE_URL = "http://localhost:4848/management";

    public RestTestBase() {
    }

    @BeforeClass
    public static void setup() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "admin".toCharArray());
            }
        });
    }

    /*
    public void setFieldValue(HtmlPage page, String fieldName, String value) {
        HtmlForm form = (HtmlForm)page.getForms().get(0);
        HtmlInput field = form.getInputByName(fieldName);
        field.setValueAttribute(value);
    }

    public String getFieldValue(HtmlPage page, String fieldName) {
        HtmlForm form = (HtmlForm)page.getForms().get(0);
        HtmlInput field = form.getInputByName(fieldName);
        return field.getValueAttribute();
    }
    */

    private HttpURLConnection getConnection(String address, String responseType) throws MalformedURLException, IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", responseType);
        return conn;
    }

    protected String get(String address, String responseType) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection conn = getConnection(address, responseType);
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

    protected String options(String address, String responseType) {
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

    protected String post(String address, Map<String, String> payload, String responseType) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection conn = getConnection(address, responseType);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            String sep = "";
            for (Map.Entry entry : payload.entrySet()) {
                out.write(sep + entry.getKey() + "=" + entry.getValue());
                sep = "&";
            }
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String decodedString;

            while ((decodedString = in.readLine()) != null) {
                sb.append(decodedString);
            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sb.toString();
    }

    protected String put(String address, Map<String, String> payload, String responseType) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection conn = getConnection(address, responseType);
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            String sep = "";
            for (Map.Entry entry : payload.entrySet()) {
                out.write(sep + entry.getKey() + "=" + entry.getValue());
                sep = "&";
            }
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String decodedString;

            while ((decodedString = in.readLine()) != null) {
                sb.append(decodedString);
            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * This method will parse the provided XML document and return a map of the attributes and values on the root element
     * @param xml
     * @return
     */
    protected Map<String, String> getEntityValues(String xml) {
        Map<String, String> map = new HashMap<String, String>();

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
        return map;
    }
}