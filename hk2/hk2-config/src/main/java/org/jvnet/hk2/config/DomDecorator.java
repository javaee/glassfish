package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;

import javax.xml.stream.XMLStreamReader;

@Contract
public interface DomDecorator<T extends Dom> {
    
    public Dom decorate(Habitat habitat, DomDocument document, T parent, ConfigModel model, XMLStreamReader in);

}
