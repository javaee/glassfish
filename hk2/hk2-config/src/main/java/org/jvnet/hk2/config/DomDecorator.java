package org.jvnet.hk2.config;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

import javax.xml.stream.XMLStreamReader;

@Contract
public interface DomDecorator<T extends Dom> {
    
    public Dom decorate(ServiceLocator habitat, DomDocument document, T parent, ConfigModel model, XMLStreamReader in);

}
