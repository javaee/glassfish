package test3.cage;

import org.jvnet.hk2.annotations.CagedBy;

/**
 * @author Kohsuke Kawaguchi
 */
@CagedBy(TestCageBuilder.class)
public interface Caged {
}
