/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel.internal;


import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelException;
import org.glassfish.hk2.runlevel.RunLevelService;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;


/**
 * Run level context.
 *
 * @author tbeerbower
 */
@Singleton
public class RunLevelContext implements Context<RunLevel> {

    /**
     * The backing maps for this context.
     */
    private Map<String, Map<ActiveDescriptor<?>, Object>> backingMaps =
            new HashMap<String, Map<ActiveDescriptor<?>, Object>>();

    /**
     * The run level services.
     */
    @Inject
    private IterableProvider<RunLevelService> allRunLevelServices;


    // ----- Context --------------------------------------------------------

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#getScope()
    */
    @Override
    public Class<? extends Annotation> getScope() {
        return org.glassfish.hk2.runlevel.RunLevel.class;
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
    */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T findOrCreate(ActiveDescriptor<T> activeDescriptor,
                              ServiceHandle<?> root) {

        Map<ActiveDescriptor<?>, Object> backingStore =
                getBackingMap(Utilities.getRunLevelServiceName(activeDescriptor));

        if (backingStore.containsKey(activeDescriptor)) {
            return (T) backingStore.get(activeDescriptor);
        }

        String scope = Utilities.getRunLevelServiceName(activeDescriptor);
        RunLevelService runLevelService =  allRunLevelServices.named(scope).get();
        if (runLevelService == null) {
            runLevelService = allRunLevelServices.get();
        }

        RunLevel.Mode mode = Utilities.getRunLevelMode(activeDescriptor);
        
        if (mode == RunLevel.Mode.VALIDATING) {
            verifyState(activeDescriptor, runLevelService);
        }

        T retVal = activeDescriptor.create(root);
        backingStore.put(activeDescriptor, retVal);

        if (mode == RunLevel.Mode.VALIDATING) {
            runLevelService.recordActivation(activeDescriptor);
        }

        return retVal;
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#find(org.glassfish.hk2.api.ActiveDescriptor)
    */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(ActiveDescriptor<T> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore =
                getBackingMap(Utilities.getRunLevelServiceName(activeDescriptor));

        return (T) backingStore.get(activeDescriptor);
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#isActive()
    */
    @Override
    public boolean isActive() {
        return true;
    }


    // ----- Utility methods ------------------------------------------------

    /**
     * Deactivate the given descriptor.
     *
     * @param activeDescriptor  the descriptor
     * @param <T>               the type of the descriptor
     */
    public <T> void deactivate(ActiveDescriptor<T> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore =
                getBackingMap(Utilities.getRunLevelServiceName(activeDescriptor));

        if (backingStore.containsKey(activeDescriptor)) {
            activeDescriptor.dispose((T) backingStore.get(activeDescriptor));
            backingStore.remove(activeDescriptor);
        }
    }

    /**
     * Verifies that the state of the RunLevelService is appropriate for this
     * instance activation.
     *
     * @param descriptor  the descriptor
     * @param service     the run level service
     *
     * @throws RunLevelException  if the verification fails
     */
    private void verifyState(ActiveDescriptor<?> descriptor,
                             RunLevelService service)
            throws RunLevelException {

        Integer runLevel = Utilities.getRunLevelValue(descriptor);
        String  scope    = Utilities.getRunLevelServiceName(descriptor);
        Integer planned  = service.getPlannedRunLevel();
        Integer current  = service.getCurrentRunLevel();

        if (!(!(planned == null && current == null) &&
                ((planned == null || runLevel <= planned) &&
                        (current == null || runLevel <= (current + 1))))) {
            throw new RunLevelException("unable to activate " + this +
                    "; minimum expected RunLevel is: " + runLevel +
                    "; planned is: " + planned +
                    "; current is: " + current);
        }
    }

    /**
     * Get the backing map associated with the given run level service name.
     *
     * @param runLevelServiceName  the run level service name
     *
     * @return the backing map
     */
    private Map<ActiveDescriptor<?>, Object> getBackingMap(String runLevelServiceName) {
        Map<ActiveDescriptor<?>, Object> retVal = backingMaps.get(runLevelServiceName);
        if (retVal == null) {
            retVal = new HashMap<ActiveDescriptor<?>, Object>();

            backingMaps.put(runLevelServiceName, retVal);
        }
        return retVal;
    }
}
