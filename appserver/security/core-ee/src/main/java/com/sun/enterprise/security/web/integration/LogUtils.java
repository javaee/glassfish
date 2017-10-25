/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.web.integration;

import java.util.logging.Logger;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Kokil Jain
 */

public class LogUtils {
private static final String LOGMSG_PREFIX = "AS-SECURITY";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "com.sun.enterprise.security.web.integration.LogMessages";

    @LoggerInfo(subsystem = "SECURITY", description = "Core-ee Security Logger", publish = true)
    public static final String LOG_DOMAIN = "javax.enterprise.system.core.security.web";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }
    
    @LogMessageInfo(
            message = "Exception while getting the CodeSource",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String EJBSM_CODSOURCEERROR = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(
            message = "[Web-Security] WebSecurityManager - Exception while getting the PolicyFactory",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String JACCFACTORY_NOTFOUND = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(
            message = "[Web-Security] setPolicy SecurityPermission required to call PolicyContext.setContextID",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String SECURITY_PERMISSION_REQUIRED = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(
            message = "[Web-Security] Unexpected Exception while setting policy context",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String POLICY_CONTEXT_EXCEPTION = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(
            message = "JACC: For the URL pattern {0}, all but the following methods have been excluded: {1}",
            level = "INFO",
            cause = "unknown",
            action = "unknown")
    public static final String NOT_EXCLUDED_METHODS = LOGMSG_PREFIX + "-00005";

    @LogMessageInfo(
            message = "JACC: For the URL pattern {0}, the following methods have been excluded: {1}",
            level = "INFO",
            cause = "unknown",
            action = "unknown")
    public static final String EXCLUDED_METHODS = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(
            message = "JACC: For the URL pattern {0}, all but the following methods were uncovered: {1}",
            level = "WARNING",
            cause = "unknown",
            action = "unknown")
    public static final String NOT_UNCOVERED_METHODS = LOGMSG_PREFIX + "-00007";

    @LogMessageInfo(
            message = "JACC: For the URL pattern {0}, the following methods were uncovered: {1}",
            level = "WARNING",
            cause = "unknown",
            action = "unknown")
    public static final String UNCOVERED_METHODS = LOGMSG_PREFIX + "-00008"; 

}
