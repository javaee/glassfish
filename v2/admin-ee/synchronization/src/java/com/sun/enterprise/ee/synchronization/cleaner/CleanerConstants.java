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
package com.sun.enterprise.ee.synchronization.cleaner;

import com.sun.enterprise.web.Constants;

/**
 * Synchronization cleaner constants.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class CleanerConstants {

    /**
     * list of file names excluded from the cleaner
     */
    public static final String[] EXCLUDE_LIST  = {

        // contains cookie information for the synchronization cleaner
        ".com_sun_appserv_cleaner_cookie",

        // contains the inventory of central repository
        ".com_sun_appserv_inventory",

        // contains GC targets computed in DAS
        ".com_sun_appserv_inventory_gc_targets",

        // trash file used by synchronization cleaner
        ".com_sun_appserv_trash",

        // Time stamp file for a synchronization request. This contains
        // the time stamp of DAS during synchronization response.
        ".configdir.timestamp",
        ".domain.xml.timestamp",
        ".keyfile.timestamp",
        ".timestamp",
        ".com_sun_appserv_timestamp",

        // seed for admin channel
        "admsn",

        // stub file for admin channel
        "admch",

        // secure seed generated at startup 
        "secure.seed",

        "secmod.db",

        "tldCache.ser",

        // For each module declared to be the default webmodule of a virtual
        // server, web-container creates a corresponding 
        // "__default-<web-module>" web module. This web module is registered
        // at the virtual server's root context.
        // If a virtual server specifies only a docroot in domain.xml (as 
        // in the case for the virtual server named "server"), web container
        // creates a dummy web module named "__default-web-module-<name_of_vs>"
        // and registers it at the virtual server's root context.
        Constants.DEFAULT_WEB_MODULE_PREFIX,

        // Contains info about last known time when timers could have been
        // delivered for a particular server instance. This info is not
        // currently used since the default implementation now writes 
        // last delivery time to the DB every time ejbTimeOut is called.
        "__timer_service_shutdown__.dat"
    };

    /** System property used to define the do not remove list */
    static final String DO_NOT_REMOVE_LIST="com.sun.appserv.doNotRemoveList";
}
