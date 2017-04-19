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
