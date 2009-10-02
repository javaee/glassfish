package numberguess;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

@Stateful
public class StatefulBean {

    @Resource
    private SessionContext sessionCtx;

    @EJB
	private SingletonBean singleton;

    @PostConstruct
	public void init() {
	System.out.println("In StatefulBean::init()");
	System.out.println("sessionCtx = " + sessionCtx);
	if( sessionCtx == null ) {
	    throw new EJBException("EE injection error");
	}
	singleton.hello();
    }

    public void hello() {
	System.out.println("In StatefulBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatefulBean::destroy()");
    }

    

}
