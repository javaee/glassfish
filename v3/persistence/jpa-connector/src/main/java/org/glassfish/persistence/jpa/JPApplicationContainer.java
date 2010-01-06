/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.persistence.jpa;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

/**
 * Represents Application Container for JPA
 * One instance of this object is created per deployed bundle.
 * @author Mitesh Meswani
 */
public class JPApplicationContainer implements ApplicationContainer {

    /**
     * emfs loaded from this bundle. These emfs are closed in stop()
     */
    List<EntityManagerFactory> emfs;

    // TODO change logger name from DPL_LOGGER to persistence logger
    private static Logger logger = LogDomains.getLogger(PersistenceUnitLoader.class, LogDomains.DPL_LOGGER);

    public JPApplicationContainer(List<EntityManagerFactory> emfs) {
        this.emfs = emfs;
    }

    private void closeAllEMFs() {
        for(EntityManagerFactory emf : emfs) {
            try {
                logger.logp(Level.FINE, "JPApplicationContainer", "closeEMF", "emf = {0}", emf);
                emf.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    //-------------- Begin Methods implementing ApplicationContainer interface -------------- //
    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContxt) {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        closeAllEMFs();
        return true;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    public ClassLoader getClassLoader() {
        //TODO: Check with Jerome. Should this return anything but null? currently it does not seem so.
        return null;
    }

    //-------------- End Methods implementing ApplicationContainer interface -------------- //

}
