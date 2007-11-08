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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import oracle.toplink.essentials.ejb.cmp3.persistence.Archive;
import oracle.toplink.essentials.ejb.cmp3.persistence.ArchiveFactoryImpl;
import oracle.toplink.essentials.ejb.cmp3.persistence.PersistenceUnitProcessor;
import oracle.toplink.essentials.exceptions.StaticWeaveException;
import oracle.toplink.essentials.internal.localization.ToStringLocalization;
import oracle.toplink.essentials.internal.weaving.AbstractStaticWeaveOutputHandler;
import oracle.toplink.essentials.internal.weaving.StaticWeaveClassTransformer;
import oracle.toplink.essentials.internal.weaving.StaticWeaveDirectoryOutputHandler;
import oracle.toplink.essentials.internal.weaving.StaticWeaveJAROutputHandler;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.DefaultSessionLog;
import oracle.toplink.essentials.logging.SessionLog;

/**
* <p>
* <b>Description</b>: The StaticWeaveProcessor controls the static weaving process.  It is invoked by both the command line 
* StaticWeave class and the StaticWeaveAntTask. 
* <p>
* <b>Responsibilities</b>:Process the source classes, performs weaving as necessary out outputs to the target
**/
public class StaticWeaveProcessor {
    private URL source;
    private URL target;
    private URL persistenceInfo;
    private Writer logWriter;
    private ClassLoader classLoader;
    private int logLevel = SessionLog.OFF; 
    
    /**
     * Constructs an instance of StaticWeaveProcessor
     * @param source the name of the location to be weaved
     * @param target the name of the location to be weaved to
     * @throws MalformedURLException
     */
    public StaticWeaveProcessor(String source,String target)throws MalformedURLException{
        if (source!=null){
            this.source=new File(source).toURL();
        }
        if (target!=null){
            this.target=new File(target).toURL();
        }
    }

    /**
     * Constructs an instance of StaticWeaveProcessor
     * @param source the File object of the source to be weaved
     * @param target the File object of the target to be weaved to
     * @throws MalformedURLException
     */
    public StaticWeaveProcessor(File source, File target)throws MalformedURLException{
        this.source=source.toURL();
        this.target=target.toURL();
    }
    
    /**
     * Constructs an instance of StaticWeaveProcessor
     * @param source the URL of the source to be weaved
     * @param target the URL of the target to be weaved to
     */
    public StaticWeaveProcessor(URL source, URL target){
        this.source=source;
        this.target=target;
    }
    
    /**
     * The method allows user to specify the ouput for the log message.
     * @param log writer - the lcation where the log message writes to. the default value is standardout
     */
    public void setLog(Writer logWriter){
        this.logWriter= logWriter;
    }
    
    /**
     * The method allows user to define nine levels toplink logging.
     * @param level - the integer value of log level. default is OFF.
     */
    public void setLogLevel(int level){
        this.logLevel=level;
    }
    
    /**
     * Set the user classloader.
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader){
        this.classLoader=classLoader;
    }
    
    /**
     * Set an explicitly identified URL of the location containing persistence.xml.
     * @param persistenceInfo the URL of the location containing persistence.xml, the URL 
     * must point to the root of META-INF/persistence.xml
     */
    public void setPersistenceInfo (URL persistenceInfo){
             this.persistenceInfo = persistenceInfo;
    }

    /**
     * Set an explicitly identified the location containing persistence.xml.
     * @param persistenceinfo the path of the location containing persistence.xml, the path 
     * must point to the root of META-INF/persistence.xml
     */
    public void setPersistenceInfo(String persistenceInfoPath) throws MalformedURLException{
        if (persistenceInfoPath!=null){
            this.persistenceInfo=new File(persistenceInfoPath).toURL();
        }
    }


    /**
     * Set an explicitly identified the location containing persistence.xml.
     * @param persistenceinfo the file containing persistence.xml, the file 
     * should contain META-INF/persistence.xml
     */
    public void setPersistenceInfo(File persistenceInfoFile) throws MalformedURLException{
        if (persistenceInfoFile!=null){
            this.persistenceInfo=persistenceInfoFile.toURL();
        }
    }
    
    
    /**
     * This method performs weaving function on the class individually from the specified source.
     * @throws Exception.
     */
    public void performWeaving() throws URISyntaxException,MalformedURLException,IOException{
        preProcess();
        process();
    }
    
    /*
     * INTERNAL:
     * This method perform all necessary steps(verification, pre-build the target directory)
     * prior to the invokation of the weaving function.
     */
    private void preProcess() throws URISyntaxException,MalformedURLException{
        //Instantiate default session log
        AbstractSessionLog.getLog().setLevel(this.logLevel);
        if(logWriter!=null){
            ((DefaultSessionLog)AbstractSessionLog.getLog()).setWriter(logWriter);
        }

        //Make sure the source is existing
        if(!(new File(source.toURI())).exists()){
            throw StaticWeaveException.missingSource();
        }
        
        //Verification target and source, two use cases create warning or exception.
        //1. If source is directory and target is jar - 
        //   This will lead unkown outcome, user attempt to use this tool to pack outcome into a Jar. 
        //   Warning message will be logged, this is can be workarounded by other utilities.
        //2. Both source and target are specified as a same jar -  
        //   User was tryint to perform waving in same Jar which is not support, Exception will be thrown.
        if(isDirectory(source) && target.toURI().toString().endsWith(".jar")){
            AbstractSessionLog.getLog().log(SessionLog.WARNING, ToStringLocalization.buildMessage("staticweave_processor_unknown_outcome", new Object[]{null}));
        }
        
        if(!isDirectory(source) && target.toString().equals(source.toString())){
            throw StaticWeaveException.weaveInplaceForJar(source.toString());
        }
        
        //pre-create target if it is directory and dose not exsit.
        //Using the method File.isDirectory() is not enough to determine what the type(dir or jar) 
        //of the target(specified by URL)that user want to create. File.isDirectory() will return false in 
        //two possibilities, the location either is not directory or the location dose not exist. 
        //Therefore pre-build of the directory target is required. Pre-build for the file(JAR) target 
        //is not required since it gets built automically by opening outputstream.  
        if(!(new File(target.toURI())).exists()){
            if(!target.toURI().toString().endsWith(".jar")){
                (new File(target.toURI())).mkdirs();
                //if directory fails to build, which may leads to unknown outcome since it will 
                //be treated as single file in the class StaticWeaveHandler and automicatlly gets built
                //by outputstream.

                //re-assign URL.
                target = (new File(target.toURI())).toURL();
            }
        }
    }
    
