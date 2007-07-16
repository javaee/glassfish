package test3;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.FromAttribute;
import org.jvnet.hk2.config.Named;
import org.jvnet.hk2.component.PostConstruct;

/**
 * @author Kohsuke Kawaguchi
 */
@Service(scope=DomainXml.class)
@Configured(name="property")
public class Property implements PostConstruct, Named {
    @FromAttribute
    public String name;

    @FromAttribute
    public String value;

    public boolean constructed;


    public String getName() {
        return name;
    }

    public void postConstruct() {
        // make sure PostConstruct interface support works
        constructed = true;
    }
}
