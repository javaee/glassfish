package com.sun.s1asdev.ejb.ejb30.interceptors.session;

public class WrongResultException
    extends Exception {

    WrongResultException() {
        super();
    }

    WrongResultException(String str) {
        super(str);
    }

    WrongResultException(String msg, Throwable th) {
        super(msg, th);
    }
}
