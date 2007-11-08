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

/*
 *  JBIConstants.java
 */

package com.sun.jbi.jsf.util;

//import com.sun.jbi.jsf.util.JBIConstants;

/**
 *
 * Contstants related to the admin-gui implementation
 *
 **/

public final class SharedConstants
{


    /**
     * deployment, component, library, or target name
     */
    public final static String KEY_NAME        = "name"; // not I18n
    public final static String KEY_TYPE        = "type"; // not I18n
    public static final String STATE_SHUT_DOWN = "Shutdown";  // not I18n
    public static final String STATE_STARTED   = "Started";  // not I18n
    public static final String STATE_STOPPED   = "Stopped";  // not I18n

    
     /**
     * Binding Engine type filtering on the list components page
     */
    public static final String DROP_DOWN_TYPE_SHOW_ALL       = "Show All";  // not I18n
    public static final String DROP_DOWN_TYPE_BINDING        = "Binding";  // not I18n
    public static final String DROP_DOWN_TYPE_ENGINE         = "Engine";  // not I18n


    /**
     * Table Type names.  Values assigned to the session variable sharedTableType
     */
    public static final String COMPONENT_TABLE_TYPE   = "bindingsEngines";
    public static final String DEPLOYMENT_TABLE_TYPE  = "deployment";
    public static final String LIBRARY_TABLE_TYPE     = "libraries";
        

    /**
     * Property alert result keys
     */
    public static final String SUCCESS_RESULT  = "success-result";  // not I18n
    public static final String FAILURE_RESULT  = "failure-result";  // not I18n
    public static final String WARNING_RESULT  = "warning-result";  // not I18n
    public static final String WARNING_SUMMARY = "warning-summary";  // not I18n
    public static final String INTERNAL_ERROR  = "internal-error";  // not I18n


    /**
     * Null value constants used when calling JBICommonClient api's
     */
    public final static String NO_STATE_CHECK      = null;  // not I18n
    public final static String NO_LIBRARY_CHECK    = null;  // not I18n
    public final static String NO_COMPONENT_CHECK  = null;  // not I18n
    public final static String NO_DEPLOYMENT_CHECK = null;  // not I18n

    /**
     * Used when uninstalling components or undeploying service assemblies
     */
    public final static boolean NO_FORCE_DELETE  = false;
    public final static boolean FORCE_DELETE     = true;

}
