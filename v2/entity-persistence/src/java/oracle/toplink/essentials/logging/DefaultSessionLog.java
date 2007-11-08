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

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;

/**
 * <p><b>Purpose</b>: Default log used for the session when message logging is enabled.
 * The session can log information such as,<ul>
 * <li> all SQL executed
 * <li> informational messages
 * <li> debugging information
 * <li> all exceptions that occur within TopLink
 * </ul>
 * As well information about the message can be logged such as,<ul>
 * <li> the session logging the message
 * <li> the connection executing the SQL
 * <li> the thread in which the log entry occured
 * <li> the exact time (to milliseconds) that the log entry occured
 * <li> the stack trace to the exception
 * </ul>
 * @see SessionLog
 * @see Session#logMessage(String)
 *
 * @author Big Country
 */
public class DefaultSessionLog extends AbstractSessionLog implements Serializable {

    /** The filename associated with this DefaultSessionLog, if it is being written out to a file **/
    protected String fileName;

    /**
     * Represents the Map that stores log levels per the name space strings.
     * The keys are category names. The values are log levels.
     */
    private Map<String, Integer> categoryLogLevelMap = new HashMap();

    /**
     * PUBLIC:
     * Create a new default session log.
     */
    public DefaultSessionLog() {
        super();
        this.level = INFO;
        for (int i = 0; i < loggerCategories.length; i++) {
            String loggerCategory = loggerCategories[i]; 
            categoryLogLevelMap.put(loggerCategory, null);
        }
    }

    /**
     * PUBLIC:
     * Create a new default session log for the given writer.
     */
    public DefaultSessionLog(Writer writer) {
        this();
        this.initialize(writer);
    }

    /**
     * Initialize the log.
     */
    protected void initialize(Writer writer) {
        this.writer = writer;
    }

    
    @Override
    public int getLevel(String category) {
        if(category != null) {
            Integer logLevel = categoryLogLevelMap.get(category);
            // if category-specific log level is not set, use parent level.
            if(logLevel != null) {
                return logLevel.intValue();
            }
        }
        return level;
    }

    @Override
    public void setLevel(int level, String category) {
        if(category == null) {
            this.level = level;
        } else if(categoryLogLevelMap.containsKey(category)) {
            categoryLogLevelMap.put(category, level);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged by the logger
     * with name space built from the given session and category.
     * Return the shouldLog for the given category from
     * </p><p>
     * @return true if the given message level will be logged
     * </p>
     */
    @Override
    public boolean shouldLog(int level, String category) {
        return (getLevel(category) <= level);
    }


    /**
     * INTERNAL:
     * Log the entry.
     * This writes the log entries information to a writer such as System.out or a file.
     * This must be synchronized as it will be called by many threads in three-tier.
     */
    public synchronized void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }

        try {
            printPrefixString(entry.getLevel());
            this.getWriter().write(getSupplementDetailString(entry));

            if (entry.hasException()) {
                if (entry.getLevel() == SEVERE) {
                    entry.getException().printStackTrace(new PrintWriter(getWriter()));
                } else if (entry.getLevel() <= WARNING) {
                    if (shouldLogExceptionStackTrace()) {
                        entry.getException().printStackTrace(new PrintWriter(getWriter()));
                    } else {
                        writeMessage(entry.getException().toString());
                    }
                }
            } else {
                writeMessage(formatMessage(entry));
            }
            getWriter().write(Helper.cr());
            getWriter().flush();
        } catch (IOException exception) {
            throw ValidationException.logIOError(exception);
        }
    }

    /**
     * PUBLIC:
     * Set the writer that will receive the
     * formatted log entries for a file name.
     */
    public void setWriter(String aFileName) {
        if (aFileName != null) {
            try {
                this.writer = new FileWriter(aFileName);
                this.fileName = aFileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * PUBLIC:
     * For the given writer, return it's associated filename.
     * If associated writer does not have a filename, return null.
     */
    public String getWriterFilename() {
        return fileName;
    }

    /**
     * Append the specified message information to the writer.
     */
    protected void writeMessage(String message) throws IOException {
        this.getWriter().write(message);
    }

    /**
     * Append the separator string to the writer.
     */
    protected void writeSeparator() throws IOException {
        this.getWriter().write("--");
    }
}
