package test1;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Start extends Assert implements ModuleStartup {
    private StartupContext context;

    @Inject
    Test[] tests;

    @Inject
    Habitat habitat;

    public void setStartupContext(StartupContext context) {
        this.context = context;
    }

    public void run() {
        assertNotNull(context);
        assertNotNull(habitat);
        assertTrue(tests.length!=0);

        for (Test test : tests) {
            System.out.println("Running "+test);
            test.run();
        }
    }
}
