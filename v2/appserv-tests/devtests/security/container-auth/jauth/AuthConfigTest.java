import com.sun.enterprise.security.jauth.*;

import javax.security.auth.Subject;
//import javax.security.auth.message.config.ServerAuthContext;
//import javax.security.auth.message.config.ClientAuthContext;;
import com.sun.enterprise.security.jauth.AuthConfig;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.*;
import java.lang.reflect.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * 1. domain.xml test:
 *
 *    %java -Ddomain.xml.url=domain.xml
 *	-Djava.security.debug=configfile,configxmlparser
 *	-classpath .:classes:config-api.jar:appserv-commons.jar:schema2beans.jar
 *	AuthConfigTest xml-parse
 * 
 * 2. sun-acc.xml test:
 *
 *    %java -Dsun-acc.xml.url=sun-acc.xml
 *	-Djava.security.debug=configfile,configxmlparser
 *	-classpath .:classes:config-api.jar:appserv-commons.jar:schema2beans.jar
 *	AuthConfigTest xml-parse
 * 
 * 3. custom module config file test:
 *
 *    %java -Djava.authconfig=testConfig/config.module
 *	-Dconfigfile.parser=file
 *	-Djava.security.debug=configfile,configfileparser
 *	-classpath .:classes:config-api.jar:appserv-commons.jar:schema2beans.jar
 *	AuthConfigTest file-parse
 */

public class AuthConfigTest extends Thread {

    private static final String SOAP = "SOAP";
    private static final String HTTP = "HTTP";
    private static final String EJB = "EJB";
    
    private static final String testId = "Sec::Container-Auth_Test_Num_";

