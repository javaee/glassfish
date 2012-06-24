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
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelException;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.runlevel.utilities.Utilities;
import org.jvnet.hk2.annotations.Service;

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
@Service @Singleton
public class RunLevelContext implements Context<RunLevel> {

    /**
     * The backing maps for this context.
     */
    private Map<String, Map<ActiveDescriptor<?>, Object>> backingMaps =
            new HashMap<String, Map<ActiveDescriptor<?>, Object>>();

    /**
     * The run level controllers.
     */
    @Inject
    private IterableProvider<RunLevelController> allRunLevelControllers;


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

        String rlsName = Utilities.getRunLevelControllerName(activeDescriptor);

        Map<ActiveDescriptor<?>, Object> backingMap = getBackingMap(rlsName);

        if (backingMap.containsKey(activeDescriptor)) {
            return (T) backingMap.get(activeDescriptor);
        }


        int mode = Utilities.getRunLevelMode(activeDescriptor);

        if (mode == RunLevel.RUNLEVEL_MODE_VALIDATING) {
            RunLevelController RunLevelController = getRunLevelController(rlsName);

            if (RunLevelController == null) {
                throw new RunLevelException("Can't find a run level controller for " + rlsName);
            }

            validate(activeDescriptor, RunLevelController);
            T service = activeDescriptor.create(root);
            backingMap.put(activeDescriptor, service);
            RunLevelController.recordActivation(activeDescriptor);
            return service;
        }

        T service = activeDescriptor.create(root);
        backingMap.put(activeDescriptor, service);

        return service;
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.Context#find(org.glassfish.hk2.api.ActiveDescriptor)
    */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(ActiveDescriptor<?> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore =
                getBackingMap(Utilities.getRunLevelControllerName(activeDescriptor));

        return backingStore.containsKey(activeDescriptor);
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
     * @param <T>               the descriptor type
     */
    public <T> void deactivate(ActiveDescriptor<T> activeDescriptor) {
        Map<ActiveDescriptor<?>, Object> backingStore =
                getBackingMap(Utilities.getRunLevelControllerName(activeDescriptor));

        if (backingStore.containsKey(activeDescriptor)) {
            activeDescriptor.dispose((T) backingStore.get(activeDescriptor));
            backingStore.remove(activeDescriptor);
        }
    }

    /**
     * Verifies that the run level value of the {@link RunLevel} annotated
     * service described by the given descriptor is valid for activation.
     * Valid means that the run level value is less than or equal to the
     * current or planned run level of the given {@link org.glassfish.hk2.runlevel.RunLevelController}.
     *
     * @param descriptor  the descriptor of the service being activated
     * @param service     the run level service
     *
     * @throws RunLevelException if the validation fails
     */
    private void validate(ActiveDescriptor<?> descriptor,
                          RunLevelController service)
            throws RunLevelException {

        Integer runLevel = Utilities.getRunLevelValue(descriptor);
        Integer planned  = service.getPlannedRunLevel();
        Integer current  = service.getCurrentRunLevel();

        if (runLevel > current && (planned == null || runLevel > planned)) {
            throw new RunLevelException("Unable to activate " + descriptor +
                    "; minimum expected RunLevel is: " + runLevel +
                    "; planned is: " + planned +
                    "; current is: " + current + ".");
        }
    }

    /**
     * Get the backing map associated with the given run level service name.
     *
     * @param name  the run level service name
     *
     * @return the backing map
     */
    private Map<ActiveDescriptor<?>, Object> getBackingMap(String name) {
        Map<ActiveDescriptor<?>, Object> backingMap = backingMaps.get(name);
        if (backingMap == null) {
            backingMap = new HashMap<ActiveDescriptor<?>, Object>();
            backingMaps.put(name, backingMap);
        }
        return backingMap;
    }

    /**
     * Get the {@link org.glassfish.hk2.runlevel.RunLevelController} for the given name.
     *
     * @param name  the run level service name
     * @param <T>   the {@link org.glassfish.hk2.runlevel.RunLevelController} type
     *
     * @return the {@link org.glassfish.hk2.runlevel.RunLevelController} for the given name; null if none
     *         exists
     */
    private <T> RunLevelController getRunLevelController(String name) {
        RunLevelController controller =
                allRunLevelControllers.named(name).get();

        return controller == null ?
                allRunLevelControllers.get() : controller;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#supportsNullCreation()
     */
    @Override
    public boolean supportsNullCreation() {
        return false;
    }
}
