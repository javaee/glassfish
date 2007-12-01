package foo;

import org.jvnet.hk2.config.Configured;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class Echo implements Responder {
    public String echo(String str) {
        return str;
    }
}
