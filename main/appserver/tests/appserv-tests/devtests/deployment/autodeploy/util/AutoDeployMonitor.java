/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

/*
  * AutoDeployMonitor.java
  *
  * Created on May 11, 2004, 3:08 PM
  */

package autodeploy.test;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

import org.glassfish.deployment.autodeploy.AutoDeployConstants;

/**
  *Autodeploys an archive to the specified autodeploy directory.
 *<p>
 *To autodeploy the app, follow these steps:
 *1. Record the modification time of the archive file in the autodeploy directory as the start time.
 *2. Copy the archive to the autodeploy directory.
 *3. Monitor the autodeploy directory until it contains a marker file (for either success or
 *   failure) with a creation date and time later than the earlier-recorded timestamp.
 *4. Choose whether to return success or failure based on which kind of marker file has appeared
 *   in the autodeploy directory.
 *
  * @author  tjquinn
  */
public class AutoDeployMonitor {
    
    private static boolean DEBUG = Boolean.getBoolean("monitor.debug");
    
    private static int ITERATION_LIMIT = Integer.getInteger("monitor.iterationLimit", 60 * 2 /* default = two minutes */).intValue();
    
    private String archiveName = null;
    
//    private String autodeployDirSpec = null;
    
    private File autodeployDir = null;

//    private String timestampFormat = null;
    
//    private String timestampString = null;
    
    private long timestamp;
    
    private Date startTime = null;
    
    private final static String LINE_SEP = System.getProperty("line.separator");
    
    /** Creates a new instance of AutoDeployer */
    public AutoDeployMonitor() {
    }
    
    /**
      * @param args the command line arguments
      */
    public static void main(String[] args) {
        int result = new AutoDeployMonitor().run(args);
        System.exit(result);
    }

