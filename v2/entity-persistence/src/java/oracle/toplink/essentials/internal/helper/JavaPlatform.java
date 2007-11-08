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
package oracle.toplink.essentials.internal.helper;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.Version;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;

/**
 *  INTERNAL:
 *  JavaPlatform abstracts the version of the JDK we are using.  It allows any operation
 *  which is dependant on JDK version to be called from a single place and then delegates
 *  the call to its JDKPlatform
 *  @see JDPlatform
 *  @author Tom Ware
 */
public class JavaPlatform {
    private static JDKPlatform platform = null;

    // 3 possible states are required for conforming like
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int UNDEFINED = 2;

    /**
     *  INTERNAL:
     *  Get the version of JDK being used from the Version class.
     *  @return JDKPlatform a platform appropriate for the version of JDK being used.
     */
    private static JDKPlatform getPlatform() {
        if (platform == null) {
            if (Version.isJDK15()) {
                try {
                    Class platformClass = null;
                    // use class.forName() to avoid loading the JDK 1.5 class unless it is needed.
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            platformClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName("oracle.toplink.essentials.internal.helper.JDK15Platform"));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        platformClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName("oracle.toplink.essentials.internal.helper.JDK15Platform");
                    }                  
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            platform = (JDKPlatform)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(platformClass));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        platform = (JDKPlatform)PrivilegedAccessHelper.newInstanceFromClass(platformClass);
                    }      
                } catch (Exception exception) {
                }
            }
            if (platform == null) {
                platform = new JDK15Platform();
            }
        }
        return platform;
    }

    /**
     *  INTERNAL:
     *  Conform an expression which uses the operator "like" for an in-memory query
     *  @return int
     *  int FALSE = 0 - if the expression does not conform
     *  int TRUE = 1 - if the expression does conform
     *  int UNDEFINED = 2 - if it cannot be determined if the expression conforms
     */
    public static int conformLike(Object left, Object right) {
        return getPlatform().conformLike(left, right);
    }

    /**
     * INTERNAL:
     * Get the milliseconds from a Calendar.
     * @param calendar the instance of calendar to get the millis from
     * @return long the number of millis
     */
    public static long getTimeInMillis(java.util.Calendar calendar) {
        return getPlatform().getTimeInMillis(calendar);
    }

    /**
     * INTERNAL:
     * Get the Map to store the query cache in
     */
    public static java.util.Map getQueryCacheMap() {
        return getPlatform().getQueryCacheMap();
    }

    /**
     * INTERNAL:
     * Set the milliseconds for a Calendar.
     */
    public static void setTimeInMillis(java.util.Calendar calendar, long millis) {
        getPlatform().setTimeInMillis(calendar, millis);
    }

    /**
     *  INTERNAL:
     *  Set the cause of an exception.  This is useful for JDK 1.4 exception chaining
     *  @param java.lang.Throwable the exception to set the cause for
     *  @param java.lang.Throwable the cause of this exception
     */
    public static void setExceptionCause(Throwable exception, Throwable cause) {
        getPlatform().setExceptionCause(exception, cause);
    }

    /**
     * INTERNAL
     * return a boolean which determines where TopLink should include the TopLink-stored
     * Internal exception in it's stack trace.  For JDK 1.4 VMs with exception chaining
     * the Internal exception can be redundant and confusing.
     * @return boolean
     */
    public static boolean shouldPrintInternalException() {
        return getPlatform().shouldPrintInternalException();
    }
}
