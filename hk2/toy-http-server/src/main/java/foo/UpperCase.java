package foo;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class UpperCase implements Responder{
    @Element("*")
    public Responder responder;

    public String echo(String str) {
        return responder.echo(str).toUpperCase();
    }
}
