/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * The JustInTimeInjectionResolver is called when an injection point
 * cannot find anything to inject.  It allows a third party systems
 * to dynamically add descriptors to the system whenever
 * an injection point would have failed to resolve (or an Optional
 * injection point found no service definitions).
 * <p>
 * All injection resolvers registered with the system will be called
 * in a random order.  Resolvers should therefore not rely on the ordering
 * of installed injection resolvers.  Any injection resolvers added as a
 * result of this callback will NOT be called until the next injection
 * resolution failure.
 * <p>
 * Implementations of this interface are placed into the registry like
 * any other service.  One use-case would be to inject the
 * {@link DynamicConfigurationService} into the implementation in order
 * to add services if this resolver can do so.  Another option would
 * be to inject a {@link ServiceLocator} and use one of the methods
 * in {@link org.glassfish.hk2.utilities.ServiceLocatorUtilities} in order
 * to add services to the registry
 * <p>
 * If any of the registered injection resolvers commits a dynamic change
 * then the system will try one more time to resolve the injection before
 * failing (or returning null if the injection point is Optional).
 * 
 * @author jwells
 */
@Contract
public interface JustInTimeInjectionResolver {
    /**
     * This method will be called whenever an injection point cannot be resolved.  If this
     * method adds anything to the configuration it should return true.  Otherwise it
     * should return false.  The injection point that failed to be resolved is given
     * in failedInjectionPoint.
     * <p>
     * If this method throws an exception that exception will be added to the set of
     * exceptions in the MultiException that may be thrown from the injection resolver
     *
     * @param failedInjectionPoint The injection point that failed to resolve
     * @return true if this method has added a descriptor to the {@link ServiceLocator}
     * which may be used to resolve the {@link Injectee}.  False if this method
     * did not add a descriptor to the {@link ServiceLocator} that might help
     * resolve the injection point
     */
    public boolean justInTimeResolution(Injectee failedInjectionPoint);

}
