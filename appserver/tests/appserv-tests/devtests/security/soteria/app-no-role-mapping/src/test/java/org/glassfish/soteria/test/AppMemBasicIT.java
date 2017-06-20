package org.glassfish.soteria.test;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.AfterClass;
import org.junit.rules.TestWatcher;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@RunWith(Arquillian.class)
public class AppMemBasicIT extends ArquillianBase {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Security::soteria::AppNoRoleMapping");

    @AfterClass
    public static void printSummary(){
      stat.printSummary();
    }
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {        
        return mavenWar();
    }

    @Test
    public void testAuthenticated() {
    	
    	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("reza", "secret1");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
        assertDefaultAuthenticated(
            readFromServer("/servlet"));
    }
    
    @Test
    public void testNotAuthenticated() {
        
        WebResponse response = responseFromServer("/servlet");
        
        assertEquals(401, response.getStatusCode());
        
        assertTrue(
            "Response did not contain the \"WWW-Authenticate\" header, but should have", 
            response.getResponseHeaderValue("WWW-Authenticate") != null);
        
        assertDefaultNotAuthenticated(
            response.getContentAsString());
    }
    
    @Test
    public void testNotAuthenticatedWrongName() {
    	
    	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("romo", "secret1");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
    	WebResponse response = responseFromServer("/servlet");
          
    	assertEquals(401, response.getStatusCode());
          
    	assertTrue(
	        "Response did not contain the \"WWW-Authenticate\" header, but should have", 
	        response.getResponseHeaderValue("WWW-Authenticate") != null);
          
    	assertDefaultNotAuthenticated(
	        response.getContentAsString());
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() {
    	
      	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("reza", "wrongpassword");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
        WebResponse response = responseFromServer("/servlet");
        
        assertEquals(401, response.getStatusCode());
          
        assertTrue(
            "Response did not contain the \"WWW-Authenticate\" header, but should have", 
            response.getResponseHeaderValue("WWW-Authenticate") != null);
          
        assertDefaultNotAuthenticated(
            response.getContentAsString());
    }

}
