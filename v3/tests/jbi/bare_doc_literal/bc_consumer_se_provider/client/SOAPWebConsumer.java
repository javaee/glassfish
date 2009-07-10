package client;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPWebConsumer {
 //   @WebServiceRef(wsdlLocation="http://localhost:8080/bare_doc_literal-bc_consumer_se_provider/webservice/CalculatorService?WSDL")
//    static CalculatorService service;
    CalculatorService service = new CalculatorService();
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("jbi");
	private static String testId = "jbi-serviceengine/bare_doc_literal/bc_consumer_se_provider";
    public static void main (String[] args) {
        stat.addDescription(testId);
	SOAPWebConsumer client = new SOAPWebConsumer();
	client.addUsingSOAPConsumer();
        stat.printSummary(testId);
    }

    private void addUsingSOAPConsumer() {
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
	        System.out.println(" Using SOAP binding's consumer to add 500 + 50 = " + port.add(50));
	        stat.addStatus(testId, stat.PASS);
		System.out.println(" No Exception thrown");

	} catch(Exception e) {
		//e.printStackTrace();
		if(e instanceof javax.xml.ws.soap.SOAPFaultException) 
                	stat.addStatus(testId, stat.FAIL);
		else {
	        	stat.addStatus(testId, stat.FAIL);
			System.out.println(" SOAPFaultException Not thrown");
		}
	}
    }
}
