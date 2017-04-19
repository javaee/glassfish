package test.ejb;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

/**
 * @author Shing Wai Chan
 */
@Stateful
@Local({SFHello.class})
@Resource(name="myDataSource", type=DataSource.class)
@TransactionAttribute(value=TransactionAttributeType.REQUIRED)
@RunAs("guest")
@PermitAll
public class StatefulTest implements SFHello {
    @EJB private SFHello mySfHello;
    private EJBContext ejbContext;
    private EJBContext ejbContext2;

    public StatefulTest() {
    }

    @TransactionAttribute(value=TransactionAttributeType.REQUIRES_NEW)
    public String sayHello(String message) {
        return "Hello, " + message + "!";
    }

    @TransactionAttribute(value=TransactionAttributeType.SUPPORTS)
    public String sayGoodMorning(String message) {
        return "Good morning, " + message + "!";
    }

    public String sayGoodAfternoon(String message) {
        return "Good afternoon, " + message + "!";
    }

    public String sayGoodNight(String message) {
        return "Good night, " + message + "!";
    }

    @Resource(description="testing")
    private void setEjbContext(EJBContext context) {
        ejbContext = context;
    }

    @Resource
    void setEjbContext2(EJBContext context) {
        ejbContext2 = context;
    }

    @Resource
    void setEjbContext3(EJBContext context) {
    }
}
