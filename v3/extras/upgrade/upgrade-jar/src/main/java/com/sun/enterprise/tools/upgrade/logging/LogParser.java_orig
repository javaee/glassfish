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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Log parsing code to look for potential error messages.
 */
public class LogParser {

    private static final Logger logger =
        LogService.getLogger(LogService.UPGRADE_LOGGER);

    // separator used in GF logs
    private static final String SEP = "|";

    // holds the "upgrade failed" cases
    private static final Set<String> FAIL_LEVELS = new HashSet<String>();

    static {
        FAIL_LEVELS.add(String.format("%s%s%s",
            SEP, Level.SEVERE.getName(), SEP));
        FAIL_LEVELS.add(String.format("%s%s%s",
            SEP, Level.WARNING.getName(), SEP));
    }

    /**
     * Iterate through a file and check for messages that could
     * represent some failure, for instance entries logged as
     * Level.SEVERE.
     *
     * @param logFile File object representing the log to parse.
     * @return True if any potential errors were found
     */
    public boolean parseLog(File logFile) throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("Parsing '%s'",
                logFile.getAbsolutePath()));
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFile));
            String line = reader.readLine();
            while (line != null) {
                if (parseForError(line)) {
                    return true;
                }
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return false;
    }

    /*
     * Checks each line to see if anything was logged at a level
     * that is in the 'FAIL_LEVELS' set. E.g., Level.SEVERE messages
     * might be considered an upgrade failure.
     */
    private boolean parseForError(String line) {
        for (String levelToken : FAIL_LEVELS) {
            if (line.indexOf(levelToken) != -1) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("Found error message: %s",
                        getMessage(line)));
                }
                return true;
            }
        }
        return false;
    }

    /*
     * Utility method for getting the message out of a GF log line.
     * Hopefully there's some code already to handle this.
     */
    private String getMessage(String line) {

        // parse off end
        line = line.substring(0, line.lastIndexOf(SEP));

        // now parse up till message
        line = line.substring(1 + line.lastIndexOf(SEP), line.length());
        
        return line;
    }

}
