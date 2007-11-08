package com.sun.ejb.devtest;

import javax.ejb.Remote;

@Remote
public interface Sless30 {

    public String sayHello();

    public boolean wasEjbCreateCalled();

}

