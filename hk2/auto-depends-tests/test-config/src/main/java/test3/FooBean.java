package test3;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured(name="domain",symbolSpace=HttpListener.class)
public class FooBean {
    public Exception e;

    @Attribute
    public int httpPort;

    public String bar;
    
    public List<String> jvmOptions = new ArrayList<String>();

    @Element("property")
    public Map<String,Property> properties = new HashMap<String, Property>();

    @Element("http-listener")
    public Map<String,HttpListener> httpListeners = new HashMap<String,HttpListener>();

    @Element("virtual-server")
    public List<VirtualServer> virtualServers = new ArrayList<VirtualServer>();

    @Element("*")
    public List<Object> all = new ArrayList<Object>();

    public FooBean() {
        e = new Exception();
    }

    @Element
    public void setBar(String bar) {
        this.bar = bar;
    }

    @Element
    public void setJvmOptions(List<String> opts) {
        this.jvmOptions.addAll(opts);
    }

    public <T> T find(Class<T> t) {
        for (Object o : all) {
            if(t.isInstance(o))
                return t.cast(o);
        }
        return null;
    }
}
