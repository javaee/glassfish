package test3;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public interface JmsHost extends ConfigBeanProxy {
    @Attribute
    String getName();

    void setName(String v);

    // trying a different variation of naming convention
    @Attribute
    String host();

    @Attribute
    int port();

    @Attribute
    boolean hasFlag();

    @Element("property")
    List<Property> getProperties();

    @Element("point")
    List<PointConfig> getPoints();
}
