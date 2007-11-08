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
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import oracle.toplink.essentials.exceptions.StaticWeaveException;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.localization.ToStringLocalization;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;

/**
* <p>
* <b>Description</b>: This is the static weave command line processing class that verifies command options and invokes 
* StaticWeaveProcessor to statically weave the classes. 
* <p>
*&nbsp;<b>Usage</b>:<br> 
*&nbsp;&nbsp;StaticWeave [options] source target<br>
*&nbsp;<b>Options</b>:<br>
*&nbsp;&nbsp;-classpath<br>
*&nbsp;&nbsp;&nbsp;&nbsp;Set the user class path, use ";" as the delimiter in Window system and ":" in Unix system.<br>
*&nbsp;&nbsp;-log <br>
*&nbsp;&nbsp;&nbsp;&nbsp;The path of log file, the standard output will be the default.<br>
*&nbsp;&nbsp;-loglevel<br>
*&nbsp;&nbsp;&nbsp;&nbsp;Specify a literal value for toplink log level(OFF,SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST). The default value is OFF.<br>
*&nbsp;&nbsp;-persistenceinfo<br>
*&nbsp;&nbsp;&nbsp;&nbsp;The path contains META-INF/persistence.xml. This is ONLY required when the source does not include it.
*&nbsp;The classpath must contain all the classes necessary in oder to perform weaving.<br><br>
*&nbsp;The weaving will be performed in place if source and target point to the same location. Weaving in place is ONLY applicable for directory-based sources.<br>
*<b>Example</b>:<br>
*&nbsp;To weave all entites contained in c:\foo-source.jar with its persistence.xml contained within c:\foo-containing-persistence-xml.jar, and output to c:\\foo-target.jar,<br>
*&nbsp;StaticWeave -persistenceinfo c:\foo-containing-persistencexml.jar -classpath c:\classpath1;c:\classpath2 c:\foo-source.jar c:\foo-target.jar
* 
**/

public class StaticWeave {

        // command line arguments
        private String[] argv;

        // The location path of the source, null if none was given 
        private String source;

        // The location path containing persistence.xml, null if none was given 
        private String persistenceinfopath;

        // The location path of the target, null if none was given 
        private String target;

        private int loglevel=SessionLog.OFF;
        
        private Writer logWriter;

        private PrintStream vout = System.out;
        
        private String[] classpaths;

        public static void main(String[] argv) {

            StaticWeave staticweaver = new StaticWeave(argv);

            try {
                // Verify the command line arguments 
                staticweaver.processCommandLine();
                staticweaver.start();
            } catch (Exception e) {
                throw StaticWeaveException.exceptionPerformWeaving(e);
            }
        }
        

        public StaticWeave(String[] argv) {
            this.argv = argv;
        }

        /**
         * Invoke StaticWeaveProcessor to perform weaving.
         */
        public void start() throws Exception {

            //perform weaving
            StaticWeaveProcessor staticWeaverProcessor= new StaticWeaveProcessor(this.source,this.target);
            if(persistenceinfopath!=null){
                staticWeaverProcessor.setPersistenceInfo(this.persistenceinfopath);
            }
            if(classpaths!=null){
                staticWeaverProcessor.setClassLoader(getClassLoader());
            }
            if(logWriter!=null){
               staticWeaverProcessor.setLog(logWriter);
            }
            staticWeaverProcessor.setLogLevel(loglevel);
            staticWeaverProcessor.performWeaving();
        }


        /*
         * Verify command line option. 
         */
        void processCommandLine() throws Exception
        {
            if (argv.length < 2 || argv.length>10) {
                printUsage();
                System.exit(1);
            }
            for (int i=0;i<this.argv.length;i++){
                if (argv[i].equalsIgnoreCase("-classpath")) {
                    // Make sure we did not run out of arguments
                    if ((i + 1) >= argv.length ){
                        printUsage();
                        System.exit(1);
                    }
                    classpaths=argv[i+1].split(File.pathSeparator);
                    i++;
                    continue;
                }
                
                if (argv[i].equalsIgnoreCase("-persistenceinfo")) {
                    if ((i + 1) >= argv.length ){
                           printUsage();
                           System.exit(1);
                    }
                    persistenceinfopath=argv[i+1];
                    i++;
                    continue;
                }
                
                if (argv[i].equalsIgnoreCase("-log")) {
                    if ((i + 1) >= argv.length ){
                           printUsage();
                           System.exit(1);
                    }
                    logWriter=new FileWriter(argv[i+1]);
                    i++;
                    continue;
                }

                if (argv[i].equalsIgnoreCase("-loglevel")) {
                    if ((i + 1) >= argv.length ) {
                           printUsage();
                           System.exit(1);
                    }

                   if ( argv[i+1].equalsIgnoreCase("OFF") ||
                        argv[i+1].equalsIgnoreCase("SEVERE") || 
                        argv[i+1].equalsIgnoreCase("WARNING") || 
                        argv[i+1].equalsIgnoreCase("INFO") || 
                        argv[i+1].equalsIgnoreCase("CONFIG") || 
                        argv[i+1].equalsIgnoreCase("FINE") || 
                        argv[i+1].equalsIgnoreCase("FINER") || 
                        argv[i+1].equalsIgnoreCase("FINEST") || 
                        argv[i+1].equalsIgnoreCase("ALL")) {
                       loglevel=AbstractSessionLog.translateStringToLoggingLevel(argv[i+1].toUpperCase());
                    } else{
                        printUsage();
                        System.exit(1);
                    }
                    i++;
                    continue;
                }
                
                if(source!=null){
                    printUsage();
                    System.exit(1);
                }
                
                if(target!=null){
                    printUsage();
                    System.exit(1);
                }

                source=argv[i];
                if((i+1)>=argv.length){
                    printUsage();
                    System.exit(1);
                }
                i++;
                if(i>=argv.length){
                    printUsage();
                    System.exit(1);
                }
                target=argv[i];
                i++;
            }
            
            
           //Ensure source and target have been specified
           if(source==null){
                printUsage();
                throw StaticWeaveException.missingSource();
           } 
           if(target==null){
                printUsage();
                throw StaticWeaveException.missingTarget();
           } 
        }

        /*
         * print command help message
         */
        private void printUsage() {
            PrintStream o = vout;
            o.println(ToStringLocalization.buildMessage("staticweave_commandline_help_message", new Object[]{null}));
        }
        
        /*
         * Convert the specified classpath arrary to URL array where new classloader will build on.
         */
        private ClassLoader getClassLoader() throws MalformedURLException{
            if (classpaths!=null){
                URL[] urls= new URL[classpaths.length];
                for(int i=0;i<classpaths.length;i++){
                   urls[i]=(new File(classpaths[i])).toURL();
                }
                return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            }else{
                return null;
            }
        }
}
