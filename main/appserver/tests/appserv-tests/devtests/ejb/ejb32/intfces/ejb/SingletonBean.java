package ejb32.intrfaces;

import javax.ejb.*;
import javax.annotation.*;

/*
    SingletonBean exposes remote interfaces St1 and St2
 */
@Remote
@Singleton
public class SingletonBean implements St1, St2 {

    @EJB(lookup = "java:module/StflEJB1!ejb32.intrfaces.St3")
    St3 st3;
    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StflEJB1!ejb32.intrfaces.St4")
    St4 st4;
    @EJB(lookup = "java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb2/StlesEJB2!ejb32.intrfaces.St7")
    St7 st7;

    @Resource
    SessionContext ctx;

    // expectation: StflEJB1.st3.StflEJB1.st4.SingletonBean.st1
    public String st1() throws Exception {
        return st3.st3() + "." + st4.st4() + "." + "SingletonBean.st1";
    }

    // expectation: StlesEJB2.st7.SingletonBean.st2
    public String st2() throws Exception {
        try {
            ctx.lookup("java:module/StlesEJB2!ejb32.intrfaces.St5");
        } catch (Exception e) {
            return st7.st7() + "." + "SingletonBean.st2";
        }
        throw new IllegalStateException("Error occurred for StlesEJB2!");
    }
}
