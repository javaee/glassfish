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
package org.apache.tools.ant.taskdefs.optional.sun.verification;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.ExecTask;

import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation for verifier invocation through Ant task.
 * This task runs the following two classses -
 * Invokes Verifier by passing the appropriate arguments, 
 * Invokes the GenReportTool class to generate the reports.
 * <p>
 * The attributes of this task are: <br>
 *    appName   ="REQUIRED"  <br>
 *    partitionOpts ="OPTIONAL"  <br>
 *    jvmArgs   ="OPTIONAL" (default: "Xss512k -Xms128m -Xmx256m")   <br>
 *    failOnError="OPTIONAL" (default: true)   <br>
 * <p>
 * 
 * These tasks require javke-ant.jar and sunone-appserv-ant.jar to be in
 * AVK_HOME/lib and APPSERVER_HOME/lib diroctries respectively.
 * 
 * @author Vikas Awasthi.
 */
public class StaticArchiveTest extends AVKTasks {
    
    private File appName = null;
    private String partitionOpts = null;
    private String reportingOpts = null;

    public void setAppName(File appName) {
        this.appName = appName;
    }

    public void setPartitionOpts(String partitionOpts) {
        this.partitionOpts = partitionOpts;
    }

    public void setReportingOpts(String reportingOpts) {
	this.reportingOpts = reportingOpts;
    }
    // this api will not be documented. It is used by CTS.
    public void setResultDir(String resultDir) {
        super.setResultDir(resultDir);
    }

    /**
     * This is called by the ant framework. 
     * @throws BuildException
     */ 
    public void execute() throws BuildException {
        
        checkArgs();
        getInstallHomes();
        invokeVerifier();
            
        if(invokeGenReportTool(createJavaTask()) != 0 && failOnError)
            throw new BuildException("Problem in executing GenReportTool command");
        
        echo.setMessage("See \""+resultDir+File.separator+"verifierSummary.html\" for results.");
        echo.execute();
    }
    
    /**
     * Invokes the com.sun.enterprise.tools.verifier.Verifier 
     */ 
    private void invokeVerifier() {
        ExecTask exec = (ExecTask)project.createTask("exec");
        String argument = "";
        //adding the partitioning options
        if(partitionOpts != null) 
            argument = addPartitionOptions();
        //adding the fail/warning reporting option
        if ((reportingOpts == null) || (reportingOpts.equals(""))) 
            argument += " -rw";
        else {// check for valid options a, w and f
            if ( reportingOpts.equals("f") || reportingOpts.equals("w")
                || reportingOpts.equals("a") )
                argument += " -r"+reportingOpts;
            else
                throw new BuildException("Provide a valid reporting option. " +
                                             "Valid options are [f, w, a] ");
        }
        createResultDir("static");
        //adding the result directory option
        argument += " -d "+resultDir;
        //do not run the runtime tests
        argument += " -R";
        // finally adding the application name in the options
        argument += " "+appName.getAbsolutePath();
        
        Commandline.Argument arg = exec.createArg();
        arg.setLine(argument);
        exec.setExecutable(j2ee_home+"/bin/verifier");
        exec.setDir(new File(j2ee_home,"bin"));
        exec.setVMLauncher(false);
        //the exit status is non-zero if any assertion failed but
        // we want to continue to invokeGenReportTool
        exec.setFailonerror(false);
        exec.execute();
    }
    
    /**
     * Invokes the com.sun.enterprise.appverification.tools.GenReportTool
     * @return integer denoting the return status of the command. 1 is failure
     */ 
    private int invokeGenReportTool(Java java) {
        List<String> list = new ArrayList<String>();
        
        list.add(resultDir + File.separator + appName.getName() + ".xml");
        list.add(resultDir);
        String[] xsls = {"verifierSummary", "appFailureDetail", "appPassedDetail", 
                         "appWarningDetail", "appNADetail", "ejbFailureDetail",
                         "ejbPassedDetail", "ejbWarningDetail", "ejbNADetail",
                         "acFailureDetail", "acPassedDetail", "acWarningDetail",
                         "acNADetail", "conFailureDetail", "conPassedDetail",
                         "conWarningDetail", "conNADetail", "errFailureDetail",
                         "webFailureDetail", "webPassedDetail", "webWarningDetail",
                         "webNADetail"};
        for (int i = 0; i < xsls.length; i++) 
            list.add(xsls[i]);
        String[] args = (String[])list.toArray(new String[1]);

        // clear the args set in the invokeVerifier call.
        java.clearArgs();
        java.setClassname("com.sun.enterprise.appverification.tools.GenReportTool");
        setArgs(java, args);

        return java.executeJava();
    }

    /**
     * Parse and add the partitioning options. It add "--" to the options.
     * @return List
     */ 
    private String addPartitionOptions() {
        StringTokenizer options = new StringTokenizer(partitionOpts, ",");
        String args = "";
        while(options.hasMoreTokens()) {
            String option = options.nextToken().trim();
            if(!(option.equals("ejb") || 
                option.equals("web") ||
                option.equals("webservices") ||
                option.equals("connector") ||
                option.equals("app") ||
                option.equals("appclient") ||
                option.equals("webservicesclient")))
                throw new BuildException("Provide a valid parititioning option. " +
                                         "Valid options are [ejb, web, app, webservices, " +
                                         "webservicesclient, connector, appclient]");
            args += " --"+option;
        }
        return args;
    }

    private void checkArgs() throws BuildException {
        if(appName == null || !appName.exists())
            throw new BuildException("Provide a valid fully qualified " +
                                     "path of the application: appName="+appName);
    }
}
