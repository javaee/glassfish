package com.sun.s1asdev.ejb31.ejblite.timer;

@javax.ejb.Local
public interface StatefulWrapper {

    public boolean doFooTest(boolean bmt);

    public void removeFoo() throws java.rmi.RemoteException, javax.ejb.RemoveException;
}
