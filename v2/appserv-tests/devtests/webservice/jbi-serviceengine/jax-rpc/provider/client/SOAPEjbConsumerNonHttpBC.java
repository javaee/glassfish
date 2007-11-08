package myclient;
import javax.naming.*;
import javax.xml.rpc.Stub;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPEjbConsumerNonHttpBC {
    private double balance = 300.00;
    private double period = 3.5;
                                                                                
    private static SimpleReporterAdapter status = new SimpleReporterAdapter();
                                                                                
    private static String testId = "jbi-serviceengine/jax-rpc/provider";

    public SOAPEjbConsumerNonHttpBC() {
        status.addDescription(testId);
    }
                                                                                
    public static void main (String[] args) {
                                                                                
        SOAPEjbConsumerNonHttpBC client = new SOAPEjbConsumerNonHttpBC();
                                                                                
        client.doTest();
        status.printSummary(testId);
   }

    private void doTest() {
        String targetEndpointAddress =
                        "http://localhost:8080/soap/web/findinterest";
                                                                                
        try {
            Context ic = new InitialContext();
            FindInterest findIntrService = (FindInterest)
                    ic.lookup("java:comp/env/service/FindInterest");
                                                                                
            InterestIF interestIFPort = findIntrService.getInterestIFPort();
                                                                                
            ((Stub)interestIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,                                                targetEndpointAddress);
                                                                                
            double interest = interestIFPort.calculateInterest(balance, period);            System.out.println("Interest on $300 for a period of 3.5 years is "
                                + interest);
                                                                                
            if (interest == 105.0) {
                status.addStatus(testId +"1 : EJB Endpoint Test", status.PASS);
            }
                                                                                
        } catch (Exception ex) {
                status.addStatus(testId +"1 : EJB Endpoint Test", status.FAIL);
            System.out.println("findintr client failed");
            ex.printStackTrace();
        }
    }
}
