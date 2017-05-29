/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;

/** Provides the logging information of all the loggers registered in the VM.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3
 */
public class LogReporter {
    
    private final StringManager sm    = StringManager.getManager(LogReporter.class);
    private final String ROOT_LOGGER  = "root";
    private final String ANON_LOGGER  = "anonymous";

    public String getLoggingReport() throws RuntimeException {
        try {
           StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            LoggingMXBean lb = LogManager.getLoggingMXBean();
            List<String> loggers = lb.getLoggerNames();
            Collections.sort(loggers);
            String lf = System.getProperty("java.util.logging.config.file");
            sb.append(sm.getString("logging.config.file", lf));
            sb.append(sm.getString("reg.loggers", loggers.size()));
            sb.append(sm.getString("logger.details.1"));
            sb.append(sm.getString("logger.details.2"));
            sb.append(sm.getString("list.of.loggers"));
            sb.append("--------------------------------------------------");
            for (String logger : loggers) {
                String ln = (logger == null) ? ANON_LOGGER : logger;
                String parent = lb.getParentLoggerName(logger);
                if (parent == null || parent.length() == 0)
                    parent = ROOT_LOGGER;
                sb.append(ln + "|" + lb.getLoggerLevel(logger) + "|" + parent);
            }
            return (sb.toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
