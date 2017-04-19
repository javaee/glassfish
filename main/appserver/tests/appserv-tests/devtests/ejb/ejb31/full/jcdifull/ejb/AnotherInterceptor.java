package test.beans.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.acme.StatelessBean;

@Interceptor
@Another
public class AnotherInterceptor {

    @AroundInvoke
    public Object process(InvocationContext ctx) throws Exception {
        System.err.println("====> AnotherInterceptor::AroundInvoke");
        if ((ctx.getTarget() instanceof StatelessBean) && ctx.getMethod().getName().equals("hello")) {
            StatelessBean sb = (StatelessBean)ctx.getTarget();
            sb.interceptorCalled(1);
        }
        return ctx.proceed();
    }

 }
