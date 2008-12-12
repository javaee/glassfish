/*
 * JAXWSServletModule.java
 *
 * Created on June 19, 2007, 5:51 PM
 * @author Mike Grogan
 */

package org.glassfish.webservices;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ResourceInjector;
import javax.servlet.ServletContext;
import com.sun.xml.ws.transport.http.servlet.ServletModule;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.WSEndpoint;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementation of JAX-WS ServletModule SPI used by WSIT WS-MetadataExchange.
 * In the current 109 design, each endpoint has a unique JAXWSContainer.  On
 * the other hand, the requirements imposed by WSIT WS-MetadataExchange
 * require that all endpoints sharing a context root share a ServletMoule.
 * Therefore, in general, multiple JAXWSContainers will share a JAXWSServletModule,
 * so JAXWSContainer must use a lookup in the static 
 * <code>JAXWSServletModule.modules</code> to find its associatiated module. 
 */

public class JAXWSServletModule extends ServletModule {
    
    //Map of context-roots to JAXWSServletModules
    private final static Hashtable<String, JAXWSServletModule> modules = 
            new Hashtable<String, JAXWSServletModule>();  
    
    //Map of uri->BoundEndpoint used to implement getBoundEndpoint.  Map is rather
    //than Set, so that when a new endpoint is redeployed at a given uri, the old
    //endpoint will be replaced by the new endpoint.  The values() method of the
    //field is returned by <code>getBoundEndpoints</code>.
     private final Hashtable<String, BoundEndpoint> endpoints = 
             new Hashtable<String, BoundEndpoint>();
     
    //the context-root for endpoints belonging to this module.
    private final String contextPath;
    
         
    public static synchronized JAXWSServletModule getServletModule(String contextPath) {
        
        JAXWSServletModule ret = modules.get(contextPath);
        if (ret == null) {
            ret = new JAXWSServletModule(contextPath);
            modules.put(contextPath, ret);
        }
        return ret;
    } 
    
    private JAXWSServletModule(String contextPath) {
            this.contextPath = contextPath;
    }
  
    public void addEndpoint(String uri, ServletAdapter adapter) {
        endpoints.put(uri, adapter);
    }
    
    public @NotNull List<BoundEndpoint> getBoundEndpoints() {
            return new ArrayList(endpoints.values());
    }

    public @NotNull String getContextPath() {
        return contextPath;
    }
    
   
}
