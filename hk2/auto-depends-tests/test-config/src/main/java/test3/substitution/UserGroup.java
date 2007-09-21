package test3.substitution;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class UserGroup implements Subject {
    @Attribute
    String name;

    public String toString() {
        return "userGroup="+name;
    }
}
