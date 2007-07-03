package test3;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Kohsuke Kawaguchi
 */
@Service(scope=DomainXml.class)
@Configured(name="foo")
public class FooBean {
    public Exception e;

    public FooBean() {
        e = new Exception();
    }
}
