package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.ejb.Local;

@Local
public interface Finder {
    Person findPerson(String name);
}
