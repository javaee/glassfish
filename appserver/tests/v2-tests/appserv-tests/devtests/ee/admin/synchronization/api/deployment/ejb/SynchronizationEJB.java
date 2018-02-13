/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.admin.ee.synchronization.api.deployment;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.io.IOException;

import java.rmi.RemoteException;

import com.sun.enterprise.ee.synchronization.api.SynchronizationClient;
import com.sun.enterprise.ee.synchronization.api.SynchronizationFactory;
import com.sun.enterprise.ee.synchronization.SynchronizationException;
import com.sun.enterprise.ee.synchronization.api.ApplicationsMgr;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigContext;

public class SynchronizationEJB
    implements SessionBean 
{
	private SessionContext context;
    private Context initialCtx;

	public void ejbCreate() {
    }

    public boolean getFile(String instanceName, String sourceFile, 
	String destLoc) {
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().
                    getConfigContext();
            ApplicationsMgr appSynchMgr = SynchronizationFactory.
                createSynchronizationContext(ctx).getApplicationsMgr();

            appSynchMgr.synchronize(sourceFile);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean get(String instanceName, String name, String type, 
        String destLoc) {
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().
                    getConfigContext();
            ApplicationsMgr appSynchMgr = SynchronizationFactory.
                createSynchronizationContext(ctx).getApplicationsMgr();

            if (type == null) { 
                return false;
            }

            if ( "J2EEApplication".equals(type) ) {
                appSynchMgr.synchronizeJ2EEApplication(name);
                return true;
            }

            if ( "WebModule".equals(type) ) {
                appSynchMgr.synchronizeWebModule(name);
                return true;
            }

            if ( "EJBModule".equals(type) ) {
                appSynchMgr.synchronizeEJBModule(name);
                return true;
            }

            if ( "ConnectorModule".equals(type) ) {
                appSynchMgr.synchronizeConnectorModule(name);
                return true;
            }

            if ( "AppclientModule".equals(type) ) {
                appSynchMgr.synchronizeAppclientModule(name);
                return true;
            }

            if ( "LifecycleModule".equals(type) ) {
                appSynchMgr.synchronizeLifecycleModule(name);
                return true;
            }

            return false;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean putFile(String instanceName, String sourceFile, 
	String destDir)  {
        try {
            SynchronizationClient sc = 
              SynchronizationFactory.createSynchronizationClient( instanceName);
            sc.connect();
            String s = sc.put(sourceFile, destDir);
            sc.disconnect();
            System.out.println("Upload file at " + s);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	public void setSessionContext(SessionContext sc) {
		this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
	}

	public void ejbRemove() {}

	public void ejbActivate() {
        System.out.println ("In SFSB.ejbActivate() " );
    }

	public void ejbPassivate() {
        System.out.println ("In SFSB.ejbPassivate() ");
    }
}
