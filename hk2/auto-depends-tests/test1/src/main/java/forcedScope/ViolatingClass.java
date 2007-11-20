package forcedScope;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
@Scoped(PerLookup.class)
public class ViolatingClass implements BaseContract{
}
