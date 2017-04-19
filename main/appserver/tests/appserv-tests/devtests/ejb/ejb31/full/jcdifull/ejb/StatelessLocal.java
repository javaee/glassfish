package com.acme;

import javax.ejb.Local;

@Local
public interface StatelessLocal {

    public void hello();

}