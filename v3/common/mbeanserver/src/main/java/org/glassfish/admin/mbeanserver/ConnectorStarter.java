/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import javax.management.MBeanServer;

import javax.management.remote.*;
import javax.security.auth.*;

import java.io.IOException;

import org.glassfish.internal.api.AdminAccessController;
import org.jvnet.hk2.component.*;

/**
Start and stop JMX connectors, base class.
 */
abstract class ConnectorStarter
{
    protected static void debug(final String s)
    {
        System.out.println(s);
    }
    protected final MBeanServer mMBeanServer;
    protected final String mAddress;
    protected final int mPort;
    protected final String mAuthRealmName;
    protected final boolean mSecurityEnabled;
    private   final Habitat mHabitat;
    protected final BootAMXListener mBootListener;
    protected volatile JMXServiceURL mJMXServiceURL = null;
    protected volatile JMXConnectorServer mConnectorServer = null;


    public JMXServiceURL getJMXServiceURL()
    {
        return mJMXServiceURL;
    }
    
    public String hostname()
    {
        if ( mAddress.equals("") || mAddress.equals("0.0.0.0") )
        {
            return Util.localhost();
        }
        
        return mAddress;
    }


    ConnectorStarter(
        final MBeanServer mbeanServer,
        final String address,
        final int port,
        final String authRealmName,
        final boolean securityEnabled,
        final Habitat habitat,
        final BootAMXListener bootListener)
    {
        mMBeanServer = mbeanServer;
        mAddress = address;
        mPort = port;
        mAuthRealmName = authRealmName;
        mSecurityEnabled = securityEnabled;
        mHabitat = habitat;
        mBootListener = bootListener;


        if (securityEnabled)
        {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting security");
        }

    }


    abstract JMXConnectorServer start() throws Exception;

    public JMXAuthenticator getAccessController() {

        // we return a proxy to avoid instantiating the jmx authenticator until it is actually
        // needed by the system.
        return new JMXAuthenticator() {

            /**
             * We actually wait for the first authentication request to delegate/
             * @param credentials
             * @return
             */
            public Subject authenticate(Object credentials) {
                // lazy init...
                // todo : lloyd, if this becomes a performance bottleneck, we should cache
                // on first access.
                JMXAuthenticator controller = mHabitat.getByContract(JMXAuthenticator.class);
                return controller.authenticate(credentials);
            }
        };
    }



    public synchronized void stop()
    {
        try
        {
            mConnectorServer.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    static protected void ignore(Throwable t)
    {
        // ignore
    }
}







