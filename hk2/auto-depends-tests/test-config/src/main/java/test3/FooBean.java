package test3;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.FromAttribute;
import org.jvnet.hk2.config.FromElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Kohsuke Kawaguchi
 */
@Service(scope=DomainXml.class)
@Configured(name="foo")
public class FooBean {
    public Exception e;

    @FromAttribute
    public int httpPort;

    public String bar;
    
    public List<String> jvmOptions = new ArrayList<String>();

    @FromElement("property")
    public Map<String,Property> properties = new HashMap<String, Property>();

    public FooBean() {
        e = new Exception();
    }

    @FromElement
    public void setBar(String bar) {
        this.bar = bar;
    }

    @FromElement("jvm-options")
    public void addJvmOptions(String opt) {
        this.jvmOptions.add(opt);
    }
}
