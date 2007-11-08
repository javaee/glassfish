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
 * NativeUtils.java
 *
 * Created on October 28, 2006, 10:28 PM
 *
 */

package com.sun.enterprise.util.natives;

/**
 * This class is the place to put any utility functions called in native code.
 * @author bnevins
 */
public class NativeUtils
{
    /**
     * native function to get a String from stdin without echoing the characters.
     * Note that the native code will be derprecated once JDK 1.6 is required.
     * @return The String the user types in
     */
    public native String getPasswordNative();
    
    static 
    {
        System.loadLibrary("cliutil");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Convenience method.  Print the prompt and then get the user's input
     * @param prompt Prompt string to display on stdout
     * 
     * @return The String the user types in
     */
    static public String getPassword(String prompt)
    {
        if(prompt != null && prompt.length() > 0)
            System.out.print(prompt);
        
        return getPassword();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Get the user's input
     * @return The String the user types in
     */
    static public String getPassword()
    {
        return new NativeUtils().getPasswordNative();
    }
}
