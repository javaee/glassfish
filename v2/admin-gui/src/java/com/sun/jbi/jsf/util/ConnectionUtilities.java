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
 *  ConnectionUtilities.java
 */

package com.sun.jbi.jsf.util;

import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.jbi.ui.client.JBIAdminCommandsClientFactory;
import com.sun.jbi.ui.common.JBIAdminCommands;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;

/**
 *
 * Utilities supporting a JBI administration common client JMX connection
 *
 **/

public final class ConnectionUtilities
{
     /**
     * Controls printing of diagnostic messages to the log
     */
    private static Logger sLog = JBILogger.getInstance();



    /**
     * obtains a JBI Administration Common Client
     * @return <code>JBIAdminCommands -</code> an implementation of the client interface
     * using a client connected connected to the local JBI runtime.
     */
    public JBIAdminCommands getClient()
    {
    JBIAdminCommands result = null;

    // check for cached copy
    if (null != mJac)
        {
        result = mJac;
        }
    // no cached copy, try to find a client
    else
        {

            mJac = tryLocalClient();
            result = mJac; // cache connection (or null, if none)
        }

    sLog.fine("ConnectionUtilities.getClient(), result=" + result);
    return result;
    }

    /**
     * tries to obtain a local JBI Administration Common Client
     * @return <code>JBIAdminCommands -</code> an implementation of the client interface
     * using a client connected to the local JBI runtime.
     */
    private JBIAdminCommands tryLocalClient()
    {
    JBIAdminCommands result = null;

    MBeanServerConnection defaultConnection =
        MBeanServerFactory.getMBeanServer();

    if (null != defaultConnection)
        {
        try
            {
                final boolean DEFAULT_CONNECTION_REMOTE_FLAG = false;
            result = JBIAdminCommandsClientFactory.getInstance(defaultConnection,
                                       DEFAULT_CONNECTION_REMOTE_FLAG);
            if ((null != result)
                &&(result.isJBIRuntimeEnabled()))
                {
                sLog.fine("ConnectionUtilities.tryLocalClient(): local client found JBI runtime enabled");
                }
            }
        catch (Exception ex)
            {
            sLog.fine("ConnectionUtilities.tryLocalClient(): caught ex=" + ex);
            ex.printStackTrace(System.err);
            }
        }

    sLog.fine("ConnectionUtilities.tryLocalClient(), result=" + result);
    return result;
    }

    /**
     * cached reference to a local JBI Administration common client.
     */
    private JBIAdminCommands mJac = null;
}

