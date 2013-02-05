package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import javax.ejb.Remote;

@Remote
public interface Tester {
    public boolean doTest();
}
