package test3;

import com.sun.enterprise.module.bootstrap.Populator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class DomainXml implements Populator {
    public void run(ConfigParser parser) {
        VariableResolverImpl vr = new VariableResolverImpl();
        DomDocument document = parser.parse(getClass().getResource("domain.xml"));
        document.setTranslator(vr);

        // use <property> elements under the root element for variable substitution.
        vr.setProperties(document.getRoot().nodeElements("property"));
    }
}
