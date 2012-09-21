package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ConfigExtensionHandler<T extends ConfigBeanProxy> {

    public T handleExtension(Object owner, Class<T> extensionType, Object[] params);

}
