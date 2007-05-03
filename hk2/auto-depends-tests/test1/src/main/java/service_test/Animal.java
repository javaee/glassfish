package service_test;

import org.jvnet.hk2.annotations.Contract;

/**
 * Check if the test1 module can load this service from the test2 module, even though
 * test1 doesn't declare a dependency to test2.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
public interface Animal {
    String bark();
}
