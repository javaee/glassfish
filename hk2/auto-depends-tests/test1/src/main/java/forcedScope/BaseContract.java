package forcedScope;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

/**
 * @author Kohsuke Kawaguchi
 */
@Contract
@Scoped(PerLookup.class)
public interface BaseContract {
}
