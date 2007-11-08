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

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.*;

public class FileUtils
{
    private FileUtils()
    {
    }
    
    /**
     */
    public static boolean protect(File f)
    {
        if(!f.exists())
            return true;
        
        if(isUNIX())
            return protectUNIX(f);
        else
            return protectWindows(f);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean protectUNIX(File f)
    {
        String fname = f.getAbsolutePath();
        String mode = "0600";
        
        if(f.isDirectory())
            mode = "0700";
        
        String cmd = "chmod " + mode + " " + fname;
        try
        {
            Process p = Runtime.getRuntime().exec(cmd);
            return p.waitFor() == 0 ? true : false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean protectWindows(File f)
    {
        // this is ugly.  We'return calling a program installed with Windows.
        // The program wants to confirm a change so we have to give it a 'Y'
        // at runtime...
        // ref: 6371534
        // cacls is a mess.  It requires a "Y" to be sent in on cacls' stdin.
        // BUT if anything goes wrong with the commandline or if, say, the user
        // has a non-NTFS drive, then cacls will not read stdin.  This will result in
        // a deadlock!
        //
        // I am commenting out all of this code.  It is overkill anyways because
        // none of the other sensitive files are protected in a Windows environment.
        // If AS ever starts setting file permissions on files in Windows,
        // this code can be revisited.
        
        return true;
        
                /*
                String fname = f.getAbsolutePath();
                String uname = System.getProperty("user.name");
                String[] cmds = new String[] { "cacls", fname, "/G", uname + ":F" };
                //String cmd = "cacls " + fname + " /G " + uname + ":F";
                 
                try
                {
                        Process p = Runtime.getRuntime().exec(cmds);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                        writer.write('Y');
                        writer.newLine();
                        writer.flush();
                        return p.waitFor() == 0 ? true : false;
                }
                catch(Exception e)
                {
                        return false;
                }
                 */
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean isUNIX()
    {
        return File.separatorChar == '/';
    }
}
