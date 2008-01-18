package companion;

import junit.framework.Assert;
import org.jvnet.hk2.annotations.CompanionOf;
import org.jvnet.hk2.annotations.Lead;
import org.jvnet.hk2.component.Inhabitant;

/**
 * @author Kohsuke Kawaguchi
 */
@CompanionOf(LeadGuy.class)
public class Companion1 extends Assert {
    // two ways to inject leads. One eagerly and theo the other lazily
    @Lead
    LeadGuy lead;

    @Lead
    Inhabitant<LeadGuy> lead2;

    public void check() {
        assertSame(lead,lead2.get());
    }
}
