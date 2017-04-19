package test.ejb;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

/**
 * @author Shing Wai Chan
 */
@Stateful(name="myStatefulTest")
@Resource(name="myDataSource1", type=DataSource.class)
@Remote({SFHello1.class})
public class StatefulTest1 extends StatefulTest implements SFHello1 {
    @EJB private SFHello sfHello;
    private EJBContext ejbContext;
    private EJBContext ejbContext2;

    public StatefulTest1() {
    }

    @TransactionAttribute(value=TransactionAttributeType.MANDATORY)
    public String sayHello(String message) {
        return super.sayHello(message);
    }

    public String sayBye(String message) {
        return "Good bye, " + message + "!";
    }

    @Resource(name="sfEjbContext")
    private void setEjbContext(EJBContext context) {
        ejbContext = context;
    }

    @Resource(name="sfEjbContext2")
    void setEjbContext2(EJBContext context) {
        ejbContext = context;
    }

    @Resource(name="sfEjbContext3")
    public void setEjbContext3(EJBContext context) {
        ejbContext = context;
    }
}
