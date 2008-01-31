package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Simple test for translated values access
 *
 * @author Jerome Dochez
 */
public class TranslatedValuesTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setup() {
        System.setProperty("com.sun.aas.instanceRoot", "cafebabe");
    }


    @Test
    public void testAppRoot() {
        Domain domain = getHabitat().getComponent(Domain.class);
        String appRoot = domain.getApplicationRoot();
        assertTrue(appRoot.startsWith("cafebabe"));
    }
}
