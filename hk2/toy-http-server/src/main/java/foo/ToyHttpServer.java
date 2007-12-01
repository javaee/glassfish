package foo;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class ToyHttpServer {
    @Element("*")
    public List<Object> allBeans;
}
