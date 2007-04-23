package test1;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Service(scope=Singleton.class)
public class SingletonScopeBean {
}
