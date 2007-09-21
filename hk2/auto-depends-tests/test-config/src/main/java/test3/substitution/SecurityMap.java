package test3.substitution;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * Emulates &lt;security-map> in domain.xml
 *
 * (princiapl|user-group)+
 * (principal+|user-group+)

 * (principal+,user-group+)
 *
 * @author Kohsuke Kawaguchi
 */
@Configured
public class SecurityMap {
    @Element("*")
    List<Subject> subjects;

    @Element
    Principal backendPrincipal;

    public String toString() {
        return subjects.toString()+",backendPrincipal="+backendPrincipal;
    }
}
