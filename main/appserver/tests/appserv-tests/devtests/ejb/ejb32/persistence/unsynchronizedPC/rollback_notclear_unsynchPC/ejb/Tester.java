package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.ejb;

import javax.ejb.Remote;

@Remote
public interface Tester {
    boolean doTest();
}
