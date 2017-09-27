/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;

/**
 * This is a greedy resolve that will add in any class
 * that has failed to be resolved.  It uses {@link ServiceLocatorUtilities#addClasses(org.glassfish.hk2.api.ServiceLocator, Class...)}
 * in order to add classes, and hence will use the default
 * class analyzer to discover injection points and constructors.
 * <p>
 * If the injected class is an interface the interface may use
 * the {@link GreedyDefualtImplementation} in order to specify
 * the class that should be used when another implementation
 * of this interface cannot be found.
 * <p>
 * WARNING: This resolve should be used with care as it
 * could cause unexpected class files to be instantiated
 * by hk2
 * 
 * @author jwells
 *
 */
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class GreedyResolver implements JustInTimeInjectionResolver {
    private final ServiceLocator locator;
    
    @Inject
    private GreedyResolver(ServiceLocator locator) {
        this.locator = locator;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.JustInTimeInjectionResolver#justInTimeResolution(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public boolean justInTimeResolution(Injectee failedInjectionPoint) {
        Type type = failedInjectionPoint.getRequiredType();
        if (type == null) return false;
        
        Class<?> clazzToAdd = null;
        if (type instanceof Class) {
            clazzToAdd = (Class<?>) type;
        }
        else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                clazzToAdd = (Class<?>) rawType;
            }
        }
        
        if (clazzToAdd == null) return false;
        if (clazzToAdd.isInterface()) {
            GreedyDefaultImplementation gdi = clazzToAdd.getAnnotation(GreedyDefaultImplementation.class);
            if (gdi != null) {
                clazzToAdd = gdi.value();
            }
            else {
                return false;
            }
        }
        
        ServiceLocatorUtilities.addClasses(locator, clazzToAdd);
        return true;
    }

}
