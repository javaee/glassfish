package client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.w3c.dom.Node;

import java.net.URL;
import java.net.URI;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Map;

public class Client {

    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
    private static final QName serviceQName = new QName("http://duke.org", "AddNumbersService");
    private static final QName portQName = new QName("http://duke.org", "AddNumbersPort");

    private static String endpointAddress =
        "http://localhost:8080/restful-2/webservice/AddNumbersService";
    private static String queryString = "num1=30&num2=20";

    public static void main (String[] args) throws Exception {
	stat.addDescription("webservices-simple-restful-svc");
        Client client = new Client();
        Service service = client.createService();
        URI endpointURI = new URI(endpointAddress);
        String path = null;
        String query = null;
        if (endpointURI != null){
            path = endpointURI.getPath();
            query = endpointURI.getQuery();
        }
        service.addPort(portQName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<Source> d = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, new String("GET"));
        requestContext.put(MessageContext.QUERY_STRING, queryString);
        requestContext.put(MessageContext.PATH_INFO, path);
        System.out.println ("Invoking Restful GET Request with query string " + queryString);
        Source result = d.invoke(null);
        printSource(result);
	stat.printSummary("webservices-simple-restful-svc");
    }

    private Service createService() {
        Service service = Service.create(serviceQName);
        return service;
    }

    private static void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            String resp = bos.toString();
            System.out.println("**** Response ******"+resp);
            bos.close();
            if(resp.indexOf("<ns:return>50</ns:return>") != -1)
                stat.addStatus("restful-svc-2", stat.PASS);
            else
                stat.addStatus("restful-svc-2", stat.FAIL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

