package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

//@javax.ejb.Remote
public interface StatefulWrapper {

    public boolean doMessageDrivenTest(String jndiName, boolean jms);

    public boolean doFooTest(String jndiName, boolean jms);

    public void removeFoo() throws java.rmi.RemoteException, javax.ejb.RemoveException;
}
