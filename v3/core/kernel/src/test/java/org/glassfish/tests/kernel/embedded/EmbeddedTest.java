package org.glassfish.tests.kernel.embedded;

import org.junit.Test;
import org.glassfish.api.embedded.Server;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: May 19, 2009
 * Time: 3:28:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedTest {

    @Test
    public void test() {

        Server server = new Server.Builder("build").build();
        server.createPort(8080);
    }
}
