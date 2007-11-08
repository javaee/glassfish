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
 * This is the implementation for running source scan through Ant task.
 * This task runs the following two classses -
 * Invokes IASMTMain by passing the appropriate arguments, 
 * Invokes the org.apache.xalan.xslt.Process class for transformation.
 * <p>
 * The attributes of this task are: <br>
 *    srcDir   ="REQUIRED"  <br>
 *    srcServer ="REQUIRED"  (Options are: sunone, weblogic5, weblogic6, websphere, jboss)<br>
 *    jvmArgs   ="OPTIONAL" (default: "-hotspot -ms96m -mx512m")   <br>
 *    failOnError="OPTIONAL" (default: true)   <br>
 * <p>
 * 
 * @author Vikas Awasthi.
 */
public class SourceScan extends AVKTasks {
    
    private File srcDir = null;
    private String srcServer = null;

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
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
        if(jvmArgs == null)
            jvmArgs = "-hotspot -ms96m -mx512m -Dasmt.home="+avk_home+" -DPACKAGE_PROPERTY_FILE="+avk_home+"/config/package.properties -DENABLE_CLIP=true";

        if(invokeIASMT() !=0 && failOnError)
            throw  new BuildException("Problem in scanning source code");

        if(invokeGenReportTool() !=0 && failOnError)
            throw  new BuildException("Problem in generating reports");

        echo.setMessage("See \""+resultDir+File.separator+"codeSummary.html\" for results.");
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
        createResultDir("scan");

        java.addSysproperty(sysp);
        java.createArg().setLine("-c -t "+resultDir+
                                 " -S "+srcServer+
                                 " -s "+srcDir+
                                 " -T sjs80PE -n -q"+
                                 " -x "+resultDir+File.separator+"result.xml");

        return java.executeJava();
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
        list.add("codeSummary");
        String[] args = (String[])list.toArray(new String[1]);

        java.setClassname("com.sun.enterprise.appverification.tools.GenReportTool");
        setArgs(java, args);

        return java.executeJava();
    }

    private Path constructPath() {
	String j2ee_lib=j2ee_home + File.separator+"lib";
        StringBuffer classPathBuffer = new StringBuffer();
        String[] CLASSPATH_ELEMENTS = {
                                       "lib"+File.separator +"ant.jar",
                                       "lib"+File.separator +"xalan.jar",
				       "lib"+File.separator +"jhall.jar",
                                       "lib"+File.separator +"dom4j.jar",
                                       "lib"+File.separator +"asmt.jar",
                                       "lib"+File.separator +"jaxb-api.jar", 
                                       "lib"+File.separator +"jaxb-impl.jar",
                                       "lib"+File.separator +"jaxb-libs.jar",
				       "lib"+File.separator +"namespace.jar",
				       "lib"+File.separator +"asmt_en.jar",
				       "lib"+File.separator +"jax-qname.jar",
				       "lib"+File.separator +"relaxngDatatype.jar"};
        classPathBuffer.append(java_home+"lib"+File.separator +
				"tools.jar"+File.pathSeparator);
        for(int i=0;i<CLASSPATH_ELEMENTS.length;i++) {
            classPathBuffer.append((new File(avk_home,CLASSPATH_ELEMENTS[i])).getPath());
            classPathBuffer.append(File.pathSeparator);
        }
        classPathBuffer.append(j2ee_lib+File.separator+
				"javaee.jar"+File.pathSeparator);
        return new Path(getProject(),classPathBuffer.toString());
    }

    /**
     * Parse the source server argument. Throw exception if invalid 
     * option is found.
     * @throws BuildException
     */ 
    private void checkArgs() throws BuildException {
        if(srcDir == null || !srcDir.exists())
            throw new BuildException("Provide a valid fullly qualified " +
                                     "path of the source directory to be scanned: srcDir = "+srcDir);

        if(srcServer == null)
            throw new BuildException("Must specify which app server was used to create the application");
        else if(srcServer.equalsIgnoreCase("sunone"))
            srcServer = "as70";
        else if(srcServer.equalsIgnoreCase("weblogic5"))
            srcServer = "wl51";
        else if(srcServer.equalsIgnoreCase("weblogic6"))
            srcServer = "wl60";
        else if(srcServer.equalsIgnoreCase("weblogic8"))
            srcServer = "wl81";
        else if(srcServer.equalsIgnoreCase("websphere4"))
            srcServer = "ws40";
        else if(srcServer.equalsIgnoreCase("websphere50"))
            srcServer = "ws50";
        else if(srcServer.equalsIgnoreCase("tomcat"))
            srcServer = "tc41";
        else if(srcServer.equalsIgnoreCase("JBoss"))
            srcServer = "jb30";
        else
            throw new BuildException("srcServer = "+srcServer+" is not one of " +
                                     "(sunone, weblogic5, weblogic6, weblogic8, websphere4, websphere50 and jboss )");
        
    }
}
