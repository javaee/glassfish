import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.ejb.TransactionManagementType;

import test.ejb.stateful.SFHello;

/**
 * @author Shing Wai Chan
 */
@Remote({SFHello.class})
@Stateful
public class StatefulTest2 implements SFHello {
    public StatefulTest2() {
    }
}
