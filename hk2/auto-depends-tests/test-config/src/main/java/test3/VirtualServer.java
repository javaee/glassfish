package test3;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class VirtualServer {
    @Attribute(reference=true)
    public List<HttpListener> httpListeners;

    @Attribute
    public boolean state;
}
