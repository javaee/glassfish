package com.acme;

import java.util.Set;
import java.util.HashSet;

import javax.ejb.*;
import javax.annotation.*;
import org.omg.CORBA.ORB;

@Singleton
@Remote(SingletonRemote.class)
@LocalBean
public class SingletonBean {

    boolean passed1 = false;
    boolean passed2 = false;
    Set<String> around_timeouts = new HashSet<String>();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
    }
    
    public void test1Passed() {
	passed1 = true;
    }

    public void test2Passed() {
	passed2 = true;
    }

    public boolean getTestPassed() {
	return passed1 && passed2;
    }

    public boolean getAroundTimeoutCalled(String s) {
        if (s == null) {
            s = "no-arg";
        }
        return around_timeouts.contains(s);
    }

    public void setAroundTimeoutCalled(String s) {
        if (s == null) {
            s = "no-arg";
        }
        around_timeouts.add(s);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
