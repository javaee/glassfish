
package com.acme;

public class SingletonBean2 {

    private int initTx = -1;

    private void init() {
	System.out.println("In SingletonBean2::init()");
        try {
            javax.naming.InitialContext ic = new javax.naming.InitialContext();
	    javax.transaction.TransactionSynchronizationRegistry tr = 
                 (javax.transaction.TransactionSynchronizationRegistry)
                 ic.lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("In SingletonBean2::init() tx status: " + tr.getTransactionStatus());
            initTx = tr.getTransactionStatus();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void foo() {
	System.out.println("In SingletonBean2::foo()");
    }

    public void foo2() { 
	System.out.println("In SingletonBean2::foo2()");
        if (initTx != 6)
            throw new RuntimeException("initTx is " + initTx);
    }

    public void fooAsync(int sleepSeconds) {
	System.out.println("In SingletonBean2::fooAsync() Sleeping for " +
			   sleepSeconds + " seconds...");
	try {
	    Thread.sleep(sleepSeconds * 1000);
	} catch(Exception e) {
	    e.printStackTrace();
	}
	System.out.println("fooAsync() awoke from Sleep");
    }

    private void destroy() {
	System.out.println("In SingletonBean2::destroy()");
    }

    private void myTimeout() {
	System.out.println("In SingletonBen2::myTimeout()");
    }

}
