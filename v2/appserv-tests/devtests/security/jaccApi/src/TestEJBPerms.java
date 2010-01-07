package javax.security.jacc;

import java.lang.reflect.*;
import java.util.Enumeration;
import java.security.*; 
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestEJBPerms {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testEJBPerms ";
    private static boolean isDebug = Boolean.getBoolean("debug");

    private static void debug(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }

    private static void testSerialization(Permission p1) {
        String description = "TestSerialization:" + p1.getName() +
            "," + p1.getActions();

        boolean result = true;

        Permission p2 = p1;
        try {
            FileOutputStream fout = new 
                FileOutputStream("serial-test-file.tmp");
            ObjectOutputStream sout = new ObjectOutputStream(fout);
            sout.writeObject(p1);
            sout.flush();
            sout.close();
            fout.close();
        } catch( Throwable t ) { 
            t.printStackTrace();
            debug( "-- Serialization Test Failed(write)-" + p1.getName() + "," + p1.getActions());
        }

        try {
            FileInputStream fin = new FileInputStream("serial-test-file.tmp");
            ObjectInputStream sin = new ObjectInputStream(fin);
            p2 = (Permission) sin.readObject();
            sin.close();
            fin.close(); 
        } catch( Throwable t ) { 
            t.printStackTrace();
            debug( "-- Serialization Test Failed(read)-" + p1.getName() + "," + p1.getActions());
            result = false;
        }

        if (result == true) {
            if (p2.equals(p1)) { 
                debug( "-- Serialization Test Succeeded -----------" + p2.getName() + "," + p2.getActions());
                stat.addStatus(description, stat.PASS);
            } else { 
                debug( "-- Serialization Test Failed-" + p1.getName() + "," + p1.getActions());
                stat.addStatus(description, stat.FAIL);
            }
        } else {
            debug( "-- Serialization Test Failed-" + p1.getName() + "," + p1.getActions());
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void doEJBMethodPermission( boolean expectedToSucceed,
                                               String name, String actions) {
        String description = "doEJBMethodPermission:" +
            expectedToSucceed + "-" + name + "-" + actions;

        boolean result = true;

        EJBMethodPermission p1,p2;

        try {
            debug( "-- Construct Test --" + expectedToSucceed +
                " " + name + " " + actions);

            p1 = new EJBMethodPermission(name,actions);

            if (expectedToSucceed) {

                p2 = new EJBMethodPermission(p1.getName(),p1.getActions()); 
                testSerialization(p2);

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);
                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        if (result == expectedToSucceed) {
            if (expectedToSucceed) {
                debug( "-- Construct Test Succeeded -------------------------------------");
            } else {
                debug( "-- Construct Test Succeeded (negative)---------------------------");
            }
            stat.addStatus(description, stat.PASS);
        } else {
            debug( "-- Construct Test Failed ----------------------------------------");
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void doEJBMethodPermission( boolean expectedToSucceed,
           String ejbName, String methodName, String methodInterface, 
           String[] methodParams) {
        String description = "doEJBMethodPermissionWithParams:" +
            expectedToSucceed + "-" + ejbName + "-" + methodName +
            "-" + methodInterface + "-" + methodParams;

        boolean result = true;

        EJBMethodPermission p1,p2;

        try {
            debug( "-- Construct Test --");

            p1 = new EJBMethodPermission(ejbName,methodName,methodInterface,
                                         methodParams);

            if (expectedToSucceed) {
                p2 = new EJBMethodPermission(p1.getName(),p1.getActions()); 
                testSerialization(p2);

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);
                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }
            
            result = false;
        } 
        if (result == expectedToSucceed) {
            if (expectedToSucceed) { 
                debug( "-- Construct Test Succeeded -------------------------------------");
            } else {
                debug( "-- Construct Test Succeeded (negative)---------------------------");
            }
            stat.addStatus(description, stat.PASS);
        } else {
            debug( "-- Construct Test Failed ----------------------------------------");
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void doEJBMethodPermission( boolean expectedToSucceed,
             String ejbName, String methodInterface, Method method) {

        String description = "doEJBMethodPermissionWithMethod:" +
            expectedToSucceed + "-" + ejbName + 
            "-" + methodInterface + "-" + method;

        boolean result = true;

        EJBMethodPermission p1,p2;
        String actions = null;

        try {
            debug( "-- Construct Test -----------------------------------------------");

            p1 = new EJBMethodPermission(ejbName,methodInterface,method);

            if (expectedToSucceed) {

                p2 = new EJBMethodPermission(p1.getName(),p1.getActions());
                actions = p1.getActions();
                testSerialization(p2);

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        if (result == expectedToSucceed) {
            if (expectedToSucceed) { 
                debug( "-- Construct Test Succeeded -------------------------------------");
            } else {
                debug( "-- Construct Test Succeeded (negative)---------------------------");
            }
            stat.addStatus(description, stat.PASS);
        } else {
            debug( "-- Construct Test Failed ----------------------------------------");
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void doEJBRoleRefPermission( boolean expectedToSucceed,
            String servletName, String roleRef) {
        String description = "doEJBRoleRefPermission:" +
            expectedToSucceed + "-" + servletName + "-" + roleRef;

        boolean result = true;

        EJBRoleRefPermission p1,p2;

        p1 = null;

        try {
            debug( "-- Construct Test -----------------------------------------------");

            p1 = new EJBRoleRefPermission(servletName,roleRef);

            if (expectedToSucceed) {

                p2 = new EJBRoleRefPermission(p1.getName(),p1.getActions()); 
                testSerialization(p2);

                if (!p1.equals(p2)) {
                    result = false;
                    debug("p != p\n\t" + p1 + "\n\t" + p2);

                }

                if (!p1.implies(p2)) {
                    result = false;
                    debug("!p.implies(p)\n\t" + p1 + "\n\t" + p2);
                }

            } else {
                debug("unexpected success\t" + p1);
                result = false;
            }

        } catch( Throwable t ) { 

            if (expectedToSucceed) {
                t.printStackTrace();
            }

            result = false;
        }

        if (result == expectedToSucceed) {
            if (expectedToSucceed) { 
                debug( "-- Construct Test Succeeded -------------------------------------" + p1.hashCode());
            } else {
                debug( "-- Construct Test Succeeded (negative)---------------------------");
            }
            stat.addStatus(description, stat.PASS);
        } else {
            debug( "-- Construct Test Failed ----------------------------------------");
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void doPermissionImplies ( boolean expectedResult,
            Permission thisP, Permission p) {
        //SEC is added there so that it will not cut off the 
        //first part with space, an issue in reporting tools
        String description = "SEC doPermissionImplies:" +
            expectedResult + "-" + thisP + "-" + p;

        try {

            debug( "-- Permission implies Test ----------------------------------------------");

            if (thisP.implies(p) != expectedResult) {
                debug(
                    (expectedResult ? "unexpected failure:" :"unexpected success:")
                     + p + (expectedResult ?" not implied by:":" implied by:") + 
                    thisP);
                debug( "-- Permission implies Test Failed ----------------------------------------");
                stat.addStatus(description, stat.FAIL);
            } else {
                debug(p + 
                   (expectedResult ? " implied by:":" not implied by:") + thisP);
                debug( "-- Permission implies Test Succeeded -------------------------------------");
                stat.addStatus(description, stat.PASS);
            }
        } catch( Throwable t ) { 
            debug("unexpected exception");
            t.printStackTrace();
            debug( "-- Permission implies Test Failed ----------------------------------------");
            stat.addStatus(description, stat.FAIL);
        }
    }

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);
        String params1[] = { "java.lang.string" };
        String params2[] = { "java.lang.string", "java.lang.int" };
        String params3[] = { "java.lang.string", "java.lang.int," };

        for (int i=0; i<100; i++) {
            doEJBMethodPermission(false,"bankbean","withdraw,");
            doEJBMethodPermission(false,"bankbean","withdraw,Home,java.lang.string,");
            doEJBMethodPermission(true,"bankbean",null);
            doEJBMethodPermission(true,"bankbean","withdraw");
            doEJBMethodPermission(true,"bankbean","withdraw,Home");
            doEJBMethodPermission(true,"bankbean","withdraw,Home,java.lang.string");
            doEJBMethodPermission(true,"bankbean",",Home");
    
            doEJBMethodPermission(true,"bankbean",",Home,java.lang.string");
    
            doEJBMethodPermission(true,"bankbean","withdraw");
            doEJBMethodPermission(true,"bankbean","withdraw,Home");
            doEJBMethodPermission(true,"bankbean","withdraw,,java.lang.string");
            doEJBMethodPermission(true,"bankbean","withdraw,,");
            doEJBMethodPermission(false,"bankBean","withdraw","Home",params3);
    
            doEJBMethodPermission(true,"bankBean","withdraw","Home",params1);
            doEJBMethodPermission(true,"bankBean","withdraw","Home",params2);
     
            doEJBMethodPermission(true,"bankBean","withdraw","Home",null);
    
            doEJBMethodPermission(true,"bankBean",null,"Home",params1);
        
            doEJBMethodPermission(true,"bankBean","withdraw",null,params2);
    
            doEJBMethodPermission(true,"bankBean",null,null,null);
            
            EJBMethodPermission a = 
                new EJBMethodPermission("bankBean","withdraw,Home,java.lang.string");
            EJBMethodPermission b = 
                new EJBMethodPermission("myBean","withdraw,Home,java.lang.string");
            EJBMethodPermission c = 
                new EJBMethodPermission("myBean",",Home,java.lang.string");
            EJBMethodPermission d = 
                new EJBMethodPermission("myBean",",,java.lang.string");
            EJBMethodPermission e = 
                new EJBMethodPermission("myBean","withdraw,,java.lang.string");
            EJBMethodPermission f = 
                new EJBMethodPermission("myBean","withdraw,,java.lang.int");
            EJBMethodPermission g = 
                new EJBMethodPermission("myBean","withdraw,Remote,");
            EJBMethodPermission h = 
                new EJBMethodPermission("myBean","withdraw","Remote",new String[0]);
            testSerialization(a);
            testSerialization(b);
            testSerialization(c);
            testSerialization(d);
            testSerialization(e);
            testSerialization(f);
            testSerialization(g);
            testSerialization(h);
    
            doPermissionImplies(false,a,b);
            doPermissionImplies(false,b,a);
            doPermissionImplies(false,c,a);
            doPermissionImplies(false,d,a);
            doPermissionImplies(false,e,a);
            doPermissionImplies(false,f,a);
            doPermissionImplies(false,f,b);
            doPermissionImplies(false,c,d);
            doPermissionImplies(false,e,d);
            doPermissionImplies(true,c,b);
            doPermissionImplies(true,d,b);
            doPermissionImplies(true,e,b);
            doPermissionImplies(true,d,c);
            doPermissionImplies(true,g,h);
            doPermissionImplies(true,h,g);
    
            doEJBRoleRefPermission(true,"EJBName","customer");

            EJBRoleRefPermission a1,a2,a3,a4;
            a1 = new EJBRoleRefPermission("EJBName1","roleRef1");
            a2 = new EJBRoleRefPermission("EJBName1","roleRef2");
            a3 = new EJBRoleRefPermission("EJBName2","roleRef1");
            a4 = new EJBRoleRefPermission("EJBName2","roleRef2");

            testSerialization(a1);
            testSerialization(a2);
            testSerialization(a3);
            testSerialization(a4);
    
            doPermissionImplies(true,a1,a1);
            doPermissionImplies(false,a1,a2);
            doPermissionImplies(false,a1,a3);
            doPermissionImplies(false,a1,a4);
            doPermissionImplies(false,a2,a1);
            doPermissionImplies(true,a2,a2);
            doPermissionImplies(false,a2,a3);
            doPermissionImplies(true,a3,a3);
            doPermissionImplies(false,a3,a4);
            doPermissionImplies(false,a4,a1);
            doPermissionImplies(false,a4,a2);
            doPermissionImplies(false,a4,a3);
            doPermissionImplies(true,a4,a4);
        }

        Method m[] = TestEJBPerms.class.getMethods();
        EJBMethodPermission p[] = new EJBMethodPermission[m.length];

        for (int i=0; i< m.length; i++) {
            doEJBMethodPermission( true, "TestEJBPerms","Home",m[i]);
            p[i] = new EJBMethodPermission("testEJBPerms","Home",m[i]);
            testSerialization(p[i]);
        }

        for (int i=0; i< m.length; i++) {
            for (int j=0; j<m.length; j++) {
                if (i == j) { 
                    doPermissionImplies(true,p[i],p[j]);
                } else {
                    doPermissionImplies(false,p[i],p[j]);
                }
            }
        }

        stat.printSummary();
    }
}
