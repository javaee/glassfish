/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.guice.bridge.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * @author jwells
 *
 */
@Singleton
public class GuiceToHk2JITResolver implements JustInTimeInjectionResolver {
    private final ServiceLocator locator;
    private final Injector guiceInjector;

    /* package */ GuiceToHk2JITResolver(ServiceLocator locator,
            Injector guiceInjector) {
        this.locator = locator;
        this.guiceInjector = guiceInjector;
    }

    /**
     * This tries every qualifier in the injectee
     * @param injectee The injectee to look for a binding for
     * @return The binding found, or null if none could be found
     */
    private Binding<?> findBinding(Injectee injectee) {
        if (injectee.getRequiredQualifiers().isEmpty()) {
            Key<?> key = Key.get(injectee.getRequiredType());

            try {
                return guiceInjector.getBinding(key);
            } catch (ConfigurationException ce) {
                return null;
            }
        }

        if (injectee.getRequiredQualifiers().size() > 1) {
            return null;
        }

        for (Annotation annotation : injectee.getRequiredQualifiers()) {
            Key<?> key = Key.get(injectee.getRequiredType(), annotation);

            Binding<?> retVal = null;
            try {
                retVal = guiceInjector.getBinding(key);
            } catch (ConfigurationException ce) {
                return null;
            }

            if (retVal != null) return retVal;
        }

        return null;
    }

    /**
     * Gets the class from the given type
     *
     * @param type The type to find the class from
     * @return The class associated with this type, or null
     * if the class cannot be found
     */
    public static Class<?> getClassFromType(Type type) {
        if (type instanceof Class) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            return (Class<?>) pt.getRawType();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.JustInTimeInjectionResolver#justInTimeResolution(org.glassfish.hk2.api.Injectee)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean justInTimeResolution(Injectee failedInjectionPoint) {
        if (failedInjectionPoint.getParent() == null) {
            // Jersey looks things up expecting to find only
            // hk2 versions of things, which sometimes confuses
            // the bridge.  The new feature added to JIT resolvers
            // which allows for JIT to work for lookups as well
            // as for Injection points breaks the jersey-guice
            // bridge, so it has been disabled to go back to the
            // old behavior.  It would be nice to go to Jersey
            // and have them fix this since having guice lookup
            // is a good feature
            return false;
        }

        Class<?> implClass = getClassFromType(failedInjectionPoint.getRequiredType());
        if (implClass == null) return false;

        Binding<?> binding = findBinding(failedInjectionPoint);
        if (binding == null) return false;

        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(failedInjectionPoint.getRequiredType());

        Set<Annotation> qualifiers = new HashSet<Annotation>(failedInjectionPoint.getRequiredQualifiers());

        GuiceServiceHk2Bean guiceBean = new GuiceServiceHk2Bean(contracts, qualifiers, implClass, binding);

        ServiceLocatorUtilities.addOneDescriptor(locator, guiceBean);

        return true;
    }

}
