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
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.taskdefs.Jar;

import java.io.*;
import java.util.*;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * This is the implementation for the Ant task <sun-appserv-update>.
 * This task updates J2EE components previously deployed in the J2EE 1.4 SDK and
 * Sun ONE Application Server 8.  The following components may be updated:
 *   <ul>
 *     <li>Enterprise application (EAR file) 
 *     <li>Web application (WAR file) 
 *     <li>Enterprise Java Bean (EJB-JAR file) 
 *     <li>Enterprise connector (RAR file) 
 *   </ul>
 * The name of this task as used in build.xml will be sun-appserv-update.
 * This task uses ear/jar/war tasks from the user's Ant project
 * to find the contents of the components to be updated.
 * <p>
 * The attributes of this task are: <br>
 *    file="REQUIRED"  <br>
 *    domain="OPTIONAL" (default: "domain1")   <br>
 * <p>
 *
 * TODO: what if ear/jar/war task is called with dynamically changing property?
 * TODO: if XML desc changed, automatically reassemble and redeploy app ??
 * TODO: what if deployment changes XML descs (e.g. adds default values) ??
 *
 * @author Sanjeev Krishnan <a href="mailto:sanjeev.krishnan@sun.com">sanjeev.krishnan@sun.com</a>
 */
public class UpdateTask extends Task {

    private static final boolean debug = false;

    private String file;
    private String sunonehome = null;
    private String domain = "domain1";

    private ArrayList fromFiles;
    private ArrayList toFiles;;

    LocalStringsManager lsm = new LocalStringsManager();

    /**
     * Set the application/module name.
     */
    public void setFile(String file) {
        // file may have the wrong separator char, "new File(..)" fixes it
        this.file = new File(file).getPath();
    }

    /**
     * Set the name of the domain where this app is deployed (default: domain1).
     */
    public void setDomain(String domain) {
	this.domain = domain;
    }

