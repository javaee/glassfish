package test3;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;
import test3.substitution.SecurityMap;

import java.util.List;
import java.util.Collection;

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

        // test the proxies
        JmsHost jms = find(foo.all, JmsHost.class);
        System.out.println(jms.toString());
        List<Property> props = jms.getProperties();
        assertEquals(2, props.size());
        assertEquals("foo",props.get(0).name);
        assertEquals("abc",props.get(0).value);

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

    private <T> T find(Collection<?> all, Class<T> type) {
        for (Object t : all) {
            if(type.isInstance(t))
                return type.cast(t);
        }
        return null;
    }
}
