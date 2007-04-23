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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.module;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Registry for RepositoryFactory instances
 * 
 * @author Jerome Dochez
 */
public class RepositoryFactories {
    
    private static RepositoryFactories _singleton = new RepositoryFactories();
    
    private ArrayList<RepositoryFactory> factories = new ArrayList();
    
    /**
     * Return the instance holding registered repository factories
     * @return the instance holding factories
     */
    public static RepositoryFactories getInstance() {
        return _singleton;        
    }
    
    /** Creates a new instance of RepositoriesFactory */
    private RepositoryFactories() {
    }
    
    /**
     * Add a new <code> RepositoryFactory </code> to the list of 
     * repository factories. 
     * @param factory the new factory to add
     */
    public void addRepositoryFactory(RepositoryFactory factory) {        
        factories.add(factory);
    }
    
    /**
     * Returns an interator of registered <code>ReposistoryFactory</code>
     * @return an iterator or registered factories
     */
    public Iterator<RepositoryFactory> getFactories() {
        return factories.iterator();
    }
    
    /**
     * Returns a <code>RespositoryFactory</code> factory instance 
     * capable of creating <code>Repository</code> repositories of 
     * the provided type
     * @param type type of the repository we request the RepositoryFactory
     */
    public RepositoryFactory getFactoryFor(String type) {
        return null;
    }
}
