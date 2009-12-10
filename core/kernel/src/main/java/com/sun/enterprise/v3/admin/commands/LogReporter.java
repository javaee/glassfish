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
                assert parent != null;
                if (parent.length() == 0)
                    parent = ROOT_LOGGER;
                sb.append(ln + "|" + lb.getLoggerLevel(logger) + "|" + parent);
                sb.append("--------------------------------------------------");
            }
            return (sb.toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
