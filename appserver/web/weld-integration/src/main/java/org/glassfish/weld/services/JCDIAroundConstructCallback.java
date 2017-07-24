package org.glassfish.weld.services;

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.construction.api.ConstructionHandle;
import org.jboss.weld.exceptions.WeldException;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import java.util.HashMap;
import java.util.Map;

/**
 * This calls back into the ejb container to perform the around construct interception.  When that's finished the
 * ejb itself is then created.
 */
public class JCDIAroundConstructCallback<T> implements AroundConstructCallback<T> {
    private BaseContainer container;
    private EJBContextImpl ejbContext;
    public JCDIAroundConstructCallback(BaseContainer container, EJBContextImpl ejbContext) {
        this.container = container;
        this.ejbContext = ejbContext;
    }

    @Override
    public T aroundConstruct(final ConstructionHandle<T> handle, AnnotatedConstructor<T> constructor, Object[] parameters, Map<String, Object> data) {
        T ejb;
        try {
            container.intercept( LifecycleCallbackDescriptor.CallbackType.AROUND_CONSTRUCT, ejbContext );

            // all the interceptors were invoked, call the constructor now
            ejb = handle.proceed( parameters, new HashMap<String, Object>() );
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WeldException(e);
        }
            return ejb;
    }
}
