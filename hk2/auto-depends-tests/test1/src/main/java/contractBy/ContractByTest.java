package contractBy;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractProvided;
import test1.Test;

/**
 * Test case for making sure that injection by {@link ContractProvided}.
 * 
 * @author Kohsuke Kawaguchi
 */
@Service
public class ContractByTest extends Test {
    @Inject
    Bar bar;

    @Inject
    Bar[] bars;

    public void run() {
        assertNotNull(bar);
        assertEquals(1,bars.length);
    }
}
