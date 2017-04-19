package com.sun.s1asdev.ejb.txprop.cmttimeout;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;


public class HelloBean implements SessionBean {

    private SessionContext sc;

    public HelloBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In HelloBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void compute(int timeout) {
        System.out.println("hello from HelloBean::compute() " + timeout);
        long deadline = System.currentTimeMillis() + (timeout * 1000);
        for (int n = 1; true; n = n << 1) {
            long now = System.currentTimeMillis();

            if (now > deadline) {
                break;
            }
            System.out.println("Hello, Sorting for n = " + n
                    + "; time left = " + ((deadline - now) / 1000)
                    + " seconds.");
            sortArray(1024);
            try {Thread.sleep(10);} catch (Exception ex) {} 
        }
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
