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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.util.io.StreamFlusher;
import com.sun.enterprise.util.OS;
import java.io.File;

/**
 *  CLIProcessExecutor
 *  A simple process executor class that is used by CLI.
 *  @author  jane.young@sun.com
 *  @version  $Revision: 1.5 $
 */
public class CLIProcessExecutor
{
    Process process;

    public CLIProcessExecutor() {
        process = null;
    }
    

    /**
     * This method invokes the runtime exec
     * @param cmd the command to execute
     * @param wait if true, wait for process to end.
     * @exception Exception
     */
    public void execute(String[] cmd, boolean wait) throws Exception
    {
        process=Runtime.getRuntime().exec(cmd);
            //process = new ProcessBuilder(cmd).start();
            
        // start stream flusher to push output to parent streams and null.
        StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), System.err, null);
        sfErr.start();

        // set flusher on stdout also, if not could stop with too much output
        StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), System.out);
        sfOut.start();
        try {
            // must sleep for a couple of seconds, so if there is a jvm startup error,
            //the parent process
            //is around to catch and report it when the process in executed in verbose mode.
            Thread.currentThread().sleep(5000);
            //wait is not required for command like start database
            //where the process does not return since start database
            //spawn it's own process.
            if (wait) {
                process.waitFor();
            }
        }
        catch (InterruptedException ie) {
        }
    }

   /**
      return the exit value of this process.
      if process is null, then there is no process running
      therefore the return value is 0.
    */
    public int exitValue() {
        if (process == null) return -1;
        return process.exitValue();
    }

}
