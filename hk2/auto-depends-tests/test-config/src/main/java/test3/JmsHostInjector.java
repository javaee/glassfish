package test3;

import org.jvnet.hk2.config.NoopConfigInjector;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.annotations.Service;

/**
 * Hand-writing code that's supposed to be generated automatically.
 *
 * <p>
 * Using another class just to capture metadata is ugly, though.
 *
 * @author Kohsuke Kawaguchi
 */
@Service(name="jms-host",
        metadata="target=test3.JmsHost,@name=required,@host=required,@port=optional,@flag=optional,<property>=collection:test3.Property")
@InjectionTarget(JmsHost.class)
public class JmsHostInjector extends NoopConfigInjector {
}
