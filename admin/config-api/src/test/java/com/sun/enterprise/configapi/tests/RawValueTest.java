package com.sun.enterprise.configapi.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.glassfish.config.support.GlassFishConfigBean;
import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 30, 2008
 * Time: 11:18:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class RawValueTest extends ConfigApiTest {

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
        Domain rawDomain = GlassFishConfigBean.getRawView(domain);
        String appRoot = domain.getApplicationRoot();
        String appRawRoot = rawDomain.getApplicationRoot();
        assertFalse(appRawRoot.equals(appRoot));
        assertTrue(appRawRoot.startsWith("${"));
    }    
}
