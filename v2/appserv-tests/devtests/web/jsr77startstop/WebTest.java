import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
import javax.management.*;
import java.util.*;
import javax.management.remote.*;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
/**
 * Fix for Bugtraq 5018278: naming: javax.naming.NamingException after a 
 * Stop/Start cycle of a web app
 */
public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;  
    private static String adminUser = null;
    private static String adminPassword = null;
    private static int adminPort = 4848;
    
    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
        adminPort = Integer.parseInt(args[5]);

        try{
            stat.addDescription("JMX undeployment event test.");
            
            System.out.println("context root: " + contextRoot);
            System.out.println("admin: " + adminUser);
            System.out.println("admin.port: " + adminPort);
            System.out.println("admin.password: " + adminPassword);
        
            manageRemoteContext(contextRoot);

            
            url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest");
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                DataOutputStream out = 
                   new DataOutputStream(urlConnection.getOutputStream());
                                    out.writeByte(1);

               int responseCode=  urlConnection.getResponseCode();
               System.out.println("responseCode: " + responseCode);
                
               if (urlConnection.getResponseCode() != 200){
                    stat.addStatus("jsr77StartStopContext", stat.FAIL);
               } else {
                    stat.addStatus("jsr77StartStopContext", stat.PASS);
               }
            }

            stat.printSummary("web/jmxUndeployEvent");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void manageRemoteContext(String contextRoot){

        try{

            javax.management.ObjectName applicationsMBean = new javax.management.ObjectName("com.sun.appserv:J2EEApplication=null,J2EEServer=server,j2eeType=WebModule,name=//server"+contextRoot);
            
            try {
                invokeHttp(contextRoot, applicationsMBean);
            } catch (Throwable t){
                invokeHttps(contextRoot, applicationsMBean);
            }

        } catch (Throwable ex){
            stat.addStatus("remote-jmx-manage", stat.FAIL);
            ex.printStackTrace();
        }
     }


    private static void invokeHttp(String contextRoot, 
                                   ObjectName applicationsMBean) throws Throwable {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("#### Stop the context: " + contextRoot);

        Object o= getMBeanServerConnection().invoke(applicationsMBean, "stop", params, signature);

        System.out.println("#### Start the context: " + contextRoot);

        o= getMBeanServerConnection().invoke(applicationsMBean, "start", params, signature);
        stat.addStatus("remote-jmx-manage", stat.PASS);
    }

    private static MBeanServerConnection getMBeanServerConnection() throws Throwable{
       return getMBeanServerConnection("localhost",adminPort,adminUser,adminPassword); 
    }


    private static MBeanServerConnection getMBeanServerConnection
                        (String host, int port,String user, String password) throws Throwable{

        final JMXServiceURL url = new JMXServiceURL("service:jmx:s1ashttp://" +
        host + ":" + port);
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
        
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user );
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME,
        DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        final JMXConnector conn = JMXConnectorFactory.connect(url, env);
        return conn.getMBeanServerConnection();
}

    private static void invokeHttps(String contextRoot,
                                    ObjectName applicationsMBean) throws Throwable {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        System.out.println("#### Stop the context: " + contextRoot);

        Object o= getSecureMBeanServerConnection()
                    .invoke(applicationsMBean, "stop", params, signature);

        System.out.println("#### Start the context: " + contextRoot);

        o= getSecureMBeanServerConnection().invoke(applicationsMBean, "start", params, signature);
        stat.addStatus("remote-jmx-manage", stat.PASS);
    }

    private static MBeanServerConnection getSecureMBeanServerConnection() throws Throwable{
       return getSecureMBeanServerConnection
                        ("localhost",adminPort,adminUser,adminPassword); 
    }

    private static MBeanServerConnection 
        getSecureMBeanServerConnection(String host, 
                                       int port,
                                       String user, 
                                       String password) throws Throwable{

        final JMXServiceURL url = new JMXServiceURL("service:jmx:s1ashttps://" +
                                  host + ":" + port);
        final Map env = new HashMap();
        final String PKGS = "com.sun.enterprise.admin.jmx.remote.protocol";
                                                                            
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, PKGS);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user );
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME,
        DefaultConfiguration.DIGEST_HTTP_AUTH_SCHEME);
        final JMXConnector conn = JMXConnectorFactory.connect(url, env);
        return conn.getMBeanServerConnection();
    }
    
}