    private static final AuthPolicy configRequest =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_SENDER, true, true);

    private static final AuthPolicy configResponse =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_CONTENT, true, false);

    private static final AuthPolicy ddRequest =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_CONTENT, false, false);

    private static final AuthPolicy ddResponse =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_SENDER, false, false);

    private static final AuthPolicy ddHttpRequest =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_NONE, false, false);

    private static final AuthPolicy ddHttpResponse =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_CONTENT, false, false);

    private static final AuthPolicy ddEjbResponse =
		new AuthPolicy(AuthPolicy.SOURCE_AUTH_NONE, true, false);

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    
    public static void main(String[] args) throws Exception {
        
        int testnum = 0;
        if (args == null || args.length == 0 ||
        args[0].equalsIgnoreCase("xml-parse")) {
            
            testnum = xmlParse(testnum);
            
        } else if (args[0].equalsIgnoreCase("file-parse")) {
            
            testnum = fileParse(testnum);
            
        } else {
            throw new Exception("unrecognized command for AuthConfigTest");
        }
    }

    private static int xmlParse(int testnum) throws Exception {

        try{
            System.setProperty("config.parser", "com.sun.enterprise.security.appclient.ConfigXMLParser");
            AuthConfig config = AuthConfig.getAuthConfig();
            ClientAuthContext cac;
            ServerAuthContext sac;
            
            Subject subject = new Subject();
            HashMap options;
            TestCredential cred1;
            
            /**
             * Test NULL return
             */
            stat.addDescription("Sec::Container-auth tests");
            String testid = testId + testnum;
            if (config.getServerAuthContext(EJB, "foo", null, null, null) != null) {
                stat.addStatus(testid, stat.FAIL);
            }else
                stat.addStatus(testid, stat.PASS);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            if (config.getServerAuthContext(EJB, null, null, null, null) != null) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
            testnum++;
            
            /**
             * SOAP - CLIENT
             */
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP, "foo", null, null, null);
            cac.secureRequest(null, subject, null);
            options = new HashMap();
            options.put("option1", "true");
            cred1 = new TestCredential("ClientModule1",
                options,
                configRequest,
                configResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);

            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                    "app1",
                    ddRequest,
                    ddResponse,
                    null);
            cac.secureRequest(null, subject, null);
            cred1 = new TestCredential("ClientModule1",
                        new HashMap(),
                        ddRequest,
                        ddResponse);
            
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);

            cac.disposeSubject(subject, null);
            testnum++;
            
            System.out.println("Testing Container-auth testid = "+testnum);
            cac = config.getClientAuthContext(SOAP,
                        "app4",
                        ddRequest,
                        null,
                        null);
            cac.secureRequest(null, subject, null);
            cred1 = new TestCredential("ClientModule1",
                            new HashMap(),
                            ddRequest,
                            null);            
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
               stat.addStatus(testid+testnum, stat.PASS);

            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                            "app3",
                                            null,
                                            ddResponse,
                                            null);
            cac.secureRequest(null, subject, null);
            cred1 = new TestCredential("ClientModule1",
                                        new HashMap(),
                                        null,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                                "foo",
                                                ddRequest,
                                                ddResponse,
                                                null);
            cac.secureRequest(null, subject, null);
            options = new HashMap();
            options.put("option1", "true");
            cred1 = new TestCredential("ClientModule1",
                                        options,
                                        ddRequest,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                                null,
                                                ddRequest,
                                                ddResponse,
                                                null);
            cac.secureRequest(null, subject, null);
            options = new HashMap();
            options.put("option1", "true");
            cred1 = new TestCredential("ClientModule1",
                                        options,
                                        ddRequest,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                                "app1",
                                                ddRequest,
                                                null,
                                                null);
            cac.secureRequest(null, subject, null);
            cred1 = new TestCredential("ClientModule1",
                                        new HashMap(),
                                        ddRequest,
                                        null);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                                "app1",
                                                null,
                                                ddResponse,
                                                null);
            cac.secureRequest(null, subject, null);
            cred1 = new TestCredential("ClientModule1",
                                        new HashMap(),
                                        null,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            // SKIP DD URI8 entry in XML-PARSE case
            
            cac = config.getClientAuthContext(SOAP,
                                                null,
                                                ddRequest,
                                                null,
                                                null);
            cac.secureRequest(null, subject, null);
            options = new HashMap();
            options.put("option1", "true");
            cred1 = new TestCredential("ClientModule1",
                                        options,
                                        ddRequest,
                                        null);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);

            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            cac = config.getClientAuthContext(SOAP,
                                                null,
                                                null,
                                                ddResponse,
                                                null);
            cac.secureRequest(null, subject, null);
            options = new HashMap();
            options.put("option1", "true");
            cred1 = new TestCredential("ClientModule1",
                                        options,
                                        null,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            /**
             * SOAP - SERVER
             */
            
            sac = config.getServerAuthContext(SOAP,
                                                null,
                                                ddRequest,
                                                null,
                                                null);
            sac.validateRequest(null, subject, null);
            cred1 = new TestCredential("ServerModule1",
                                        new HashMap(),
                                        ddRequest,
                                        null);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            sac = config.getServerAuthContext(SOAP,
                                                null,
                                                null,
                                                ddResponse,
                                                null);
            sac.validateRequest(null, subject, null);
            cred1 = new TestCredential("ServerModule1",
                                        new HashMap(),
                                        null,
                                        ddResponse);
            if (!subject.getPublicCredentials().contains(cred1)) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            cac.disposeSubject(subject, null);
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
            
            /**
             * SOAP - check null request/response policies
             */
            
            if (config.getServerAuthContext(SOAP,
                                            "app6",
                                            null,
                                            null,
                                            null) != null) {
                stat.addStatus(testid+testnum, stat.FAIL);
            }else
                stat.addStatus(testid+testnum, stat.PASS);
                
            testnum++;
            System.out.println("Testing Container-auth testid = "+testnum);
        }finally{
            stat.printSummary();
        }
        
        return testnum;
    }
    
    private static int fileParse(int testnum) throws Exception {
        
        AuthConfig config = AuthConfig.getAuthConfig();
        ClientAuthContext cac;
        ServerAuthContext sac;
        
        Subject subject = new Subject();
        HashMap options;
        TestCredential cred1;
        TestCredential cred2;
        
        testnum = xmlParse(testnum);
        
        /**
         * test case for multiple modules
         */
        
        cac = config.getClientAuthContext(SOAP,
        "app5",
        ddRequest,
        ddResponse,
        null);
        cac.secureRequest(null, subject, null);
        cred1 = new TestCredential("ClientModule1",
        new HashMap(),
        ddRequest,
        ddResponse);
        cred2 = new TestCredential("ClientModule2",
        new HashMap(),
        ddRequest,
        ddResponse);
        if (!subject.getPublicCredentials().contains(cred1) ||
        !subject.getPublicCredentials().contains(cred2)) {
            throw new SecurityException("test " + testnum++ + " failed");
        }
        cac.disposeSubject(subject, null);
        testnum++;
        
        /**
         * HTTP - SERVER
         */
        
        sac = config.getServerAuthContext(HTTP,
        null,
        ddHttpRequest,
        null,
        null);
        sac.validateRequest(null, subject, null);
        cred1 = new TestCredential("ServerModule1",
        new HashMap(),
        ddHttpRequest,
        configResponse);
        cred2 = null;
        if (!subject.getPublicCredentials().contains(cred1)) {
            throw new SecurityException("test " + testnum++ + " failed");
        }
        sac.disposeSubject(subject, null);
        testnum++;
        
        sac = config.getServerAuthContext(HTTP,
        "app9",
        null,
        ddHttpResponse,
        null);
        sac.validateRequest(null, subject, null);
        cred1 = new TestCredential("ServerModule1",
        new HashMap(),
        configRequest,
        ddHttpResponse);
        cred2 = null;
        if (!subject.getPublicCredentials().contains(cred1)) {
            throw new SecurityException("test " + testnum++ + " failed");
        }
        sac.disposeSubject(subject, null);
        testnum++;
        
        /**
         * EJB - SERVER
         */
        sac = config.getServerAuthContext(EJB,
        "app10",
        null,
        ddEjbResponse,
        null);
        sac.validateRequest(null, subject, null);
        cred1 = new TestCredential("ServerModule1",
        new HashMap(),
        configRequest,
        ddEjbResponse);
        cred2 = null;
        if (!subject.getPublicCredentials().contains(cred1)) {
            throw new SecurityException("test " + testnum++ + " failed");
        }
        sac.disposeSubject(subject, null);
        testnum++;
        
        return testnum;
    }
}
