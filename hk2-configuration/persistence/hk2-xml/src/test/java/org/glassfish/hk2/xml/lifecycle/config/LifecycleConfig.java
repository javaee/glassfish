/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public interface LifecycleConfig {

    @XmlElement
    Runtimes getRuntimes();
    void setRuntimes(Runtimes runtimes);

    @XmlElement
    Environments getEnvironments();
    void setEnvironments(Environments environments);
    
    @XmlElement
    Tenants getTenants();
    void setTenants(Tenants tenants);

    @XmlElement
    Plugins getPlugins();
    void setPlugins(Tenants plugins);
}
