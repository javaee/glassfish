/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.util;

import java.io.InputStream;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.logging.LogManager;

import org.apache.commons.logging.impl.Jdk14Logger;

/**
 * JDO-specific subclass of the apache commons logging Log
 * implementation that wraps the standard JDK 1.4 logging.
 * This class configures the JDK LogManager using a properties file
 * called logging.properties found via the CLASSPATH.
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 1.1
 */
public class JDOJdk14Logger
    extends Jdk14Logger
{
    /** Logging properties file name. */
    public static final String PROPERIES_FILE = "logging.properties"; //NOI18N
    
    /** Indicates whether JDK 1.4 logging has been configured by this class. */
    private static boolean configured = false;
    
    /** I18N support. */
    private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.util.Bundle"); // NOI18N

    /** 
     * Constructor checking whether JDK 1.4 logging should be
     * configuared after calling super constructor. 
     */
    public JDOJdk14Logger(String name) {
        super(name);
        if (!configured) {
            configured = true;
            configureJDK14Logger();
        }
    }

    /** 
     * Configures JDK 1.4 LogManager.
     */
    private void configureJDK14Logger() {
        final LogManager logManager = LogManager.getLogManager();
        final ClassLoader cl = getClass().getClassLoader();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run () {
                try {
                    InputStream config = cl.getResourceAsStream(PROPERIES_FILE);
                    logManager.readConfiguration(config);
                    return null;
                }
                catch (IOException ex) {
                    throw new RuntimeException(
                        msg.msg("EXC_LoggerSetupIOException", //NOI18N
                                PROPERIES_FILE) + ex); 
                }
                catch (SecurityException ex) {
                    throw new RuntimeException(
                        msg.msg("EXC_LoggerSetupSecurityException", // NOI18N
                                PROPERIES_FILE) + ex);
                }
            }
            });
    }
    
}
