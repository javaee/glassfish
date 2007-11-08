package client;

import javax.xml.ws.WebServiceRef;

import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class JAXWSClient {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/server/web/client";
        
            @WebServiceRef(wsdlLocation="http://localhost:8080/calculatorservice/webservice/CalculatorService?WSDL")
            static CalculatorService service;

        public static void main(String[] args) {
            try {
	    stat.addDescription(testId);
            //CalculatorService service = new CalculatorService();
            JAXWSClient client = new JAXWSClient();
            client.doTest(args, service);
	    stat.printSummary(testId);
            } catch(Exception e) {
               e.printStackTrace();
            }
       }

       public void doTest(String[] args, CalculatorService service) {
            try {
                System.out.println(" Before getting port service is : " + service);
                Calculator port = service.getCalculatorPort();
                for (int i=0;i<10;i++) {
                    int ret = port.add(i, 10);
		    if(ret != (i + 10)) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(testId, stat.FAIL);
                        return;
		    }
                    System.out.println(" Adding : " + i + " + 10 = "  + ret);
                }
                stat.addStatus(testId, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
       }
}

