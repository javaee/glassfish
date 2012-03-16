package com.sun.s1asdev.ejb32.ejblite.timer;

import javax.ejb.*;
import javax.annotation.*;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FooBMT extends TimerStuffImpl implements Foo {

    @Resource private SessionContext sc;

    @Timeout
    @Schedule(second="*/2", minute="*", hour="*", info="Automatic BMT", persistent=false)
    public void timeout(Timer t) {
        try {
            System.out.println("In FooBMT::Timeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooBMT::Timeout -> " 
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleTimeout threw exception");
            e.printStackTrace();
        }

    }

    @PostConstruct
    private void init() throws EJBException {
	System.out.println("In ejblite.timer.Foo::init !!");
        isBMT = true;
        setContext(sc);
        getTimerService("init", true);
        doTimerStuff("init", false);
    }

    @PreDestroy
    public void remove() throws EJBException {
        System.out.println("In FooBMT::remove");
        getTimerService("remove", true);
        doTimerStuff("remove", false);
    }

}
