package test3;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentManager;
import junit.framework.Assert;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Main extends Assert implements ModuleStartup {
    private StartupContext context;

    @Inject
    FooBean foo;

    @Inject
    ComponentManager manager;

    public void setStartupContext(StartupContext context) {
        this.context = context;
    }

    public void run() {
        assertNotNull(foo);
        foo.e.printStackTrace();
        assertEquals(80,foo.httpPort);
        assertEquals(foo.bar,"qwerty");

        assertEquals(2,foo.properties.size());
        assertNotNull(foo.properties.get("xyz"));
        assertNotNull(foo.properties.get("qqq"));

        for (Property p : foo.properties.values())
            assertTrue(p.constructed);

        assertEquals(2,foo.jvmOptions.size());
        assertEquals(foo.jvmOptions.get(0),"-Xmx256m");
    }
}
