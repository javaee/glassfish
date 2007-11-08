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
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the implementation for running runtime deployment descriptor
 * translation through an Ant task.
 * This task runs the following two classses -
 * Invokes IASMTMain by passing the appropriate arguments,
 * Invokes the org.apache.xalan.xslt.Process class for transformation.
 * <p>
 * The attributes of this task are: <br>
 *    archiveName   ="REQUIRED"  <br>
 *    srcServer ="REQUIRED"  (Options are: sunone, weblogic5, weblogic6, weblogi61, websphere, tomcat)<br>
 *    failOnError="OPTIONAL" (default: true)   <br>
 * <p>
 *
 */
public class TranslateRuntimeDD extends AVKTasks {

    private File archiveName = null;
    private String srcServer = null;

    public void setArchiveName(File archiveName) {
        this.archiveName = archiveName;
    }

    public void setSrcServer(String srcServer) {
        this.srcServer = srcServer;
    }

    /**
     * This is called by the ant framework.
     * @throws BuildException
     */
    public void execute() throws BuildException {

        checkArgs();
        getInstallHomes();

// temporary until migration code has CLIP interface as default
            jvmArgs = "-hotspot -ms96m -mx512m -DENABLE_CLIP=true -Dasmt.home="+avk_home+" -DPACKAGE_PROPERTY_FILE="+avk_home+"/config/package.properties ";

        if(invokeIASMT() != 0 && failOnError)
            throw  new BuildException("Problem in executing java command");

        if(invokeGenReportTool() != 0 && failOnError)
            throw  new BuildException("Problem in executing java command");

// user needs to know what happened and what to do next.
        echo.setMessage("See \""+resultDir+File.separator+"translationSummary.html\" for results.");
        echo.execute();
	echo.setMessage("We attempted to create an ear file even if minor errors occurred. The application with translated deployment descriptors is in a new .ear file located in "+resultDir+File.separator+"Input_Archives"+File.separator+"asmtbuild ");
        echo.execute();
    }


    /**
     * Invokes the sun.iasmt.user.IASMTMain
     * @return integer denoting the return status of the command. 1 is failure
     */
    private int invokeIASMT() {
        Java java = (Java)project.createTask("java");
        java.setClassname("sun.iasmt.user.IASMTMain");
        setCommonVMSettings(java);
        java.setClasspath(constructPath());
        Environment.Variable sysp = new Environment.Variable();
        sysp.setKey("asmt.home");
        sysp.setValue(avk_home);

        createResultDir("translate");
        java.addSysproperty(sysp);
        java.createArg().setLine("-c -t "+resultDir+
                                 " -S "+srcServer+
                                 " -x "+resultDir+File.separator+"result.xml "+
                                 "-n -q -T sjs80PE "+ archiveName);

        int returnCode = java.executeJava();

        // when the migration code starts generating ear in case of 
	// failure, we will remove this method
        createEar();

        return returnCode;
    }

    /**
     * create the ear if the migration code failed to generate. The method first checks
     * whether the ear has been created by checking the
     * "<avk_home>/reporttool/translate/Input_Archives/asmtbuild/<app.ear>" file. In
     * case the file does not exist then java command is invoked on the build_ear.xml file
     * present in this directory.
     */

    private void createEar() {
        String archivePath = resultDir + File.separator + "Input_Archives";
        File earFile = new File(archivePath + File.separator + "asmtbuild",
                                archiveName.getName());
        String buildFile = archivePath+File.separator+"build_ear.xml";
        if(!earFile.exists() && new File(buildFile).exists()) {
            echo.setMessage("Some xml files may not have translated correctly."); 
            echo.execute();
            Java java = (Java)project.createTask("java");

            java.setClassname("org.apache.tools.ant.Main");
            setCommonVMSettings(java);
            java.setClasspath(getPathForCreateEar());
            addEnvVariablesForCreateEar(java);
            java.createArg().setLine("-buildfile "+buildFile+" -quiet");
            int returnStatus = java.executeJava();
            if(returnStatus==1) {
                echo.setMessage("Some errors occurred while building the ear. You can find " +
                                "the generated code at ["+archivePath+"]");
                echo.execute();
            }else
            echo.setMessage( "The EAR file may not deploy successfully in "+
			    "the Application Server. Please check the " +
                            "runtime descriptors before deploying.");

        }
    }

    // setting the classpath required for running the java command for createEar
    private Path getPathForCreateEar() {
	String j2ee_lib = j2ee_home + File.separator + "lib";
        StringBuffer classPathBuffer = new StringBuffer();
        String[] CLASSPATH_ELEMENTS = { "lib"+File.separator+"asmt-rt.jar",
					"lib"+File.separator+"ant.jar"};
        classPathBuffer.append(java_home+"lib" + File.separator+
                               "tools.jar"+File.pathSeparator);
        classPathBuffer.append(j2ee_lib + File.separator+ 
			       "javaee.jar"+File.pathSeparator);
//        classPathBuffer.append(j2ee_lib+ File.separator + "ant" +
//			   	"lib"+ File.separator +
//		 		"ant.jar"+File.pathSeparator);
        for(int i=0;i<CLASSPATH_ELEMENTS.length;i++) {
            classPathBuffer.append((new File(avk_home,CLASSPATH_ELEMENTS[i])).getPath());
            classPathBuffer.append(File.pathSeparator);
        }
        return new Path(getProject(),classPathBuffer.toString());
    }

