/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.internal;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import javax.inject.Singleton;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.InheritableThread;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.reflection.Logger;

/**
 * @author jwells
 */
@Singleton @Visibility(DescriptorVisibility.LOCAL)
public class InheritableThreadContext implements Context<InheritableThread> {
    private final static boolean LOG_THREAD_DESTRUCTION = AccessController.<Boolean>doPrivileged(new PrivilegedAction<Boolean>() {

        @Override
        public Boolean run() {
            return Boolean.parseBoolean(System.getProperty("org.hk2.debug.inheritablethreadcontext.log", "false"));
        }

    });

    private InheritableThreadLocal<InheritableContextThreadWrapper> threadMap
            = new InheritableThreadLocal<InheritableContextThreadWrapper>() {
                public InheritableContextThreadWrapper initialValue() {
                    return new InheritableContextThreadWrapper();
        }
    };

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return InheritableThread.class;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        U retVal = (U) threadMap.get().get(activeDescriptor);
        if (retVal == null) {
            retVal = activeDescriptor.create(root);
            threadMap.get().put(activeDescriptor, retVal);
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#find(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return threadMap.get().has(descriptor);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#supportsNullCreation()
     */
    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#supportsNullCreation()
     */
    @Override
    public void shutdown() {
        threadMap = null;
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        // per-thread instances live for the life of the thread,
        // so we will ignore any request to destroy a descriptor

    }

    private static class InheritableContextThreadWrapper {
        private final HashMap<ActiveDescriptor<?>, Object> instances =
                new HashMap<ActiveDescriptor<?>, Object>();
        private final long id = Thread.currentThread().getId();

        public boolean has(ActiveDescriptor<?> d) {
            return instances.containsKey(d);
        }

        public Object get(ActiveDescriptor<?> d) {
            return instances.get(d);
        }

        public void put(ActiveDescriptor<?> d, Object v) {
            instances.put(d, v);
        }

        @Override
        public void finalize() throws Throwable {
            instances.clear();

            if (LOG_THREAD_DESTRUCTION) {
                Logger.getLogger().debug("Removing PerThreadContext data for thread " + id);
            }
        }

    }
}
