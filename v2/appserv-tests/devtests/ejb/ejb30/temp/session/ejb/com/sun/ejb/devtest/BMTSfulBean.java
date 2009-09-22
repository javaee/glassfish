package com.sun.ejb.devtest;

import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import javax.naming.InitialContext;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateful
public class BMTSfulBean
    implements BMTOperation {

    public boolean ping() {
        return true;
    }

    public boolean lookupUserTransaction() {
        boolean result = false;
        try {
            (new InitialContext()).lookup("java:comp/UserTransaction");
            result = true;
        } catch (Exception ex) {
            System.out.println("I am a BMT bean but couldn't lookup UTx");
        }

        return result;
    }

}
