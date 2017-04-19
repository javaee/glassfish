package com.sun.s1asdev.ejb.ejb30.persistence.extendedem;

import javax.ejb.Remote;

@Remote
public interface Sful {

    void createPerson(String name);

    Person findPerson();

    boolean removePerson();

    Person nonTxFindPerson();

    boolean refreshAndFindPerson();

}
