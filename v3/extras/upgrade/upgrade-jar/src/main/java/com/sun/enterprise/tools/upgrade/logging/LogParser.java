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

package com.sun.enterprise.tools.upgrade.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Log parsing code to look for potential error messages.
 */
public class LogParser {

    private static final Logger logger = LogService.getLogger();

    // separator used in GF logs
    private static final String SEP = "|";

    private static final Pattern errorPattern;

    /*
     * Search rule: pattern in log msg (e.g. |WARNING|).
     * Add levels here that should be included, separated
     * by | character.
     */
    static {
        StringBuilder sb = new StringBuilder();
        sb.append(".*\\|(");
        sb.append(Level.SEVERE.getName());
        sb.append("|");
        sb.append(Level.WARNING.getName());
        sb.append(")\\|.*");
        errorPattern = Pattern.compile(sb.toString());
    }


    // Search rule: find pattern of this type (e.g.
    // at java.util.ResourceBundle.getObject(ResourceBundle.java:384))
    private Pattern pattern = Pattern.compile("at .*[(].*:[0-9]*[)]");
    // sequence that ends log msg
    private static final String endToken = SEP + "#]";

    private long startPoint = 0;
    private File logFile;

    public LogParser(File logFile) throws FileNotFoundException, IOException {
        this.logFile = logFile;
        // record the endpoint of the data.
        RandomAccessFile raf = new RandomAccessFile(logFile, "r");
        startPoint = raf.length();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("Parsing file: %s",
                    logFile.getAbsolutePath()));
            logger.fine("File length: " + startPoint);
        }
        raf.close();
    }

    public void setStartPoint(long start){
        if (start < 0){
            startPoint = 0;
        } else {
            startPoint = start;
        }
    }

    /**
     * Iterate through a file and check for messages that could
     * represent some failure, for instance entries logged as
     * Level.SEVERE.
     *
     * @param logFile File object representing the log to parse.
     * @return collected messages.
     */
    public StringBuilder parseLog() throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("Parsing file: %s", logFile.getAbsolutePath()));
        }
        StringBuilder sBuf = new StringBuilder();
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile(logFile, "r");
            // skip past prior log mgs. Process only msg for this startup
            reader.seek(startPoint);
            String line = reader.readLine();
            while (line != null) {
                if (line.length() > 0 ) {
                        if (line.endsWith(endToken)){
                            if (matchErrorPattern(line)){
                                sBuf.append(line).append("\n\n");
                            }
                        } else {
                            // log msg consists of multiple lines.  Get full msg
                            String tmpBuf = getFullMsg(line, reader);

                            // check for and print a stack trace from any log level
                            Matcher matcher = pattern.matcher(tmpBuf);
                            if (matcher.find()) {
                                sBuf.append(tmpBuf).append("\n\n");
                            }
                        }
                }
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return sBuf;
    }


    /**
     * Check msg for log level of interest.
     *
     * @param line
     * @return
     */
    private boolean matchErrorPattern(String line) {
        Matcher matcher = errorPattern.matcher(line);
        if (matcher.find()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("Found error message: %s", line));
            }
            return true;
        }
        return false;
    }


    /**
     * Read a multi-line msg.
     *
     * @param l     inital line of multi-line msg
     * @param reader    utility reading the data
     * @return  The full msg as a string
     * @throws java.io.IOException
     */

    private String getFullMsg(String l, RandomAccessFile reader) throws java.io.IOException {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(l + "\n");

        String line = reader.readLine();
        while (line != null) {
            sbuf.append(line  + "\n");
            if (line.endsWith(endToken)) {
                break;
            }
            line = reader.readLine();
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("multi-line message: %s",
                    sbuf.toString()));
        }
        return sbuf.toString();

    }

}
