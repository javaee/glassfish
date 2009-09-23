/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import java.util.ArrayList;
import java.util.List;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * This is a dummy implementation of the NetworkListener interface. This is used to create a fake
 * network-listener elements. This is used only to support lazyInit attribute of iiop and jms services through the
 * light weight listener. Ultimately, these services will move to their own network-listener
 * element in domain.xml (at which point we have to get rid of this fake object). But till the time IIOP and JMS
 * service elements in domain.xml can move to use network-listener element, we will create this "fake network-listener"
 * which in turn will help start light weight listener for these services.
 */
public class DummyNetworkListener implements NetworkListener {
    private String address = "0.0.0.0";
    private String enabled = "true";
    private String name;
    private String port;
    private String protocol;
    private String pool;
    private String transport;
    private String jkEnabled;
    private final List<Property> properties = new ArrayList();

    public DummyNetworkListener() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        address = value;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String value) {
        port = value;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String value) {
        protocol = value;
    }

    public String getThreadPool() {
        return pool;
    }

    public void setThreadPool(String value) {
        pool = value;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String value) {
        transport = value;
    }

    public String getJkEnabled() {
        return jkEnabled;
    }

    public void setJkEnabled(String value) {
        jkEnabled = value;
    }

    public void injectedInto(Object target){}

    @DuckTyped
    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
        throw new UnsupportedOperationException();
    }

    @DuckTyped
    public Protocol findProtocol() {
        return null;
    }

    @DuckTyped
    public Protocol findHttpProtocol() {
        return null;
    }

    @DuckTyped
    public ThreadPool findThreadPool() {
        throw new UnsupportedOperationException();
    }

    @DuckTyped
    public Transport findTransport() {
        throw new UnsupportedOperationException();
    }

    @DuckTyped
    public ConfigBeanProxy getParent() {
        throw new UnsupportedOperationException();
    }

    @DuckTyped
    public <T extends ConfigBeanProxy> T getParent(Class<T> c) {
        throw new UnsupportedOperationException();
    }

    public List<Property> getProperty() {
        return properties;
    }

    public Property getProperty(String name) {
        if (name == null) return null;
        
        for(Property property : properties) {
            if (name.equals(property.getName())) {
                return property;
            }
        }

        return null;
    }

    public String getPropertyValue(String name) {
        return getPropertyValue(name, null);
    }

    public String getPropertyValue(String name, String defaultValue) {
        final Property property = getProperty(name);
        if (property != null) {
            return property.getValue();
        }

        return defaultValue;
    }
}

