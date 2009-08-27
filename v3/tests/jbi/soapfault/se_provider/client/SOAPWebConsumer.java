package client;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPWebConsumer {
    //@WebServiceRef(wsdlLocation="http://localhost:12011/calculatorendpoint/CalculatorService?WSDL")
    //static CalculatorService service;
    CalculatorService service = new CalculatorService();
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("jbi");
	private static String testId = "jbi-serviceengine/soapfault/se_provider";

    public static void main (String[] args) {
        stat.addDescription(testId);
	SOAPWebConsumer client = new SOAPWebConsumer();
	client.addUsingSOAPConsumer();
        stat.printSummary(testId );
    }

    private void addUsingSOAPConsumer() {
	com.example.calculator.Calculator port= null;

                port = service.getCalculatorPort();

		// Get Stub
		BindingProvider stub = (BindingProvider)port;
		String endpointURI ="http://localhost:12011/calculatorendpoint";
		stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		      endpointURI);

		String failedMsg = null;

		try {
	        System.out.println("\nInvoking throwRuntimeException");
		   	port.throwRuntimeException("bhavani");
		} catch(Exception ex) {
			System.out.println(ex);
			if(!(ex instanceof SOAPFaultException) || 
			!(ex.getMessage().equals("java.lang.RuntimeException: Calculator :: Threw Runtime Exception"))) {
				failedMsg = "port.throwRuntimeException() did not receive RuntimeException 'Calculator :: Threw Runtime Exception'";
			}
		}

		try {
	        System.out.println("\nInvoking throwApplicationException");
		   	port.throwApplicationException("bhavani");
		} catch(Exception ex) {
			System.out.println(ex);
			if(!(ex instanceof com.example.calculator.Exception_Exception)) {
				failedMsg = "port.throwApplicationException() did not throw ApplicationException";
			}
		}

		if(failedMsg != null) {
	        stat.addStatus(testId, stat.FAIL);
		} else {
        	stat.addStatus(testId, stat.PASS);
		}
    }
}
