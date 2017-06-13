package client;

import javax.xml.ws.WebServiceRef;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.w3c.dom.Node;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class Client {

    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) throws Exception {
	stat.addDescription("webservices-simple-restful-svc");
        String endpointAddress = 
            "http://localhost:8080/AddNumbersImplService/endpoint.AddNumbersImpl";
        URL url = new URL(endpointAddress+"?num1=10&num2=20");
        System.out.println ("Invoking URL="+url);
        process(url, args);
	stat.printSummary("webservices-simple-restful-svc");
    }

    private static void process(URL url, String[] args) throws Exception {
        InputStream in = url.openStream();
        StreamSource source = new StreamSource(in);
        printSource(source, args);
    }

    private static void printSource(Source source, String[] args) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
	    String resp = bos.toString();
            System.out.println("**** Response ******"+resp);
            bos.close();
	    if(resp.indexOf("<ns:return>30</ns:return>") != -1)
                stat.addStatus(args[0], stat.PASS);
	    else
                stat.addStatus(args[0], stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

