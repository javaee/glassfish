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
/*
 * JxtaBackingStoreFactory.java
 *
 * Created on October 6, 2006, 1:09 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.Properties;
import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class JxtaBackingStoreFactory implements BackingStoreFactory {
    
    /** Creates a new instance of JxtaBackingStoreFactory */
    public JxtaBackingStoreFactory() {
    }

    /**
     * This method is called to create a BackingStore that will store
     * <code>SimpleMetadata</code> or <code>CompositeMetadata</code>. This
     * class must be thread safe.
     * <p>
     * The factory must return a fully initialized and operational BackingStore
     * 
     * @param type
     *            The type of data that will be saved (using the
     *            <code>save()</code> method in BackingStore) in the store.
     * @param appId
     *            the application id for which this store is created
     * @param env
     *            Properties that contain any additional configuration paramters
     *            to successfully initialize and use the store.
     * @return a BackingStore. The returned BackingStore will be used only to
     *         store data of type K. The returned BackingStore must be thread
     *         safe.
     * @throws BackingStoreException
     *             If the store could not be created
     */
    public <K extends Metadata> BackingStore<K> createBackingStore(
            Class<K> type, String appId, Properties env)
            throws BackingStoreException {
        JxtaBackingStoreImpl backingStore = new JxtaBackingStoreImpl(appId, env);
        if(type.getName().equalsIgnoreCase(CompositeMetadata.class.getName())) {
            backingStore.setCompositeBackingStore(true);
        }
        //return new JxtaBackingStoreImpl(appId, env);
        return backingStore;
    }
    
    //FIXME remove after testing
    //env will contain props from LifecycleModule
    public BackingStore createSimpleStore(
			String appId,
			Properties env) {
        return new JxtaBackingStoreImpl(appId, env);
    }
    
    //env will contain props from LifecycleModule
    public BackingStore createCompositeStore(
			String appId,
			Properties env) {
        //FIXME do composite impl later
        return new JxtaBackingStoreImpl(appId, env);
    }    

    //env will contain props from LifecycleModule
    public BatchBackingStore createBatchBackingStore(
                        Properties env) {
        // FIXME
        return null;
    }    
    
}
