package test3;

import org.jvnet.hk2.config.Configured;

/**
 * Hypothetical component that's not statically known by {@link FooBean}.
 * In reality this will come from other modules.
 *
 * @author Kohsuke Kawaguchi
 */
@Configured
public class JBI {
    // 
}
