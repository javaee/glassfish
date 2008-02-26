package test3.cage;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class TestCageBuilder implements CageBuilder {
    public final Set<Inhabitant> inhabitants = new HashSet<Inhabitant>();
    
    public void onEntered(Inhabitant<?> i) {
        inhabitants.add(i);
    }
}
