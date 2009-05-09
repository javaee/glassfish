/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * The one and only type of Exception that will be thrown out of this package.
 * I18N is wired in.  If a String message is found in the resource bundle, it will
 * use that String.  If not, it will use the String itself.
 * @author bnevins
 */
public class GFLauncherException extends Exception {

    /**
     * 
     * @param msg The message is either pointing at a I18N key in the resource 
     * bundle or will be treated as a plain string.
     */
    public GFLauncherException(String msg)
    {
        super(strings.get(msg));
    }

    /**
     * 
     * @param msg The message is either pointing at a I18N key in the resource 
     * bundle or will be treated as a plain string that will get formatted with
     * objs.
     * @param objs Objects used for formatting the message.
     */
    public GFLauncherException(String msg, Object... objs)
    {
        super(strings.get(msg, objs));
    }

    /**
     * 
     * @param msg The message is either pointing at a I18N key in the resource 
     * bundle or will be treated as a plain string.
     * @param t The causing Throwable.
     */
    public GFLauncherException(String msg, Throwable t)
    {
        super(strings.get(msg), t);
    }

    /**
     * 
     * @param msg The message is either pointing at a I18N key in the resource 
     * bundle or will be treated as a plain string that will get formatted with
     * objs.
     * @param t The causing Throwable.
     * @param objs Objects used for formatting the message.
     */
    public GFLauncherException(String msg, Throwable t, Object... objs)
    {
        super(strings.get(msg, objs), t);
    }
    /**
     * 
     * @param t The causing Throwable.
     */
    public GFLauncherException(Throwable t)
    {
        super(t);
    }
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncherException.class);
}
