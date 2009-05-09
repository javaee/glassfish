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


import org.glassfish.api.container.CompositeSniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;


/**
 * Sniffer handling ears
 *
 * @author Mitesh Meswani
 */
@Service(name = "jpaCompositeSniffer")
@Scoped(Singleton.class)
public class JPACompositeSniffer extends JPASniffer implements CompositeSniffer {

    /**
     * Decides whether we have any pu roots at ear level
     */
    public boolean handles(DeploymentContext context) {
        // Scans for pu roots in the "lib" dir of an application.
        // We do not scan for PU roots in root of .ear. JPA 2.0 spec will clarify that it is  not a portable use case.
        // It is not portable use case because JavaEE spec implies that jars in root of ears are not visible by default
        // to components (Unless an explicit Class-Path manifest entry is present) and can potentially be loaded by
        // different class loaders (corresponding to each component that refers to it) thus residing in different name
        // space. It does not make sense to make them visible at ear level (and thus in a single name space)
        boolean isJPAApplication = false;
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        ReadableArchive appRoot = context.getSource();
        if (holder != null && holder.app != null) {
            isJPAApplication = scanForPURootsInLibDir(appRoot, holder.app.getLibraryDirectory());
        }
        return isJPAApplication;
    }
}