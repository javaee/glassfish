package com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation;

import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface Sful {

    void setName(String name);

    Map<String, Boolean> doTests();

}
