package test.ejb.stateless;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.sql.DataSource;

@Resource(name="jdbc/myDS", type=DataSource.class)
public class InterceptorA {
    @EJB(beanName="myStatelessTest1") private SLHello slHello;

    @AroundInvoke
    Object aroundInvokeA(InvocationContext ctx) {
        return null;
    }

    @PostConstruct
    void postConstruct() {
    }
}
