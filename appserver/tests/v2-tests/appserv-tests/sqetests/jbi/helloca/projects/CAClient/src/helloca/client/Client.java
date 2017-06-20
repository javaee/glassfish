/*
 * Main.java
 *
 * Created on January 13, 2007, 7:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package helloca.client;

import caclient.ejbws.Account;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Iterator;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
/**
 *
 * @author Sony Manuel
 */
public class Client {
    
    static SimpleReporterAdapter reporter = new SimpleReporterAdapter();
    /** Creates a new instance of Main */
    public Client() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        reporter.addDescription("Hello Composite App Test");
        // TODO code application logic here
        Client m = new Client();
        m.testPing();
        m.testInOut();
        //m.testFault();
            reporter.printSummary("HelloCa Test");
    }
    
    public void testPing() {
        String testId = "jbi.helloca.testapp.testPing";
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.ping();
            reporter.addStatus(testId, reporter.PASS);
        } catch (Exception ex) {
            reporter.addStatus(testId, reporter.FAIL);
            ex.printStackTrace();
        }
    }
    
    public void testInOut() {
        String testId = "jbi.helloca.testapp.testInOut";
        Account acc = new Account();
        Holder<Account> holder = new Holder<Account>();
        holder.value = acc;
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.createAccount("Foo", 100.00, holder);
            if (holder.value.getId() == 1000 &&
                    holder.value.getName().equals("Foo") &&
                    holder.value.getBalance() == 100.00)
                reporter.addStatus(testId, reporter.PASS);
            else
                reporter.addStatus(testId, reporter.FAIL);
        } catch (Exception ex) {
            reporter.addStatus(testId, reporter.FAIL);
            ex.printStackTrace();
        }
    }
    
    
    public void testFault() {
        String testId = "jbi.helloca.testapp.testFault";
        Account acc = new Account();
        Holder<Account> holder = new Holder<Account>();
        holder.value = acc;
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.createAccount("Bar", 0.0, holder);
            reporter.addStatus(testId, reporter.FAIL);
        } catch (SOAPFaultException sfe) {
            reporter.addStatus(testId, reporter.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            reporter.addStatus(testId, reporter.FAIL);
        }
    }
}
