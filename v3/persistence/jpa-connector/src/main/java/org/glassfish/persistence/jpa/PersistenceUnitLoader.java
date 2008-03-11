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


package org.glassfish.persistence.jpa;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.ClassTransformer;
import javax.sql.DataSource;
import java.util.Collection;

/**
 * This is used by {@link JPADeployer} to load persistence units 
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface PersistenceUnitLoader {
    /**
     * This encapsulates information needed by {@link PersistenceUnitLoader}
     * to load or unload persistence units.
     */
    interface ApplicationInfo {

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
         * @return Adds ClassTransformer to underlying Application's classloader
         */
        public void addTransformer(ClassTransformer transformer);


        /**
         * @return absolute path of the location where application is exploded.
         */
        String getApplicationLocation();

        /**
         * This method returns the precise list of PUs that are
         * referenced by the components of this application.
         * A component references by a PU using one of the four
         * methods: @PersistenceContext, @PersistenceUnit,
         * <persistence-context-ref> and <persistence-unit-ref>.
         * @return list of PU that are actually referenced by this application.
         */
        Collection<? extends PersistenceUnitDescriptor> getReferencedPUs();

        /**
         * Returns the EntityManagerFactories that needs to be closed.
         *
         * @return the list of EMFs that need to be closed.
         */
        Collection<? extends EntityManagerFactory> getEntityManagerFactories();

        /**
         * Looks up DataSource with JNDI name given by <code>dataSourceName</code>
         * @param dataSourceName
         * @return DataSource with JNDI name given by <code>dataSourceName</code> 
         * @throws NamingException
         */
        DataSource lookupDataSource(String dataSourceName) throws NamingException;

        /**
         * Looks up Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
         * @param dataSourceName
         * @return Non transactional DataSource with JNDI name given by <code>dataSourceName</code> 
         * @throws NamingException
         */
        DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException;
    }

    /**
     * Load all the persistence units contained inside the application described
     * by ApplicationInfo object. Loading involves calling the {@link
     * javax.persistence.spi.PersistenceProvider} to create an {@link
     * javax.persistence.EntityManagerFactory} and registering the {@link
     * javax.persistence.EntityManagerFactory} at the appropriate level in
     * {@link Application} descriptor object tree.
     */
    void load();

    /**
     * Unload all the persistence units contained inside the application
     * described by ApplicationInfo object. Unloading involves calling {@link
     * javax.persistence.EntityManagerFactory#close()} for each {@link
     * javax.persistence.EntityManagerFactory} in {@link Application} descriptor
     * object tree.
     */
    void unload();
}
