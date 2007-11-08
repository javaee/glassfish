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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
/*
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;
import com.sun.enterprise.util.FileUtil;
*/

/**
 * This is the base class of all the custom ant tasks. It has  
 * the common code required by the tasks.
 * 
 * @author Vikas Awasthi.
 */
public class AVKTasks extends Task {

    protected String jvmArgs = null;
    protected boolean failOnError = true;
    protected String j2ee_home = null;
    protected String avk_home = null;
    protected String java_home = null;
    protected String resultDir = null;
    protected Echo echo = null;

    public AVKTasks() {
        java_home = System.getProperty("java.home")+"/../";//"java.home returns jdk/jre, hence ....
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
    
    protected void setResultDir(String resultDir) {
        this.resultDir = resultDir.trim();
    }

    private void createEchoTask () {
        echo = (Echo)getProject().createTask("echo");
    }

    /**
     * Create a java task with classpath and JVM arguments set
     * @return Java task
     */
    protected Java createJavaTask() {
        Java java = (Java)project.createTask("java");
        java.setClasspath(constructJVMPath());
        if(jvmArgs == null)
            jvmArgs = "-Xss512k -Xms128m -Xmx256m";
        setCommonVMSettings(java);
        setSystemProperties(java);
        return java;
    }

    protected void setSystemProperties(Java java) {
        Environment.Variable j2ee_app_home = new Environment.Variable();
        j2ee_app_home.setKey("j2ee.appverification.home");
        j2ee_app_home.setValue(avk_home);
        java.addSysproperty(j2ee_app_home);

        Environment.Variable java_ext_dir = new Environment.Variable();
        java_ext_dir.setKey("java.ext.dirs");
        java_ext_dir.setValue(System.getProperty("java.ext.dirs") +
                              File.pathSeparator + j2ee_home +
                              File.separator + "domains" +
                              File.separator + "domain1" +
                              File.separator + "lib" +
                              File.separator + "ext");
        java.addSysproperty(java_ext_dir);

        Environment.Variable installRoot = new Environment.Variable();
        installRoot.setKey("com.sun.aas.installRoot");
        installRoot.setValue(j2ee_home);
        java.addSysproperty(installRoot);

	// following 2 are required for the migration code
        Environment.Variable j2eeHome = new Environment.Variable();
        j2eeHome.setKey("j2ee.home");
        j2eeHome.setValue(j2ee_home);
        java.addSysproperty(j2eeHome);

        Environment.Variable javaHome = new Environment.Variable();
        javaHome.setKey("as.java");
        javaHome.setValue(java_home);
        java.addSysproperty(javaHome);

        Environment.Variable endorsedDir = new Environment.Variable();
        endorsedDir.setKey("java.endorsed.dirs");
        endorsedDir.setValue(j2ee_home+File.separator + "lib" + File.separator + "endorsed");
        java.addSysproperty(endorsedDir);

        Environment.Variable xsl = new Environment.Variable();
        xsl.setKey("com.sun.aas.verifier.xsl");
        xsl.setValue(avk_home + File.separator + "xsl");
        java.addSysproperty(xsl);
    }

    protected Path constructJVMPath() {
        StringBuffer classPathBuffer = new StringBuffer();
        classPathBuffer.append(avk_home+"/lib/javke.jar:");
        classPathBuffer.append(j2ee_home+"/lib/appserv-rt.jar:");
        classPathBuffer.append(j2ee_home+"/lib/javaee.jar:");
        classPathBuffer.append(j2ee_home+"/lib/appserv-ext.jar:");
        classPathBuffer.append(j2ee_home+"/lib/appserv-cmp.jar:");
        classPathBuffer.append(j2ee_home+"/lib/appserv-admin.jar:");
        classPathBuffer.append(j2ee_home+"/lib/install/applications/jmsra/imqjmsra.jar:");
        classPathBuffer.append(j2ee_home+"/lib/jhall.jar:");

//        classPathBuffer.append(Path.systemClasspath);
        return new Path(getProject(),classPathBuffer.toString());
    }

    protected void getInstallHomes() throws BuildException {
//      create the echo task that displays the messages        
        createEchoTask();        

        if(avk_home == null)
            try {
                avk_home = new File(getProject().getProperty("avk.home")).getCanonicalPath();
            } catch (IOException e) {//we donot expect this exception. Continue with absolutePath anyway
                avk_home = new File(getProject().getProperty("avk.home")).getAbsolutePath();
            }

        try {
            File avkENV = new File(avk_home+File.separatorChar+"config", "avkenv.conf");
            Properties props = new Properties();
            props.load(new FileInputStream(avkENV));
            if(j2ee_home == null)
                try {
                    j2ee_home = new File(props.getProperty("J2EE_HOME")).getCanonicalPath();
                } catch (IOException e) {//we donot expect this exception. Continue with absolutePath anyway
                    j2ee_home = new File(props.getProperty("J2EE_HOME")).getAbsolutePath();
                }
        } catch (IOException e) {
            echo.setMessage("Problem in getting J2EE_HOME. Please ensure that avkenv.conf " +
                    "is kept in the config directory. "+e.getMessage());
            echo.execute();
        }
    }
    
    protected void setCommonVMSettings(Java java) {
        java.setFork(true);
        java.setFailonerror(failOnError);
//        java.setDir(new File(avk_home, "bin"));
        java.createJvmarg().setLine(jvmArgs);
    }
    
    /**
     * creates the directory dirName ( static, scan or translate) in the 
     * "avk_home/reporttool" directory.
     * @param dirName
     */ 
    protected void createResultDir(String dirName) {
        if(resultDir == null) 
            resultDir = avk_home+File.separator + "reporttool";
        resultDir = resultDir + File.separator +dirName;
        File file = new File(resultDir);
        try {
            cleanDir(resultDir);
            if ( file.getParent() !=null) {
                (new File (file.getParent())).mkdirs();
                file.mkdir();
            }
        }catch( Exception e) {
            throw new BuildException("Could not create output directory "+resultDir);
        }
    }

    protected void setArgs(Java java, String[] args) {
        for (int i = 0; i < args.length; i++) {
            java.createArg().setLine(args[i] + " ");
        }
    }

    protected void cleanDir(String resultDir) {
        Delete delete = (Delete)getProject().createTask("delete");
        delete.setDir(new File(resultDir));
        delete.setQuiet(true);
        delete.setIncludeEmptyDirs(true);
        delete.execute();
    }

}
