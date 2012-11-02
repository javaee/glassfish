package ejb32.methodintf;


import javax.ejb.*;
import javax.interceptor.*;
import javax.annotation.*;

@Stateful
public class StfulEJB implements St {

    private static boolean pc = false;
    private static boolean intf = false;
    private String caller = null;

    @PostConstruct
    public void test() {
        if (caller == null) {
            System.out.println("In StfulEJB: test LC");
            pc = Verifier.verify_tx(true);
        } else if (caller.equals("intf")) {
            System.out.println("In StfulEJB: test remote");
            intf = Verifier.verify_tx(false);
        }
        caller = null;
    }

    @AroundInvoke
    private Object around_invoke(InvocationContext ctx) throws Exception {
        caller = "intf";
        return ctx.proceed();
    }

    public boolean verify() {
        return pc && intf;
    }

}
