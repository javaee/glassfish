package org.jvnet.hk2.config.test;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDecorator;
import org.jvnet.hk2.config.DomDocument;

import javax.xml.stream.XMLStreamReader;

@Service
public class SimpleConfigBeanDomDecorator
    implements DomDecorator<SimpleConfigBeanWrapper> {

    @Override
    public Dom decorate(ServiceLocator habitat, DomDocument document, SimpleConfigBeanWrapper parent, ConfigModel model, XMLStreamReader in) {
        return new SimpleConfigBeanWrapper(habitat, document, (SimpleConfigBeanWrapper) parent, model, in);
    }
}
