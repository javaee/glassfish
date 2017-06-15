package com.sun.ejte.ccl.webrunner.webtest;

import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;

/**
 *This class is called from WebTest.
 *
 * @author       Deepa Singh (deepa.singh@sun.com)
 *Company       Sun Microsystems Inc.
 *
 */
public class SendRequest {
    
    private int nThreads;
    private int id ;
    private byte[] filebuf;
    private String website;
    private String cookie_name;
    private String cookie_value;
    private String cookie_path;
    Logger logger = Logger.getLogger("bank.admin");
    ConsoleHandler ch = new ConsoleHandler();    
    private String TEST_SUITE_ID = "WEBCLIENT";
    
    private static String CRLF="\r\n";
    
    int status,OK=0;
    int notOK=0;
    int url_size;
    double start,end;
    boolean ifdone=false;
    boolean setCookie=false;
    boolean serverCookieSet=false;
    FileOutputStream errout=null;
    //int port=8000;
    private int m_port=80;
    int port;
    private String m_host=new String("local");
    boolean serverSet=false;    
    private static int buffer_size=8192;
    private String ws_root;
    Reporter reporter=null;
    
    
    public SendRequest(String ws_root,String testsuite_id)
    {
        this.ws_root=ws_root;
        this.TEST_SUITE_ID=testsuite_id;
        reporter = Reporter.getInstance(ws_root);
    }
    
    



    public void setServerProperties(String host,int port) {
        this.m_host=host;
        this.m_port=port;
        serverSet=true;
    }
    
    
    
    /**This is a thread safe method so that multiple session of web application can be generated simulataneosly.
     *It parses the byte array and extract individual requests.Uses MimeHeader class for parsing MIME headers.Handles session management by checking if server has set "Set-Cookie" in it's response header.
     *@param host String host name of the web server where web application is to be run e.g. dsingh.sfbay.sun.com
     *@param filebuf byte[] byte array which contains script file.
     *@return int Status Code of web server response.
     *
     */
    public synchronized int processUrl(byte[] filebuf) {        
	    int responseCode=0;
	    int nRequest=0;
	    int code=0;
	    reporter.setTestSuite(TEST_SUITE_ID, "J2EE Web Client","This test suite integrates web and ejb");


	    try {
		    Socket server=null;
		    InetAddress serverAddress=null;
		    BufferedReader bufferedStream=null;
		    DataOutputStream server_out= null;

		    int pos=0;
		    int retVal=0;
		    double currentstart,currentstop;
		    double temp=0;
		    double time=0;
		    char c;
		    String hostName = new String("");

		    int i=0,j=0;
		    MimeHeader mh;
		    while(i<filebuf.length) {
			    c=(char)filebuf[i];

			    switch(c) {
				    case '!':

					    if(filebuf[i+1] =='\n' && filebuf[i+2]=='!') {
						    nRequest=nRequest+1;
						    String temp1=new String(filebuf,j,i-1-j);
						    mh=new MimeHeader(temp1);
						    if(setCookie) {
							    //Cookie: JSESSIONID=4659B62637AC12324972EA5072064469
							    //String full_cookie_value="JSESSIONID="+cookie_value+";Path="+cookie_path;
							    //String full_cookie_value="JSESSIONID="+cookie_value;
							    String full_cookie_value=cookie_name+"="+cookie_value;
							    full_cookie_value=full_cookie_value.trim();
							    String oldcookie=mh.get("Cookie");
							    //  System.out.println("Old Cookie value"+oldcookie);
							    mh.put("Cookie",full_cookie_value);
						    }

						    int stIdx = temp1.indexOf("Host: ");
						    stIdx+=5;
						    String tp = temp1.substring(stIdx);

						    int endIdx = tp.indexOf('\n');
						    endIdx+=stIdx;
						    hostName = temp1.substring(stIdx, endIdx);

						    int portIdx=hostName.indexOf(':');
						    String strport=new String();
						    if(portIdx > 0){
						    strport=hostName.substring(portIdx+1).trim();
							    hostName=hostName.substring(0,portIdx);
						    //System.out.println("port is"+strport);
}
						    hostName = hostName.trim();
						    port=new Integer(strport).intValue();
                                                    // if serverSet is true,then use m_host and m_port from the commandline
                                                    //otherwise read host and port from the script.txt
                                                    if(serverSet){
						    System.out.println("HTTP port :"+m_port);
						    System.out.println("HTTP hostname :"+m_host);
						    server=new Socket(m_host,m_port);
                                                    }
                                                    else 
                                                        server=new Socket(hostName,port);
						    serverAddress=server.getInetAddress();
						    bufferedStream=new BufferedReader(new InputStreamReader(server.getInputStream()));
						    server_out= new DataOutputStream(server.getOutputStream());
						    String req=new String();
						    if(mh.ifPOSTRequest()) {
							    //System.out.println("POST REQUEST");
							    req=mh.getRequestHeader()+ CRLF + mh+CRLF+ mh.getPostData()+CRLF+CRLF;
							    //String post="POST" + " " + server_url+ " " + "HTTP/1.0" + CRLF + inmh
							    //+ CRLF + postdata+ CRLF+CRLF;

						    }
						    else
							    req=mh.getRequestHeader()+ CRLF + mh + CRLF + CRLF;

						    String requestLine=mh.getRequestHeader();
						    int fsp=requestLine.indexOf(' ');
						    int nsp=requestLine.indexOf(' ',fsp+1);
						    int eol=requestLine.indexOf('\n');
						    requestLine=requestLine.substring(fsp+1,nsp);

						    //Following are debug output statements
						    //System.out.println("%%%%%%%%%%%%%%%%%%%%%%Request String Sent to Server%%%%%%%%%%%%%%%%%%%");
						    //System.out.println(mh.getRequestHeader());
						    //System.out.println(mh);

						    try {
							    if(server_out!=null)
								    server_out.write(req.getBytes());
							    else System.out.println("Server_out is null");
						    }
						    catch(IOException e) {
							    String errMsg=e.getMessage();
							    System.out.println(e.getMessage());
						    }

						    code = readHeader(bufferedStream);
						    reporter.addTest(TEST_SUITE_ID,requestLine );
						    reporter.addTestCase(TEST_SUITE_ID,requestLine, requestLine );


						    if(code==500 || code==404) {
							    if(code==404){
								    System.out.println("WebServer returned error code 404");
								    System.out.println("Request Resource not available");
								    System.out.println("There is some deployment error");
							    }
							    System.out.println("!!Server returned error..Application Exiting");
							    reporter.setTestCaseStatus(TEST_SUITE_ID,requestLine,requestLine,ReporterConstants.FAIL);
							    //stat.addStatus("WEBCLIENT "+ requestLine,stat.FAIL);
							    System.out.println(TEST_SUITE_ID+"\t"+requestLine+"\t FAIL");
                                                            reporter.generateValidReport();
							    return 0;
						    }
						    if (code==200){
							    reporter.setTestCaseStatus(TEST_SUITE_ID,requestLine,requestLine,ReporterConstants.PASS);
							    //stat.addStatus("WEBCLIENT"+" "+ requestLine,stat.PASS);
							    System.out.println(TEST_SUITE_ID+"\t"+requestLine+"\t"+"PASS");
						    }

					    }
					    i=i+3;
					    j=i;

				    default:

					    i=i+1;
			    }
			    if(time>=temp)
				    temp=time;

		    }
		    // System.out.println("while loop ended");
		    bufferedStream.close();
		    server_out.close();
		    reporter.generateValidReport();
		    return code;
	    }
	    catch(Exception e) {
		    e.printStackTrace();
		    String msg=e.getMessage();
	    }
	    return code;
    }
    
