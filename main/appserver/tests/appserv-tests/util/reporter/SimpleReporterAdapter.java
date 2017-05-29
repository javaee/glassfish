/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.ccl.reporter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.io.*;

//public class SimpleReporterAdapter extends Thread implements Serializable {
public class SimpleReporterAdapter implements Serializable {
    private boolean debug=true;
    private String testSuiteDescription;
    private HashMap testCaseStatus;
    public static String PASS = "pass";
    public static String DID_NOT_RUN = "did_not_run";
    public static String FAIL = "fail";
    private String testSuiteName = "undefined";
    private String testSuiteID = null;
    private String ws_home="sqe-pe";
    private String outputDir=null;
    private Reporter reporter=null;
    private String resultFile="test_results";
    private boolean isFileSet=false;

    
    public SimpleReporterAdapter()  {
        testSuiteName = "undefined";
        testSuiteID = null;
        ws_home = "appserv-tests";
        outputDir = null;
        reporter = null;
        try {
            outputDir = null;
        }
        catch(Exception ex) { }
    }
   
    
    public SimpleReporterAdapter(String ws_root)     {
        testSuiteName = "undefined";
        testSuiteID = null;
        ws_home = "appserv-tests";
        outputDir = null;
        reporter = null;
        try {
            outputDir = null;
            ws_home = ws_root;
        }
        catch(Exception ex) { }
    }
    
    //used by webrunner
    public SimpleReporterAdapter(String resultFilePath,boolean isResultFileSet){
        try{
            this.isFileSet=true;
            this.resultFile=resultFilePath;
        }catch( Exception ex ){ }
    }
    
    

    public synchronized void addStatus(String s, String status)    {
        if( testSuiteName.compareTo("undefined") == 0 ) {
            int blankIndex = s.indexOf(" ");

            if(blankIndex!=-1){
                testSuiteName = s.substring(0, blankIndex);
            } else {
                testSuiteName = s;
            }
            testSuiteID = s + "ID";
        }
        if( testCaseStatus == null ){
            testCaseStatus = new HashMap(5);
        }

        int blankIndex = s.indexOf(" ");        
        String key = s;
        if (blankIndex!=-1){
            key = s.substring(s.indexOf( " " ));
        } 
        if(debug)
            System.out.println("Value of key is:"+key);

        if( !testCaseStatus.containsKey( key ) ){
            testCaseStatus.put( key , status.toLowerCase() );
        }
    }

    public void addDescription(String s)    {
        testSuiteDescription = s;
    }

    public void printStatus() {
        try{
            //Reporter reporter;
            Set keySets;
            Iterator keySetsIT;
            Properties p;

            if(testSuiteName.compareTo("undefined") == 0)
                testSuiteName = getTestSuiteName();
            String rootpath = (new File(".")).getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(ws_home));
            outputDir = ejte_home + ws_home;
            reporter = Reporter.getInstance(ws_home);
            if(debug)
                System.out.println("Generating report at \t" + outputDir + 
                        File.separatorChar + "test_results.xml");
            reporter.setTestSuite(testSuiteID, testSuiteName, testSuiteDescription);
            reporter.addTest(testSuiteID, testSuiteID, testSuiteName);
            keySets = testCaseStatus.keySet();
            keySetsIT = keySets.iterator();
            String tcName;
            
            int pass= 0;
            int fail = 0;
            int d_n_r = 0;
            String status;
            System.out.println("\n\n-----------------------------------------");
            while( keySetsIT.hasNext() ){
                tcName = keySetsIT.next().toString();
                status =  testCaseStatus.get( tcName ).toString();
                
                if (status.equalsIgnoreCase(PASS)){
                    pass++;
                } else if(status.equalsIgnoreCase(DID_NOT_RUN)){
                    d_n_r++;
                } else {
                    fail++;
                }
                
                System.out.println("-\t " + tcName + ": " + status.toUpperCase() + "\t-");
                reporter.addTestCase( testSuiteID, testSuiteID, tcName + "ID", tcName );
                reporter.setTestCaseStatus( testSuiteID, testSuiteID, tcName + "ID", status );
            }
            System.out.println("-----------------------------------------");
            System.out.println("Total PASS:\t" + pass);
            System.out.println("Total FAIL:\t" + fail);
            System.out.println("Total DID NOT RUN: " + d_n_r);
            System.out.println("-----------------------------------------");
            //reporter.flush(testSuiteID);
            reporter.generateValidReport();
            createConfirmationFile();
        }
        catch( Throwable ex )        {
            System.out.println( "Reporter exception occurred!" );
            if(debug)
                ex.printStackTrace();
        }
    }

    public void createConfirmationFile(){
        try{
            FileOutputStream fout = new FileOutputStream("RepRunConf.txt");
            String text = "Test has been reported";
            fout.write(text.getBytes());
            fout.close();
        } catch (Exception e){
            System.out.println("Exception while creating confirmation file!");
            if(debug)
                e.printStackTrace();
        }
    }


    public void printSummary(String s) {
        printStatus();
    }

    public void printSummary() {
        printStatus();
    }

    public void run() {
        printSummary();
        reporter.generateValidReport();
    }

    private String getTestSuiteName()    {
        String s = new File( "" ).getAbsolutePath();
        return s.substring(s.lastIndexOf(File.separator)+1, s.length());
    }

    public void clearStatus()    {
        testCaseStatus.clear();
        testSuiteName = "undefined";
    }
    
    /**
     *A dead function.
     * J2EE server security contraints don't allow use of exec
     */
    
    public Properties getEnvVars() throws Throwable {
        Process p = null;
        Properties envVars = new Properties();
        Runtime r = Runtime.getRuntime();
        String OS = System.getProperty("os.name").toLowerCase();
        // System.out.println(OS);
        if (OS.indexOf("windows 9") > -1) {
            p = r.exec( "command.com /c set" );
        }
        else if ( (OS.indexOf("nt") > -1) || (OS.indexOf("windows 2000") > -1) ) {
            p = r.exec( "cmd.exe /c set" );
        }
        else {  
            p = r.exec( "env" );
        }
        BufferedReader br = 
            new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while( (line = br.readLine()) != null ) {
            int idx = line.indexOf( '=' );
            String key = line.substring( 0, idx );
            String value = line.substring( idx+1 );
            envVars.setProperty( key, value );
            if(debug)
             System.out.println( key + " = " + value );
        }
        return envVars;
    }
}
