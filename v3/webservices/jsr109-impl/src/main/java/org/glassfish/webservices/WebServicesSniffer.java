package org.glassfish.webservices;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.v3.deployment.GenericSniffer;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * This is the Sniffer for Webservices
 * @author Bhakti Mehta
 */
@Service(name="webservices")
public class WebServicesSniffer extends GenericSniffer {

    final String[] containers = { "org.glassfish.webservices.WebServicesContainer" };

    public WebServicesSniffer() {
        super("webservices", "WEB-INF/web.xml", null);
    }


    /**
     * Returns the list of Containers that this Sniffer enables.
     * <p/>
     * The runtime will look up each container implementing
     * using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames() {
        return containers;
    }

     public java.lang.Class<? extends java.lang.annotation.Annotation>[] getAnnotationTypes() {
         ArrayList webserviceAnnotations = new ArrayList();
         webserviceAnnotations.add(javax.jws.WebService.class);
         webserviceAnnotations.add(javax.xml.ws.WebServiceProvider.class);
         return (Class<? extends java.lang.annotation.Annotation>[]) webserviceAnnotations.toArray();

         
     }
}