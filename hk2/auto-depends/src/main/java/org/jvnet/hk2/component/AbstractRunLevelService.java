/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

/**
 * A default implementation for the {@link RunLevelService} that component
 * provides can derive from to provide their own component-related lifecycle.
 * 
 * <p/>
 * The targetEnvironment is a placeholder type to represent a sub-component
 * set of {@link RunLevel}-gated services.  It can be anything of the caller's
 * choosing as long as it is kept unique and does not clash with the
 * default internal run level service.
 * 
 * @author Jeff Trent
 */
public abstract class AbstractRunLevelService<T>
        implements RunLevelService<T>, InhabitantListener {

    @Inject
    private Habitat habitat;

    private final Class<?> targetEnv;
    
    private volatile DefaultRunLevelService delegate;
    

    protected AbstractRunLevelService(Class<?> targetEnvironment) {
        if (null == targetEnvironment || DefaultRunLevelService.ENVIRONMENT == targetEnvironment) {
            throw new IllegalStateException("invalid target environment");
        }
        
        this.targetEnv = targetEnvironment;
    }

    @Override
    public String toString() {
        return getClass().getName() + "-" + System.identityHashCode(this) + "(" + delegate + ")";
    }
    
    @SuppressWarnings("unchecked")
    protected RunLevelService<T> getDelegate() {
        if (null == delegate) {
            if (null == habitat) {
                throw new IllegalStateException("habitat was not initialized");
            }

            synchronized (this) {
                if (null == delegate) {
                    delegate = new DefaultRunLevelService(habitat, 
                            DefaultRunLevelService.ASYNC_ENABLED,
                            targetEnv.getName(), targetEnv, null);
                    delegate.setParent(this);
                }
            }
        }
        
        return (RunLevelService<T>) delegate;
    }
    
    protected DefaultRunLevelService getDefault() {
        RunLevelService<T> rls = getDelegate();
        if (!DefaultRunLevelService.class.isInstance(rls)) {
            throw new UnsupportedOperationException();
        }

        return DefaultRunLevelService.class.cast(rls);
    }
    
    /**
     * Overrides the default behavior of getting all listeners from the habitat
     * to use a stand-in listener instead. If set to null, the default behavior
     * will be restored.
     * 
     * @param listener the alternative, stand-in listener
     */
    public void setListener(RunLevelListener listener) {
        getDefault().setListener(listener);
    }
    
    /**
     * Overrides the default behavior of getting the inhabitant sorter from the habitat
     * to use a stand-in sorter instead. If set to null, the default behavior
     * will be restored.
     * 
     * @param sorter the alternative, stand-in sorter
     */
    public void setInhabitantSorter(InhabitantSorter sorter) {
        getDefault().setInhabitantSorter(sorter);
    }

    /**
     * Overrides the default behavior of getting the inhabitant activator from the habitat
     * to use a stand-in activator instead. If set to null, the default behavior
     * will be restored.
     * 
     * @param activator the alternative, stand-in activator
     */
    public void setInhabitantActivator(InhabitantActivator activator) {
        getDefault().setInhabitantActivator(activator);
    }
    
    @Override
    public RunLevelState<T> getState() {
        return getDelegate().getState();
    }

    @Override
    public void proceedTo(int runLevel) {
        getDelegate().proceedTo(runLevel);
    }

    @Override
    public void interrupt() {
        getDelegate().interrupt();
    }

    @Override
    public void interrupt(int runLevel) {
        getDelegate().interrupt(runLevel);
    }

    @Override
    public boolean inhabitantChanged(EventType eventType, Inhabitant<?> inhabitant) {
        return getDefault().inhabitantChanged(eventType, inhabitant);
    }

}
