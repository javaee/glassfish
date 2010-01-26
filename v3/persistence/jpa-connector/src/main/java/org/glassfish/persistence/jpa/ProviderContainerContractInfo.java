/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

import javax.persistence.spi.ClassTransformer;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.naming.NamingException;
import javax.validation.ValidatorFactory;

import org.glassfish.api.deployment.DeploymentContext;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;


/**
 * @author Mitesh Meswani
 * This encapsulates information needed  to load or unload persistence units.
 */
public interface ProviderContainerContractInfo {

    /**
     *
     * @return a class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getClassLoader();

    /**
     *
     * @return a temp class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getTempClassloader();

    /**
     *
     * Adds ClassTransformer to underlying Application's classloader
     */
    void addTransformer(ClassTransformer transformer);


    /**
     * @return absolute path of the location where application is exploded.
     */
    String getApplicationLocation();

    /**
     * Looks up DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws javax.naming.NamingException
     */
    DataSource lookupDataSource(String dataSourceName) throws NamingException;

    /**
     * Looks up Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws NamingException
     */
    DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException;

    /**
     * get instance of ValidatorFactory for this environment
     */
    ValidatorFactory getValidatorFactory();

    /**
     * Answers whether an application is being deployed
     * @return true if application is being deployed false otherwise
     */
    boolean isDeploy();

    /**
     * @return DeploymentContext associated with this instance.
     */
    DeploymentContext getDeploymentContext();


    /**
     * Register the give emf with underlying container
     * @param unitName Name of correspoding PersistenceUnit
     * @param persistenceRootUri URI within application (excluding META-INF) for root of corresponding PersistenceUnit
     * @param containingBundle The bundle that contains PU for the given EMF
     * @param emf The emf that needs to be registered
     */
    void registerEMF(String unitName, String persistenceRootUri, RootDeploymentDescriptor containingBundle, EntityManagerFactory emf);

    /**
     * Returns JTA DataSource override if any
     */
    String getJTADataSourceOverride();
}
