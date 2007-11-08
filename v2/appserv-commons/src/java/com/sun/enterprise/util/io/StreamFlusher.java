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

package com.sun.enterprise.util.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;



public class StreamFlusher extends Thread {
    
    private InputStream _input=null;
    private OutputStream _output=null;
    private String _logFile=null;

    
    public StreamFlusher(InputStream input, OutputStream output) {
        this(input, output, null);
    }

    
    public StreamFlusher(InputStream input, OutputStream output, String logFile) {
        this._input=input;
        this._output=output;
        this._logFile=logFile;
    }
    
    public void run() {
        
        // check for null stream
        if (_input == null) return;
        
        PrintStream printStream=null;
        
        // If applicable, write to a log file
        if (_logFile != null) {
            try {
                if(createFileStructure(_logFile)) {
                    // reset streams to logfile
                    printStream = new PrintStream(new FileOutputStream(_logFile, true), true);
                } else {
                    // could not write to log for some reason
                    _logFile=null;
                }
            } catch (IOException ie) {
                ie.printStackTrace();
                _logFile=null;
            }
        }
        
        // transfer bytes from input to output stream
        try {
            int byteCnt=0;
            byte[] buffer=new byte[4096];
            while ((byteCnt=_input.read(buffer)) != -1) {
                if (_output != null && byteCnt > 0) {
                    _output.write(buffer, 0, byteCnt);
                    _output.flush();
                    
                    // also send to log, if it exists
                    if (_logFile != null) {
                        printStream.write(buffer, 0, byteCnt);
                        printStream.flush();
                    }
                }
                yield();
            }
        } catch (IOException e) {
            // shouldn't matter
        }
    }

    
    /**
     * createFileStructure - This method validates that that the file can be written to.  It the
     * if the parent directory structure does not exist, it will be created
     *
     * @param logFile - fully qualified path of the logfile
     */
    protected boolean createFileStructure(String logFile) {
        boolean bRet=false;
        File outputFile=new File(logFile);
        
        try {
            // Verify that we can write to the output file
            File parentFile = new File(outputFile.getParent());
            // To take care of non-existent log directories
            if ( !parentFile.exists() ) {
                // Trying to create non-existent parent directories
                parentFile.mkdirs();
            }
            // create the file if it doesn't exist
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            if (outputFile.canWrite()) {
                // everything is okay to logfile
                bRet=true;
            }
        } catch (IOException e) {
            // will only see on verbose more, so okay
            e.printStackTrace();
        }

        return bRet;
    }    
}