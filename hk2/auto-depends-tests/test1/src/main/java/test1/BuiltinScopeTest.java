package test1;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Tests the built-in scopes.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
public class BuiltinScopeTest extends Test {
    @Inject
    public PrototypeScopeBean foo1;

    @Inject
    public PrototypeScopeBean foo2;

    @Inject
    public SingletonScopeBean bar1;

    @Inject
    public SingletonScopeBean bar2;

    public void run() {
        System.out.println("Hello!");

        assertNotNull(foo1);
        assertNotNull(foo2);
        assertNotNull(bar1);
        assertNotNull(bar2);

        assertSame(bar1,bar2);
        assertNotSame(foo1,foo2);
    }
}
