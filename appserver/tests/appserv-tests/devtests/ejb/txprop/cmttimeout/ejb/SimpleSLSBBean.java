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

package com.sun.s1asdev.ejb.slsb;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;


public class SimpleSLSBBean
    implements SessionBean
{

    private SessionContext sc;

    public SimpleSLSBBean() {
    }

    public void ejbCreate() throws RemoteException {
	    System.out.println("In SimpleSLSBHome::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	    this.sc = sc;
    }

    public boolean doSomething(int timeout) {
        boolean result = sc.getRollbackOnly();

        try {
            System.out.println("Inside doSomething(" + timeout + ")");
            System.out.println("Before entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
            long deadline = System.currentTimeMillis() + (timeout * 1000);
            for (int n = 1; true; n = n << 1) {
                long now = System.currentTimeMillis();
    
                if (now > deadline) {
                    break;
                }
                result = sc.getRollbackOnly();
                System.out.println("Hello, Sorting for n = " + n
                        + "; time left = " + ((deadline - now) / 1000)
                        + " seconds. result: " + result);
                sortArray(1024);
                try {Thread.sleep(5 * 1000);} catch (Exception ex) {} 
            }

            result = sc.getRollbackOnly();
            System.out.println("After entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }

    public boolean doSomethingAndRollback() {
        boolean result = sc.getRollbackOnly();

        try {
            System.out.println("Inside doSomethingAndRollback()");
            System.out.println("Before entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);

            sc.setRollbackOnly();

            result = sc.getRollbackOnly();
            System.out.println("After entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    private void sortArray(int n) {
        int[] a = new int[n];
        for (int i=0; i < n; i++) {
            for (int j=i+1; j<n; j++) {
                if (a[j] < a[i]) {
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
    }

}
