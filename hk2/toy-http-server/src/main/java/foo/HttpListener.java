package foo;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.apache.commons.io.IOUtils;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
@Configured
public class HttpListener implements ModuleStartup {
    @Attribute
    public int port;

    @Element("*")
    public Responder responder;

    public void setStartupContext(StartupContext context) {
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true) {
                Socket s = ss.accept();
                String text = IOUtils.toString(s.getInputStream());
                text = responder.echo(text);
                s.getOutputStream().write(text.getBytes());
                s.close();
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
