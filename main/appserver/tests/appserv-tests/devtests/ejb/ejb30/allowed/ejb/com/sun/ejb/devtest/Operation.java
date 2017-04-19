package com.sun.ejb.devtest;

import javax.ejb.Local;

@Local
public interface Operation {

    public boolean ping();

    public boolean lookupUserTransaction();

}

