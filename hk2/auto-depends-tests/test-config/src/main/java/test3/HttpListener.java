package test3;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;

/**
 * This object can be looked up by its id.
 * @author Kohsuke Kawaguchi
 */
@Configured
public class HttpListener extends Thread {
    @Attribute(key=true,required=true)
    public String id;

    @Attribute
    public int acceptorThreads;

    @Attribute
    public int port;

    public void run() {
        // TODO
        super.run();
    }
}
