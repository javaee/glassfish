import javax.naming.*;
import javax.rmi.*;

import java.util.Properties;

import javax.ejb.EJBObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.common.SecurityConstants;

/**
 * This test is for BACKWARD COMPATIBILITY ONLY
 * Clients should NOT be using LoginContextDriver.doClientLogin
 * They should be using the ProgrammaticLogin API
 */
public class LoginContextDriverTest {

	private static String testId="Standalone-client-login-context-driver";
    private static boolean testStatus=false;
    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter();

    private static InitialContext ic = null;
    
    private static MySession1Remote my1r = null;
        
    public static void main(String[] args) {

        stat.addDescription("Security::EJB Method permissions test using " +
                "Login Context Driver Standalone Client");
    
        System.out.println("*** EJBMethod Permission Test using Login Context Driver Standalone client ***");  


        try{
            // Use the default callback handler for login - using textauth (false)
            LoginCallbackHandler handler = new LoginCallbackHandler(false);
            LoginContextDriver.doClientLogin(
                SecurityConstants.USERNAME_PASSWORD, handler);

            // Initialize the Context
            ic = new InitialContext();
            
            System.out.println("EJB lookup start...");
            java.lang.Object objref = ic.lookup("ejb/MySession1Bean");		
            
            MySession1RemoteHome my1rh = (MySession1RemoteHome)
              PortableRemoteObject.narrow(objref, MySession1RemoteHome.class);

            my1r = my1rh.create(); 
            
	     	String retValue = my1r.businessMethod("blah");
            System.out.println("retValue="+retValue);

            testStatus = true;

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( testStatus) 
                stat.addStatus(testId, stat.PASS);
            else
                stat.addStatus(testId, stat.FAIL);

            stat.printSummary(testId);
        }
    }
    
}
