package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.admin.ConfigBean;

import javax.xml.stream.XMLStreamReader;

/**
 * plug our DomElement implementation
 * 
 */
public class GlassFishDocument extends DomDocument {

    public GlassFishDocument(Habitat habitat) {
        super(habitat);
    }

    public Dom make(Habitat habitat, XMLStreamReader xmlStreamReader, Dom dom, ConfigModel configModel) {
        return new ConfigBean(habitat,this, dom, configModel, xmlStreamReader);
    }
}
