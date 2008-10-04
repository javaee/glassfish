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

package com.sun.enterprise.cli.framework;

public class InvalidCommandException extends java.lang.Exception 
{
    static String msg = "";
    static {
        try
        {
            final LocalStringsManager lsm = LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            msg = lsm.getString("InvalidCommand");
        }
        catch (CommandValidationException cve)
        {
            msg =  LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }
    

    /**
     * Creates new <code>InvalidCommandException</code> without detail message.
     */
    public InvalidCommandException() 
    {
        super(msg + ".");
    }


    /**
     * Constructs an <code>InvalidCommandException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidCommandException(String cmdName) 
    {
        super(msg + ", " + cmdName + ".");
    }


    /**
     *  Constructs a new <code>InvalidCommandException</code> exception with the specified 
     *  cause
     */
    public InvalidCommandException(Throwable cause) 
    {
        super(cause);
    }


    /**
     *  Constructs a new <code>InvalidCommandException</code> exception with the specified 
     *  detailed message and cause
     */
    public InvalidCommandException(String msg, Throwable cause) 
    {
        super(msg, cause);
    }

    /** 
     *  returns the localized string from framework's LocalStrings.properties.
     *  Calls the LocalStringsManagerFactory.getFrameworkLocalStringsManager()
     *  method, returns "Key not found" if it cannot find the key
     *  @param key, the string to be localized
     *  @param toInsert, the strings to be inserted in the placeholders
     */
    private String getLocalizedString(String key, Object[] toInsert)
    {
        try
        {
            final LocalStringsManager lsm = 
            LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            return lsm.getString(key, toInsert);
        }
        catch (CommandValidationException cve)
        {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }

}


