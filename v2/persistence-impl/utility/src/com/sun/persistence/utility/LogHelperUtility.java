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


package com.sun.persistence.utility;

import com.sun.persistence.utility.logging.LogHelper;
import com.sun.persistence.utility.logging.Logger;

/**
 * LogHelper for utility package.
 * @author Dave Bristor
 */
public class LogHelperUtility {
    /**
     * The component name for this component
     */
    private static final String componentName = "utility"; // NOI18N

    /**
     * The class loader for this component
     */
    private static final ClassLoader loader = LogHelperUtility.class.getClassLoader();

    /**
     * The bundle name for this component
     */
    private static final String bundleName = "com.sun.persistence.utility.Bundle"; // NOI18N

    /**
     * Return the logger for this component
     */
    public static Logger getLogger() {
        return LogHelper.getLogger(componentName, bundleName, loader);
    }
}
