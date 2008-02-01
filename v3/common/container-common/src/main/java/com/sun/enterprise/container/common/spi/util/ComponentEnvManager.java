package com.sun.enterprise.container.common.spi.util;

import com.sun.enterprise.deployment.JndiNameEnvironment;
import org.glassfish.api.naming.JNDIBinding;
import org.jvnet.hk2.annotations.Contract;

import javax.naming.NamingException;
import java.util.Collection;

@Contract
public interface ComponentEnvManager {

    public JndiNameEnvironment getJndiNameEnvironment(String componentId);

    public String bindToComponentNamespace(JndiNameEnvironment env)
        throws NamingException;

    public void unbindFromComponentNamespace(JndiNameEnvironment env)
        throws NamingException;
    
    public Collection<? extends JNDIBinding> getJNDIBindings(JndiNameEnvironment env);

}
