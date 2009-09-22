package com.sun.ejb.devtest;

import javax.ejb.Stateless;
import javax.ejb.EJB;

@Stateless
public class SlessBean
    implements Sless {

    @EJB
    private BMTOperation bmtOp;

    @EJB
    private CMTOperation cmtOp;

    public String sayHello() {
	return "Hello";
    }

    public boolean lookupUserTransactionFromCMTBean() {
	return cmtOp.lookupUserTransaction();
    }

    public boolean lookupUserTransactionFromBMTBean() {
	return bmtOp.lookupUserTransaction();
    }

}
