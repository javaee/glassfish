package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.ejb.Local;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;

@Local({Hello1.class})
@Stateless public class HelloStateless extends HelloStatelessSuper implements Hello1 {

    @Resource TimerService timerSvc;
    private boolean initialized = false;

    @PostConstruct public void create() {
        System.out.println("in HelloStateless:create");
        initialized = true;
    }

    public void hello(String s) {
        if( !initialized ) {
            throw new EJBException("not initialized");
        }
        
        System.out.println("HelloStateless: " + s);
        timerSvc.createTimer(1, "quick timer"); 
        System.out.println("Created quick timer");
    }

}
