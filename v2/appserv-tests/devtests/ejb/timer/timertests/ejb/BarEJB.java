package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.*;

public abstract class BarEJB 
    extends TimerStuffImpl 
    implements EntityBean, TimedObject 
{

    private EJBContext context_;

    //
    // CMP fields
    //

    // primary key

    public abstract Long getId();      
    public abstract void setId(Long timerId);

    public abstract String getValue2();
    public abstract void setValue2(String value2);

/*
    public abstract Timer getTimer();
    public abstract void setTimer(Timer t);
*/
    public BarEJB(){}

    public void ejbTimeout(Timer t) {

        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In BarEJB::ejbTimeout --> " + t.getInfo());
        } catch(Exception e) {
            System.out.println("got exception while calling getInfo");
            e.printStackTrace();
        }

        try {
            handleEjbTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
        }

    }

    public BarPrimaryKey ejbCreate(Long id, String value2) 
        throws CreateException {
        String methodName = "ejbCreate";
        System.out.println("In BarEJB::ejbCreate");
//PG->        super.setupJmsConnection();
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        setId(id);
        setValue2(value2);
        return new BarPrimaryKey(id, value2);
    }

    public BarPrimaryKey ejbCreateWithTimer(Long id, String value2) 
        throws CreateException {
        String methodName = "ejbCreateWithTimer";
        System.out.println("In BarEJB::ejbCreateWithTimer");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        setId(id);
        setValue2(value2);
        return new BarPrimaryKey(id, value2);
    }
    
    public void ejbPostCreate(Long id, String value2) throws CreateException {
        String methodName = "ejbPostCreate";
        System.out.println("In BarEJB::ejbPostCreate");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }

    public void ejbPostCreateWithTimer(Long id, String value2) throws CreateException {
        String methodName = "ejbPostCreateWithTimer";
        System.out.println("In BarEJB::ejbPostCreateWithTimer");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
        try {
            TimerService timerService = context_.getTimerService();
            Timer timer = timerService.createTimer
                (1, 1, methodName + id);
                                                         
        } catch(Exception e) {
            e.printStackTrace();
            throw new CreateException(e.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        context_ = context;
        setContext(context);
        String methodName = "setEntityContext";
        System.out.println("In BarEJB::setEntityContext");
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, false);
        doTimerStuff(methodName, false);
    }

    public void unsetEntityContext() {
        String methodName = "unsetEntityContext";
        System.out.println("In BarEJB::unsetEntityContext");
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        context_ = null;
    }

    public void ejbHomeNewTimerAndRemoveBean(Long id, String value2) 
        throws RemoteException {
        String methodName = "ejbHomeNewTimerAndRemoveBean";
        checkCallerSecurityAccess(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            Bar b = home.createWithTimer(id, value2);
            b.remove();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbHomeNewTimerAndRemoveBeanAndRollback(Long id, String value2) 
        throws RemoteException {
        getTimerService("ejbHomeNewTimerAndRemoveBeanAndRollback", true);
        doTimerStuff("ejbHomeNewTimerAndRemoveBeanAndRollback", false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            Bar b = home.createWithTimer(id, value2);
            b.remove();
            context_.setRollbackOnly();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbHomeNixBeanAndRollback(Bar b) 
        throws RemoteException {
        getTimerService("ejbHomeNixBeanAndRollback", true);
        doTimerStuff("ejbHomeNixBeanAndRollback", false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            home.remove(b.getPrimaryKey());
            context_.setRollbackOnly();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbRemove() {
        String methodName = "ejbRemove";
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
        cleanup();
    }
    
    public void ejbLoad() {
        String methodName = "ejbLoad";
        System.out.println("In BarEJB::ejbLoad");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }
    
    public void ejbStore() {
        String methodName = "ejbStore";
        System.out.println("In BarEJB::ejbStore");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }
    
    public void ejbPassivate() {
        String methodName = "ejbPassivate";
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
    }
    
    public void ejbActivate() {
        String methodName = "ejbActivate";
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
    }

}
