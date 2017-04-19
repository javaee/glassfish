package com.acme;

import javax.ejb.Local;

@Local
public interface StatefulLocal {

    public void hello();

}