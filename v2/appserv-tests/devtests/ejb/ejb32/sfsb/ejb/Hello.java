package com.acme;

@javax.ejb.Remote
public interface Hello {

    String test(String value, int count);

    void testRemove();
}
