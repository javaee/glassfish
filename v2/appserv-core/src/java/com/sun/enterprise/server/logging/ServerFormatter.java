
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
package com.sun.enterprise.server.logging;



import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;

import java.text.MessageFormat;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Class ServerFormatter is a formatter designed for use by the ServerHandler
 * and designed for use by ereport (the web core native logging facility).
 * ereport is responsible for example for outputting the message header.
 */
public class ServerFormatter extends SimpleFormatter {

    private MessageFormat formatter;

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    // If we need to change the default line-seperator, it should be
    // done here.
    private static final String lineSeparator  =
        (String) java.security.AccessController
            .doPrivileged(new sun.security.action
                .GetPropertyAction("line.separator"));
    private static final String fieldSeparator = " ";

    /**
     * Method format
     *
     *
     * @param record
     *
     * @return
     */
    public synchronized String format(LogRecord record) {

        StringBuffer sb = new StringBuffer();

        // Get localized message after formatting per resource bundle if any.
        String message = formatMessage(record);

        sb.append(message);

        // Get stack trace if message was a result of exception.
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter  pw = new PrintWriter(sw);

                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(lineSeparator);
                sb.append(sw.toString());
            } catch (Exception ex) {}
        }
        sb.append(lineSeparator);

        return sb.toString();
    }
}

