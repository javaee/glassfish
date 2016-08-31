/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.BeanDatabaseUpdateListener;
import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.CommitFailedException;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.PrepareFailedException;
import org.glassfish.hk2.configuration.hub.api.RollbackFailedException;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
@ContractsProvided(Hub.class)
@Visibility(DescriptorVisibility.LOCAL)
public class HubImpl implements Hub {
    private static final AtomicLong revisionCounter = new AtomicLong(1);
    
    private final Object lock = new Object();
    private BeanDatabaseImpl currentDatabase = new BeanDatabaseImpl(revisionCounter.getAndIncrement());
    
    @Inject
    private IterableProvider<BeanDatabaseUpdateListener> listeners;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.Hub#getCurrentDatabase()
     */
    @Override
    public BeanDatabase getCurrentDatabase() {
        synchronized (lock) {
            return currentDatabase;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.hub.api.Hub#getWriteableDatabaseCopy()
     */
    @Override
    public WriteableBeanDatabase getWriteableDatabaseCopy() {
        synchronized (lock) {
            return new WriteableBeanDatabaseImpl(this, currentDatabase);
        }
    }
    
    /* package */ void setCurrentDatabase(WriteableBeanDatabaseImpl writeableDatabase, Object commitMessage, List<Change> changes) {
        synchronized (lock) {
            long currentRevision = currentDatabase.getRevision();
            long writeRevision = writeableDatabase.getBaseRevision();
            
            if (currentRevision != writeRevision) {
                throw new IllegalStateException("commit was called on a WriteableDatabase but the current database has changed after that copy was made");
            }
            
            LinkedList<BeanDatabaseUpdateListener> completedListeners = new LinkedList<BeanDatabaseUpdateListener>();
            for (BeanDatabaseUpdateListener listener : listeners) {
                try {
                    listener.prepareDatabaseChange(currentDatabase, writeableDatabase, commitMessage, changes);
                    completedListeners.add(listener);
                }
                catch (Throwable th) {
                    // Rollback time
                    MultiException throwMe = new MultiException(new PrepareFailedException(th));
                    
                    for (BeanDatabaseUpdateListener completedListener : completedListeners) {
                        try {
                            completedListener.rollbackDatabaseChange(currentDatabase, writeableDatabase, commitMessage, changes);
                        }
                        catch (Throwable rollTh) {
                            throwMe.addError(new RollbackFailedException(rollTh));
                        }
                    }
                    
                    throw throwMe;
                }
            }
            
            // success!
            BeanDatabaseImpl oldDatabase = currentDatabase;
            currentDatabase = new BeanDatabaseImpl(revisionCounter.getAndIncrement(), writeableDatabase);
            
            MultiException commitError = null;
            for (BeanDatabaseUpdateListener completedListener : completedListeners) {
                try {
                    completedListener.commitDatabaseChange(oldDatabase, currentDatabase, commitMessage, changes);
                }
                catch (Throwable th) {
                    if (commitError == null) {
                        commitError = new MultiException(new CommitFailedException(th));
                    }
                    else {
                        commitError.addError(new CommitFailedException(th));
                    }
                }
            }
            
            if (commitError != null) throw commitError;
        }
        
        
    }
}
