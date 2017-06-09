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

package com.sun.s1asdev.ejb.ejb30.sfsb.lifecycle.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

@CallbackListener("com.sun.s1asdev.ejb.ejb30.sfsb.lifecycle.ejb.LifecycleListener")
@Stateful
public class SFSBEJB
    implements SessionBean 
{

    private Context envCtx;
    private Context envSubCtx;
    private Context javaCtx;
    private Context javaCompCtx;

    private transient String message;

    private SessionContext              sessionCtx;
    private Context                     initialCtx;

    private String                      sfsbName;
    private String                      envEntryTagValue;
    private SFSBHome                    sfsbHome;
    private SFSB                        sfsbRemote;    
    private SFSBLocalHome               sfsbLocalHome;
    private SFSBLocal                   sfsbLocal;

    private HomeHandle                  homeHandle;
    private Handle                      handle;
    private UserTransaction             userTransaction1;
    private UserTransaction             userTransaction2;

    private int				activationCount;
    private int				passivationCount;
    private Object			nonSerializableState;

    public void ejbCreate(String sfsbName) {
        System.out.println ("In SFSB.ejbCreate() for name -> " + sfsbName);
        this.sfsbName = sfsbName;

        try {
            sfsbHome = (SFSBHome) sessionCtx.getEJBHome();
            sfsbLocalHome = (SFSBLocalHome) sessionCtx.getEJBLocalHome();
            
            homeHandle = sfsbHome.getHomeHandle();
            handle = sessionCtx.getEJBObject().getHandle();
            
            userTransaction1 = sessionCtx.getUserTransaction();
            userTransaction2 = (UserTransaction) new InitialContext().
	    lookup("java:comp/UserTransaction");
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO
        }
    }

    public String getName() {
        System.out.println("In getName() for " + sfsbName);
        return this.sfsbName;
    }

    public boolean checkSessionContext() {
        boolean status = sessionCtx != null;
        status = status && (sessionCtx.getEJBObject() != null);
        return status;
    }

    public boolean checkInitialContext() {
        boolean status = (initialCtx != null);
        return status;
    }

    public boolean checkHomeHandle() {
        boolean status = homeHandle != null;
        try {
            if (status) {
                Object homeRef = homeHandle.getEJBHome();
                SFSBHome h = (SFSBHome)
                    PortableRemoteObject.narrow(homeRef, SFSBHome.class);
                EJBMetaData metaData2 = h.getEJBMetaData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }

        return status;
    }

    public boolean checkHandle() {
        boolean status = handle != null;
        try {
            if (status) {
                Object ref = handle.getEJBObject();
                SFSB ejbRef = (SFSB)
                    PortableRemoteObject.narrow(ref, SFSB.class);
                ejbRef.isIdentical(sessionCtx.getEJBObject());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    public boolean checkUserTransaction() {
        boolean status =
            ((userTransaction1 != null) && (userTransaction2 != null));
        
        try {
            if( status ) {
                userTransaction1.begin();
                userTransaction1.commit();

                userTransaction2.begin();
                userTransaction2.commit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        }

        return status;
    }

    public boolean isOK(String name) {
        String fieldName = "Name";
        boolean ok = name.equals(sfsbName);

        try {
            if (ok) {
                fieldName = "SessionContext";
                ok = sessionCtx != null;
            }

            if (ok) {
                fieldName = "InitialContext";
                ok = initialCtx != null;
            }

            if (ok) {
                fieldName = "checkHomeHandle";
                ok = checkHomeHandle();
            }

            if (ok) {
                fieldName = "checkHandle";
                ok = checkHandle();
            }

            if (ok) {
                fieldName = "checkUserTransaction";
                ok = checkUserTransaction();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }
        
        this.message = (ok) ? null : (fieldName + " not restored properly");
        
        return ok;
    }

    public String getMessage() {
        return this.message;
    }

    public int getActivationCount() {
	return this.activationCount;
    }

    public int getPassivationCount() {
	return this.passivationCount;
    }

    public void makeStateNonSerializable() {
	nonSerializableState = new Object();
    }

    public void sleepForSeconds(int sec) {
	try {
	    Thread.currentThread().sleep(sec * 1000);
	} catch (Exception ex) {
	}
    }

    public void unusedMethod() {
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
        try {
            this.initialCtx = new InitialContext();
            this.javaCtx = (Context) initialCtx.lookup("java:");
            this.javaCompCtx = (Context) initialCtx.lookup("java:comp");
            this.envCtx = (Context) initialCtx.lookup("java:comp/env");
            this.envEntryTagValue = (String) envCtx.lookup("TagValue");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
	throw new EJBException("Container called ejbActivate?????");
    }

    void postActivate() {
        activationCount++;
    }

    public void ejbPassivate() {
	throw new EJBException("Container called ejbPassivate?????");
    }

    @PrePassivate
    public void prePassivate() {
        passivationCount++;
    }

}
