package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.io.File;

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
        System.setProperty("com.sun.aas.javaRoot", System.getProperty("user.home"));
    }


    @Test
    public void testAppRoot() {
        Domain domain = getHabitat().getComponent(Domain.class);
        String appRoot = domain.getApplicationRoot();
        assertTrue(appRoot.startsWith("cafebabe"));
    }

    @Test
    public void testJavaRoot() {
        if (System.getProperty("user.home").contains(File.separator)) {
            JavaConfig config = getHabitat().getComponent(JavaConfig.class);
            String javaRoot = config.getJavaHome();
            assertTrue(javaRoot.indexOf(File.separatorChar)!=-1);
        }
        assertTrue(true);
    }

}
