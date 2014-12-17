/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public interface LifecycleConfig {

    @XmlElement
    void setRuntimes(Runtimes runtimes);
    Runtimes getRuntimes();

    @XmlElement
    void setEnvironments(Environments environments);
    Environments getEnvironments();
    
    @XmlElement
    void setTenants(Tenants tenants);
    Tenants getTenants();

    @XmlElement
    void setPlugins(Tenants plugins);
    Plugins getPlugins();
}
