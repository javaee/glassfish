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

/*
 * HADBVersion.java
 *
 * Created on January 21, 2006, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.File;
import java.util.StringTokenizer;

/**
 * A simple class for parsing the version numbers and supplying them to callers.
 * @author bnevins
 */
class HADBVersion
{
    HADBVersion(HADBInfo info)
    {
        try
        {
            String[] commands = new String[] { "--version" };
            HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);		

            int exitValue = exec.exec();

            if(exitValue == 0)
            {
                String ver = parse(exec.getStdout());
                set(ver);
                LoggerHelper.fine("hadbmgmt-res.Version", ver);
                return;
            }
        }
        catch(Exception e)
        {
            // fall through...
        }

        // either exitValue != 0 **or** an Exception was thrown.
        // in either case -- set the default...
        set("");

    }
    
    ///////////////////////////////////////////////////////////////////////////

    public String toString()
    {
        return "" + a + '.' + b + '.' + c + '.' + d;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    double getMajor()
    {
        // e.g. 4.3, 4.4, 4.5
        try
        {
            return Double.parseDouble("" + a + "." + b);
        }
        catch(Exception e)
        {
            return 4.5;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    double getMinor()
    {
        // e.g. 2.7, 1.5, etc.
        try
        {
            return Double.parseDouble("" + c + "." + d);
        }
        catch(Exception e)
        {
            return 0.2;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    int getA()
    {
        return a;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    int getB()
    {
        return b;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    int getC()
    {
        return c;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    int getD()
    {
        return d;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void set (String hadbroot)
    {
        // the String must be formatted like so: "4.5.0-2"
        if(hadbroot.length() == 7)
        {
            try
            {
                a = Integer.parseInt(hadbroot.substring(0, 1));
                b = Integer.parseInt(hadbroot.substring(2, 3));
                c = Integer.parseInt(hadbroot.substring(4, 5));
                d = Integer.parseInt(hadbroot.substring(6, 7));
                return;
            }
            catch(Exception e)
            {
                // dropdown
            }
        }
        // if we get here, there was an error!
        setDefault();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setDefault()
    {
        a = Constants.DEFAULT_HADB_VERSION[0];
        b = Constants.DEFAULT_HADB_VERSION[1];
        c = Constants.DEFAULT_HADB_VERSION[2];
        d = Constants.DEFAULT_HADB_VERSION[3];
        LoggerHelper.warning("hadbmgmt-res.NoVersion", this);
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private String parse(String s)
    {
        /**
         * Typical Output from hadbm
Sun Java System High Availability Database 4.5 Database Management Client
Version     : 4.5.0.6 [V4-5-0-6 2005-11-18 13:32:18 pakker@edeber13]  (Win_2003ee_ix86)
        **/
        
        StringTokenizer st = new StringTokenizer(s);
        
        while(st.hasMoreTokens() && !st.nextToken().equals("Version")) 
        {
        }
        while(st.hasMoreTokens() && !st.nextToken().equals(":")) 
        {
        }
        if(st.hasMoreTokens())
        {
            return st.nextToken();
        }

        return "";
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    private int a, b, c, d;
}
