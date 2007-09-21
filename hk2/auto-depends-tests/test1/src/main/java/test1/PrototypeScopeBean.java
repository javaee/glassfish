package test1;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

/**
 * Per-lookup component test.
 * 
 * @author Kohsuke Kawaguchi
 */
@Service @Scoped(PerLookup.class)
public class PrototypeScopeBean {
}
