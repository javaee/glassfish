package test.ejb.stateless;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Shing Wai Chan
 */
@Stateless
@Local({SLHello.class})
@TransactionAttribute(value=TransactionAttributeType.MANDATORY)
public class StatelessTest2 implements SLHello {
    public StatelessTest2() {
    }

    @RolesAllowed(value={"j2ee", "staff"})
    public String sayHello(String message) {
        return null;
    }

    @PermitAll
    @TransactionAttribute(value=TransactionAttributeType.REQUIRES_NEW)
    public String sayGoodMorning(String message) {
        return null;
    }

    @DenyAll
    public String sayGoodAfternoon(String message) {
        return null;
    }
}
