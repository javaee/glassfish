package test3.cage;

import org.jvnet.hk2.annotations.CompanionOf;
import test3.JmsHost;

/**
 * @author Kohsuke Kawaguchi
 */
@CompanionOf(JmsHost.class)
public class JmsCompanion implements Caged {
}