    // setting the Environment Variables required for running the java command for createEar
    private void addEnvVariablesForCreateEar(Java java) {
        Environment.Variable asmt = new Environment.Variable();
        asmt.setKey("asmt.home");
        asmt.setValue(avk_home);
        java.addSysproperty(asmt);

        Environment.Variable vml = new Environment.Variable();
        vml.setKey("vml");
        vml.setValue("true");
        java.addSysproperty(vml);

        Environment.Variable javahome = new Environment.Variable();
        javahome.setKey("javahome");
        javahome.setValue(java_home);
        java.addSysproperty(javahome);

        Environment.Variable iashome = new Environment.Variable();
        iashome.setKey("ias.home");
        iashome.setValue(j2ee_home);
        java.addSysproperty(iashome);
    }

    /**
     * Invokes the com.sun.enterprise.appverification.tools.GenReportTool
     * @return integer denoting the return status of the command. 1 is failure
     */
    private int invokeGenReportTool() {
        Java java = createJavaTask();
        List<String> list = new ArrayList<String>();

        list.add(resultDir + File.separator + "result.xml");
        list.add(resultDir);
        list.add("translationSummary");
        String[] args = (String[])list.toArray(new String[1]);

        java.setClassname("com.sun.enterprise.appverification.tools.GenReportTool");
        setArgs(java, args);

        return java.executeJava();
    }


    private Path constructPath() {
	String j2ee_lib = j2ee_home + File.separator + "lib";
        StringBuffer classPathBuffer = new StringBuffer();


        String[] CLASSPATH_ELEMENTS = {"lib"+File.separator+"xalan.jar",
                                       "lib"+File.separator +"ant.jar",
                                       "lib"+File.separator+"dom4j.jar",
                                       "lib"+File.separator+"namespace.jar",
                                       "lib"+File.separator+"asmt.jar",
                                       "lib"+File.separator+"relaxngDatatype.jar",
                                       "lib"+File.separator+"jaxb-api.jar",
                                       "lib"+File.separator+"jaxb-impl.jar",
                                       "lib"+File.separator+"jaxb-libs.jar",
                                       "lib"+File.separator+"jax-qname.jar",
                                       "lib"+File.separator+"jhall.jar",
                                       "lib"+File.separator+"asmt_en.jar"};
        classPathBuffer.append(java_home+"lib" +File.separator+
				"tools.jar"+File.pathSeparator);

        for(int i=0;i<CLASSPATH_ELEMENTS.length;i++) {
            classPathBuffer.append((new File(avk_home,CLASSPATH_ELEMENTS[i])).getPath());
            classPathBuffer.append(File.pathSeparator);
        }
        classPathBuffer.append(j2ee_lib+File.separator+
				"j2ee.jar"+File.pathSeparator);
        return new Path(getProject(),classPathBuffer.toString());
    }

    /**
     * Parse the source server argument. Throw exception if invalid
     * option is found.
     * @throws BuildException
     */
    private void checkArgs() throws BuildException {
        if(archiveName == null || !archiveName.exists())
            throw new BuildException("Provide a valid fully qualified " +
                                     "path of the archive to be translated: archiveName = "+archiveName);

        if( !archiveName.isFile())
            throw new BuildException("Provide a valid fullly qualified " +
                                     "archive be translated: archiveName = "+archiveName);

        if(srcServer == null)
            throw new BuildException("Must specify which app server was used to create the application");
        else if(srcServer.equalsIgnoreCase("sunone65"))
            srcServer = "as65";
        else if(srcServer.equalsIgnoreCase("sjsappserver7"))
            srcServer = "as70";
        else if(srcServer.equalsIgnoreCase("weblogic5"))
            srcServer = "wl51";
        else if(srcServer.equalsIgnoreCase("weblogic6"))
            srcServer = "wl60";
        else if(srcServer.equalsIgnoreCase("weblogic61"))
            srcServer = "wl61";
        else if(srcServer.equalsIgnoreCase("weblogic81"))
            srcServer = "wl81";
        else if(srcServer.equalsIgnoreCase("websphere4"))
            srcServer = "ws40";
        else if(srcServer.equalsIgnoreCase("websphere51"))
            srcServer = "ws50";
        else if(srcServer.equalsIgnoreCase("tomcat"))
            srcServer = "tc41";
        else if(srcServer.equalsIgnoreCase("J2EESDK13"))
            srcServer = "ri13";
        else if(srcServer.equalsIgnoreCase("J2EESDK14"))
            srcServer = "ri14";
        else if(srcServer.equalsIgnoreCase("JBoss3"))
            srcServer = "jb30";
        else
            throw new BuildException("srcServer = "+srcServer+" is not one of " +
                                     "(sunone65, sjsappserver7, weblogic5, weblogic6, weblogic61, weblogic81, websphere4, websphere51, j2eesdk13, j2eesdk14, jboss3 and tomcat)");

    }
}
