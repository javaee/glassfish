/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest extends Thread
{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");
    private static final String HOST = "localhost";
    private static final int PORT = 7007;
    private ServerSocket ss = null;

    public WebTest() {
        try {
           ss = new ServerSocket(PORT, 0, InetAddress.getByName(HOST));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            stat.addDescription("SocketImpl Test");
            Socket s = ss.accept();
            s.setSoLinger(false, 10);
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            int count = 1;
            os.write(count);        
            
            while (true) {                
                if (count > 10) {
                    s.close();
                    break;
                }
                System.out.println("Count is :" + count);
                int i = is.read();
                if (count != i) throw new RuntimeException ("Data Wrong: Expected :" + count + " Obtained:" + i);                
                if (count == 5) { 
                    Thread.sleep(6000);                    
                    count++;
                    int j = is.read();
                    if (count != j) throw new RuntimeException ("Data Wrong: Expected :" + count + " Obtained:" + i);
                }
                os.write(++count);
            }        
        } catch (RuntimeException rex) {
            rex.printStackTrace();
            stat.addStatus("SocketImpl SocketImpl", stat.FAIL);
        } catch (Exception ex) {
            stat.addStatus("SocketImpl SocketImpl", stat.FAIL);
            ex.printStackTrace();
        }
        stat.addStatus("SocketImpl SocketImpl", stat.PASS);
    }

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone Servlet/Filter war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        WebTest test = new WebTest();
        test.start();
        
        try {
            goGet(host, port, "FILTER", contextRoot + "/ServletTest" );
            Thread.sleep(2000);
            //Thread.currentThread().join();
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("Test standalonewarUNPREDICTED-FAILURE",
                            stat.FAIL);
        }           
        stat.printSummary("web/standalonewar---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        long time = System.currentTimeMillis();
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write(("Host: localhost\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-requestdispatcher: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-requestdispatcher: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
         }
   }
  
}
