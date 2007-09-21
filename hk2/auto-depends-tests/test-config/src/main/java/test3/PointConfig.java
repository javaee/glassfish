package test3;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class PointConfig {
    @Attribute
    public int x;
    @Attribute
    public int y;
}
