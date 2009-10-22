package numberguess;

import javax.interceptor.*;
import javax.annotation.*;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import javax.inject.Inject;

public class InterceptorB {

    @EJB StatelessLocal sbean;

    @AroundInvoke
	public Object around(InvocationContext ctx) throws Exception {

	System.out.println("In InterceptorB::around");
	return ctx.proceed();

    }

    @PostConstruct
	public void init(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorB::init()");

	if( sbean == null ) {
	    throw new EJBException("null sbean");
	}

	System.out.println("StatelessBean = " + sbean);

	ctx.proceed();
    }

    @PreDestroy
	public void destroy(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorB::destroy()");
	ctx.proceed();
    }

}
