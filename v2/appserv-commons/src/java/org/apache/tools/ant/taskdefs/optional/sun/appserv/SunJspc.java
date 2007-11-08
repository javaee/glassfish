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
 * SunJspc.java
 *
 * Created on May 22, 2002, 8:46 PM
 */

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;

/** Ant task to run the jsp compiler.for SunONE Application Server
 * <p> This task takes the given jsp files and compiles them into java
 * files. It is then up to the user to compile the java files into classes.
 *
 * <p> The task requires the srcdir and destdir attributes to be
 * set. This Task is a MatchingTask, so the files to be compiled can be
 * specified using includes/excludes attributes or nested include/exclude
 * elements. Optional attributes are verbose (set the verbosity level passed
 * to jasper), package (name of the destination package for generated java
 * classes and classpath (the classpath to use when running the jsp
 * compiler).
 *
 * <p><h4>Notes</h4>
 * <p>The includes directive is necessary in the ant targets
 * <p>The taskdef can contain in its classpath the path to the SunJspc compiled file
 *
 * <p><h4>Usage</h4>
 * <pre>
 * &lt;taskdef 
 *      name="sun-appserv-jspc" 
 *      classname="org.apache.tools.ant.taskdefs.optional.sunone.SunJspc"
 *      classpath=&lt;classpath to the compiled file>/&gt;
 *
 * &lt;sun-appserv-jspc srcdir="${basedir}/src/war"
 *       destdir="${basedir}/gensrc"
 *       package="com.i3sp.jsp"
 *       verbose="9"&gt;
 *   &lt;include name="**\/*.jsp" /&gt;
 * &lt;/s1jspc&gt;
 *
 *&lt;sun-appserv-jspc 
 *       destdir="${basedir}/gensrc"
 *       verbose="9"&gt;
 *       webapp=${basedir}
 * &lt;/sun-apserv-jspc&gt;
 * </pre>
 *
 * @author <a href="mailto:irfan@sun.com">Irfan Ahmed</a>
 * <p> Large Amount of cutting and pasting from the Javac task...
 * @since SunONE Application Server 7 SE
 */

public class SunJspc extends MatchingTask
{
    private File destDir = null;
    private File srcDir;
    private String packageName;
    private String verboseLevel;
    private File uriRoot;
    private File uriBase;
    private Path classPath;
    private int compileListLength;
    private boolean failOnError = true;
    private File webAppBaseDir;
    
    private File sunoneHome;
    private File asinstalldir; 

    /** Appserver runtime Libraries, expressed relative to the installation directory */
    private static final String[] CLASSPATH_ELEMENTS = {
                            "lib", 
							"lib/appserv-rt.jar",
							"lib/javaee.jar",
							"lib/appserv-ext.jar"};
    
    LocalStringsManager lsm = new LocalStringsManager();

    public void setSunonehome(File sunoneHome)
    {
        final String msg = lsm.getString("DeprecatedAttribute", new Object[] {"sunonehome",
         "asinstalldir"});
        log(msg, Project.MSG_WARN);
        this.asinstalldir = sunoneHome;
    }

    
    /**
	 * Specifies the installation directory for the Sun ONE Application Server
	 * 8.  This may be used if the application server is installed on the 
	 * local machine.
	 *
	 * @param asinstalldir The home directory for the user's app server 
	 *                   installation.
	 */
	public void setAsinstalldir(File asinstalldir) {
		this.asinstalldir = asinstalldir;
	}

        
	/**
	 * Returns the asinstalldir attribute specify by in the build script.
	 * If asinstalldir hasn't been explicitly set (using
	 * the <code>setAsinstalldir</code> method), the value stored in the <code>
	 * sunone.home</code> property will be returned.
	 *
	 * @return File representing the app server installation directory.  Returns
	 *         <code>null</code> if the installation directory hasn't been
	 *         explictly set and the <code>sunone.home</code> property isn't set.
     * @throws ClassNotFoundException if asinstalldir is an invalid directory
	 */
	protected File getAsinstalldir() throws ClassNotFoundException {
		if (asinstalldir == null) {
			String home = getProject().getProperty("asinstall.dir");
			if (home != null) {
                asinstalldir = new File(home);
			}
            else {
                home = getProject().getProperty("sunone.home");
                if (home != null)
                {
                    final String msg = lsm.getString("DeprecatedProperty", new Object[] {"sunone.home", "asinstall.dir"});
                    log(msg, Project.MSG_WARN);
                    asinstalldir = new File(home);
                }
                
            }
		}
        if (asinstalldir!=null) verifyAsinstalldir(asinstalldir);
		return asinstalldir;
	}


