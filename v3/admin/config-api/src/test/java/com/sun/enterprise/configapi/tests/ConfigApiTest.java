package com.sun.enterprise.configapi.tests;

import org.glassfish.config.support.GlassFishDocument;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.DomDocument;
import org.junit.Ignore;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 12:38:30 PM
 */
@Ignore
public class ConfigApiTest extends org.glassfish.tests.utils.ConfigApiTest {

    public DomDocument getDocument(Habitat habitat) {
        DomDocument doc = habitat.getByType(GlassFishDocument.class);
        if (doc==null) {
            return new GlassFishDocument(habitat);
        }
        return doc;
    }
}
