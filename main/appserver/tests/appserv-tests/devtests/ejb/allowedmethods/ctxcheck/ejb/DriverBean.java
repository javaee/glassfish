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

package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class DriverBean
    implements SessionBean
{

    private SessionContext sc;

    public DriverBean() {
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In DriverBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void localSlsbGetEJBObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();
            local.doSomethingHere();
            local.accessEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }
        
    public void localSlsbGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("5", "5");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("5");
            local.localEntityGetEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }
        
    public void localEntityGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("6", "6");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("6");
            local.localEntityGetEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("7", "7");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("7");
            local.localEntityGetEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("8", "8");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("8");
            local.localEntityGetEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
    
}
