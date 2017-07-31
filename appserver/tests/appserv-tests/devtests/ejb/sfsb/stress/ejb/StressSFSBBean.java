/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.sfsb.stress.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

public class StressSFSBBean
    implements SessionBean 
{
    private SessionContext              sessionCtx;
    private Context                     initialCtx;
    private String                      name;

    public void ejbCreate(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSameName(String name) {
        return  this.name.equals(name);
    }

    public void ping() {
    }

    public boolean doWork(long millis) {
	return true;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        //System.out.println ("In SFSB.ejbActivate() for name -> " + sfsbName);
    }

    public void ejbPassivate() {
        //System.out.println ("####In SFSB.ejbPassivate() for: " + sfsbName);
    }

    /*
    private boolean lookupEntityHome() {
        boolean status = false;
        try {
            Object homeRef = initialCtx.lookup("java:comp/env/ejb/SimpleEntityHome");
            this.entityHome = (SimpleEntityHome)
                PortableRemoteObject.narrow(homeRef, SimpleEntityHome.class);

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    private boolean lookupEntityLocalHome() {
        boolean status = false;
        try {
            Object homeRef = envSubCtx.lookup("SimpleEntityLocalHome");
            this.entityLocalHome = (SimpleEntityLocalHome) homeRef;

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }
    */
}
