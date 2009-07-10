package client;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import service.web.example.calculator.*;
import javax.xml.ws.*;

public class SOAPWebConsumer {
//    @WebServiceRef(wsdlLocation="http://localhost:8080/rpc-literal-bundled-wsdl-bc-consumer-se-provider/webservice/CalculatorService?WSDL")
//    static CalculatorService service;
    CalculatorService service = new CalculatorService();
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("jbi");

	private static String testId = "jbi-serviceengine/rpc-literal/bc_consumer_sc_provider/bundled-wsdl";

    public static void main(String[] args) {
        stat.addDescription(testId);
        SOAPWebConsumer client = new SOAPWebConsumer();
        client.addUsingSOAPConsumer();
        stat.printSummary(testId);
    }
    
    private void addUsingSOAPConsumer() {
        Calculator port= null;
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
            
            IncomeTaxDetails itDetails = new IncomeTaxDetails();
            itDetails.setFirstName( "bhavani");
            itDetails.setLastName("s");
            itDetails.setAnnualIncome( 400000);
            itDetails.setStatus("salaried");
            
            long startTime = System.currentTimeMillis();
            long ret = 0;
            // Make 100 calls to see how much time it takes.
            //for(int i=0; i<1000; i++) {
            ret = port.calculateIncomeTax(itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    );
            //}
            long timeTaken = System.currentTimeMillis() - startTime;
            
            //int ret = port.add(1, 2);
            printSuccess("Your income tax is : Rs ", ret, timeTaken);
            startTime = System.currentTimeMillis();
            int k = port.add(505, 50);
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("Sum of 505 and 50 is : ", k, timeTaken);
            
            startTime = System.currentTimeMillis();
            String hi = port.sayHi();
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("Output from webservice : ",  hi, timeTaken);
            
			/*
            startTime = System.currentTimeMillis();
            port.printHi();
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("SUCCESS : ",  "Webservice has successfully printed hi in server.log", timeTaken);
            
            startTime = System.currentTimeMillis();
            port.printHiToMe("JavaEEServiceEngine");
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("SUCCESS : ",  "Webservice has successfully printed hi to me in server.log", timeTaken);
            */
            stat.addStatus(testId, stat.PASS);
            
        } catch(java.lang.Exception e) {
            stat.addStatus(testId, stat.FAIL);
        }
    }
    
    public void printFailure(String errMsg) {
        System.out.println("<html>");
        System.out.println("<head>");
        System.out.println("<title>TestServlet</title>");
        System.out.println("</head>");
        System.out.println("<body>");
        System.out.println("<p>");
        System.out.println("Test FAILED: Error message - " + errMsg);
        System.out.println("</p>");
        System.out.println("</body>");
        System.out.println("</html>");
    }
    
    public void printSuccess(String message, long result, long timeTaken) {
        System.out.println("\n\n");
        System.out.println(message + result);
        System.out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
    }
    
    public void printSuccess(String message, String result, long timeTaken) {
        System.out.println("\n\n");
        System.out.println(message + result);
        System.out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
    }
    
}
