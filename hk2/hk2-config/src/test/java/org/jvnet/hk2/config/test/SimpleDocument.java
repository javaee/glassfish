package org.jvnet.hk2.config.test;

import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.OnDeleteCascade;

public class SimpleDocument extends DomDocument<ConfigBean>{

    public SimpleDocument(ServiceLocator habitat) {
        super(habitat);
    }

    @Override
    public ConfigBean make(final ServiceLocator habitat, XMLStreamReader xmlStreamReader,
            ConfigBean dom, ConfigModel configModel) {
        ConfigBean configBean = new ConfigBean(habitat,this, dom, configModel, xmlStreamReader);
        configBean.addInterceptor(Object.class, new OnDeleteCascade());
        return configBean;
    }
}
