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

package com.sun.ejb.base.sfsb.initialization;

import java.io.File;

import java.util.logging.Level;

import java.util.HashMap;
import java.util.Map;

import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.container.SFSBContainerInitialization;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;

import com.sun.ejb.base.sfsb.store.FileStoreManager;
import com.sun.ejb.base.sfsb.store.FileStoreManagerConstants;

import com.sun.enterprise.util.io.FileUtils;

import com.sun.ejb.base.container.util.CacheProperties;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

/**
 * Class to initialize Container with File based persistence
 *
 * @author Mahesh Kannan
 */
@Service(name = "file")
public class FileStrategyBuilder
        extends AbstractPersistenceStrategyBuilder {

    @Inject
    CacheProperties cacheProps;

    /**
     * Creates a new instance of FileStrategyBuilder
     */
    public FileStrategyBuilder() {
    }

    public void initializePersistenceStrategy(
            SFSBContainerInitialization container, EjbDescriptor descriptor) {
        try {
            super.initializeStrategy(container, descriptor, cacheProps);

            SFSBStoreManager storeManager = createStoreManager(
                    container, descriptor);
            container.setSFSBStoreManager(storeManager);

        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Could not initialize container "
                    + "using FilestrategyBuilder", th);
        }
    }

    private SFSBStoreManager createStoreManager(
            SFSBContainerInitialization container, EjbDescriptor descriptor) {
        //Step 1. Create the StoreManager
        FileStoreManager manager = new FileStoreManager();

        //Step 2. Create a Map that contains store specific paramteters
        Map map = new HashMap();
        map.put(FileStoreManagerConstants.STORE_MANAGER_NAME,
                descriptor.getName());

        map.put(FileStoreManagerConstants.CONTAINER_ID,
                new Long(descriptor.getUniqueId()));

        this.cacheProps.init(descriptor);

        map.put(FileStoreManagerConstants.SESSION_TIMEOUT_IN_SECONDS,
                new Integer(cacheProps.getRemovalTimeoutInSeconds()));

        map.put(FileStoreManagerConstants.GRACE_SESSION_TIMEOUT_IN_SECONDS,
                new Integer(super.getRemovalGracePeriodInSeconds()));

        map.put(FileStoreManagerConstants.PASSIVATION_DIRECTORY_NAME,
                getPassivationDirectoryName(descriptor));

        //Step 3. Initialize the store with the Map
        _logger.log(Level.FINE, "Initialzing FileStoreManager with: " + map);
        manager.initSessionStore(map);

        _logger.log(Level.FINE, "INITIALZED FileStoreManager: " + manager);
        return manager;
    }

    private String getPassivationDirectoryName(EjbDescriptor desc) {

        String passivationDirName = null;

        try {

            // application object associated with this ejb
            Application application = desc.getApplication();
            String dirName = null;
            String componentSeparator = "_";
            if (application.isVirtual()) {
                // ejb is part of a stand alone ejb module
                String archURI = desc.getEjbBundleDescriptor().
                        getModuleDescriptor().getArchiveUri();
                passivationDirName = cacheProps.getPassivationStorePath()
                        + File.separator + FileUtils.makeFriendlyFilename(archURI)
                        + componentSeparator + desc.getName();
            } else {
                // ejb is part of an application
                passivationDirName = cacheProps.getPassivationStorePath()
                        + File.separator + application.getRegistrationName()
                        + componentSeparator + desc.getName()
                        + componentSeparator + desc.getUniqueId();
            }

        } catch (Throwable th) {
            _logger.log(Level.SEVERE,
                    "ejb.sfsb_helper_get_passivation_dir_failed", th);
        }

        return passivationDirName;
    }

}
    
