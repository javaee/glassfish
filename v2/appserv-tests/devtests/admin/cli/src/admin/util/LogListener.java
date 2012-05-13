/*
 *   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *   Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
 * 
 *   The contents of this file are subject to the terms of either the GNU
 *   General Public License Version 2 only ("GPL") or the Common Development
 *   and Distribution License("CDDL") (collectively, the "License").  You
 *   may not use this file except in compliance with the License.  You can
 *   obtain a copy of the License at
 *   https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *   or packager/legal/LICENSE.txt.  See the License for the specific
 *   language governing permissions and limitations under the License.
 * 
 *   When distributing the software, include this License Header Notice in each
 *   file and include the License file at packager/legal/LICENSE.txt.
 * 
 *   GPL Classpath Exception:
 *   Oracle designates this particular file as subject to the "Classpath"
 *   exception as provided by Oracle in the GPL Version 2 section of the License
 *   file that accompanied this code.
 * 
 *   Modifications:
 *   If applicable, add the following below the License Header, with the fields
 *   enclosed by brackets [] replaced by your own identifying information:
 *  "Portions Copyright [year] [name of copyright owner]"
 * 
 *   Contributor(s):
 *   If you wish your version of this file to be governed by only the CDDL or
 *   only the GPL Version 2, indicate your decision by adding "[Contributor]
 *   elects to include this software in this distribution under the [CDDL or GPL
 *   Version 2] license."  If you don't indicate a single choice of license, a
 *   recipient has the option to distribute your version of this file under
 *   either the CDDL, the GPL Version 2 or to extend the choice of license to
 *   its licensees as provided above.  However, if you add GPL Version 2 code
 *   and therefore, elected the GPL Version 2 license, then the option applies
 *   only if the new code is made subject to such option by the copyright
 *   holder.
 */
package admin.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple class designed to check on new logged messages. When you create it
 * it automatically sets the file pointer to the end of the log-file. Then you
 * can call the NON-BLOCKING method, getLatest() to see the latest stuff written
 * since your last check. Created May 27, 2011
 *
 * @author Byron Nevins
 */
public class LogListener {

    public static void main(String[] args) {
        LogListener ll = new LogListener("domain1");
        System.out.println("LogListener Main");
        System.out.println("length() returned: " + ll.length());

        while (true) {
            String s = ll.getLatest();

            if (s.length() > 0)
                System.out.println("\nLATEST:  [[[" + s + "]]]");
            else
                System.out.print(".");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                System.exit(0);
            }
        }
    }

    public LogListener() {
        this("domain1");
    }

    public LogListener(String domainName) {
        if (domainName == null || domainName.isEmpty())
            domainName = "domain1";

        RandomAccessFile di = null; // so reader can be final
        File f = null;

        try {
            // for filelayout change
            f = new File(new File(System.getenv("S1AS_HOME")),
                    "domains/" + domainName + "/server/logs/server.log");
            if(!f.exists())
                f = new File(new File(System.getenv("S1AS_HOME")),
                    "domains/" + domainName + "/logs/server.log");

            di = new RandomAccessFile(f, "rws");
            di.seek(f.length());
        }
        catch (Exception ex) {
            di = null;
            f = null;
        }
        reader = di;
        logfile = f;
    }

    public String getLatest(int secondsToWait) {
        try {
            Thread.sleep(1000 * secondsToWait);
            return getLatest();
        }
        catch (InterruptedException ex) {
            return "";
        }
    }

    public void close() {
        try {
            if (reader != null)
                reader.close();
        }
        catch (IOException ex) {
            // nothing to do
        }
    }

    public String getLatest() {
        try {
            long cur = length();
            long ptr = reader.getFilePointer();
            long numNew = cur - reader.getFilePointer() - 1;

            if (numNew == 0)
                return "";

            if (numNew < 0) {
                if (cur <= 1)
                    reader.seek(0);
                else
                    reader.seek(cur - 1);

                return "";
            }

            byte[] bytes = new byte[(int) numNew];
            reader.read(bytes);
            return new String(bytes);
        }
        catch (IOException ex) {
            return "";
        }
    }

    public final File getFile() {
        return logfile;
    }

    private long length() {
        return logfile.length();
    }
    private final File logfile;
    private final RandomAccessFile reader;
}