    /**
     * Execute the task.
     */
    public void execute() throws BuildException {

	if ( file == null || file.equals("") ) {
	    throw new BuildException(lsm.getString("AttributeFileNoProvided"));
	}

	try {
	    if ( sunonehome == null ) {
		// figure out sunonehome from sun-appserv-ant.jar
		ClassLoader cl = this.getClass().getClassLoader();
		URL url = cl.getResource(
			   "org/apache/tools/ant/taskdefs/defaults.properties");
		String path = new File(url.getFile()).getPath();
		// path is like "<SUNONE_HOME>/lib/sun-appserv-ant.jar...."

		// remove any file: prefix
		if ( path.indexOf("file:") != -1 ) {
		    path = path.substring(5);
		}

		// remove the /lib/sun-appserv-ant.jar.... suffix.
		int jarIndex = path.indexOf("sun-appserv-ant.jar");
		sunonehome = path.substring(0, jarIndex - 5);
	    }

	    System.out.println(lsm.getString("UpdateMessage", new Object[] {file, sunonehome, domain}));

	    // Get the ear/jar/war task that created this app from the project
	    Jar[] creatorTasks = findCreatorTasks(file);
	    if ( creatorTasks == null ) {
		throw new BuildException(lsm.getString("UnableToCreateArchive", new Object[] {file})); 
	    }
	    if ( debug ) {
		System.out.println(lsm.getString("TaskThatCreatedArchive", new Object[] {file}));
	    }

	    // Initialize list of files to be copied
	    fromFiles = new ArrayList();
	    toFiles = new ArrayList();

	    // Phase 1: make list of files to be copied
	    String sep = File.separator;
	    String appsDirName = sunonehome + sep + "domains" + sep + 
				 domain + sep + "applications";
	    String appname = getAppNameFromFile(file);
	    String deployedDir;
	    if ( file.endsWith(".ear") ) {
		// The app directory is like
		// domains/domain1/server/applications/j2ee-apps/appname
		String j2eeAppsDir = appsDirName + sep + "j2ee-apps";
		deployedDir = j2eeAppsDir + File.separator + appname;

		// Get JAR/WAR/RAR modules in this EAR
		String[] moduleFiles = getModuleFiles(creatorTasks, file);

		// Call updateModule for each module in the EAR
		for ( int i=0; i<moduleFiles.length; i++ ) {
		    String modFile = moduleFiles[i];

		    // Get the task that created this module
		    Jar[] modCreatorTasks = findCreatorTasks(modFile);    
		    if ( modCreatorTasks == null ) {
			System.err.println(lsm.getString("UnableToFindTask", new Object[] {modFile})); 
			continue;
		    }

		    // Get the dir where module is deployed
		    String modName = getAppNameFromFile(modFile);
		    String modDeployedDir;
		    if ( modFile.endsWith(".war") ) {
			modDeployedDir = deployedDir + sep + modName + "_war";
		    }
		    else if ( modFile.endsWith(".jar") ) {
			modDeployedDir = deployedDir + sep + modName + "_jar";
		    }
		    else if ( modFile.endsWith(".rar") ) {
			modDeployedDir = deployedDir + sep + modName + "_rar";
		    }
		    else if ( modFile.equals(file) ) {
			modDeployedDir = deployedDir;
		    }
		    else {
			System.err.println(lsm.getString("InvalidModule", new Object[] {modFile}));
			continue;
		    }
	    
		    updateModule(modCreatorTasks, modDeployedDir);
		}
	    }
	    else { 
		// A standalone JAR/WAR module, the directory is 
		// domains/domain1/server/applications/j2ee-modules.
		// XXX Dir will be named j2ee-web-modules, j2ee-ejb-modules,
		// j2ee-rar-modules based on latest S1AS file layout proposal.
		String modulesDir = appsDirName + sep + "j2ee-modules";
		deployedDir = modulesDir + sep + appname;

		updateModule(creatorTasks, deployedDir);
	    }

	    // Phase 2: copy files
	    if ( fromFiles.size() > 0 ) {
		for ( int i=0; i<fromFiles.size(); i++ ) {
		    copyFile((File)fromFiles.get(i), (File)toFiles.get(i));
		}

		// Touch the .reload file in deployedDir to cause reload.
		File reload = new File(deployedDir, ".reload");
		if ( !reload.createNewFile() ) {
		    reload.setLastModified(System.currentTimeMillis());
		}

		System.out.println(lsm.getString("AplicationUpdated"));
	    }
	    else {
            System.out.println(lsm.getString("FilesUpdateToDate"));
	    }

	} catch ( Exception ex ) {
	    System.err.println(lsm.getString("UpdateError"));
	    if ( debug )
		ex.printStackTrace();
	    throw new BuildException(ex);
	}
    }


    /**
     * Return an array of JAR/WAR/RAR modules in the earFile.
     */
    private String[] getModuleFiles(Jar[] earTasks, String earFile) 
						throws BuildException {
	// Get the filesets of the EAR 
	ArrayList filesets = getFilesets(earTasks);

	// Iterate over each fileset
	ArrayList modules = new ArrayList();
	for ( int i=0; i<filesets.size(); i++ ) {
	    FileSet fs = (FileSet)filesets.get(i);

	    // If an invalid fileset, ignore it
	    if ( !fs.getDir(getProject()).exists() )
		continue;

	    // get list of files from FileSet
	    DirectoryScanner ds = fs.getDirectoryScanner(getProject());
	    File fromDir = fs.getDir(getProject());

	    String[] files = ds.getIncludedFiles();
	    for ( int j=0; j<files.length; j++ ) {
		String f = files[j];
		if ( f.endsWith(".jar") || f.endsWith(".war") 
					|| f.endsWith(".rar") ) {
		    modules.add(new File(fromDir, f).toString());
		}
	    }
	}
	modules.add(earFile);
	
	return (String[])modules.toArray(new String[modules.size()]);
    }


