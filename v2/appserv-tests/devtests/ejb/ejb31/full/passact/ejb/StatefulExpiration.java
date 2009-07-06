package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.*;

@Stateful
@StatefulTimeout(value=5, unit=TimeUnit.SECONDS)
public class StatefulExpiration {

    @PostConstruct
    public void init() {
	System.out.println("In StatefulExpiration::init()");
    }

    public String hello() {
	System.out.println("In StatefulExpiration::hello()");
	return "hello, world\n";
    }

    @PreDestroy
    public void destroy() {
	System.out.println("In StatefulExpiration::destroy()");
    }


}