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
package org.glassfish.admin.mbeanserver;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

import org.glassfish.api.Startup;
import org.glassfish.api.Async;

import org.jvnet.hk2.component.Habitat;

/**
    Manages the startup of the minimal but critical items for AMX support eg
    the AMXBooter MBean and the JMXConnectors.
 */
final class AMXBooterMgr
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private final MBeanServer         mMBeanServer;
    private final Habitat             mHabitat;
    private volatile Booter           mAMXBooter;
    private final ConnectorsStarter   mConnectorsStarter;
    
    public AMXBooterMgr( final Habitat habitat, final MBeanServer mbeanServer)
    {
        //debug( "AppserverMBeanServerFactory.AppserverMBeanServerFactory" );
        mMBeanServer    = mbeanServer;
        mHabitat        = habitat;
        
        // create the connectors.  Use a thread since this network stuff could take time
        mConnectorsStarter = new ConnectorsStarter( mMBeanServer );
        
        mAMXBooter = Booter.create( mHabitat, mMBeanServer );
        
        new ConnectorsStarterThread(mConnectorsStarter).start();
        
        //debug( "AppserverMBeanServerFactory: MBeanServer, AMX booter and JMXConnector started" );
    }
    
    private static final class ConnectorsStarterThread extends Thread
    {
        private final ConnectorsStarter mConnectorsStarter;
        public ConnectorsStarterThread( final ConnectorsStarter cs ) { mConnectorsStarter = cs; }
        
        public void run()
        {
            try
            {
                mConnectorsStarter.startConnectors();
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
        }
    }
}