    /**
     * Update contents of the module in the appserver's repository.
     */
    private void updateModule(Jar[] creatorTasks, String deployedDir) 
						throws BuildException {

	try {
	    File deployedDirFile = new File(deployedDir);
	    if ( !deployedDirFile.exists() ) {
		System.err.println(lsm.getString("ModuleDoesNotExist", new Object[] {file})); 
		return;
	    }

	    // Get the filesets of the archive from the jar/war Task.
	    ArrayList filesets = getFilesets(creatorTasks);

	    // Iterate over each fileset
	    for ( int i=0; i<filesets.size(); i++ ) {
		FileSet fs = (FileSet)filesets.get(i);

                // If an invalid fileset, ignore it
                if ( !fs.getDir(getProject()).exists() )
                    continue;

		// get list of files from FileSet
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                File fromDir = fs.getDir(getProject());
                String[] srcFiles = ds.getIncludedFiles();

		String prefix = "";
		String fullpath = "";
		if ( fs instanceof ZipFileSet ) {
		    ZipFileSet zfs = (ZipFileSet)fs;
		    // Below calls to getPrefix and getFullpath work only
		    // on Ant 1.6.2 and higher.
		    String tmpPrefix = zfs.getPrefix(getProject());
		    String tmpFullpath = zfs.getFullpath(getProject());
		    if ( tmpPrefix != null && !tmpPrefix.equals("") ) {
			prefix = tmpPrefix;
		    }
		    else if ( tmpFullpath != null && !tmpFullpath.equals("") ) {
			// This means the fileset contains a single file
			// which is at the exact location of fullpath.
			fullpath = tmpFullpath;
		    }

		    // XXX support src attr of ZipFileSet
		    // XXX support zipgroupfileset.
		}

		// For EAR files, only update stuff in META-INF
		if ( file.endsWith(".ear") ) {
		    if ( fullpath.equals("META-INF/application.xml") 
			    || prefix.startsWith("META-INF") ) {
			compareAndCopy(fromDir, srcFiles, 
				       deployedDirFile, prefix, fullpath);
		    }
		}
		else {
		    compareAndCopy(fromDir, srcFiles, 
				   deployedDirFile, prefix, fullpath);
		}
	    }

	} catch ( Exception ex ) {
	    System.err.println(lsm.getString("UpdateError"));
	    if ( debug )
		ex.printStackTrace();
	    throw new BuildException(ex);
	}
    }


    /**
     * Get the name of the J2EE application from the archive file.
     */
    private String getAppNameFromFile(String file) {
	String appname = file.substring(0, file.length()-4); // remove .ext
	if ( appname.lastIndexOf(File.separator) != -1 ) {
	    appname = appname.substring(appname.lastIndexOf(File.separator)+1);
	}
	return appname;
    }


    /**
     * Search all Ant targets in this project for the ear/jar/war tasks
     * that created the given archive file.
     * @return null if no task could be found
     */
    private Jar[] findCreatorTasks(String file) throws IOException {

	if ( debug ) {
	    System.err.println("In findCreatorTasks for file " + file 
		+ " canonical path is " + new File(file).getCanonicalPath());
	}

	Hashtable targets = project.getTargets();
	Enumeration e = targets.elements();
	ArrayList creators = new ArrayList();
	while ( e.hasMoreElements() ) {
	    Target t = (Target)e.nextElement();
	    Task[] tasks = t.getTasks();

	    // For Ant 1.6.2: initialize UnknownElements so that the
	    // actual Jar tasks are created.
	    for ( int i=0; i<tasks.length; i++ ) {
		if ( tasks[i] instanceof org.apache.tools.ant.UnknownElement ) {
                    try {
                        tasks[i].maybeConfigure();
                    } catch ( Exception ex ) {
                        // Ignore.
                    }
		}
	    }

	    tasks = t.getTasks();
	    for ( int i=0; i<tasks.length; i++ ) {
		// Note: Ear and War are subclasses of Jar
		if ( tasks[i] instanceof org.apache.tools.ant.taskdefs.Jar ) {

		    Jar task = (Jar)tasks[i];

		    // make sure that attributes are set on the task
		    try {
			task.maybeConfigure(); 
		    } catch ( Exception ex ) {
			// Ignore.
		    }

		    //if ( debug ) {
		    //	System.err.println("Checking task that created " 
		    //			    + task.getDestFile());
		    //}

		    // check if this task created our archive file.
		    if ( task.getDestFile() != null 
			 && task.getDestFile().getCanonicalPath().equals(
				    new File(file).getCanonicalPath()) ) {
			creators.add(task);
		    }
		}
	    }
	}

	if ( creators.size() == 0 ) {
	    return null;
	}
	else {
	    return (Jar[])creators.toArray(new Jar[creators.size()]);
	}
    }


