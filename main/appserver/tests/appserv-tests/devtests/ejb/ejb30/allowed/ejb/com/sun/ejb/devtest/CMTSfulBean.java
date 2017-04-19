package com.sun.ejb.devtest;

import javax.ejb.Stateful;
import javax.naming.InitialContext;

@Stateful
public class CMTSfulBean
    implements CMTOperation {

    public boolean ping() {
        return true;
    }

    public boolean lookupUserTransaction() {
        boolean result = false;
        try {
            InitialContext ctx = new InitialContext();
            Object obj = ctx.lookup("java:comp/UserTransaction");
            System.out.println("CMT  Also got UT");
        } catch (Exception ex) {
            result = true;
            System.out.println("**CMT Got expected exception");
        }

        return result;
    }

}
