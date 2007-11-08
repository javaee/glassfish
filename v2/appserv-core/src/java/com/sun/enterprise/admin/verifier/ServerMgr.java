/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/*
 * ServerCheck.java
 *
 */

package com.sun.enterprise.admin.verifier;


import java.util.Vector;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.*;
//import com.sun.enterprise.tools.verifier.*;

import com.sun.enterprise.tools.verifier.TestInformation;
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.admin.verifier.Result;
import com.sun.enterprise.tools.verifier.StringManagerHelper;

import java.util.StringTokenizer;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.admin.common.exception.AFRuntimeException;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 *  ServerMgr reads all TestCases and verifies it with server.xml file
 * 
 * <p><b>NOT THREAD SAFE: mutable instance variables with stomp: 'result'</b>
 */

public class ServerMgr implements ConfigContextEventListener {
    
    // Logging
    static final Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);

    public final String testFileName = "ServerTestList.xml";
    public static final HashMap testCases = new HashMap();
    public static volatile String fileUrl = null;
    public volatile String description = "Tests for server.xml";
    public final boolean debug;
    
    public volatile com.sun.enterprise.admin.verifier.Result result=null;
   // public final Vector vresult=null;
    
    public final com.sun.enterprise.util.LocalStringManagerImpl smh =
            StringManagerHelper.getLocalStringsManager();
    
    /** Creates a new instance of ServerCheck */
    public ServerMgr() {
        debug = false;
    }
    
    public ServerMgr(boolean verbose){
        debug = true;
    }
    
    public static void setFile(String file){
        fileUrl = file;
    }
    
    public boolean loadTestInfo() {
        boolean allIsWell = true;
        
        if(testCases.isEmpty()) {
            if (debug) {
                // Logging
                _logger.log(Level.INFO, "serverxmlverifier.getting_testnamefrom_propertyfile");
            }  
            File inputFile = getTestFile(testFileName);
            try {
                // parse the xml file
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(inputFile);
                NodeList list = doc.getElementsByTagName("description");
                if (list.getLength()>0) {
                    Element e = (Element) list.item(0);
                    description = e.getFirstChild().getNodeValue().trim();
                }

                list = doc.getElementsByTagName("test");
                for (int i=0;i<list.getLength();i++) {
                    Element e = (Element) list.item(i);
                    NodeList nl = e.getChildNodes();
                    TestInformation ti = new TestInformation();
                    String testName = "";
                    for (int j=0;j<nl.getLength();j++) {
                        String nodeName = nl.item(j).getNodeName();
                        if("test-name".equals(nodeName.trim())) {
                            Node el = (Node)nl.item(j);
                            testName = el.getFirstChild().getNodeValue().trim();
                        }
                        if ("test-class".equals(nodeName.trim())) {
                            Node el = (Node) nl.item(j);
                            ti.setClassName(el.getFirstChild().getNodeValue().trim());
                        }
                        if ("minimum-version".equals(nodeName.trim())) {
                            Node el = (Node) nl.item(j);
                            ti.setMinimumVersion(el.getFirstChild().getNodeValue().trim());
                        }                                        
                        if ("maximum-version".equals(nodeName.trim())) {
                            Node el = (Node) nl.item(j);
                            ti.setMaximumVersion(el.getFirstChild().getNodeValue().trim());
                        }   
                    }
                    testCases.put(testName,ti);
                }            
            } catch (ParserConfigurationException e) {
                // Logging
                _logger.log(Level.WARNING, "serverxmlverifier.parser_error", e);
                allIsWell = false;
            } catch (org.xml.sax.SAXException e) {
                // Logging
                _logger.log(Level.WARNING, "serverxmlverifier.sax_error", e);
                allIsWell = false;            
            } catch (IOException e) {
                // Logging
                _logger.log(Level.WARNING, "serverxmlverifier.error_loading_xmlfile");
                allIsWell = false;
            } 
        }
        
        return allIsWell;
    }
    
    public HashMap getTests() {
        return testCases;
    }
    
    private File getTestFile(String name) {
        
        String iasHome = System.getProperty("s1as.home");
        if(iasHome!=null) {
            File temp = new File(iasHome,"lib");
            temp = new File(temp,name);
            if(temp.exists())
                return temp;
            else
                return null;
        }
        else
            return getFileFromCP(name);
    }
    
    private File getFileFromCP(String name) {
        File cand = null;
        String classPath = System.getProperty("java.class.path");
        String classPathSep = File.pathSeparator;
        StringTokenizer tokens = new StringTokenizer(classPath,classPathSep);
        while(tokens.hasMoreTokens()) {
            String fileName = tokens.nextToken();
            if(fileName.endsWith("appserv-rt.jar")) {
                // File Path separator in classpath set in server.xml is
                // always forward-slash (/), so trying forward slash first
                int slashPos = fileName.lastIndexOf('/');
                if (slashPos == -1) {
                    slashPos = fileName.lastIndexOf(File.separator);
                }
                if (slashPos != -1) {
                    String libPath = fileName.substring(0, slashPos);
                    cand = new File(libPath,name);
                }
                break;
            }
        }
        return cand;
    }
    
    /*public boolean check(ConfigContext context) {
        if(testCases.isEmpty())
            loadTestInfo();
        Iterator testClasses = testCases.values().iterator();
        ResultMgr resultMgr = new ResultMgr();
        while(testClasses.hasNext()) {
            TestInformation ti = (TestInformation)testClasses.next();
            String testClass = ti.getClassName();
            try {
                Result r = null;
                Class test = Class.forName(testClass);
                ServerCheck tester = (ServerCheck)test.newInstance();
                r = tester.check(context);
                r.setStatus(0);
                resultMgr.addResults(r);
                r.setStatus(1);
                resultMgr.addResults(r);
            }
            catch (Throwable tt) {
                // Logging
                _logger.log(Level.FINE, "serverxmlverifier.error_check", tt);
            }
        }
        writeToFile(resultMgr);
        return true;
    }
    
    // <addition> srini@sun.com
    // Function added to return a boolean after verification of all tests
    // true if all tests passed
    // false if one test failed
    
    public boolean testStatus(ConfigContext context) {
        boolean retValue = true;
        if(testCases.isEmpty())
              loadTestInfo();
        Iterator testClasses = testCases.values().iterator();
        ResultMgr resultMgr = new ResultMgr();
        while(testClasses.hasNext()) {
            TestInformation ti = (TestInformation)testClasses.next();
            String testClass = ti.getClassName();
            try {
                Result r = null;
                Class test = Class.forName(testClass);
                ServerCheck tester = (ServerCheck)test.newInstance();
                r = tester.check(context);
                r.setStatus(0);
                resultMgr.addResults(r);
                r.setStatus(1);
                resultMgr.addResults(r);
                Vector vobj = resultMgr.getFailedResults();
                for(int i=0;i<vobj.size();i++)  {
                        result=(Result)vobj.elementAt(i);
                        vresult=result.getErrorDetails();
                        if(vresult.size() > 0)  
                            retValue = false;
                }
            }
            catch (Throwable tt) {
                // Logging
                _logger.log(Level.FINE, "serverxmlverifier.error_check", tt);
                return false;
            }
        } 
        writeToFile(resultMgr);
        return retValue;
     }
     
     // </addition>
    
     public void writeToFile(ResultMgr resultMgr) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        Calendar calendar = Calendar.getInstance();
        String timestamp = new Integer(calendar.get(Calendar.HOUR_OF_DAY)).toString() +
                           new Integer(calendar.get(Calendar.MINUTE)).toString() +
                           new Integer(calendar.get(Calendar.MILLISECOND)).toString();
        if(fileUrl == null)
            fileUrl = tmpDir + "/TestResults" + timestamp + ".txt";
        // Print the file it is writing the output
        // Logging
        _logger.log(Level.INFO, "serverxmlverifier.test_output", fileUrl);
        try {
            FileOutputStream fout = new FileOutputStream(fileUrl);
            PrintWriter pout = new PrintWriter(fout);
            pout.println("------------------------------------");
            pout.println("SERVER.XML VERIFICATION TEST RESULTS");
            pout.println("------------------------------------");
            pout.println();
            printFailedDetails(resultMgr.getFailedResults(), pout);
            pout.close();
        } catch(IOException e) {
            // Logging
            _logger.log(Level.WARNING, "serverxmlverifier.error_writing_file");
        }
    }

    public void printFailedDetails(Vector vobj, PrintWriter pout){
        pout.println("    FAILED TESTS   ");
        pout.println("    ------------   ");
        pout.println();
        boolean tests = true;
        for(int i=0;i<vobj.size();i++)  {
                result=(Result)vobj.elementAt(i);
                vresult=result.getErrorDetails();
                if(vresult.size() > 0) {
                    tests =false;
                    pout.println(result.getTestName() + ":");
                }
                for(int j=0;j<vresult.size();j++) 
                   pout.println("       " + (String)vresult.elementAt(j));
                pout.println();
        }
        if(tests)  
            pout.println("                      ***** None *****");
    }*/

    //public boolean check(String name, Object value, ConfigContext context, String choice) {
    public boolean check(ConfigContextEvent ccce) {
        String name = ccce.getName();
        Object value = ccce.getObject();
        ConfigContext context = ccce.getConfigContext();
        String choice = ccce.getChoice();
        String beanName = ccce.getBeanName();
        
        if(name == null && beanName == null)
                return true;
        boolean retValue = false;
        if(testCases.isEmpty())
            loadTestInfo();
        TestInformation ti = (TestInformation)testCases.get(name);
        if(ti == null && beanName != null)
                    ti =(TestInformation)testCases.get(beanName);
        String testClass;
        try {
            testClass = ti.getClassName();
        }
        catch(Exception e){
               return true;
        }

        try {
            Class test = Class.forName(testClass);
            ServerCheck tester = (ServerCheck)test.newInstance();
            result = tester.check(ccce);
            if (result.getStatus() == Result.PASSED)
                retValue = true;
            else
                retValue = false;
        }
        catch(Throwable tt) {
            // Logger
            _logger.log(Level.FINE, "serverxmlverifier.error_check", tt);
            retValue = true;
        }
        return retValue;
    }
    
    /**
     * after config add, delete, set, update or flush. type is in ccce
     */
    public void postAccessNotification(ConfigContextEvent ccce) {
    }
    
    /**
     * after config add, delete, set, update or flush. type is in ccce
     */
    public void postChangeNotification(ConfigContextEvent ccce)  {
    }
    
    /**
     * before config add, delete, set, update or flush. type is in ccce
     */
    public void preAccessNotification(ConfigContextEvent ccce) {
    }
    
    /**
     * before config add, delete, set, update or flush. type is in ccce
     */
    public void preChangeNotification(ConfigContextEvent ccce) {
         if(! check(ccce))
                throw new AFRuntimeException(result.getErrorDetails().toString());
    }
    
}
   
