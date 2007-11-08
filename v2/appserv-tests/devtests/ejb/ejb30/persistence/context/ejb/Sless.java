package com.sun.s1asdev.ejb.ejb30.persistence.context;

import javax.ejb.Remote;

@Remote
public interface Sless {

    void createPerson(String name);

    Person findPerson(String name);
    Person nonTxFindPerson(String name);
    void nonTxTest2(String name);

    void removePerson(String name);

}
