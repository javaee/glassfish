package com.acme;

import javax.ejb.*;
import javax.annotation.*;

public class StatefulBean {

    @EJB private StatelessBean slsb;    

    public void foo() {

	slsb.foo();
	
    }

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