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
 * ServerXmlVerifier.java
 *
*/

package com.sun.enterprise.admin.verifier;

import java.util.*;
import java.io.*;

import java.io.File;

import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.tools.verifier.StringManagerHelper;
//import com.sun.enterprise.tools.verifier.Result;
//import com.sun.enterprise.tools.verifier.ResultMgr;
//import com.sun.enterprise.tools.verifier.ResultsReport;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;


/*  ServerXML Verifier is the main program which is called from command line to verify all attributes in server.xml
*/

public class ServerXMLVerifier {
    
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public boolean debug = false;
    public String outputFileStr = null;
    public final static int FAIL = 0;
    public final static int WARN = 1;
    public final static int ALL  = 2;
    
    private static int reportLevel = ALL;
    
    //public ResultMgr resultMgr = null;
    //private ResultsReport resultReport = null;
    
    public String xmlFileName;
    public ConfigContext configContext = null;
    
    public com.sun.enterprise.util.LocalStringManagerImpl smh;
    public static ServerMgr serverMgr;
    
    public ServerXMLVerifier() {
        try {
            //resultMgr = new ResultMgr();
            //resultReport = new ResultsReport();
            StringManagerHelper.setLocalStringsManager(Class.forName(getClass().getName()));
            smh = StringManagerHelper.getLocalStringsManager();
        }
        catch (ClassNotFoundException e) {
            // Logging
            _logger.log(Level.FINE, "serverxmlverifier.error_getting_localstringsmanager", e);
        }
        init();
    }
    
    /*public boolean verify(String name, Object value, ConfigContext context) {
        boolean retValue = false;
        configContext = context;
        //System.out.println("Name : " + name);
        //System.out.println("Value : " + value.getClass().getName());
        if(serverMgr==null)
            serverMgr = new ServerMgr(true);
        retValue = serverMgr.check(name,value,configContext);
        return retValue;
    }*/
    
    /*public boolean verify() {
        boolean retValue = false;
        try {
            configContext = ConfigFactory.createConfigContext(xmlFileName);
            configContext.refresh();
            if(serverMgr==null)
                serverMgr = new ServerMgr(true);
            serverMgr.check(configContext);
            retValue = true;
        }
        catch(ConfigException cex){
            // Logging
            _logger.log(Level.FINE, "serverxmlverifier.error_creating_object", cex);
            retValue = false;
        }
        return retValue;
    }
    
    // <addition> srini@sun.com 
    
    // Function added to check whether all tests has been passed
    // return true when all tests passed
    // return false when one test failed
    // throws ConfigException if give xml file is not exist or invalid
    
    public boolean isPassed(String xmlFileName) throws ConfigException {
        boolean retValue = false;
        configContext = ConfigFactory.createConfigContext(xmlFileName);
        configContext.refresh();
        if(serverMgr == null)
                serverMgr = new ServerMgr(debug);
        retValue = serverMgr.testStatus(configContext);
        return retValue;
    }*/
    
    // </addition>

    
    public void init() {
        File testNames = new File("lib/TestNamesMBean.xml");
        if(testNames.exists())
            // Logging
            _logger.log(Level.INFO, "serverxmlverifier.looking_file", testNames.getAbsolutePath());
    }
    
    /*
     * Standalone verification of Server.xml
     */
    
    public static void main(String args[]) {
        ServerXMLVerifier serverVerifier = new ServerXMLVerifier();
        serverVerifier.parseArgs(args);
    }
    
    protected void parseArgs(String args[]) {
        if (args.length < 1)
            usage();
        else {
            //default report level is set to failures and warnings
            for (int i = 0; i < args.length;  i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    try {
                        switch (arg.charAt(1)) {
                            case 'v':  //verbose
                                debug = true;
                                debug(smh.getLocalString(getClass().getName() + ".verboseFlag",
                                "Setting verbose flag to TRUE."));
                                break;
                            case 'h': // Help Message
                                debug
                                (smh.getLocalString(getClass().getName() + ".helpMessage",
                                "Displaying help message."));
                                usage();
                                break;
                            case 'o': // output file name
                                debug
                                (smh.getLocalString(getClass().getName() + ".outputFileName",
                                "Retrieving results output filename."));
                                char vhstr[] = new char[arg.length()-2];
                                arg.getChars(2, arg.length(), vhstr, 0);
                                outputFileStr = new String(vhstr);
                                debug("Using this output file = " + outputFileStr);
                                ServerMgr.setFile(outputFileStr);
                                //resultReport.setUserSpecifiedOutputFile(true);
                                break;
                            default:
                                usage();
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        debug(e);
                        usage();
                    }
                }
                else
                {
                    if (xmlFileName == null){
                        xmlFileName = (new File(arg)).getAbsolutePath();
                        debug(smh.getLocalString(getClass().getName() + ".xmlFileName",
                        "XML filename: {0}",
                        new Object[]
                        {xmlFileName}));
                    } else {
                        debug(smh.getLocalString(getClass().getName() + "invalidArg",
                        "invalid argument \"{0}\"",
                        new Object[]
                        {arg}));
                        usage();
                        return;
                    }
                }
            }
            if(xmlFileName == null ){
                usage();
                return;
            }
            //verify();
        }
    }
    
    /** Display usage message to user upon encountering invalid option
     *
     */
    public void usage() {
        System.err.println
        ("\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine1",
        "usage: server-verifier [optional_params] <server-xml-filename>"))
        +   "\n\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine2",
        "where :"))
        +   "\n\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine3",
        "  [optional_params]: Must be: "))
        +   "\n\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine4",
        "     -v : verbose debug turned on "))
        +   "\n\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine5",
        "    -o<output file> : test results written to this file (.xml file preferred)"))
        +   "\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine6",
        "                      (Overrides default file - Results.xml)"))
        +   "\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine6a",
        "                      which is created in system defined <tmp> directory"))
        +   "\n" +
        (smh.getLocalString(getClass().getName() + ".usageLine17",
        "<server-xml-filename>: Jar file to perform static verification on "))
        +   "\n\n" );
    }
    
    
    public void debug(String s) {
        if(debug)
            // Logging
            _logger.log(Level.INFO, s);
    }
    
    public void debug(Exception e) {
        if(debug)
            // Logging
            _logger.log(Level.INFO, "serverxmlverifier.error_check", e);
    }
}
