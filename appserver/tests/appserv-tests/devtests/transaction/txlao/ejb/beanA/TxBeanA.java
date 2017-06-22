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

package com.sun.s1peqe.transaction.txlao.ejb.beanA;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.rmi.RemoteException;
import com.sun.s1peqe.transaction.txlao.ejb.beanB.*;

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
    public boolean firstXAJDBCSecondNonXAJDBC() throws RemoteException {
        boolean result = false;
        System.out.println("firstXAJDBCSecondNonXAJDBC in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }
            tx.begin();
            beanB.firstXAJDBCSecondNonXAJDBC("A1001", 3000);
            tx.commit();

            tx.begin();
            result = beanB.verifyResults("A1001", "DB1", "XA");
            result = result && beanB.verifyResults("A1001", "DB2", "NonXA");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    public boolean firstNonXAJDBCSecondXAJDBC() throws RemoteException {
        boolean result = false;
        System.out.println("firstNonXAJDBCSecondXAJDBC in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.firstNonXAJDBCSecondXAJDBC("A1002", 3000);
            tx.commit();

            tx.begin();
            result = beanB.verifyResults("A1002", "DB1", "NonXA");
            result = result && beanB.verifyResults("A1002", "DB2", "XA");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstNonXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    public boolean firstXAJDBCSecondXAJDBC() throws RemoteException {
        boolean result = false;
        System.out.println("firstXAJDBCSecondXAJDBC in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.firstXAJDBCSecondXAJDBC("A1003", 3000);
            tx.commit();

            tx.begin();
            result = beanB.verifyResults("A1003", "DB1", "XA");
            result = result && beanB.verifyResults("A1003", "DB2", "XA");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    public boolean firstNonXAJDBCSecondNonXAJDBC() throws RemoteException {
       boolean result = false;
        System.out.println("firstNonXAJDBCSecondNonXAJDBC in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.firstNonXAJDBCSecondNonXAJDBC("A1004", 3000);
            tx.commit();

            tx.begin();
            result = beanB.verifyResults("A1004", "DB1", "NonXA");
            result = result && beanB.verifyResults("A1004", "DB2", "NonXA");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
            result = true;
        }
        return result;
    }

    public boolean firstXAJMSSecondNonXAJDBC() throws RemoteException {
        boolean result = false;
        System.out.println("firstXAJMSSecondNonXAJDBC in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.firstXAJMSSecondNonXAJDBC("JMS Message-1","A1005", 3000);
            tx.commit();
             System.out.println("beanA:firstXAJMSSecondNonXAJDBC:verifying..");
            tx.begin();
            result = beanB.verifyResults("A1005", "DB2", "NonXA");
                System.out.println("beanA:firstXAJMSSecondNonXAJDBC:A1005+result.."+result);
            result = result && beanB.verifyResults("JMS Message-1", "JMS", "");
                System.out.println("beanA:firstXAJMSSecondNonXAJDBC:JMS+result.."+result);
            tx.commit();

            System.out.println("beanA:firstXAJMSSecondNonXAJDBC:verification over");
            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstXAJMSSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    public boolean firstNonXAJDBCOnly() throws RemoteException {
        boolean result = false;
        System.out.println("firstNonXAJDBCOnly in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.firstNonXAJDBCOnly("A1006", 3000);
            tx.commit();

            tx.begin();
            result = beanB.verifyResults("A1006", "DB1", "NonXA");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in firstNonXAJDBCOnly: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }
    public void cleanup() throws RemoteException {
        System.out.println("cleanup in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.delete("A1001");
            tx.commit();
            tx.begin();
            beanB.delete("A1002");
            tx.commit();
            tx.begin();
            beanB.delete("A1003");
            tx.commit();
            tx.begin();
            beanB.delete("A1004");
            tx.commit();
            tx.begin();
            beanB.delete("A1005");
            tx.commit();
            tx.begin();
            beanB.delete("A1006");
            tx.commit();

            beanB.remove();
        } catch (Exception ex) {
            try{
                if(tx != null)
                tx.rollback();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exception in cleanup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public boolean rollbackXAJDBCNonXAJDBC()  throws RemoteException{
      boolean result = true;
        System.out.println("txRollback in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.rollbackXAJDBCNonXAJDBC("A1007", 8000);
            tx.commit();

            result = !beanB.verifyResults("A1007", "DB1", "XA");
            beanB.remove();
        } catch (Throwable ex) {
            System.out.println("Exception in txRollback: " + ex.toString());
            ex.printStackTrace();
            try{
                if(tx != null)
                tx.rollback();
            }catch(Throwable e) {
                e.printStackTrace();
            }
            result = true;
        }
        return result;
    }
    public boolean rollbackNonXAJDBCXAJDBC() throws RemoteException {
        boolean result = true;
        System.out.println("txRollback in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.rollbackXAJDBCNonXAJDBC("A1008", 8000);
            tx.commit();

            result = !beanB.verifyResults("A1008", "DB1", "NonXA");
            beanB.remove();
        } catch (Throwable ex) {
            System.out.println("Exception in txRollback: " + ex.toString());
            ex.printStackTrace();
            try{
                if(tx != null)
                tx.rollback();
            }catch(Throwable e) {
                e.printStackTrace();
            }
            result = true;
        }
        return result;
    }

    public boolean txCommit() throws RemoteException {
        boolean result = true;;
        /*System.out.println("txCommit in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.delete("A1000");
            beanB.insert("A1001", 3000);
            beanB.sendJMSMessage("JMS Message-1");
            beanB.insert("A1002", 5000);
            tx.commit();

            result = beanB.verifyResults("A1002", "DB1");
            result = result && beanB.verifyResults("A1002", "DB2");
            result = result && beanB.verifyResults("JMS Message-1", "JMS");

            beanB.remove();
        } catch (Exception ex) {
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }       */
        return result;
    }

    public boolean txRollback() throws RemoteException {
        boolean result = true;
        /*System.out.println("txRollback in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }
     
            tx.begin();
            beanB.delete("A1001");
            beanB.insert("A1003", 8000);
            beanB.sendJMSMessage("JMS Message-2");
            tx.rollback();

            result = !beanB.verifyResults("A1003", "DB1");
            result = result && !beanB.verifyResults("A1003", "DB2");
            result = result && !beanB.verifyResults("JMS Message-2", "JMS");

            beanB.remove();
        } catch (Exception ex) {
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }  */
        return result;
    }
}

