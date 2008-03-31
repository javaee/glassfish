package org.glassfish.tests.utils;

import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.config.support.GlassFishConfigBean;
import org.junit.Ignore;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * This document will create the appropriate ConfigBean implementation but will
 * not save the modified config tree.
 *
 * User: Jerome Dochez
 */
@Ignore
public class TestDocument extends DomDocument {

    public TestDocument(Habitat habitat) {
        super(habitat);
    }

    public Dom make(final Habitat habitat, XMLStreamReader xmlStreamReader, Dom dom, ConfigModel configModel) {
        // by default, people get the translated view.
        return new GlassFishConfigBean(habitat,this, dom, configModel, xmlStreamReader);
    }
}
