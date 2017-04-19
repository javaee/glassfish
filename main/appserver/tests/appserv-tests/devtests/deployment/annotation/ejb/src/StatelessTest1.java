package test.ejb.stateless;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

/**
 * @author Shing Wai Chan
 */
@Stateless(name="myStatelessTest1")
@Local({SLHello.class})
@TransactionAttribute(value=TransactionAttributeType.REQUIRED)
@RolesAllowed(value={"staff"})
@RunAs(value="user")
@Interceptors(InterceptorB.class)
public class StatelessTest1 implements SLHello {
    public StatelessTest1() {
    }

    @RolesAllowed(value={"j2ee", "staff"})
    public String sayHello(String message) {
        return "Hello, " + message + "!";
    }

    @PermitAll
    @TransactionAttribute(value=TransactionAttributeType.REQUIRES_NEW)
    public String sayGoodMorning(String message) {
        return "Good morning, " + message + "!";
    }

    public String sayGoodAfternoon(String message) {
        return "Good afternoon, " + message + "!";
    }
}
