package test3;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured(local=true)
public class Property implements PostConstruct {
    @Attribute(key=true,required=true)
    public String name;

    @Attribute(required=true)
    public String value;

    public boolean constructed;

    public void postConstruct() {
        // make sure PostConstruct interface support works
        constructed = true;
    }
}
