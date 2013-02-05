package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb;

import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface Tester {
    Map<String, Boolean> doTest();
}
