/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.logging;

import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.io.*;
import java.text.*;
import java.util.Date;
import oracle.toplink.essentials.internal.security.*;

/**
 * <p>
 * Print a brief summary of a TopLink LogRecord in a human readable
 * format.  The summary will typically be 1 or 2 lines.
 * </p>
 */
public class TopLinkSimpleFormatter extends SimpleFormatter {
    Date dat = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;
    private Object[] args = new Object[1];

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = (String)PrivilegedAccessHelper.getLineSeparator();

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record0) {
        if (!(record0 instanceof TopLinkLogRecord)) {
            return super.format(record0);
        } else {
            TopLinkLogRecord record = (TopLinkLogRecord)record0;

            StringBuffer sb = new StringBuffer();

            if (record.shouldPrintDate()) {
                // Minimize memory allocations here.
                dat.setTime(record.getMillis());
                args[0] = dat;
                StringBuffer text = new StringBuffer();
                if (formatter == null) {
                    formatter = new MessageFormat(format);
                }
                formatter.format(args, text, null);
                sb.append(text);
                sb.append(" ");
            }            
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());
            } else {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null) {
                sb.append(" ");
                sb.append(record.getSourceMethodName());
            }
            if (record.getSessionString() != null) {
                sb.append(" ");
                sb.append(record.getSessionString());
            }
            if (record.getConnection() != null) {
                sb.append(" ");
                sb.append(AbstractSessionLog.CONNECTION_STRING + "(" + String.valueOf(System.identityHashCode(record.getConnection())) + ")");
            }
            if (record.shouldPrintThread()) {
                sb.append(" ");
                sb.append(AbstractSessionLog.THREAD_STRING + "(" + String.valueOf(record.getThreadID()) + ")");
            }
            sb.append(lineSeparator);
            String message = formatMessage(record);
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
            sb.append(message);
            sb.append(lineSeparator);
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    if (record.getLevel().intValue() == Level.SEVERE.intValue()) {
                        record.getThrown().printStackTrace(pw);
                    } else if (record.getLevel().intValue() <= Level.WARNING.intValue()) {
                        if (record.shouldLogExceptionStackTrace()) {
                            record.getThrown().printStackTrace(pw);
                        } else {
                            pw.write(record.getThrown().toString());
                            pw.write(lineSeparator);
                        }
                    }
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            }
            return sb.toString();
        }
    }
}
