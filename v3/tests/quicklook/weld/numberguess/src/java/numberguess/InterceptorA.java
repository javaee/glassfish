package numberguess;

import javax.interceptor.*;
import javax.annotation.*;

import javax.inject.Inject;

@Interceptor
@SomeBindingType
public class InterceptorA {

    @Inject
    private StatelessLocal statelessLocal;

    @AroundInvoke
	public Object around(InvocationContext ctx) throws Exception {

	System.out.println("In InterceptorA::around");
	return ctx.proceed();

    }

    @PostConstruct
	public void init(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorA::init()");

	System.out.println("statelessLocal = " + statelessLocal);

	ctx.proceed();
    }

    @PreDestroy
	public void destroy(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorA::destroy()");
	ctx.proceed();
    }

}
