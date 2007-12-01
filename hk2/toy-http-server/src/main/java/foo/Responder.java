package foo;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author Kohsuke Kawaguchi
 */
@Contract
public interface Responder {
    String echo(String str);
}
