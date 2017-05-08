/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * Very simple example implementation of a Java-based test driver for
 * deployment tests.
 *
 * This implementation relies on the ant-based tests and common files and
 * delegates to targets in those ant scripts.  Over time this delegation
 * could be replaced by Java implementations of the equivalent logic.
 *
 * @author: tjquinn
 */
public abstract class DeploymentTest {
    
    /** indicates if the test class has been initialized yet */
    private boolean isInited = false;
    
    /** ant project based on the build.xml in the current directory */
    protected Project project;
    
    /** the build.xml file in the current directory */
    protected File buildFile;
    
    /** Our current message output status. Follows Project.MSG_XXX. */
    private int msgOutputLevel = Project.MSG_INFO;

    /**Saved values of the streams */
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    
    /**
     * The Ant logger class. There may be only one logger. It will have
     * the right to use the 'out' PrintStream. The class must implements the
     * BuildLogger interface.
     */
    private String loggerClassname = null;

    /** Creates a new instance of DeploymentTest */
    public DeploymentTest() {
        init();
    }
    
    /**
     *Reports if the class has been initialized or not.
     *@return whether the initialized has occurred or not
     */
    protected boolean isInited() {
        return isInited;
    }
    
    /**
     *Prepare the callable ant environment for the test.
     */
    private void init() {
        if (! isInited()) {

            err = System.err;
            out = System.out;
            in = System.in;
            
            /*
             *Use the build.xml in the current directory.
             */
            buildFile = new File("build.xml");
            
            /*
             *To call into ant, create a new Project and prepare it for use.
             */
            project = new Project();
//            msgOutputLevel = Project.MSG_VERBOSE;
            project.addBuildListener(createLogger());
            project.setBasedir(".");
            
            project.init();
            
            /*
             *Set up the project to use the build.xml in the current directory.
             */
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(project, buildFile);
            
            isInited = true;
        }
    }
    
    /**
     *Assembles the required jar, war, ear files, etc.
     */
    protected void assemble() throws BuildException {
        project.executeTarget("assemble");
    }

    /**
     *Deploys the application using asadmin.
     */
    protected void deploy() {
        project.executeTarget("deploy.asadmin");
    }
    
    /**
     *Redeploys the application using asadmin.
     *Removes app refs first, then deploys, then creates app refs.
     */
    protected void redeploy() {
        project.executeTarget("redeploy.asadmin");
    }

    /**
     *Undeploys the app using asadmin.
     */
    protected void undeploy() {
        project.executeTarget("undeploy.asadmin");
    }

    /**
     *Cleans out the build directory.
     */
    protected void clean() {
        project.executeTarget("clean");
    }    

    /**
     *Constructs an ExecTask instance, linked to the current project, for
     *running the client.
     *This implementation prepares the exec task so it will run the appclient
     *script.
     *@return ExecTask initialized for running the appclient script to execute
     *the generated and retrieved app client
     */
    protected ExecTask prepareRunExecTask() {
        /*
         This is the ant excerpt imitated by the exec task built by this method:
         
         <exec executable="${APPCLIENT}" resultproperty="result" failonerror="false" output="${build}/${log.id}.output.log">
                    <arg line="-client ${archivedir}/${testName}Client.jar"/>
                </exec>
        */
        ExecTask exec = new ExecTask();
        exec.setProject(project);
        String appclientValue = project.getProperty("APPCLIENT");
        exec.setExecutable(appclientValue);
        exec.setFailonerror(false);
        exec.setTaskName("runclient");
        return exec;
    }
    
    /**
     *Prepares a command line argument object for use with the appclient
     *exec task that uses the default set of arguments: the default
     *location and name for the retreived app client jar file.
     *@param exec the ExecTask with which the command line should be associated
     *@return the argument, associated with the exec task, and set to invoke the gen'd app client
     */
    protected Commandline.Argument prepareRunCommandlineArg(ExecTask exec) {
        Commandline.Argument arg = exec.createArg();
        String archiveDir = project.getProperty("archivedir");
        String testName = project.getProperty("testName");
        arg.setLine("-client " + archiveDir + "/" + testName + "Client.jar");
        return arg;
    }
    
    /**
     *Default implementation for executing the app client
     */
    protected void runClient() {
        ExecTask exec = prepareRunExecTask();
        prepareRunCommandlineArg(exec);
        exec.execute();
    }
    
    /**
     *Prepares the test environment by cleaning the build directory
     *and assembling the required jar files.
     */
    @Configuration(beforeTestClass=true)
    public void setup() {
        clean();
        assemble();
    }
    
    /**
     *Cleans up after all tests have run.  In this case, undeploys
     *the application.
     */
    @Configuration(afterTestClass=true)
    public void unsetup() {
        undeploy();
    }

    // The following is inspired by the code in ant itself.
    
    /**
     * Creates the default build logger for sending build events to the ant
     * log.
     *
     * @return the logger instance for this build.
     */
    private BuildLogger createLogger() {
        BuildLogger logger = null;
        if (loggerClassname != null) {
            try {
                Class loggerClass = Class.forName(loggerClassname);
                logger = (BuildLogger) (loggerClass.newInstance());
            } catch (ClassCastException e) {
                System.err.println("The specified logger class "
                    + loggerClassname
                    + " does not implement the BuildLogger interface");
                throw new RuntimeException();
            } catch (Exception e) {
                System.err.println("Unable to instantiate specified logger "
                    + "class " + loggerClassname + " : "
                    + e.getClass().getName());
                throw new RuntimeException();
            }
        } else {
            logger = new DefaultLogger();
        }

        logger.setMessageOutputLevel(msgOutputLevel);
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);

        return logger;
    }
}
