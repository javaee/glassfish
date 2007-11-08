package test.ejb.stateful;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import test.ejb.stateless.*;

/**
 * @author Shing Wai Chan
 */
@TransactionManagement(value=TransactionManagementType.BEAN)
@Stateful(name="myStatefulTest1")
@Remote({SFHello.class})
public class StatefulTest1 implements SFHello {
    @EJB(beanName="myStatelessTest1") private SLHello slHello1;
    @EJB(beanName="myStatelessTest1") private SLHello slHello2;

    public StatefulTest1() {
    }
}
