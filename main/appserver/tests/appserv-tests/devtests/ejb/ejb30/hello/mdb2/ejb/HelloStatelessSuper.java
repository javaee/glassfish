package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import javax.ejb.*;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.naming.InitialContext;

public class HelloStatelessSuper {

    static boolean timeoutHappened = false;

    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void mytimeoutmethod(Timer t) {

        System.out.println("In HelloStatelessSuper:mytimeoutmethod");

        try {
            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
                new InitialContext().lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in mytimeoutmethod()");
                
                timeoutHappened = true;
                
            } else {
                System.out.println("Invalid tx status for TX_NOT_SUPPORTED" +
                                   " method " + txStatus);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }



    }


}
