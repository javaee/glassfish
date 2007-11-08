package client;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPWebConsumer {
    @WebServiceRef(wsdlLocation="http://localhost:8080/calculatorservice-web/webservice/CalculatorService?WSDL")
    static CalculatorService service;
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/service_unit/compApp-server";

    public static void main (String[] args) {
        stat.addDescription(testId + args[0]);
	SOAPWebConsumer client = new SOAPWebConsumer();
	client.addUsingSOAPConsumer(args);
        stat.printSummary(testId + args[0]);
    }

    private void addUsingSOAPConsumer(String[] args) {
	com.example.calculator.Calculator port= null;
	try {

		System.out.println(" After creating CalculatorService");

                port = service.getCalculatorPort();
		System.out.println(" After getting port");

		// Get Stub
		BindingProvider stub = (BindingProvider)port;
		String endpointURI ="http://localhost:12011/calculatorendpoint";
		stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		      endpointURI);
		System.out.println(" After setting endpoint address URI");
	        System.out.println(" Using SOAP binding's consumer to add 1 + 2 = " + port.add(1,2));
                if(args[0].equals("jbi-enabled-true"))
		    stat.addStatus(testId, stat.PASS);
                else 
	            stat.addStatus(testId, stat.FAIL);

	} catch(Exception e) {
		e.printStackTrace();
                if(args[0].equals("jbi-enabled-false"))
                    stat.addStatus(testId, stat.PASS);
                else
		    stat.addStatus(testId, stat.FAIL);
	}
    }
}
