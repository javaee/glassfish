package factoryBy;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Factory;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
@FactoryFor(Pig.class)
public class PigFactory implements Factory {
    public Object getObject() {
        return Pig.BABE;
    }
}
