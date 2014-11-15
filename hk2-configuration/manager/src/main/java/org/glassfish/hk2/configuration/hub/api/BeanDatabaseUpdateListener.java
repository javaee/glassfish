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
package org.glassfish.hk2.configuration.hub.api;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * This is a listener that is notified when changes are made to
 * the current {@link BeanDatabase}
 * 
 * @author jwells
 */
@Contract
public interface BeanDatabaseUpdateListener {
    /**
     * This method will be called prior to the bean database being updated.
     * If this method throws an exception subsequent listeners prepare methods
     * will not be called and the rollback method of any listeners that had
     * run previously will be called and the proposedDatabase will not become
     * the current database.  If all the registered bean update listeners
     * prepare methods return normally then the proposedDatabase will
     * become the current database
     * 
     * @param currentDatabase The bean database that is current in effect
     * @param proposedDatabase The bean database that will go into effect
     * @param commitMessage An object passed to the commit method in a dynamic change
     * @param changes The changes that were made to the current database
     */
    public void prepareDatabaseChange(BeanDatabase currentDatabase, BeanDatabase proposedDatabase, Object commitMessage, List<Change> changes);
    
    /**
     * This method is called after the change of database has already happened.
     * If this method throws an exception subsequent listeners commit methods
     * will be called, but the {@link WriteableBeanDatabase#commit()} method
     * will throw an exception, indicating a possibly inconsistent state
     * 
     * @param oldDatabase The database from which the current database was derived
     * @param currentDatabase The current bean database
     * @param commitMessage An object passed to the commit method in a dynamic change
     * @param changes The changes that were made to arrive at the current database
     */
    public void commitDatabaseChange(BeanDatabase oldDatabase, BeanDatabase currentDatabase, Object commitMessage, List<Change> changes);
    
    /**
     * If any {@link #prepareDatabaseChange(BeanDatabase, BeanDatabase, Object, List)}
     * throws an exception this method will be called on all listeners whose
     * {@link #prepareDatabaseChange(BeanDatabase, BeanDatabase, Object, List)} had already
     * been succesfully called.  If this method throws an exception subsequent listeners
     * rollback methods will be called and the exception will be returned in the exception
     * thrown to the caller of {@link WriteableBeanDatabase#commit()} method
     * 
     * @param currentDatabase The bean database that is current in effect
     * @param proposedDatabase The bean database that was to go into effect (but which will not)
     * @param commitMessage An object passed to the commit method in a dynamic change
     * @param changes The changes that were proposed to be made to the current database
     */
    public void rollbackDatabaseChange(BeanDatabase currentDatabase, BeanDatabase proposedDatabase, Object commitMessage, List<Change> changes);
}
