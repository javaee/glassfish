package com.oracle.hk2devtest.isolation1;

import javax.ejb.Remote;

@Remote
public interface Isolation1 {

    public String helloWorld();

}
