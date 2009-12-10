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
package com.sun.enterprise.security;

import com.sun.enterprise.security.ssl.SSLUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.*;
import javax.security.auth.callback.*;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.*;


/**
 * This implementation of LoginDialog, first looks at the environment
 * variables "login.username" and "login.password". If these are set it 
 * uses these for username & password respectively.
 * If these are not set, then it queries the user in the command window.
 *
 * @author Harish Prabandham
 */
public final class TextLoginDialog implements LoginDialog {

    private static Logger _logger=null;
    static {
        _logger=LogDomains.getLogger(TextLoginDialog.class, LogDomains.SECURITY_LOGGER);
    }

    private String username = null;
    private String password = null;
    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(TextLoginDialog.class);

    public TextLoginDialog() {
        BufferedReader d = 
            new BufferedReader(new InputStreamReader(System.in));
        String defaultUname = System.getProperty("user.name");
        do {
            System.err.print
                (localStrings.getLocalString
                    ("enterprise.security.login.username",
                    "User Name"));
            if(defaultUname != null){
                System.err.print("[" + defaultUname + "]: ");
            }else{
                System.err.print(": ");
            }
            try {
                username = d.readLine();
                if((defaultUname != null) && ((username == null) || (username.trim().length() == 0))){
                    username = defaultUname;
                }
            } catch(IOException e) {
            }
        } while ((username == null) || (username.trim().length() == 0));
            
        do {
            System.err.print
                (localStrings.getLocalString
                    ("enterprise.security.login.password",
                    "Password: "));
            try {
                password = d.readLine();
            }catch (IOException e) {
            }
        } while ((password == null) || (password.trim().length() == 0));
    }

    public TextLoginDialog(Callback[] callbacks) 
    {
	try {
	    for(int i = 0; i < callbacks.length; i++) {
		if(callbacks[i] instanceof NameCallback) {
		    NameCallback nc = (NameCallback) callbacks[i];
		    System.err.print(nc.getPrompt());
                    if(nc.getDefaultName() != null){
                        System.err.print("[" + nc.getDefaultName() + "]: ");
                    }else{
                        System.err.print(": ");
                    }

		    System.err.flush();
		    username=(new BufferedReader
				(new
				 InputStreamReader(System.in))).readLine();
                    if((nc.getDefaultName() != null) && ((username == null) || (username.trim().length() == 0))){
                        username=nc.getDefaultName();
                    }
                    nc.setName(username);
		    
		} else if(callbacks[i] instanceof PasswordCallback) {
		    PasswordCallback pc = (PasswordCallback) callbacks[i];
                    char[] passwd = null;
                    Object consoleObj = null;
                    Method readPasswordMethod = null;
                    try {
                        Method consoleMethod = System.class.getMethod("console");
                        consoleObj = consoleMethod.invoke(null);
                        readPasswordMethod =
                            consoleObj.getClass().getMethod(
                            "readPassword", String.class,
                            Array.newInstance(Object.class, 1).getClass()); 
                    } catch(Exception ex) {
                    }

                    if (consoleObj != null && readPasswordMethod != null) {
                        passwd = (char[])readPasswordMethod.invoke(
                            consoleObj, "%s",
                            new Object[] { pc.getPrompt() });
                    } else {
		        System.err.print(pc.getPrompt());
		        System.err.flush();
		    
		        String psswd = 
			    new BufferedReader
			    (new InputStreamReader(System.in)).readLine();
                        if (psswd != null) {
                            passwd = psswd.toCharArray();
                        }
                    }
                    if (passwd != null) {
		        pc.setPassword(passwd);
                        Arrays.fill(passwd, ' ');
                    }
		} else if(callbacks[i] instanceof ChoiceCallback) {
		    ChoiceCallback cc = (ChoiceCallback) callbacks[i];
		    /* Get the keystore password to see if the user is 
		     * authorized to see the list of certificates
		     */
		    String lbl = (localStrings.getLocalString
				  ("enterprise.security.keystore",
				   "Enter the KeyStore Password "));
		    String keystorePass = SSLUtils.getKeyStorePass();
		    System.out.println (lbl+
					" : (max 3 tries)"); 
		    int cnt=0;
		    for (cnt=0; cnt<3; cnt++){
			// Let the user try putting password thrice
			System.out.println (lbl+" : "); 
			String kp = 
			    (new BufferedReader
			     (new InputStreamReader(System.in))).readLine();
			if (kp.equals (keystorePass)) {
			    break; 
			} else{
			    String errmessage = localStrings.getLocalString("enterprise.security.IncorrectKeystorePassword","Incorrect Keystore Password");
			    System.err.println (errmessage); 
			}
		    }
		    if (cnt>=3){
			cc.setSelectedIndex (-1);
		    } else {
			System.err.println(cc.getPrompt());
			System.err.flush();
			String[] choices = cc.getChoices();
			for(int j = 0; j < choices.length; j++) {
			    System.err.print("[" + j + "] ");
			    System.err.println(choices[j]);
			}
			String line = 
			    (new BufferedReader
			     (new InputStreamReader(System.in))).readLine();
			
			int sel = new Integer(line).intValue();
			// System.out.println("SELECTED VAL:" + sel);
			cc.setSelectedIndex(sel);
		    }
		}
	    }
	} catch(Exception e) {
	    _logger.log(Level.SEVERE,
                        "java_security.name_password_entry_exception",e);
	}

    }

  
    /**
     * @return The username of the user.
     */
    public String getUserName() {
	return username;
    }
    /**
     *@return The password of the user in plain text...
     */
    public String getPassword() {
	return password;
    }
}
