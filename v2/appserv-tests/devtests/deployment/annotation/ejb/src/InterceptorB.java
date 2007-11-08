package test.ejb.stateless;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class InterceptorB extends InterceptorA {
    @Resource SessionContext sessionCtx;

    @AroundInvoke
    Object aroundInvokeB(InvocationContext ctx) {
        return null;
    }  

    @PreDestroy
    void preDestroy() {
    }
}
