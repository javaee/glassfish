package javax.security.jacc;

import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import java.security.AccessControlException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestPCF {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testPCF ";

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);
        String description = null;
        boolean expectACException =
            (args != null && args.length > 0) ? 
            Boolean.parseBoolean(args[0]) : true;
        String expectedException = 
            (args != null && args.length > 1) ? args[1] : null;
        System.out.println("expect AccessControlException: " + expectACException);
        System.out.println("expected Exception: " + expectedException);
        
        description = testSuite + "-" + expectACException + "-" +
            expectedException + " without SecurityManager";
        try {
            PolicyConfigurationFactory f = 
                PolicyConfigurationFactory.getPolicyConfigurationFactory();
            stat.addStatus(description, stat.PASS);
        } catch(Exception ex) {
            //It should be one of the following:
            //    java.lang.ClassNotFoundException
            //    java.lang.ClassCastException
            //    javax.security.jacc.PolicyContextException
            if (ex.getClass().getName().equals(expectedException)) {
                stat.addStatus(description, stat.PASS);
            } else {
                ex.printStackTrace();
                stat.addStatus(description, stat.FAIL);
            }
        }

        System.out.println( "--START SECURITY MANAGER -->>");
        System.setSecurityManager(new SecurityManager());

        description = testSuite + "-" + expectACException + "-" +
            expectedException + " with SecurityManager";
        try {
            PolicyConfigurationFactory f = 
                PolicyConfigurationFactory.getPolicyConfigurationFactory();
            stat.addStatus(description, stat.PASS);
        } catch(AccessControlException ace) {
            if (!expectACException) {
                ace.printStackTrace();
            }
            stat.addStatus(description,
                (expectACException) ? stat.PASS : stat.FAIL);
        } catch(Exception ex) {
            //It should be one of the following:
            //    java.lang.ClassNotFoundException
            //    javax.security.jacc.PolicyContextException
            if (ex.getClass().getName().equals(expectedException)) {
                stat.addStatus(description, stat.PASS);
            } else {
                ex.printStackTrace();
                stat.addStatus(description, stat.FAIL);
            }
        }

        stat.printSummary(testSuite);
    }
}










