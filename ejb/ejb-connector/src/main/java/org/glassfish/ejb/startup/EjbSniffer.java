/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.ejb.startup;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import com.sun.enterprise.deployment.annotation.introspection.EjbComponentAnnotationScanner;

import org.glassfish.api.deployment.archive.ReadableArchive;

import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.lang.annotation.Annotation;

/**
 * Implementation of the Sniffer for the Ejb container.
 * 
 * @author Mahesh Kannan
 */
@Service(name="Ejb")
@Scoped(Singleton.class)
public class EjbSniffer  extends GenericSniffer implements Sniffer {

    private static final Class[]  ejbAnnotations = new Class[] {
            javax.ejb.Stateless.class, javax.ejb.Stateful.class};

    public EjbSniffer() {
        this("ejb", "META-INF/ejb-jar.xml", null);
    }
    
    public EjbSniffer(String containerName, String appStigma, String urlPattern) {
        super(containerName, appStigma, urlPattern);
    }    

    final String[] containers = {
            "org.glassfish.ejb.startup.EjbContainerStarter",
    };
        
    public String[] getContainersNames() {
        return containers;
    }    

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        boolean result = super.handles(location, loader);    //Check ejb-jar.xml

        if (result == false) {
            try {
                result = location.exists("WEB-INF/ejb-jar.xml");
                if (result == false) {//Else scan for annotations
                    AnnotationDetector detector =
                           new AnnotationDetector(new EjbComponentAnnotationScanner());
                    result = detector.hasAnnotationInArchive(location);
                }
            } catch (IOException ioEx) {
                //TODO
            }
        }

        return result;
    }

    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return ejbAnnotations;
    }

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }
}
