package ejb32.methodintf;


import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
public class StlesEJB implements St {

    private static boolean tm = false;
    private static boolean intf = false;

    private String caller = null;

    @EJB SingletonBean singleton;

    @Schedule(second="*/2",minute="*",hour="*")
    public void test() {
        if (caller != null) {
            if (caller.equals("timeout")) {
                System.out.println("In StlesEJB: test timeout");
                tm = Verifier.verify_tx(false);
            } else {
                System.out.println("In StlesEJB: test remote");
                intf = Verifier.verify_tx(true);
                singleton.test();
            }
        }
        caller = null;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }

    @AroundTimeout
    private Object around_timeout(InvocationContext ctx) throws Exception {
        caller = "timeout";
        return ctx.proceed();
    }

    public boolean verify() {
        boolean rc = singleton.verifyResult();
        return tm && intf && rc;
    }

}
