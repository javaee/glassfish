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

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * Manages the cookie for the cleaner thread.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class CookieHandler {

    CookieHandler() {
        // server instance root
        String iRoot = System.getProperty(
            SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

        // cookie file
        _cookieFile = new File(iRoot + File.separator
                    + PEFileLayout.GENERATED_DIR + File.separator + COOKIE);
        
        String wp = System.getProperty(COOKIE_ENV_PROPERTY, DEF_WAIT_PERIOD);
        _waitPeriod = Long.parseLong(wp);
    }

    /**
     * Returns true if cookie time expires.
     *
     * @return   true if cookie time expires
     */
    boolean isExpired() {
        boolean expired   = false;
        long currentTime  = System.currentTimeMillis();
        long cookieTime   = getCookie();

        if ( (currentTime-cookieTime) > _waitPeriod ) {
            expired = true;
        }

        return expired;
    }
            
    /**
     * Returns the timestamp from the cookie.
     *
     * @return   cookie time stamp
     */
    long getCookie() {
        long time = 0;

        BufferedReader is = null;
        try {
            if (_cookieFile.exists()) {
                is = new BufferedReader( new FileReader(_cookieFile) );
                time = Long.parseLong(is.readLine());
                is.close();
                is = null;
            } else {
                // create the directories
                _cookieFile.getParentFile().mkdirs();

                updateCookie();
            }

        } catch (Exception e) {
            // ignore
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) { }
            }
        }

        return time;
    }

    /**
     * Updates the cookie with current system timestamp.
     */
    void updateCookie() {

        FileWriter fw = null;

        try {
            // current system time
            long time = System.currentTimeMillis();

            // writes the timestamp to the cookie file
            fw = new FileWriter(_cookieFile);
            fw.write(Long.toString(time));
            fw.flush();
            fw.close();
            fw = null;
        } catch (Exception e) {
            // ignore
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) { }
            }
        }
    }

    // ---- INSTANCE VARIABLE(S) --------------------------------
    private static File _cookieFile       = null;
    private static long _waitPeriod       = 0;
    private static final String COOKIE    = ".com_sun_appserv_cleaner_cookie";
    private static final String DEF_WAIT_PERIOD = "60000";
    private static final String COOKIE_ENV_PROPERTY = 
                            "com.sun.appserv.cleaner.cookie";
}
