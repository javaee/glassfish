package test3;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public interface PointConfig extends ConfigBeanProxy {
    @Attribute
    public int x();
    @Attribute
    public int y();
}


