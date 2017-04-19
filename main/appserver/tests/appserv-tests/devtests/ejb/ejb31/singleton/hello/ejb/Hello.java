package com.acme;

public interface Hello {

    String hello();

    @javax.ejb.Asynchronous
    void async();

    void test_Err_or(String s1, String s2);

}
