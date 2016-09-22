/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.twophaseresources;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.TwoPhaseResource;
import org.glassfish.hk2.api.TwoPhaseTransactionData;

/**
 * @author jwells
 *
 */
public class RecordingResource implements TwoPhaseResource {
    private final List<TwoPhaseTransactionData> prepares = new ArrayList<TwoPhaseTransactionData>();
    private final List<TwoPhaseTransactionData> commits = new ArrayList<TwoPhaseTransactionData>();
    private final List<TwoPhaseTransactionData> rollbacks = new ArrayList<TwoPhaseTransactionData>();
    
    private final boolean failInPrepare;
    private final boolean failInActivate;
    private final boolean failInRollback;
    
    public RecordingResource(boolean failInPrepare, boolean failInActivate, boolean failInRollback) {
        this.failInPrepare = failInPrepare;
        this.failInActivate = failInActivate;
        this.failInRollback = failInRollback;
    }
    
    public List<TwoPhaseTransactionData> getPrepares() {
        return prepares;
    }
    
    public List<TwoPhaseTransactionData> getCommits() {
        return commits;
    }
    
    public List<TwoPhaseTransactionData> getRollbacks() {
        return rollbacks;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.TwoPhaseResource#prepareDynamicConfiguration(org.glassfish.hk2.api.TwoPhaseTransactionData)
     */
    @Override
    public synchronized void prepareDynamicConfiguration(
            TwoPhaseTransactionData dynamicConfiguration) throws MultiException {
        prepares.add(dynamicConfiguration);
        if (failInPrepare) {
            throw new MultiException(new IllegalStateException("Was told to fail in prepare"));
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.TwoPhaseResource#activateDynamicConfiguration(org.glassfish.hk2.api.TwoPhaseTransactionData)
     */
    @Override
    public void activateDynamicConfiguration(
            TwoPhaseTransactionData dynamicConfiguration) {
        commits.add(dynamicConfiguration);
        if (failInActivate) {
            throw new MultiException(new IllegalStateException("Was told to fail in activate"));
        }

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.TwoPhaseResource#rollbackDynamicConfiguration(org.glassfish.hk2.api.TwoPhaseTransactionData)
     */
    @Override
    public void rollbackDynamicConfiguration(
            TwoPhaseTransactionData dynamicConfiguration) {
        rollbacks.add(dynamicConfiguration);
        if (failInRollback) {
            throw new MultiException(new IllegalStateException("Was told to fail in rollback"));
        }

    }

}
