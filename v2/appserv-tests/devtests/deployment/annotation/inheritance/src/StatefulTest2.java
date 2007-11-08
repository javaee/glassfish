import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJBContext;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import test.ejb.SFHello1;
import test.ejb.StatefulTest;

/**
 * @author Shing Wai Chan
 */
@TransactionAttribute(value=TransactionAttributeType.SUPPORTS)
@Stateful
@Remote({SFHello1.class})
@RunAs("staff")
public class StatefulTest2 extends StatefulTest implements SFHello1 {
    private EJBContext ejbContext;
    private EJBContext ejbContext2;

    public StatefulTest2() {
    }

    @TransactionAttribute(value=TransactionAttributeType.MANDATORY)
    public String sayHello(String message) {
        return super.sayHello(message);
    }

    @RolesAllowed("member")
    public String sayBye(String message) {
        return "Good bye, " + message + "!";
    }

    @Resource(name="sfContext")
    private void setEjbContext(EJBContext context) {
        ejbContext = context;
    }

    @Resource(name="sfContext2")
    void setEjbContext2(EJBContext context) {
        ejbContext = context;
    }
}
