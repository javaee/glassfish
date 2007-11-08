package client;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.Service;
import entapp.ejb.*;
import entapp.web.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/service_unit/enterprise_app";

    public static void main (String[] args) {
        stat.addDescription(testId);
		Client client = new Client();
		client.invoke();
        stat.printSummary(testId );
    }

    private void invoke() {
		try {

			HelloEJBService helloEJBService = new HelloEJBService();
			HelloEJB port = helloEJBService.getHelloEJBPort();
			String hello = port.sayHello("Bhavani");
			System.out.println("Output :: " + hello);

			HelloWebService helloWebService = new HelloWebService();
			HelloWeb webPort = helloWebService.getHelloWebPort();
			hello = webPort.sayHello("Bhavani");
			System.out.println("Output :: " + hello);

			stat.addStatus(testId, stat.PASS);

		} catch(Exception ex) {
			ex.printStackTrace();
	        stat.addStatus(testId, stat.FAIL);
    	}
	}
}
