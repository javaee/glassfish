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
package org.glassfish.hk2.configuration.hub.internal;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;

/**
 * @author jwells
 *
 */
public class WriteableBeanDatabaseImpl implements WriteableBeanDatabase {
    private final long baseRevision;
    private final HashMap<String, WriteableTypeImpl> types = new HashMap<String, WriteableTypeImpl>();
    private final HubImpl hub;
    
    private final LinkedList<Change> changes = new LinkedList<Change>();
    private final LinkedList<WriteableTypeImpl> removedTypes = new LinkedList<WriteableTypeImpl>();
    private boolean committed = false;
    
    /* package */ WriteableBeanDatabaseImpl(HubImpl hub, BeanDatabaseImpl currentDatabase) {
        this.hub = hub;
        baseRevision = currentDatabase.getRevision();
        
        for (Type type : currentDatabase.getAllTypes()) {
            types.put(type.getName(), new WriteableTypeImpl(this, (TypeImpl) type));
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getAllTypes()
     */
    @Override
    public synchronized Set<Type> getAllTypes() {
        return Collections.unmodifiableSet(new HashSet<Type>(types.values()));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getType(java.lang.String)
     */
    @Override
    public synchronized Type getType(String type) {
        return types.get(type);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#getInstance(java.lang.String, java.lang.Object)
     */
    @Override
    public synchronized Instance getInstance(String type, String instanceKey) {
        Type t = getType(type);
        if (t == null) return null;
        
        return t.getInstance(instanceKey);
    }
    
    private void checkState() {
        if (committed) throw new IllegalStateException("This database has already been committed");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#addType(java.lang.String)
     */
    @Override
    public synchronized WriteableType addType(String typeName) {
        if (typeName == null) throw new IllegalArgumentException();
        checkState();
        
        WriteableTypeImpl wti = new WriteableTypeImpl(this, typeName);
        
        changes.add(new ChangeImpl(Change.ChangeCategory.ADD_TYPE,
                                   wti,
                                   null,
                                   null,
                                   null));
        
        types.put(typeName, wti);
        
        return wti;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#removeType(java.lang.String)
     */
    @Override
    public synchronized Type removeType(String typeName) {
        if (typeName == null) throw new IllegalArgumentException();
        checkState();
        
        WriteableTypeImpl retVal = types.remove(typeName);
        if (retVal == null) return null;
        
        Map<String, Instance> instances = retVal.getInstances();
        for (String key : new HashSet<String>(instances.keySet())) {
            retVal.removeInstance(key);
        }
        
        changes.add(new ChangeImpl(Change.ChangeCategory.REMOVE_TYPE,
                retVal,
                null,
                null,
                null));
        
        removedTypes.add(retVal);
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#getWriteableType(java.lang.String)
     */
    @Override
    public synchronized WriteableType getWriteableType(String typeName) {
        checkState();
        return types.get(typeName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#findOrAddWriteableType(java.lang.String)
     */
    @Override
    public synchronized WriteableType findOrAddWriteableType(String typeName) {
        if (typeName == null) throw new IllegalArgumentException();
        checkState();
        
        WriteableTypeImpl wti = types.get(typeName);
        if (wti == null) {
            return addType(typeName);
        }
        
        return wti;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#commit()
     */
    @Override
    public void commit() {
        commit(null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase#commit()
     */
    @Override
    public void commit(Object commitMessage) {
        synchronized (this) {
            checkState();
        
            committed = true;
        }
        
        // Outside of lock
        hub.setCurrentDatabase(this, commitMessage, changes);
        
        for (WriteableTypeImpl removedType : removedTypes) {
            removedType.getHelper().dispose();
        }
        
        removedTypes.clear();
    }
    
    /* package */ long getBaseRevision() {
        return baseRevision;
    }
    
    /* package */ synchronized void addChange(Change change) {
        changes.add(change);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#dumpDatabase()
     */
    @Override
    public void dumpDatabase() {
        dumpDatabase(System.err);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.BeanDatabase#dumpDatabase(java.io.PrintStream)
     */
    @Override
    public synchronized void dumpDatabase(PrintStream output) {
        Utilities.dumpDatabase(this, output);        
    }

}
