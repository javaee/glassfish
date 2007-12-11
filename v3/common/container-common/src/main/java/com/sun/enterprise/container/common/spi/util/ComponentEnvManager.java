package com.sun.enterprise.container.common.spi.util;

import com.sun.enterprise.deployment.JndiNameEnvironment;

import com.sun.enterprise.naming.spi.JNDIBinding;

import org.jvnet.hk2.annotations.Contract;

import java.util.Collection;

@Contract
public interface ComponentEnvManager {

    public Collection<? extends JNDIBinding> getJNDIBindings(JndiNameEnvironment env);

}