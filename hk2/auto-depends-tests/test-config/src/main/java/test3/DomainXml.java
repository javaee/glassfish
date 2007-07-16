package test3;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfiguredScope;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
@Service(scope= Singleton.class)
public class DomainXml extends ConfiguredScope {
    protected StreamSource getConfigFile() throws IOException {
        return new StreamSource(getClass().getResourceAsStream("domain.xml"));
    }
}