    /*
     * INTERNAL:
     * The method performs weaving function
     */
    private void process() throws IOException,URISyntaxException{
        //Instantiate output handler
        AbstractStaticWeaveOutputHandler swoh;
        if(isDirectory(this.target)){
            swoh= new StaticWeaveDirectoryOutputHandler(this.source,this.target);
        }else{
            swoh= new StaticWeaveJAROutputHandler(new JarOutputStream(new FileOutputStream(new File(this.target.toURI()))));
        }
        
        //Instantiate classloader
        this.classLoader = (this.classLoader == null)? Thread.currentThread().getContextClassLoader():this.classLoader;
        this.classLoader = new URLClassLoader(getURLs(), this.classLoader);
        
        //Instantiate the classtransformer, we check if the persistenceinfo URL has been specified.
        StaticWeaveClassTransformer classTransformer=null;
        if(persistenceInfo!=null){
            classTransformer = new StaticWeaveClassTransformer(persistenceInfo, this.classLoader,this.logWriter,this.logLevel);
        } else{
            classTransformer = new StaticWeaveClassTransformer(source, this.classLoader,this.logWriter,this.logLevel);
        }

        //Starting process...
        Archive sourceArchive =(new ArchiveFactoryImpl()).createArchive(source);
        Iterator entries = sourceArchive.getEntries();
        while (entries.hasNext()){
            String entryName = (String)entries.next();
            InputStream entryInputStream = sourceArchive.getEntry(entryName);
            String className = PersistenceUnitProcessor.buildClassNameFromEntryString(entryName) ;
            
            //Add a directory entry
            swoh.addDirEntry(getDirectoryFromEntryName(entryName));
            
            //Add a regular entry
            JarEntry newEntry = new JarEntry(entryName);
            
            byte[] originalClassBytes=null;
            byte[] transferredClassBytes=null;
            try {
                Class thisClass = this.classLoader.loadClass(className);
                //if the class is not in the classpath, we simply copy the entry
                //to the target(no weaving).
                if (thisClass == null){
                    swoh.addEntry(entryInputStream, newEntry);
                    continue;
                }
                
                //Try to read the loaded class bytes, the class bytes is required for
                //classtransformer to perform transfer. Simply copy entry to the target(no weaving)
                //if the class bytes can't be read.
                InputStream is = this.classLoader.getResourceAsStream(entryName);
                if (is!=null){
                    originalClassBytes = new byte[is.available()];
                    is.read(originalClassBytes);
                }else{
                    swoh.addEntry(entryInputStream, newEntry);
                    continue;
                }
                
                //If everything is OK so far, we perform the weaving. we need three paramteres in order to
                //class to perform weaving for that class, the class name,the class object and class bytes.
                transferredClassBytes = classTransformer.transform(className.replace('.', '/'), thisClass, originalClassBytes);
                
                //if transferredClassBytes is null means the class dose not get woven.
                if(transferredClassBytes!=null){
                    swoh.addEntry(newEntry, transferredClassBytes);
                } else{
                    swoh.addEntry(entryInputStream, newEntry);
                }
            } catch (IllegalClassFormatException e) {
                //Anything went wrong, we need log a warning message, copy the entry to the target and
                //process next entry.
                swoh.addEntry(entryInputStream, newEntry);
                continue;
            } catch (ClassNotFoundException e){
                swoh.addEntry(entryInputStream, newEntry);
                continue;
            }finally{
                //need close the inputstream for current entry before processing next one. 
                entryInputStream.close();
            }
        }
        swoh.closeOutputStream();
    }

    
    //Extract directory from entry name.    
    public static String getDirectoryFromEntryName(String entryName){
        String result="";
        if (entryName==null ) {
            return result;
        }
        if(entryName.lastIndexOf("/")>=0){
            result=entryName.substring(0, entryName.lastIndexOf("/"))+File.separator;
        } 
        return result;
    }
    
    /*
     *  Determine whether or not the URL is pointing to directory.
     */
    private boolean isDirectory(URL url) throws URISyntaxException{
        File file = new File(url.toURI());
        if (file.isDirectory()) {
            return true;
        }else{
            return false;
        }
    }
    
    /*
     *  Generate URL array for specified source and persistenceinfo
     */
    private URL[] getURLs(){
        if((this.source!=null) && (this.persistenceInfo!=null)){
            return new URL[]{this.persistenceInfo,this.source};
        } else if(this.source!=null){
            return new URL[]{this.source};
        } else if (this.persistenceInfo!=null){
            return new URL[]{this.persistenceInfo};
        }
        return new URL[]{};
    }
}
