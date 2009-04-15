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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.persistence.jpa;


import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.net.URL;


/**
 * Implementation of the Sniffer for JPA.
 *
 * @author Mitesh Meswani
 */
@Service(name="jpaSniffer")
@Scoped(Singleton.class)
public class JPASniffer  extends GenericSniffer implements Sniffer {

    private static final String[] containers = { "org.glassfish.persistence.jpa.JPAContainer" };

    public JPASniffer() {
        // We do not haGenericSniffer(String containerName, String appStigma, String urlPattern
        super("jpa", null /* appStigma */, null /* urlPattern */);
    }

    private static char SEPERATOR_CHAR = '/';
    private static final String META_INF_PERSISTENCE_XML = "META-INF" + SEPERATOR_CHAR + "persistence.xml";
    /**
     * Returns true if the archive contains persistence.xml as defined by packaging rules of JPA
     * Tries to getResource("META-INF/persitsence.xml") on curernt classLoader. If it succeeds, currrent archive is a pu
     * root.
     * This method will be called for each bundle inside an application which would include
     * .war (the resource can be present in WEB-INF/classes or WEB-INF/lib/pu.jar),
     * ejb.jar (the resource can be present in root of the jar),
     * .ear (the resource can be present in lib dir of the ear)
     */
    @Override
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        // scan for persistence.xml in META-INF/persitennce.xml If at least one is found, this is
        // a jpa archive
        URL persistencexmlURL = loader.getResource(META_INF_PERSISTENCE_XML);
        return  persistencexmlURL != null; 
    }

    public String[] getContainersNames() {
        return containers;
    }
}

