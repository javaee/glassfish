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

package com.sun.enterprise.tools.upgrade.certconversion;

import java.io.*;
import java.util.logging.*;

import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;

public class ProcessAdaptor{
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private static StringManager sm = StringManager.getManager(LogService.UPGRADE_CERTCONVERSION_LOGGER);
    public static int executeProcess(String str, OutputStream outputStream){
        String[] args = str.split(" ");
        return executeProcess(args, outputStream);
    }
    
    public static int executeProcess(String[] str, OutputStream outputStream){
        int exitVal = -1;
        try{
            Process p = Runtime.getRuntime().exec(str);
            PrintStream errorOut = new PrintStream(p.getErrorStream(), outputStream);
            PrintStream resultOut = new PrintStream(p.getInputStream(), outputStream);
            errorOut.start();
            resultOut.start();
            errorOut.join();
            resultOut.join();
            exitVal = p.waitFor();
            p.destroy();
            return  exitVal;
        }catch(InterruptedException ie){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),ie);
        }catch(SecurityException se){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),se);
        }catch(IOException ioe){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),ioe);
        }
        return  exitVal;
    }
    
    public static int executeProcess(String str, Writer writer){
        String[] args = str.split(" ");
        int exitVal = executeProcess(args, writer);
        return  exitVal;
    }
    
    public static int executeProcess(String[] str, Writer writer){
        int exitVal = -1;
        try{
            // Runtime rt = Runtime.getRuntime();
            Process p = Runtime.getRuntime().exec(str);
            PrintStream errorOut = new PrintStream(p.getErrorStream(), writer);
            PrintStream resultOut = new PrintStream(p.getInputStream(), writer);
            errorOut.start();
            resultOut.start();
            errorOut.join();
            resultOut.join();
            exitVal = p.waitFor();
            p.destroy();
            return  exitVal;
        }catch(InterruptedException ie){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),ie);
        }catch(SecurityException se){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),se);
        }catch(IOException ioe){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),ioe);
        }
        return  exitVal;
    }
}

class PrintStream extends Thread {
    InputStream inpStream = null;
    OutputStream outpStream = null;
    Writer writer = null;
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private static StringManager sm = StringManager.getManager(LogService.UPGRADE_LOGGER);
    
    PrintStream(InputStream in, OutputStream out) {
        this.inpStream = in;
        this.outpStream = out;
    }
    
    PrintStream(InputStream in, Writer writer) {
        this.inpStream = in;
        this.writer = writer;
    }
    
    public void run() {
        try {
            InputStreamReader inpStreamReader = new InputStreamReader(inpStream);
            BufferedReader bufferedReader = new BufferedReader(inpStreamReader);
            String line=null;
            while((line = bufferedReader.readLine()) != null) {
                if (writer == null) {
                    outpStream.write(line.getBytes());
                    outpStream.write(new String("\n").getBytes());
                    outpStream.flush();
                } else {
                    writer.write(line+"\n");
                    writer.flush();
                }
            }
        } catch(IOException ioe) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.process_execution_error"),ioe);
        }
    }
}
