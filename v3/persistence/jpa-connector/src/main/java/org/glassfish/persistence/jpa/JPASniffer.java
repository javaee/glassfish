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


import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.InputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;


/**
 * Implementation of the Sniffer for JPA.
 *
 * @author Mitesh Meswani
 */
@Service(name="jpa")
@Scoped(Singleton.class)
public class JPASniffer  extends GenericSniffer implements Sniffer {

    private static final String[] containers = { "org.glassfish.persistence.jpa.JPAContainer" };
    private static final Class[]  handledAnnotations = new Class[] {javax.persistence.PersistenceContext.class,
            javax.persistence.PersistenceContexts.class,javax.persistence.PersistenceUnit.class,
            javax.persistence.PersistenceUnits.class};


    public JPASniffer() {
        // We do not haGenericSniffer(String containerName, String appStigma, String urlPattern
        super("jpa", null /* appStigma */, null /* urlPattern */);
    }

    /**
     * Returns true if the archive contains persistence.xml as defined by packaging rules of JPA
     * Curently only scans for persitsece.xml in WEB-INF/classes/META-INF
     * TODO : Enhance this to handle all the cases
     */
    @Override public boolean handles(ReadableArchive location, ClassLoader loader) {
        boolean isJPAArchive = false;
        InputStream is;
        try {
            is = location.getEntry("WEB-INF/classes/META-INF/persistence.xml");
            if (is != null) {
                is.close();
                isJPAArchive = true;
            }
        } catch (IOException e) {
            // ignore
        }
        return isJPAArchive;
    }

    @Override public Class<? extends Annotation>[] getAnnotationTypes() {
        //TODO: Implementing handles that scans the whole package might be very expensive
        // Discuss with Jerome, would it more efficient if we scan for @Entiy in an application and if present try to
        // do the heavy lifting in JPAContainer
        return handledAnnotations;
    }

    public String[] getContainersNames() {
        return containers;
    }
}