    private void processArguments(String[] args) {
        if (args.length < 4) {
            usage();
            System.exit(1);
        }
        
        archiveName = args[0];
        String autodeployDirSpec = args[1];
        String timestampFormat = args[2];
        String timestampString = args[3];

        autodeployDir = new File(autodeployDirSpec);

        debug("Archive to autodeploy: " + archiveName);
        debug("Autodeploy directory: " + autodeployDir.getAbsolutePath());

        StringBuffer result = new StringBuffer();
        
        /*
         *Check auto-deploy directory.
         */
        if ( ! autodeployDir.exists()) {
            result.append(LINE_SEP).append("Autodeploy directory " + autodeployDir.getAbsolutePath() + " does not exist but should.");
        }
        
        /*
         *Check archive file name.
         */
        if (archiveName.length() == 0) {
            result.append(LINE_SEP).append("Archive file name must not be empty but is.");
        }
        
        /*
         *Check the timestamp format and value.
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timestampFormat);
        Date timestampDate = null;
        try {
            timestampDate = simpleDateFormat.parse(timestampString);
            timestamp = timestampDate.getTime();
            debug("Timestamp to use for filtering autodeploy status files: " + timestampDate.toString());
        } catch (ParseException pe) {
            result.append(LINE_SEP).append("Could not interpret timestamp " + timestampString + " using format " + timestampFormat).append(LINE_SEP).append("    ").append(pe.getMessage());
        }
        
        if (result.length() > 0) {
            throw new IllegalArgumentException(result.toString());
        }
    }
    
    private int run(String[] args) {
        /*
         *The calling script should pass these command line arguments:
         *
         *arg[0] archive file name and type
         *arg[1] auto-deploy directory spec
         *arg[2] SimpleDateFormat pattern to use in parsing the timestamp string
         *arg[3] timestamp string
         *
         */

        try {
            processArguments(args);
            
            /*
             *The timestamp handling allows the logic below to identify a marker file (..._deployed, 
             *..._undeployed, ..._deployFailed, ..._undeployFailed) that was created after the
             *archive was deposited into (for autodeploy) or removed from (for autoundeploy) the
             *autodeploy directory.  We need to do this to make sure we do not accidentally take
             *action on an old marker file that is still there because the autodeployer has not
             *yet acted on the new file.
             */

            /*
             *For auto-deploy, the archive will have just been copied into the autodeploy directory.
             *For auto-undeploy, the archive will have just been deleted from there.  The file filter
             *below uses both the file name and also a timestamp to make sure it accepts
             *marker files created after the archive itself.  
             *
             *Note that the following is both a declaration of the filter and its initialization by
             *invoking its init method.
             */

            FileFilter filter = new FileFilter () { 

                private long timestamp;
                private String archiveName = null; 

                public boolean accept(File candidateFile) {
                    /*
                     *Make sure the name of the candidate starts with the name of the archive and that
                     *the candidate is no older than the archive file.  Make sure it's one of the marker
                     *file types we're interested in.
                     */
                    String candidateFileSpec = candidateFile.getAbsolutePath();
                    long candidateTimestamp = candidateFile.lastModified();
                    String candidateNameAndType = candidateFileSpec.substring(candidateFileSpec.lastIndexOf(File.separator) + 1);
                    String candidateType = candidateFileSpec.substring(candidateFileSpec.lastIndexOf('.') + 1);
                    
                    boolean answer = (candidateTimestamp > this.timestamp)
                        && (candidateNameAndType.equals(archiveName + AutoDeployConstants.DEPLOYED) ||
                            candidateNameAndType.equals(archiveName + AutoDeployConstants.UNDEPLOYED) ||
                            candidateNameAndType.equals(archiveName + AutoDeployConstants.DEPLOY_FAILED) ||
                            candidateNameAndType.equals(archiveName + AutoDeployConstants.UNDEPLOY_FAILED)
                           );
                    if (DEBUG) {
                        Date candidateTimestampDate = new Date(candidateTimestamp);
                        debug("Result of filtering file " + candidateFileSpec + " with modification date " + candidateTimestampDate.toString() + " : " + answer);
                    }
                    return answer;
                }

                public FileFilter init(String archiveName, long timestamp) {
                    this.archiveName = archiveName;
                    this.timestamp = timestamp;
                    return this;
                }
                }.init(archiveName, timestamp);
            
            File [] matches = null;

            /*
             *Begin waiting for a new marker file to appear in the autodeploy directory.  Don't wait longer than 
             *two minutes (by default) in case there is some problem.
             */
            int iterationCount = 0;
            do {
                debug("Starting polling iteration " + iterationCount + "...");
                Thread.currentThread().sleep(1000);
                /*
                 *List the files that have the same name as the archive but are not the archive itself.
                 *This should mean we get only the marker file(s) for this archive.
                 */
                matches = autodeployDir.listFiles(filter);

            } while ( (matches.length == 0) && (iterationCount++ < ITERATION_LIMIT));
                
            /*
             *Return 0 if the marker file found ends with _deployed, 1 if the marker ends with
             *_deployFailed, and -1 otherwise.
             */
            if (DEBUG) {
                System.out.println("Matched files:");
                for (int i = 0; i < matches.length; i++) {
                    System.out.println("  " + matches[i].getAbsolutePath());
                }
            }
            String matchFileSpec = matches[0].getAbsolutePath();
            int answer = -1;
            if (matchFileSpec.endsWith(AutoDeployConstants.DEPLOYED) || matchFileSpec.endsWith(AutoDeployConstants.UNDEPLOYED)) {
                answer = 0;
                debug("Found successful marker file: " + matchFileSpec);
            } else if (matchFileSpec.endsWith(AutoDeployConstants.DEPLOY_FAILED) || matchFileSpec.endsWith(AutoDeployConstants.UNDEPLOY_FAILED)) {
                answer = 1;
                debug("Found unsuccessful marker file: " + matchFileSpec);
            } else {
                debug("Found no marker file at all");
            }
            return answer;
        } catch (Throwable thr) {
            System.err.println("Error monitoring autodeployment/autoundeployment");
            thr.printStackTrace();
            return -1;
        }
    }
    
    private void usage() {
        System.err.println("Usage:");
        System.err.println("    autodeploy.loader.client.AutoDeployMonitor <archive-name> <autodeploy-directory> <SimpleDateFormat-pattern-for-timestamp> <timestamp-value>");
    }

    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}
