package com.sun.s1asdev.ejb.ejb32.mdb.ejb;

import com.sun.s1asdev.ejb.ejb32.mdb.ra.Command;
import com.sun.s1asdev.ejb.ejb32.mdb.ra.CommandListener;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.interceptor.Interceptors;

/**
 * @author David Blevins
 */
@MessageDriven
@Interceptors(EnsureProxied.class)
public class ModernBean implements CommandListener {

    @EJB
    private ResultsBean resultsBean;

    @Resource
    private MessageDrivenContext messageDrivenContext;

    @Command
    public void doSomething() {
        resultsBean.addInvoked("one" + getInterceptorData());
    }

    @Command
    public void doSomethingElse() {
        resultsBean.addInvoked("two" + getInterceptorData());
    }

    @Command
    public void doItOneMoreTime() {
        resultsBean.addInvoked("three" + getInterceptorData());
    }

    /**
     * Ensure that the bean was invoked via a proxy with interceptors
     */
    private Object getInterceptorData() {
        return messageDrivenContext.getContextData().get("data");
    }

}
