package numberguess;

import javax.interceptor.*;
import javax.annotation.*;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;

@Interceptor
@SomeBindingType
public class InterceptorA implements Serializable {

    @EJB
    private StatelessLocal statelessLocal;

    @Inject Foo foo;

    @AroundInvoke
	public Object around(InvocationContext ctx) throws Exception {

	System.out.println("In InterceptorA::around");
	return ctx.proceed();

    }

    @PostConstruct
	public void init(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorA::init()");

	System.out.println("statelessLocal = " + statelessLocal);
	System.out.println("foo = " + foo);

	ctx.proceed();
    }

    @PreDestroy
	public void destroy(InvocationContext ctx) throws Exception {
	System.out.println("In InterceptorA::destroy()");
	ctx.proceed();
    }

}
