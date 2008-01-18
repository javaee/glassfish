package companion;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import test1.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class CompanionTest extends Test {
    @Inject
    Habitat habitat;

    @Inject
    LeadGuy lead;

    public void run() {
        Inhabitant<LeadGuy> i = habitat.getInhabitantByType(LeadGuy.class);
        assertSame(lead,i.get());

        assertEquals(2,i.companions().size());
        for (Inhabitant c : i.companions()) {
            Object o = c.get();
            if (o instanceof Companion1) {
                Companion1 c1 = (Companion1) o;
                c1.check();
                assertSame(c1.lead,lead);
                assertEquals(1,c.companions().size()); // check nested companion
                continue;
            }
            if (o instanceof Companion2) {
                continue;
            }
            fail();
        }
    }
}

