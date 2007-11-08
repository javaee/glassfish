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
package oracle.toplink.essentials.platform.server;

import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.logging.*;

/**
 * <p>
 * Basic logging class that provides framework for integration with the application
 * server log. This class is used when messages need to be logged through an application
 * server, e.g. OC4J.
 *
 *  @see SessionLog
 *  @see AbstractSessionLog
 *  @see SessionLogEntry
 *  @see Session
 * </p>
 */
public class ServerLog extends AbstractSessionLog {

    /**
     * PUBLIC:
     * <p>
     * Create and return a new ServerLog.
     * </p>
     */
    public ServerLog() {
        super();
        setLevel(INFO);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a SessionLogEntry
     * </p><p>
     *
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * </p>
     */
    public void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel())) {
            return;
        }

        String message = getSupplementDetailString(entry);

        if (entry.hasException()) {
            message += entry.getException();
        } else {
            message += formatMessage(entry);
        }

        basicLog(entry.getLevel(), message);
    }

    /**
     * <p>
     * Log message to a writer by default.  It needs to be overridden by the subclasses.
     * </p><p>
     *
     * @param level the log request level
     * </p><p>
     * @param message the formatted string message
     * </p>
     */
    protected void basicLog(int level, String message) {
        try {
            printPrefixString(level);
            getWriter().write(message);
            getWriter().write(Helper.cr());
            getWriter().flush();
        } catch (IOException exception) {
            throw ValidationException.logIOError(exception);
        }
    }
}