    /**
     * verify if asinsatlldir attribute is valid.
     * asinstalldir must be a valid directory and must contain the config directory.
     *
     * @return true if asinstalldir is valid
     * @throws ClassNotFoundException if asinstalldir is an invalid directory
     */
    private boolean verifyAsinstalldir(File home) throws ClassNotFoundException{
        if (home!= null && home.isDirectory()) {
            if ( new File(home, "config").isDirectory() ) {
                return true;
            } 
        }
        throw new ClassNotFoundException("ClassCouldNotBeFound");
    }
    
    
    public void setDestdir(File dest)
    {
        destDir = dest;
    }
    
    public File getDestdir()
    {
        return destDir;
    }
    
    public void setSrcdir(File src)
    {
        srcDir = src;
    }
    
    public File getSrcdir()
    {
        return srcDir;
    }
    
    public void setPackage(String name)
    {
        packageName = name;
    }
    
    public String getPackage()
    {
        return packageName;
    }
    
    public void setVerbose(String level)
    {
        verboseLevel = level;
    }
    
    public String getVerbose()
    {
        return verboseLevel;
    }
    
    public void setFailonerror(boolean fail)
    {
        failOnError = fail;
    }
    
    public boolean getFailonerror()
    {
        return failOnError;
    }
    
    public void setUribase(File base)
    {
        uriBase = base;
    }
    
    public File getUribase()    
    {
        if(uriBase!=null)
            return uriBase;
        return uriRoot;
    }
    
    public void setUriroot(File root)
    {
        uriRoot = root;
    }
    
    public File getUriroot()
    {
        return uriRoot;
    }
    
    public void setClasspath(Path cp)
    {
        if(classPath == null)
            classPath = cp;
        else
            classPath.append(cp);
    }
    
    /** Nested ClassPath Element */
    public Path createClasspath()
    {
        if(classPath==null)
            classPath = new Path(project);
        return classPath.createPath();
    }
    
    /** Class Path Reference */
    public void setClasspathref(Reference ref)
    {
        createClasspath().setRefid(ref);
    }
    
    public void setWebapp(File baseDir)
    {
        webAppBaseDir = baseDir;
    }
    
    public File getWebapp()
    {
        return webAppBaseDir;
    }

    
    public void execute()throws BuildException
    {
        
        CheckForMutuallyExclusiveAttribute();
        
        if(webAppBaseDir==null)
        {
            if(srcDir==null)
                throw new BuildException(lsm.getString("SourceDirectoryProviced"), location);
            if(!srcDir.exists() || !srcDir.isDirectory())
                throw new BuildException(lsm.getString("SourceDirectoryDoesNotExist",
                                                       new Object[] {srcDir.getAbsolutePath()}),
                                         location);
        }
        else
        {
            if(!webAppBaseDir.exists() || !webAppBaseDir.isDirectory())
                throw new BuildException(lsm.getString("WebAppDirectoryDoesNotExist",
                                                       new Object [] {webAppBaseDir.getAbsolutePath()}),
                                         location);
        }
            
        if(destDir!=null)
        {
            if(!destDir.exists())
                throw new BuildException(lsm.getString("DestinationDirectoryDoesNotExist",
                                                       new Object[] {destDir}));
            if(!destDir.isDirectory())
                throw new BuildException(lsm.getString("InvalidDestinationDirectory",
                                                       new Object[] {destDir}));
        }
        else
        {
            throw new BuildException(lsm.getString("DestinationDirectoryNoProvided"));
        }
        
        
        String args[] = getCommandString();
        if(srcDir!=null)
            log(lsm.getString("PreCompilation", new Object[] {String.valueOf(compileListLength),
                                                              destDir.getAbsolutePath()})); 
        if(!doCompilation(args))
            throw new BuildException(lsm.getString("CompilationFailed"));
    }


