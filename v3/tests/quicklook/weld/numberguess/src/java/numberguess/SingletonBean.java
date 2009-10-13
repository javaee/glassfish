package numberguess;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

@Singleton
@Startup
public class SingletonBean {

    @Resource
    private SessionContext sessionCtx;

    @Inject
    private StatelessLocal statelessLocal;

    @PostConstruct
	public void init() {
	System.out.println("In SingletonBean::init()");
	System.out.println("sessionCtx = " + sessionCtx);
	if( sessionCtx == null ) {
	    throw new EJBException("EE injection error");
	}
    }

    public void hello() {
	System.out.println("In SingletonBean::hello()");
	statelessLocal.hello();
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In SingletonBean::destroy()");
    }

    

}
