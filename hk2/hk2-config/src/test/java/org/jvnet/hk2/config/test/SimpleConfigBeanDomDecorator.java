package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;

import javax.xml.stream.XMLStreamReader;

@Service
public class SimpleConfigBeanDomDecorator
    implements DomDecorator<SimpleConfigBeanWrapper> {

    @Override
    public Dom decorate(Habitat habitat, DomDocument document, SimpleConfigBeanWrapper parent, ConfigModel model, XMLStreamReader in) {
        return new SimpleConfigBeanWrapper(habitat, document, (SimpleConfigBeanWrapper) parent, model, in);
    }
}
