package test3;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;
import test3.substitution.SecurityMap;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Main extends Assert implements ModuleStartup {
    @Inject
    FooBean foo;

    @Inject
    Habitat manager;

    public void setStartupContext(StartupContext context) {
    }

    public void run() {
        assertNotNull(foo);
        foo.e.printStackTrace();
        assertEquals(80,foo.httpPort);
        assertEquals(foo.bar,"qwerty");

        assertEquals(3,foo.properties.size());
        assertNotNull(foo.properties.get("xyz"));
        assertNotNull(foo.properties.get("qqq"));
        assertNotNull(foo.properties.get("adminPort"));

        for (Property p : foo.properties.values())
            assertTrue(p.constructed);

        assertEquals(2,foo.jvmOptions.size());
        assertEquals(foo.jvmOptions.get(0),"-Xmx256m");
        assertEquals(foo.jvmOptions.get(1),"-verbose:abcwww");

        assertEquals(2,foo.httpListeners.size());

        HttpListener listener = manager.getComponent(HttpListener.class, "a");
        assertEquals("a",listener.id);

        assertEquals(1,foo.virtualServers.size());
        VirtualServer vserver = foo.virtualServers.get(0);
        assertEquals(2,vserver.httpListeners.size());
        assertTrue(vserver.httpListeners.contains(manager.getComponent(HttpListener.class, "a")));
        assertTrue(vserver.httpListeners.contains(manager.getComponent(HttpListener.class, "b")));

        // test substitutability
        System.out.println(foo.find(SecurityMap.class).toString());

        // testing dynamic reconfiguration
        assertEquals(5,listener.acceptorThreads);
        Dom i = (Dom)manager.getInhabitant(HttpListener.class, "a");
        i.attribute("acceptor-threads","56");
        assertEquals(56,listener.acceptorThreads);
    }
}
