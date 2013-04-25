package ejb32.intrfaces;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

/*
   StflEJB exposes remote interface St7. St5 isn't business interface.
*/
@Stateful
public class StflEJB implements St5, St7 {
    @EJB(lookup = "java:module/StlesEJB1!ejb32.intrfaces.St6")
    St6 st6;
    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StlesEJB1!ejb32.intrfaces.StlesEJB1")
    StlesEJB1 stlesEJB1;
    @EJB(lookup = "java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb2/StlesEJB1!ejb32.intrfaces.St7")
    St7 st7;

    @Resource
    SessionContext ctx;

    @Override
    public String st5() throws Exception {
        return st6.st6() + "." + "StflEJB.st5";
    }

    // expectation: StlesEJB1.st6.StlesEJB1.st7.StflEJB.st7
    @Override
    public String st7() throws Exception {
        try {
            ctx.lookup("java:module/StlesEJB1!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            // St5 isn't business interface of StlesEJB1
            return stlesEJB1.st6() + "." + st7.st7() + "." + "StflEJB.st7";
        }
        throw new IllegalStateException("Error occurred for StflEJB!");
    }
}
