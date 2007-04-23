package test1;

import org.jvnet.hk2.annotations.Contract;
import junit.framework.Assert;

/**
 * Components that run tests.
 * @author Kohsuke Kawaguchi
 */
@Contract
public abstract class Test extends Assert implements Runnable {
}
