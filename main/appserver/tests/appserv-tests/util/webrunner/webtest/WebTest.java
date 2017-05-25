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

package com.sun.ejte.ccl.webrunner.webtest;

import java.io.FileInputStream;
import java.io.File;

/**
*This is the main class for Web Test.It takes a text script file as an argument.
*
* @author       Deepa Singh (deepa.singh@sun.com)
 *Company       Sun Microsystems Inc.
*
*/
public class WebTest
{
    private String ws_root="appserv-tests";
    private String testsuite_id="";

    WebTest(){}
    
    public void setTestSuiteID(String testsuiteid)
    {
        this.testsuite_id=testsuiteid;
    }    

    public void setResultFileLocation(String workspace_root)
    {
        this.ws_root=workspace_root;
    }    

    /**
     *Reads script file and converts into a byte array.Sends byte array to SendRequest class.
     *@author Deepa Singh deepa.singh@sun.com
     *@param file String fully qualified location of file
     *@param host String host name of web server where web application is to be run.
     *
     */
    public void readFile(String file,String s_host,String s_port)
    {
        try
        {
            FileInputStream fin=new FileInputStream(file);
            File f=new File(file);
            byte buffer[]=new byte[(int)f.length()];
            System.out.println("size of buffer is"+buffer.length);
            int pos=0;
            int n;
            while((n=fin.read())>=0)
            {
                if(pos>(int)f.length())
                {
                    System.out.println("EOF reached");
                    break;
                }
                buffer[pos]=(byte)n;
                pos=pos+1;
            }

            fin.close();
            SendRequest sendRequest=new SendRequest(ws_root,testsuite_id);
            int port=new Integer(s_port).intValue();
	    sendRequest.setServerProperties(s_host,port);
            sendRequest.processUrl(buffer);
        }
        catch(Exception e)
        {
            System.out.println("Error in reading Script File");
            e.printStackTrace();
        }
    }

    
    
    public static void main(String [] args)
    {
        
        if(args.length<4)
        {
            System.err.println("usage:\t WebTest <<full_file_name>> <<web_server_host_name>> <<web_server_port>> <<outputfile>> <<testsuiteid>>");
            System.exit(0);
        }
        String file= args[0];
        String serverhost=args[1];
        String serverport=args[2];
        String ws_root=args[3];
        String testsuiteid=args[4];

        WebTest webTest=new WebTest();        
        webTest.setResultFileLocation(ws_root);
        webTest.setTestSuiteID(testsuiteid);
        webTest.readFile(file,serverhost,serverport);
        
    }
}

            
