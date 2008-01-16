package factoryBy;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import test1.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class FactoryByTest extends Test {
    @Inject
    Pig pig;

    public void run() {
        System.out.println("I'm "+pig);
        assertSame(Pig.BABE,pig);
    }
}
