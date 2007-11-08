package client;

import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.Version;
import com.sun.xml.ws.transport.tcp.util.VersionController;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.annotation.Resource;

import servlet_endpoint.ServletHelloService;
import servlet_endpoint.ServletHello;

import ejb_endpoint.WSHelloEJBService;
import ejb_endpoint.WSHelloEJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    @Resource
    static javax.transaction.UserTransaction ut;
    
    public static final int TIME_OUT = 1000 * 60;
    
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    
    public static void main(String[] args) {
        stat.addDescription("soaptcp-test");
        String host = "localhost";
        int port = 8080;
        if (args.length == 1) { // run with appclient?
            args = args[0].split(" ");
        }
        
        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        System.out.println("Connecting to host: " + host + " port: " + port);
        Client client = new Client();
        if (client.doPortUnificationTest(host, port)) {
            client.doServletTest();
            client.doEjbTest();
        } else {
            stat.addStatus("SOAP/TCP-Servlet-Endpoint", stat.DID_NOT_RUN);
            stat.addStatus("SOAP/TCP-EJB-Endpoint", stat.DID_NOT_RUN);
        }

        stat.printSummary("soaptcp-test");
    }
    
    public void doServletTest() {
        try {
            ServletHelloService svc = new ServletHelloService();
            ServletHello port = svc.getServletHelloPort();
            
            for (int i=0;i<10;i++) {
                String ret = port.sayServletHello("Appserver Tester !");
                if(ret.indexOf("WebSvcTest-Servlet-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("SOAP/TCP-Servlet-Endpoint", stat.FAIL);
                    return;
                }
                System.out.println(ret);
            }
            stat.addStatus("SOAP/TCP-Servlet-Endpoint", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("SOAP/TCP-Servlet-Endpoint", stat.FAIL);
        }
    }
    
    public void doEjbTest() {
        try {
            WSHelloEJBService svc = new  WSHelloEJBService();
            WSHelloEJB port = svc.getWSHelloEJBPort();
            ut.getStatus();
            for (int i=0;i<10;i++) {
                String ret = port.sayEjbHello("Appserver Tester !");
                if(ret.indexOf("WebSvcTest-EJB-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("SOAP/TCP-EJB-Endpoint", stat.FAIL);
                    return;
                }
                System.out.println(ret);
            }
            stat.addStatus("SOAP/TCP-EJB-Endpoint", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("SOAP/TCP-EJB-Endpoint", stat.FAIL);
        }
    }
    
    public boolean doPortUnificationTest(String host, int port) {
        try {
            Socket s = new Socket("localhost", 8080);
            s.setSoTimeout(TIME_OUT);
            final VersionController versionController = VersionController.getInstance();
            final Version framingVersion = versionController.getFramingVersion();
            final Version connectionManagementVersion = versionController.getConnectionManagementVersion();
            
            final OutputStream outputStream = s.getOutputStream();
            outputStream.write(TCPConstants.PROTOCOL_SCHEMA.getBytes("US-ASCII"));
            
            DataInOutUtils.writeInts4(outputStream, framingVersion.getMajor(),
                    framingVersion.getMinor(),
                    connectionManagementVersion.getMajor(),
                    connectionManagementVersion.getMinor());
            outputStream.flush();
            
            final InputStream inputStream = s.getInputStream();
            final int[] versionInfo = new int[4];
            
            DataInOutUtils.readInts4(inputStream, versionInfo, 4);
            
            final Version serverFramingVersion = new Version(versionInfo[0], versionInfo[1]);
            final Version serverConnectionManagementVersion = new Version(versionInfo[2], versionInfo[3]);
            
            final boolean success = versionController.isVersionSupported(serverFramingVersion, serverConnectionManagementVersion);
            if (success) {
                stat.addStatus("SOAP/TCP-PortUnification", stat.PASS);
                return true;
            } else {
                System.out.println("SOAP/TCP version mismatch. Sent{fv: " + framingVersion + "; cmv: " + connectionManagementVersion + "}" +
                        " Received{fv: " + serverFramingVersion + "; cmv: " + serverConnectionManagementVersion + "}");
                stat.addStatus("SOAP/TCP-PortUnification", stat.FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("SOAP/TCP-PortUnification", stat.FAIL);
        }
        
        return false;
    }
}

