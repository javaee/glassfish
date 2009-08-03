/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.webservices;

import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

import javax.xml.ws.WebServiceRef;
import java.util.ArrayList;
import java.util.Enumeration;
import java.lang.annotation.Annotation;
import java.io.IOException;

/**
 * This is the Sniffer for Webservices
 * @author Bhakti Mehta
 */
@Service(name="webservices")
@Scoped(Singleton.class)
public class WebServicesSniffer extends GenericSniffer {

    private static final Class[]  handledAnnotations = new Class[] {javax.jws.WebService.class,
            javax.xml.ws.WebServiceProvider.class, javax.xml.ws.WebServiceRef.class};

    final String[] containers = { "org.glassfish.webservices.WebServicesContainer" };

    public WebServicesSniffer() {
        super("webservices", null, null);
    }

    /**
     * .ear (the resource can be present in lib dir of the ear)
     * Returns true if the archive contains webservices.xml either in WEB-INF or META-INF directories
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        return isEntryPresent(location, "WEB-INF/webservices.xml") ||
                isEntryPresent(location, "META-INF/webservices.xml");
    }

    private boolean isEntryPresent(ReadableArchive location, String entry) {
        boolean entryPresent = false;
        try {
            entryPresent = location.exists(entry);
        } catch (IOException e) {
            // ignore
        }
        return entryPresent;
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
        return handledAnnotations;


    }

    public boolean isUserVisible() {
        return true;
    }

    @Override
    public String[] getURLPatterns() {
//        // anything finishing with jsp or jspx
//        return new String[] { "*.jsp", "*.jspx" };
        return null;
    }

}
