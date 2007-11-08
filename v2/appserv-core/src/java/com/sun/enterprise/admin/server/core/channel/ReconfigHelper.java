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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.server.core.channel;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.ExecException;


/**
 * ReconfigHelper executes reconfig command in a sub-process, which triggers
 * refresh within web container.
 */
public class ReconfigHelper {

    private static boolean reconfigEnabled = false;
    private static Class j2eeRunnerClass = null;
    private static Method reconfigMethod = null;

    static void enableWebCoreReconfig() throws ClassNotFoundException,
            NoSuchMethodException, SecurityException {
        findReconfigMethod();
        if (reconfigMethod != null) {
            reconfigEnabled = true;
        }
    }

    /**
     * Send reconfigure message to specified instance
     */
    public static void sendReconfigMessage(String instanceName) {
/*
        int ret = reconfig(instanceName);
        if (ret != 0) {
            AdminChannel.warn(RECONFIG_ERROR);
        }
*/
        if (reconfigEnabled) {
            try {
                reconfigMethod.invoke( null, (Object[])null);
            } catch (IllegalAccessException access) {
                AdminChannel.warn(RECONFIG_ERROR);
                AdminChannel.debug(access);
            } catch (IllegalArgumentException arg) {
                AdminChannel.warn(RECONFIG_ERROR);
                AdminChannel.debug(arg);
            } catch (InvocationTargetException ite) {
                AdminChannel.warn(RECONFIG_ERROR);
                AdminChannel.debug(ite.getTargetException());
                AdminChannel.debug(ite);
            }
        }
    }

    private static int reconfig(String instanceName) {
        int retval = 0;
        String[] cmd = getReconfigCommand(instanceName);
        if (cmd != null) {
            ProcessExecutor pe = new ProcessExecutor(cmd);
            try {
                pe.execute();
            } catch (ExecException ee) {
                AdminChannel.debug(ee);
                retval = 1;
            }
        }
        return retval;
    }

    private static String[] getReconfigCommand(String instance) {
        String pfx = AdminChannel.instanceRoot + File.separator + instance
                + File.separator;
        if (OS.isUnix()) {
            return new String[]{pfx + "reconfig"};
        } else if (OS.isWindows()) {
            return new String[]{pfx + "reconfig.bat"};
        } else {
            return null;
        }
    }

    private static void findReconfigMethod() throws ClassNotFoundException,
            NoSuchMethodException, SecurityException {
        j2eeRunnerClass = Class.forName(J2EE_RUNNER_CLASS);
        reconfigMethod = j2eeRunnerClass.getMethod(RECONFIG_METHOD, (Class[])null);
    }   

    private final static String J2EE_RUNNER_CLASS =
            "com.sun.enterprise.server.J2EERunner";
    private final static String RECONFIG_METHOD = "requestReconfiguration";
    private final static String RECONFIG_ERROR = "channel.reconfig_error";
}
