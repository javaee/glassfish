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

import java.util.*;
import java.util.logging.XMLFormatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * <p>
 * Format a TopLink LogRecord into a standard XML format.
 * </p>
 */
public class TopLinkXMLFormatter extends XMLFormatter {
    // Append a two digit number.
    private void a2(StringBuffer sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }

    // Append the time and date in ISO 8601 format
    private void appendISO8601(StringBuffer sb, long millis) {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date(millis));
        sb.append(date.get(Calendar.YEAR));
        sb.append('-');
        a2(sb, date.get(Calendar.MONTH) + 1);
        sb.append('-');
        a2(sb, date.get(Calendar.DATE));
        sb.append('T');
        a2(sb, date.get(Calendar.HOUR_OF_DAY));
        sb.append(':');
        a2(sb, date.get(Calendar.MINUTE));
        sb.append(':');
        a2(sb, date.get(Calendar.SECOND));
    }

    // Append to the given StringBuffer an escaped version of the
    // given text string where XML special characters have been escaped.
    // For a null string we appebd "<null>"
    private void escape(StringBuffer sb, String text) {
        if (text == null) {
            text = "<null>";
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
    }

    /**
     * Format the given message to XML.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record0) {
        if (!(record0 instanceof TopLinkLogRecord)) {
            return super.format(record0);
        } else {
            TopLinkLogRecord record = (TopLinkLogRecord)record0;

            StringBuffer sb = new StringBuffer(500);
            sb.append("<record>\n");

            if (record.shouldPrintDate()) {
                sb.append("  <date>");
                appendISO8601(sb, record.getMillis());
                sb.append("</date>\n");
    
                sb.append("  <millis>");
                sb.append(record.getMillis());
                sb.append("</millis>\n");
            }

            sb.append("  <sequence>");
            sb.append(record.getSequenceNumber());
            sb.append("</sequence>\n");

            String name = record.getLoggerName();
            if (name != null) {
                sb.append("  <logger>");
                escape(sb, name);
                sb.append("</logger>\n");
            }

            sb.append("  <level>");
            escape(sb, record.getLevel().toString());
            sb.append("</level>\n");

            if (record.getSourceClassName() != null) {
                sb.append("  <class>");
                escape(sb, record.getSourceClassName());
                sb.append("</class>\n");
            }

            if (record.getSourceMethodName() != null) {
                sb.append("  <method>");
                escape(sb, record.getSourceMethodName());
                sb.append("</method>\n");
            }

            if (record.getSessionString() != null) {
                sb.append("  <session>");
                sb.append(record.getSessionString());
                sb.append("</session>\n");
            }

            if (record.getConnection() != null) {
                sb.append("  <connection>");
                sb.append(String.valueOf(System.identityHashCode(record.getConnection())));
                sb.append("</connection>\n");
            }

            if (record.shouldPrintThread()) {
                sb.append("  <thread>");
                sb.append(record.getThreadID());
                sb.append("</thread>\n");
            }

            if (record.getMessage() != null) {
                // Format the message string and its accompanying parameters.
                String message = formatMessage(record);
                sb.append("  <message>");
                escape(sb, message);
                sb.append("</message>");
                sb.append("\n");
            }

            // If the message is being localized, output the key, resource
            // bundle name, and params.
            ResourceBundle bundle = record.getResourceBundle();
            try {
                if ((bundle != null) && (bundle.getString(record.getMessage()) != null)) {
                    sb.append("  <key>");
                    escape(sb, record.getMessage());
                    sb.append("</key>\n");
                    sb.append("  <catalog>");
                    escape(sb, record.getResourceBundleName());
                    sb.append("</catalog>\n");
                    Object[] parameters = record.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        sb.append("  <param>");
                        try {
                            escape(sb, parameters[i].toString());
                        } catch (Exception ex) {
                            sb.append("???");
                        }
                        sb.append("</param>\n");
                    }
                }
            } catch (Exception ex) {
                // The message is not in the catalog.  Drop through.
            }

            if (record.getThrown() != null) {
                // Report on the state of the throwable.
                Throwable th = record.getThrown();
                sb.append("  <exception>\n");
                sb.append("    <message>");
                escape(sb, th.toString());
                sb.append("</message>\n");

                if ((record.getLevel().intValue() == Level.SEVERE.intValue()) || 
                        ((record.getLevel().intValue() <= Level.WARNING.intValue()) && record.shouldLogExceptionStackTrace())) {
                    StackTraceElement[] trace = th.getStackTrace();
                    for (int i = 0; i < trace.length; i++) {
                        StackTraceElement frame = trace[i];
                        sb.append("    <frame>\n");
                        sb.append("      <class>");
                        escape(sb, frame.getClassName());
                        sb.append("</class>\n");
                        sb.append("      <method>");
                        escape(sb, frame.getMethodName());
                        sb.append("</method>\n");
                        // Check for a line number.
                        if (frame.getLineNumber() >= 0) {
                            sb.append("      <line>");
                            sb.append(frame.getLineNumber());
                            sb.append("</line>\n");
                        }
                        sb.append("    </frame>\n");
                    }
                }
                
                sb.append("  </exception>\n");
            }

            sb.append("</record>\n");
            return sb.toString();
        }
    }
}
