package com.oracle.hk2devtest.isolation1;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
@EJB(name="java:app/env/forappclient", beanInterface=Isolation1.class)
public class Isolation1Bean implements Isolation1, Serializable {

    public String helloWorld() {
      return "Hello, World!";
    }

}
