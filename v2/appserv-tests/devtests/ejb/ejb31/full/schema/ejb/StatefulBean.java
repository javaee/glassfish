package com.acme;

public class StatefulBean {

    public void foo() {}

    public void afterBegin() {
	System.out.println("In StatefulBean::afterBegin");
    }


    private void beforeCompletion() {
	System.out.println("In StatefulBean::beforeCompletion");
    }


    void afterCompletion(boolean committed) {
	System.out.println("In StatefulBean::afterCompletion c = " +
			   committed);
    }

}