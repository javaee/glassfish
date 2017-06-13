package org.glassfish.jaccApi.programmaticauthentication;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.glassfish.jaccApi.common.ArquillianBase;
import static org.glassfish.jaccApi.common.ArquillianBase.mavenWar;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

/**
 * This tests that a call from a Servlet to HttpServletRequest#authenticate can result
 * in a successful authentication.
 *
 * 
 */
@RunWith(Arquillian.class)
public class ProgrammaticAuthenticationIT extends ArquillianBase {

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }


    @Test
    public void testSubjectPrincipals() throws IOException, SAXException {
        String response = getFromServerPath("public/authenticate");
        assertTrue("Should contain web user test and architect in subject principals", response.contains("Principals: test, architect"));
    }
   

}
