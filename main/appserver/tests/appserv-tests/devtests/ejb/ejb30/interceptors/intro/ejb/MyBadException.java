package com.sun.s1asdev.ejb.ejb30.interceptors.intro;

public class MyBadException
    extends Exception {

    MyBadException() {
        super();
    }

    MyBadException(String str) {
        super(str);
    }

    MyBadException(String msg, Throwable th) {
        super(msg, th);
    }
}
