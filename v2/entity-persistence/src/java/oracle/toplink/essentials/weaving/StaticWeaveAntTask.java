/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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

package oracle.toplink.essentials.weaving;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import oracle.toplink.essentials.exceptions.StaticWeaveException;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
* <p>
* <b>Description</b>: This is the static weave ant task definition class that verifies the value of specified attributes and invokes 
*  StaticWeaveProcessor to weave classes.
* <p>
* <b>Usage</b>:
* <ul>
* <li>Ensure the classpath contains all the classes necessary to load the classes in the source.
* <li>The lib containing this weaving Ant task must be added into the classpath by using the -lib option on Ant command line instead of using the classpath attribute of the taskdef Ant task.
* <li>Define weaving Ant task and Ant target by using following attributes:
* source  - specify source location. In the default configuration StaticWeaveAntTask assumes the source contains the persistence.xml,if this is not the case, the location containing the persistence.xml must be explicitly identified by the attribute 'persistenceinfo'.
* target - specify the output location (either a directory or a jar).  
* persistenceinfo - specify the location containing the persistence.xml. This is optional and should only be specified if the source does not contain the persistence.xml.
* log - specify a logging file. This is optional
* loglevel - specify a literal value of toplink logging level(OFF,SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST) The default value is OFF(8). This is optional.
* <li>The weaving will be performed in place if source and target point to the same location. Weaving in place is ONLY applicable for directory-based sources.
* </ul>
*<b>Example</b>:
*<code>
*&lt;target name="define.task" description="New task definition for toplink static weaving"/&gt;<br>
*&nbsp;&nbsp;&lt;taskdef name="weave" classname="oracle.toplink.essentials.weaving.StaticWeaverAntTask"/&gt;<br>
*&lt;/target&gt;<br>
*&lt;target name="weaving" description="perform weaving." depends="define.task"&gt;<br>
*&nbsp;&nbsp;&lt;weave source= "c:\foo.jar" target = "c:\wovenfoo.jar" persistenceinfo="c:\foo-containing-persistenceinfo.jar"&gt;<br>
*&nbsp;&nbsp;&nbsp;&nbsp;&lt;classpath&gt;<br>
*&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement path="c:\foo-dependent.jar"/&gt;<br>
*&nbsp;&nbsp;&nbsp;&nbsp;&lt;/classpath&gt;<br>
*&nbsp;&nbsp;&lt;/weave&gt;<br>
*&lt;/target&gt;
*</code>
*/


public class StaticWeaveAntTask extends Task{
    
    private String source;
    private String persistenceinfo;
    private String target;
    private Vector classPaths = new Vector();
    private int logLevel=SessionLog.OFF;
    private Writer logWriter;
    
    
    /**
     * Set the input archive to be used to weave.
     * @param inputJarFile
     */
    public void setSource(String source) {
           this.source = source;
    }

    /**
     * Set output archive to be used to weave to
     * @param outputJarFile
     */
    public void setTarget(String target) {
           this.target = target;
    }
   
    /**
     * Set the archive containing persistence.xml while input archive does not contain it.
     * @param inputMainJarFile
     */
    public void setLog(String logFile) throws IOException {
        try{
           this.logWriter = new FileWriter(logFile);
        }catch(Exception e){
            throw StaticWeaveException.openLoggingFileException(logFile,e);
        }
    }
    
    public void setLogLevel(String logLevel) {
        if (logLevel.equalsIgnoreCase("OFF") ||
            logLevel.equalsIgnoreCase("SEVERE") || 
            logLevel.equalsIgnoreCase("WARNING") || 
            logLevel.equalsIgnoreCase("INFO") || 
            logLevel.equalsIgnoreCase("CONFIG") || 
            logLevel.equalsIgnoreCase("FINE") || 
            logLevel.equalsIgnoreCase("FINER") || 
            logLevel.equalsIgnoreCase("FINEST") || 
            logLevel.equalsIgnoreCase("ALL")) {
            this.logLevel=AbstractSessionLog.translateStringToLoggingLevel(logLevel.toUpperCase());
        } else{
            throw StaticWeaveException.illegalLoggingLevel(logLevel);
        }
    }
    
    public void setPersistenceinfo(String persistenceinfo) {
        this.persistenceinfo = persistenceinfo;
    }
    
    /**
     * Add the dependent classpath in order to load classes from the specified input jar.  
     * @param path
     */
    public void addClasspath(Path path) {
        classPaths.add(path);
    }
    
    /*
     * Parse the class path element and store them into vector.
     */
    private Vector getPathElement(){
        Vector pathElements = new Vector();
        for(int i=0;i<classPaths.size();i++){
            String thisPath = ((Path)classPaths.get(i)).toString();
            if(thisPath!=null){
               String[] thisSplitedPath=thisPath.split(File.pathSeparator);
               if(thisSplitedPath!=null){
                   for(int j=0;j<thisSplitedPath.length;j++){
                     pathElements.add(thisSplitedPath[j]);
                   }
               }
            }
        }
        return pathElements;
    }
    
    /*
     * Convert the path element into the URL which further pass into
     * the classloader. 
     */
    private URL[] getURLs(){
        Vector pathElements = getPathElement();
        URL[] urls = new URL[pathElements.size()];
        for(int i=0;i<pathElements.size();i++){
           try {
               urls[i] = (new File((String)pathElements.get(i))).toURI().toURL();
           } catch (MalformedURLException e) {
               throw StaticWeaveException.exceptionPerformWeaving(e);
           }
        }
        return urls;
    }

    /**
     * Execute ant task
     */
    public void execute() {
       verifyOptions();
       start();
    }
   
    /**
     * Verify the value of attributes.
     * @throws BuildException
     */
    private void verifyOptions() throws BuildException{
       if(source==null) {
           throw StaticWeaveException.missingSource();
       }
       
       if(target==null) {
           throw StaticWeaveException.missingTarget();
       }
    }
   
    /**
     * Invoke weaving process..
     */
    private void start(){
       try{
           StaticWeaveProcessor weave = new StaticWeaveProcessor(source, target);
           URL[] urls = getURLs();
           if(urls!=null){
               URLClassLoader classLoader = new URLClassLoader(getURLs(), Thread.currentThread().getContextClassLoader());
               weave.setClassLoader(classLoader);
           }
           if(persistenceinfo!=null){
               weave.setPersistenceInfo(persistenceinfo);
           }
           if(logWriter!=null){
               weave.setLog(logWriter);
           }
           weave.setLogLevel(this.logLevel);
           weave.performWeaving();
       }catch(Exception e){
           throw StaticWeaveException.exceptionPerformWeaving(e);
       }
    }
}
