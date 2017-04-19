package ejb32.methodintf;

import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;
import javax.naming.InitialContext;

@Singleton
@Startup
public class SingletonBean {

    private boolean _pc = false;
    private boolean _intf = false;
    private String caller = null;

    @PostConstruct
    public void test() {
        if (caller == null) {
            System.out.println("In SingletonBean: test LC");
            _pc = Verifier.verify_tx(false);
        } else if (caller.equals("intf")) {
            System.out.println("In SingletonBean: test local");
            _intf = Verifier.verify_tx(true);
        }
        caller = null;
    }

    public boolean verifyResult() {
	return _pc && _intf;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }
}