    public synchronized int readHeader(BufferedReader inStream) {
        int status=0;
        String reasonPhrase = new String("");
        String st=null;
        try {
            StringBuffer result=new StringBuffer();
            st=inStream.readLine();
                /*if(st!=null)
                System.out.println("*****Server Response Starts *****\n"+st );*/
            
            //st should be of form HTTP/1.1 200 OK
            int fsp=st.indexOf(' ');
            int nsp=st.indexOf(' ',fsp+1);
            int eol=st.indexOf('\n');
            status=Integer.parseInt(st.substring(fsp+1,nsp));
            String line;
            while((line=inStream.readLine())!=null) {
                
                //Trying to parse
                //Set-Cookie: JSESSIONID=E649A94960EB0304CFA985067EF90893;Path=/bookstore1
                if(line.startsWith("Set-Cookie:") && !setCookie) {
                    if(line!=null) {
                        //   System.out.println("Setting cookie from server");
                        //  System.out.println("COOKIES\t"+line);
                    }
                    
                    int colon=line.indexOf(':');
                    int firstEquals=line.indexOf('=');
                    int semicolon=line.indexOf(';');
                    cookie_name=line.substring(colon+1,firstEquals).trim(); //gives JSESSIONID
                    cookie_value=line.substring(firstEquals+1,semicolon).trim();//gives SessionID
                    cookie_path=line.substring(line.indexOf("Path")+5).trim();
                    setCookie=true;
                }
                if(line!=null) {
                    StringBuffer server_response=new StringBuffer();
                    server_response.append(line);
                    String response=server_response.toString();
                    
                    //System.out.println(line);
                }
                
                
            }
            //System.out.println("************Server Response Ends*****************");
            
        }
        catch(Exception e) {
            System.err.println("-------------------- Read Header Fail---------------------------");
            System.err.println("Error details:" + e.getMessage());
            e.printStackTrace();
            
            return status;
        }
        
        return status;
    }
    
}
