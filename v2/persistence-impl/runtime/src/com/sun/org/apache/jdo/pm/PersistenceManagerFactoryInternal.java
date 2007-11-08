/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * PersistenceManagerFactoryInternal.java
 *
 * Create on March 3, 2000
 */

package com.sun.org.apache.jdo.pm;


import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.store.TranscriberFactory;
import com.sun.persistence.support.PersistenceManager;
import com.sun.persistence.support.PersistenceManagerFactory;


/**
* JDORI-internal PMF.
*
* @author Dave Bristor
*/
public interface PersistenceManagerFactoryInternal
    extends PersistenceManagerFactory {

    /**
    * Provides the factory which can make Transcribers for this PMF.
    * @return A TranscriberFactory particular to a kind of PMF.
    */
    public TranscriberFactory getTranscriberFactory();

    /**
    * In order for the application to construct instance of the ObjectId
    * class it needs to know the class being used by the JDO implementation.
    * @param cls the PersistenceCapable Class
    * @return the Class of the ObjectId of the parameter
    */
    public Class getObjectIdClass(Class cls);

    /**
    * Provides a StoreManager that is ready to accept operations on it such
    * as insert, etc.
    * @param pm PersistenceManager that is requesting a StoreManager.
    */
    public StoreManager getStoreManager(PersistenceManager pm);

    /**
    * Allows the PMF to release any resources associated with the given PM's
    * store manager.
    * @param pm PersistenceManager that is releasing a StoreManager.
    */
    public void releaseStoreManager(PersistenceManager pm);

    /**
    * Returns store-specific mapping between Java classes and tracked SCO
    * classes supported by this PMF. Called by PersistenceManager inside
    *  requests for a new tracked instance.
    * @param type Class to find mapping for.
    * @return A Class for the tracked SCO or null if this Java class is not
    * supported as tracked SCO.
    */
    public Class getTrackedClass(Class type);

}
