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


package com.sun.persistence.utility.logging;

/**
 * This interface provides for isolation between the JDO components that need
 * logging services and the implementation of the service. <P> This interface
 * has no JDK 1.4 dependencies.
 * @author Craig Russell
 * @version 1.0
 */

public interface LoggerFactory {

    /**
     * Get a Logger.  The class that implements this interface is responsible
     * for creating a logger for the named component. The bundle name and class
     * loader are passed to allow the implementation to properly find and
     * construct the internationalization bundle.
     * @param loggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    Logger getLogger(String loggerName, String bundleName, ClassLoader loader);
}

