/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)ProcessExecutor.java - ver 1.1 - 01/04/2006
 *
 * Copyright 2004-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

package test.jbi;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;


/**
 *
 *This class is used to execute external commands 
 */

    
    /**
     *  This is the inner class that consumes the error and output stream
     */
    class StreamConsumer extends Thread {
        /**
         * The inputstream from the process
         */
        InputStream is;
        /**
         * The string in which the results will be stored
         */
        StringBuffer resultStore;
        /**
         * The boolean that denotes if this is installation
         * this is true if this is installation
         */
        StreamConsumer (
                InputStream is, 
                StringBuffer resultStore) 
        {
            this.is = is;
            this.resultStore = resultStore;
        }
        /**
         * The method that returns the stored result. i.e output 
         * from the executed process
         */
        public StringBuffer getResult() {
            return resultStore;
        }
    
        /**
         * the thread execution method. This method consumes the input from the
         * given stream and write it in the given resultStore. If no resultStore is given
         * then it is written into the debug file. Installer uses resultStore for commands
         * where the output has to be analysed and is predictably small. For example 
         * output of asadmin start-domain. But for cases like invoking ant commands to
         * install files into the domain/or to install sample components, installer does not
         * supply result store and thus the output goes to the debug files.
         */
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null) {
                        resultStore.append(line);
                    }
            } catch (Exception ioe) {
                   ioe.printStackTrace();
            }
        }
    }
    
    /**
     * This class is used to timeout from the exec-ed process.
     * When the started process does not finish in the given time, this process
     * destroys the exec-ed process.
     */
    class Watcher extends Thread {
        
        /**
         * member that holds the timeout value
         */
        long timeout = 0;
        
        /**
         *member that holds reference to the exec-ed process
         */
        Process process = null;
        
        /**
         * This method is used to set the timeout value
         * @param timeout value of timeout
         */
        public void setTimeOut(long timeout){
            this.timeout=timeout;
        }
        
        /**
         * This method is used to set the reference to the exec-ed process
         * @param process reference to the process
         */
        public void setProcessReference(Process process){
            this.process=process;
        }
        
        public void run() {
            try {
                sleep(timeout);
            } catch (InterruptedException ie) {
            } finally {
                process.destroy();
            }
        }
    }
    
/**
 * This class executes the given external  command and returns the exitValue
 * output and error.
 */
 public class ProcessExecutor  {
     
     /**
      * This member holds the value for the default timeout in milliseconds
      */
     private static long defaultTimeOut = 2*60*1000;
 
   
    /**
     * This method is used to execute a given external command 
     * @return int the execute value
     * @param output after the command is executed 
     * @param error after the command is executed 
     */
     public int execute(
                        String cmd, 
                        StringBuffer output, 
                        StringBuffer error) {
        return execute(cmd, output, error, defaultTimeOut);
    }
     
       
       
    /**
     * This method is used to execute a given external command 
     * @return int the execute value
     * @param output after the command is executed 
     * the output is available in this StringBuffer
     * @param error after the command is executed 
     * the error is available in this StringBuffer
     * This method is useful in situations where 
     * we have to execute a command like
     * sh -c cd ~;ls. If the entire thing is passed as a single string 
     * the way java makes a 
     * string array out of it makes this command not work. So we make
     * the required array as we want.
     */
    public int execute(
            String cmd, 
            StringBuffer output, 
            StringBuffer error, 
            long timeout) 
    {
        try {
            Runtime rt = Runtime.getRuntime();
            //System.out.println(cmd);
            final Process proc = rt.exec(cmd);
            if (timeout > 0) {
                Watcher watcher = new Watcher();
                watcher.setTimeOut(timeout);
                watcher.setProcessReference(proc);
                watcher.start();

            }//timeout
            StreamConsumer errorConsumer = 
                    new StreamConsumer(
                        proc.getErrorStream(), 
                        output);
            StreamConsumer outputConsumer = 
                    new StreamConsumer(
                    proc.getInputStream(), 
                    error);
            errorConsumer.start();
            outputConsumer.start();
            int exitVal = proc.waitFor();
            if (output != null) {
                output.insert(0, outputConsumer.getResult().toString());
            } 
            if (error != null) {
                error.insert(0, errorConsumer.getResult().toString());
            }
            return exitVal;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    
}
