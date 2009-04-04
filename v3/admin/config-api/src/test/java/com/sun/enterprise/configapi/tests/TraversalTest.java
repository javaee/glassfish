package com.sun.enterprise.configapi.tests;

import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.component.Habitat;
import org.junit.Ignore;
import org.junit.Test;
import org.glassfish.api.admin.config.Property;

import java.util.Set;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Traverse a config tree using the hk2 model APIs.
 *
 * @author Jerome Dochez
 */
public class TraversalTest extends ConfigApiTest {

    static Logger logger = Logger.getAnonymousLogger();

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void traverse() {
        Habitat habitat = super.getHabitat();
        Domain domain = habitat.getComponent(Domain.class);
        introspect(0, Dom.unwrap(domain));
    }


    @Ignore
    private void introspect(int indent, Dom proxy) {
        indent = indent + 1;
        Set<String> ss = proxy.getAttributeNames();
        String id = "";
        for (int i = 0; i < indent; i++) {
            id = id + "    ";
        }
        logger.fine(id + "--------" + proxy.model.key);
        for (String a : ss) {

            logger.fine(id + a + "=" + proxy.attribute(a));
        }


        Set<String> elem = proxy.getElementNames();

        for (String bb : elem) {


            logger.fine(id + "<" + bb + ">");
            org.jvnet.hk2.config.ConfigModel.Property prop = proxy.model.getElement(bb);
            if (prop != null && proxy.model.getElement(bb).isLeaf()) {
                logger.fine(proxy.leafElement(bb));
            } else {
                introspect(indent, proxy.element(bb));
            }
            logger.fine(id + "</" + bb + ">");
            logger.fine("    ");

        }
    }
}
