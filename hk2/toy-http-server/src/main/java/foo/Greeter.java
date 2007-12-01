package foo;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class Greeter implements Responder {
    @Attribute
    public String greeting;

    public String echo(String str) {
        return greeting+' '+str;
    }
}
