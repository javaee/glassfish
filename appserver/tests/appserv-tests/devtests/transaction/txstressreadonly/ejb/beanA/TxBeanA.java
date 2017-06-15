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

package com.sun.s1peqe.transaction.txstressreadonly.ejb.beanA;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.rmi.RemoteException;
import com.sun.s1peqe.transaction.txstressreadonly.ejb.beanB.*;

public class TxBeanA implements SessionBean {

    private TxRemoteHomeB home = null;
    private UserTransaction tx = null;
    private SessionContext context = null;

    // ------------------------------------------------------------------------
    // Container Required Methods
    // ------------------------------------------------------------------------
    public void ejbCreate() throws RemoteException {
        Class homeClass = TxRemoteHomeB.class;
        System.out.println("ejbCreate in BeanA");
        try {
            Context ic = new InitialContext();
            java.lang.Object obj = ic.lookup("java:comp/env/ejb/TxBeanB");
            home = (TxRemoteHomeB) PortableRemoteObject.narrow(obj, homeClass);
         } catch (Exception ex) {
            System.out.println("Exception in ejbCreate: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanA");
        this.context = sc;
    }

    public void ejbRemove() {
        System.out.println("ejbRemove in BeanA");
    }

    public void ejbDestroy() {
        System.out.println("ejbDestroy in BeanA");
    }

    public void ejbActivate() {
        System.out.println("ejbActivate in BeanA");
    }

    public void ejbPassivate() {
        System.out.println("ejbPassivate in BeanA");
    }


    // ------------------------------------------------------------------------
    // Business Logic Methods
    // ------------------------------------------------------------------------
    public void txCommit(int identity) throws RemoteException {
        System.out.println("txCommit in BeanA");
        try {
            TxRemoteB beanB = home.create();
            beanB.insert("A1001", 3000,identity);
            beanB.remove();
            System.out.println("After beanB Remove call");
        } catch (Exception ex) {
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }
    }


}