        /**
         * This private class checks for any mutually exclusive attributes.
         * If mutually exclusive attributes that are specified, then a
         * BuildException is thrown.
         */
    private void CheckForMutuallyExclusiveAttribute() throws BuildException
    {
        if(webAppBaseDir!=null && srcDir!=null) {
            final String msg = lsm.getString("MutuallyExclusivelyAttribute",
                                             new Object[] {"srcdir",
                                                           "webapp"});
            throw new BuildException(msg, getLocation());
        }
    }

    
    protected boolean doCompilation(String args[])
    {
        try
        {
            Java java = (Java)project.createTask("java");
            java.setClasspath(constructPath());
            java.setClassname("org.apache.jasper.JspC");
            for(int i=0;i<args.length;i++)
            {
                java.createArg().setValue(args[i]);
            }
            java.setFailonerror(failOnError);
            java.setFork(true);
            log("Executing Jasper Compiler");
            int returnCode = java.executeJava();
            if(returnCode == 1)
            {
                log(lsm.getString("SetVerbose"));
                return false;
            }
            return true;
        }
        catch(Exception ex)
        {
            log(lsm.getString("ExceptionMessage", new Object[] {ex.toString()})); 
            return false;
        }
    }
    
    protected String[] getCommandString()
    {
        ArrayList commandList = new ArrayList();
        
        commandList.add("-d");
        commandList.add(destDir.getAbsolutePath());
        
        if(packageName!=null && packageName.length()>0)
        {
            commandList.add("-p");
            commandList.add(packageName);
        }
        
        if(verboseLevel!=null)
            commandList.add("-v".concat(verboseLevel));
        
        if(uriRoot!=null && uriRoot.exists())
        {
            commandList.add("-uriroot");
            commandList.add(uriRoot.getAbsolutePath());
        }
        
        if(uriBase!=null && uriBase.exists())
        {
            commandList.add("-uribase");
            commandList.add(uriBase.getAbsolutePath());
        }
        else if(uriRoot!=null && uriRoot.exists())
        {
            commandList.add("-uribase");
            commandList.add(uriRoot.getAbsolutePath());
        }

        // START PWC 6386258
        commandList.add("-dtds");
        commandList.add("/dtds/");

        commandList.add("-schemas");
        commandList.add("/schemas/");
        // END PWC 6386258
 
        commandList.add("-die1");
        if(webAppBaseDir!=null)
        {
            commandList.add("-webapp");
            commandList.add(webAppBaseDir.getAbsolutePath());
        }
        else
        {
            DirectoryScanner ds = super.getDirectoryScanner(srcDir);
            String files[] = ds.getIncludedFiles();
            compileListLength = files.length;
            for(int i=0;i<files.length;i++)
            {
                File tempFile = new File(srcDir,files[i]);
                commandList.add(tempFile.getAbsolutePath());
            }
        }

        String args[] = (String[])commandList.toArray(new String[commandList.size()]);
        return args;
    }
    
    private Path constructPath()  throws ClassNotFoundException
    {
        StringBuffer classPathBuffer = new StringBuffer();
        if(getAsinstalldir()!=null)
        {
            for(int i=0;i<CLASSPATH_ELEMENTS.length;i++)
            {
                classPathBuffer.append((new File(getAsinstalldir(),CLASSPATH_ELEMENTS[i])).getPath());
                classPathBuffer.append(":");
            }
        }
        if(classPath!=null)
        {
            classPathBuffer.append(classPath);
            classPathBuffer.append(":");
        }
        classPathBuffer.append(Path.systemClasspath);
        return new Path(getProject(),classPathBuffer.toString());
    }
}
