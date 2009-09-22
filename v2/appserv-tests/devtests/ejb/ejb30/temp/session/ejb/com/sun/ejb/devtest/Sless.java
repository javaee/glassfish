package com.sun.ejb.devtest;

import javax.ejb.Remote;

@Remote
public interface Sless {

    public String sayHello();

    public boolean lookupUserTransactionFromCMTBean();

    public boolean lookupUserTransactionFromBMTBean();

}

