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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.naming.util;

import java.util.logging.Level;

/**
 * Capture JavaMail debug output.
 */
public class MailLogOutputStream extends LogOutputStream {
    private static final String JAVAMAIL_DOMAIN = "javax.mail";

    public MailLogOutputStream() {
        super(JAVAMAIL_DOMAIN, Level.FINE);
    }

    /**
     * All output except detained protocol traces starts with "DEBUG".
     * Log protocol traces at lower level.
     * <p/>
     * NOTE: protocol trace output can include lines that start with
     * "DEBUG" so this isn't perfect.
     */
    protected void log(String msg) {
        if (msg.startsWith("DEBUG"))
            logger.log(Level.FINE, msg);
        else
            logger.log(Level.FINER, msg);
    }
}
