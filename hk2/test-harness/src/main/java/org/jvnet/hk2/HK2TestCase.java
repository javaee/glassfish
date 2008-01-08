package org.jvnet.hk2;

import junit.framework.TestCase;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

/**
 * Base-class for all test cases in HK2.
 *
 * <p>
 * At runtime all the services of this contract are discovered and run.
 * The execution semantics of JUnit requires that a new instance be created
 * for calling each <tt>testXXX</tt> methods, so the implementations
 * must be {@link PerLookup} scope. 
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@Scoped(PerLookup.class)
public class HK2TestCase extends TestCase {

}
