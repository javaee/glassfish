/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.configuration.hub.test;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.ManagerUtilities;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.configuration.hub.internal.HubImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Some Hub error case tests
 * @author jwells
 *
 */
public class NegativeHubTest extends HK2Runner {
    private final static String ERROR_TYPE = "ErrorType";
    private final static String ERROR_NAME = "ErrorName";
    private Hub hub;
    
    @Before
    public void before() {
        super.before();
        
        // This is necessary to make running in an IDE easier
        ManagerUtilities.enableConfigurationHub(testLocator);
        
        this.hub = testLocator.getService(Hub.class);
    }
    
    /**
     * Null in addType is a fail
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullTypeInAdd() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.addType(null);
    }
    
    /**
     * Null in removeType is a fail
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullTypeInRemove() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.removeType(null);
    }
    
    /**
     * Null in findOrAddType is a fail
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullTypeInFindOrAdd() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.findOrAddWriteableType(null);
    }
    
    /**
     * Use addType after commit
     */
    @Test(expected=IllegalStateException.class)
    public void testInvalidStatePostCommitAdd() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.commit();
        
        wbd.addType(ERROR_TYPE);
    }
    
    /**
     * Use addType after commit
     */
    @Test(expected=IllegalStateException.class)
    public void testInvalidStatePostCommitRemove() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.commit();
        
        wbd.removeType(ERROR_TYPE);
    }
    
    /**
     * Use addType after commit
     */
    @Test(expected=IllegalStateException.class)
    public void testInvalidStatePostCommitFind() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.commit();
        
        wbd.findOrAddWriteableType(ERROR_TYPE);
    }
    
    /**
     * Try to commit twice
     */
    @Test(expected=IllegalStateException.class)
    public void testDoubleCommit() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        wbd.commit();
        
        wbd.commit();
    }
    
    /**
     * Null as name for addInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAddInstanceWithNullKey() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.addInstance(null, new HashMap<String, Object>());
    }
    
    /**
     * Null as bean for addInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAddInstanceWithNullValue() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.addInstance(ERROR_NAME, null);
    }
    
    /**
     * Null as key and bean for addInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAddInstanceWithNullBoth() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.addInstance(null, null);
    }
    
    /**
     * Null as name for addInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRemoveInstanceWithNullKey() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.removeInstance(null);
    }
    
    /**
     * Null as name for modifyInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testModifyInstanceWithNullKey() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.modifyInstance(null, new HashMap<String, Object>());
    }
    
    /**
     * Null as bean for modifyInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testModifyInstanceWithNullValue() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.modifyInstance(ERROR_NAME, null);
    }
    
    /**
     * Null as key and bean for modifyInstance
     */
    @Test(expected=IllegalArgumentException.class)
    public void testModifyInstanceWithNullBoth() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        wt.modifyInstance(null, null);
    }
    
    /**
     * Attempt to modify an instance that does not exist
     */
    @Test(expected=IllegalStateException.class)
    public void testModifyInstanceThatDoesNotExist() {
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        WriteableType wt = wbd.addType(ERROR_TYPE);
        HashMap<String, Object> bean = new HashMap<String, Object>();
        
        wt.modifyInstance(ERROR_NAME, bean,
                new PropertyChangeEvent(bean, ERROR_NAME, "", ""));
    }

}
