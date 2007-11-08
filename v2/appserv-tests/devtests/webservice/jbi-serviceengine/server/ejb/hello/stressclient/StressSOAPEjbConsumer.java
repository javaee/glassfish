package stressclient;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;
import endpoint.jaxws.HiEJBService;
import endpoint.jaxws.Hi;
import com.example.subtractor.Subtractor;
import com.example.subtractor.SubtractorService;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSOAPEjbConsumer {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/server/ejb/hello/stressclient";

        @WebServiceRef
        static HelloEJBService service;

	 @WebServiceRef
         static HiEJBService service1;

         @WebServiceRef(wsdlLocation="http://localhost:8080/subtractorservice/webservice/SubtractorService?WSDL")
         static SubtractorService service2;


        static long startTime  = 0;
	static int minutesToRun = 0;
	static long endTime = 0;

        StressSOAPEjbConsumer() {
	    //create multiple instances of iterative test clients.
            StressClient clients[] = new StressClient[100];
            for(int i = 0 ; i < 100 ; i++) {
	        clients[i] = new StressClient(i,stat);
                clients[i].setServiceHandle(service,service1,service2);
                clients[i].start();
            }
	}
        public static void main(String[] args) throws Exception {
           stat.addDescription(testId);
	   
	   if( args != null && args.length > 0 && args[0] != null) 
	       try {
                   minutesToRun = Integer.parseInt(args[0]);
	       } catch(NumberFormatException numEx) {
		   minutesToRun = 3;
	       }
	   System.out.println("Time to run is: "+minutesToRun); 
	   Thread.currentThread().sleep(2000);
	   StressClient.setTimeToRun(minutesToRun);
           StressSOAPEjbConsumer stressClient = new StressSOAPEjbConsumer();
           //stat.addStatus("jsr108-serverside-webservices-ejb-noname-annotation", stat.PASS);
	   //stat.printSummary("jsr108-serverside-webservices-ejb-noname-annotation");
       }
}
