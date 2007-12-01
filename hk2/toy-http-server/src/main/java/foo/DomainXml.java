package foo;

import com.sun.enterprise.module.bootstrap.Populator;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class DomainXml implements Populator {
    public void run(ConfigParser parser) {
        try {
            File file = new File("domain.xml");
            LOGGER.info("Loading "+file);
            DomDocument document = parser.parse(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DomainXml.class.getName());
}
