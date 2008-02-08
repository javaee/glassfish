/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.javaee.services;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigListener;
import org.glassfish.api.Startup;
import org.glassfish.api.naming.GlassfishNamingManager;
import com.sun.enterprise.config.serverbeans.JdbcResource;

import javax.naming.NamingException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;

/**
 * Binds proxy objects in the jndi namespace for all the JdbcResources defined in the
 * configuration. Those objects will delay the instantiation of the associated
 * resource adapeters until code looks them up in the naming manager.
 *
 * @author Jerome Dochez
 */
@Service
public class ResourceAdaptersBinder implements Startup, PostConstruct, ConfigListener {

    @Inject
    JdbcResource[] resources;

    @Inject
    GlassfishNamingManager manager;

    @Inject
    Logger logger;
    
    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        for (JdbcResource resource : resources) {
            try {
                manager.publishObject(resource.getJndiName(), new ResourceAdapterProxy(resource), true);
            } catch (NamingException e) {
                logger.log(Level.SEVERE, "Cannot bind " + resource.getPoolName() + " to naming manager", e);
            }
        }
    }

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    public void changed(PropertyChangeEvent[] propertyChangeEvents) {
        for (PropertyChangeEvent evt : propertyChangeEvents) {
            System.out.println("evt" + evt.getOldValue() + " : " + evt.getNewValue());
        }
    }
}
