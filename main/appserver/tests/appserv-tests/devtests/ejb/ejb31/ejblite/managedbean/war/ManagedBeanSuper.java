package com.acme;

public class ManagedBeanSuper {

    public void newInterceptorInstance() {}

    public int getNumInstances() { return 0; }

    public int getNumInterceptorInstances() { return 0; }

    public String getAroundInvokeSequence() { return ""; }

    public String throwAppException() throws AppException {
	throw new AppException();
    }

    public String throwIllegalArgumentException() {
	throw new IllegalArgumentException();
    }

					     
}