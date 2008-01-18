package companion;

import org.jvnet.hk2.annotations.CompanionOf;
import org.jvnet.hk2.annotations.Lead;

/**
 * @author Kohsuke Kawaguchi
 */
@CompanionOf(Companion1.class)
// should work with package private class, too.
public class NestedCompanion {
    @Lead
    Companion1 lead;
}
