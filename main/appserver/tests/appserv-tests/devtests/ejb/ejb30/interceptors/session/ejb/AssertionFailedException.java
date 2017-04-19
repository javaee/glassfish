package com.sun.s1asdev.ejb.ejb30.interceptors.session;

public class AssertionFailedException
    extends Exception {

    AssertionFailedException() {
        super();
    }

    AssertionFailedException(String str) {
        super(str);
    }

    AssertionFailedException(String msg, Throwable th) {
        super(msg, th);
    }
}