    /**
     * Get the list of filesets that describe the contents of the archive
     * created by the task.
     */
    private ArrayList getFilesets(Jar[] tasks) throws BuildException {

	try {
	    // The Zip class has a private filesets field, and the
	    // MatchingTask class has a protected field fileset. Get them using 
	    // reflection because theres no getter methods for them :-(
	    // No problems till the current Ant 1.6.1.

	    Class zipClass = Class.forName("org.apache.tools.ant.taskdefs.Zip");
	    Field filesetsField = zipClass.getDeclaredField("filesets");
	    filesetsField.setAccessible(true);
	    Field basedirField = zipClass.getDeclaredField("baseDir");
	    basedirField.setAccessible(true);

	    Class mtClass = Class.forName(
				"org.apache.tools.ant.taskdefs.MatchingTask");
	    Field filesetField = mtClass.getDeclaredField("fileset");
	    filesetField.setAccessible(true);

	    ArrayList filesets = new ArrayList();

	    for ( int i=0; i<tasks.length; i++ ) {

		// get the nested fileset elements of the task
		Vector fs = (Vector)filesetsField.get(tasks[i]);
		filesets.addAll(fs);

		// get the implicit fileset of the task
		FileSet implFileset = (FileSet)filesetField.get(tasks[i]);
		File baseDir = (File)basedirField.get(tasks[i]);
		if ( implFileset != null && baseDir != null ) {
		    FileSet fileset = (FileSet)implFileset.clone();
		    fileset.setDir(baseDir);
		    filesets.add(fileset);
		}
	    }

	    return filesets;

	} catch ( Exception ex ) {
	    throw (BuildException)(new BuildException().initCause(ex));
	}
    }


    /**
     * Compare last modified timestamps of srcFiles in fromDir with the
     * timestamps in toDir, copy if fromDir file has later timestamp.
     */
    private void compareAndCopy(File fromDir, String[] srcFiles, File toDir,
				   String prefix, String fullpath)
    {
	if ( prefix != null && !prefix.equals("") ) {
	    toDir = new File(toDir, prefix);
	}

	for ( int i=0; i<srcFiles.length; i++ ) {
	    String srcFile = srcFiles[i];

	    if ( debug ) {
		System.out.println("In compareAndCopy, fromDir = " + fromDir 
		    + " srcFile = " + srcFile + " toDir = " + toDir 
		    + " prefix = " + prefix + " fullpath = " + fullpath);
	    }

	    File from = new File(fromDir, srcFile);
	    File to;
	    if ( fullpath != null && !fullpath.equals("") ) {
		to = new File(toDir, fullpath);
	    }
	    else {
		to = new File(toDir, srcFile);
	    }
	    if ( from.lastModified() > to.lastModified() ) {
		fromFiles.add(from);
		toFiles.add(to);
	    }
	}
    }

    private void copyFile(File from, File to) throws BuildException {

	System.out.println("Copying file " + from + " to " + to);

	FileInputStream in = null;
	FileOutputStream out = null;
	try {
	    if ( !to.exists() ) {
		if ( !to.getParentFile().exists() ) {
		    // create all parent dirs
		    to.getParentFile().mkdirs();
		}
		to.createNewFile();
	    }

	    in = new FileInputStream(from);
	    out = new FileOutputStream(to);

	    byte[] buffer = new byte[8 * 1024];
	    int count = 0;
	    while ( (count = in.read(buffer, 0, buffer.length)) != -1 ) {
		out.write(buffer, 0, count);
	    } 

	} catch ( Exception ex ) {
	    throw new RuntimeException(lsm.getString("UnableToCopy", new Object[] {from}), ex);
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
		if (in != null) {
		    in.close();
		}
	    } catch ( Exception ex ) {}
	}
    }
}
