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
 *  $Id: CliUtil.java,v 1.4 2006/11/14 01:01:50 janey Exp $
 */

package com.sun.enterprise.cli.framework;

import java.io.*;

/**
 * This is a CLI utility class that uses the cliutil native code.
 * @author  Jane Young
 */
public class CliUtil 
{
    // the getEnv method only return variables that start with AS_ADMIN_
    public native String[] getEnv(String prefix);
    // added method for use in ProcessLauncher to get CLASSPATH env Variable
    public native String[] getAllEnv();
    public native String getPassword();
    public native char getKeyboardInput();

        /**
         * This function prompts the user for the password without echoing
         * the characters to the terminal.
         * @param prompt - prompt to display
         * @return the password entered by the user
         **/
    public String getPassword(String prompt){
	    InputsAndOutputs.getInstance().getUserOutput().print( prompt );
	    return getPassword();
    }

    static 
    {
        System.loadLibrary("cliutil");
    }

    public static void main(String[] args) 
    {
        final String sEnvPrefix = "PS_ADMIN_";
        boolean bContinue = true;

        while (bContinue) {
            System.out.println("Menu");
            System.out.println("[1] get environment");
            System.out.println("[2] get password");
            System.out.println("[3] get keyboard press");
            System.out.println("[4] get all environment");
            System.out.println("[5] exit");
        
            String line = getText();

            if (line.equals("1")) {
                String [] sEnvVal = new CliUtil().getEnv(sEnvPrefix);
                for (int ii=0; ii<sEnvVal.length; ii++) {
                        //check for prefix AS_ADMIN
                    String sName = sEnvVal[ii];
                    if (sName.regionMatches(true, 0, sEnvPrefix, 0,
                                            sEnvPrefix.length())) {
                        System.out.println(sName);
                    }
                }
            }
            else if (line.equals("2")) {
                String sPassword = new CliUtil().getPassword("Enter password>");
                System.out.println("Password entered = " + sPassword);
            }
            else if (line.equals("3")) {
                char c = new CliUtil().getKeyboardInput();
                System.out.println("\n key entered = " + c);
            }
            else if (line.equals("4")) {
                String[] envs = new CliUtil().getAllEnv();
                System.out.println("Get All Environment");
                for (String env : envs) {
                    System.out.println(env);
                }
            }
            else if (line.equals("5")) {
                bContinue =false;
            }
            else {
                System.out.println("You did not entered the right option.");
            }
        }
   }

    private static String getText()
    {
        String s = null;

        try
        {
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(System.in));
            s = in.readLine();
        }
        catch (IOException exc)
        {
            System.err.println("Caught exception: " + exc);
        }

        return (s);
    }
}

