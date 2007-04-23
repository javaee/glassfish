package test1;

import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class TestFactory implements Factory {
    public FactoryTest getObject() throws ComponentException {
        return new FactoryTest(5);
    }
}
