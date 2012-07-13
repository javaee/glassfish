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
package org.jvnet.hk2.component;

import java.util.*;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.deprecated.internal.CreatorImpl;
import org.jvnet.hk2.deprecated.internal.MetadataIndexFilter;
import org.jvnet.hk2.deprecated.internal.Utilities;

import com.sun.hk2.component.InjectionResolver;

/**
 * {@link Creator} factory.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public class Creators {
    
    /**
     * This will be implemented as a find or create
     * @param c
     * @param habitat
     * @param metadata
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Creator<T> create(Class<T> c, ServiceLocator habitat, Map<String, List<String>> metadata) {
        MetadataIndexFilter mif = new MetadataIndexFilter(c, metadata);
        
        List<ActiveDescriptor<?>> descriptors = habitat.getDescriptors(mif);
        
        ActiveDescriptor<?> foundDescriptor;
        if (descriptors.isEmpty()) {
            try {
                foundDescriptor = ServiceLocatorUtilities.addOneDescriptor(habitat,
                        BuilderHelper.createDescriptorFromClass(c));
            }
            catch (MultiException me) {
                Utilities.printThrowable(me);
                
                return null;
            }
        }
        else {
            foundDescriptor = descriptors.get(0);
        }
        
        return new CreatorImpl(c, habitat, metadata, foundDescriptor);
    }

    /**
     * Returns all currently available injection annotations
     * @param habitat the service registry
     * @return the list of injection resolvers registered in this service registry in its parent.
     *
     */
    public static List<Inhabitant<? extends InjectionResolver>> getAllInjectionResolvers(ServiceLocator habitat) {
        throw new AssertionError("getAllInjectionResolvers in Creators is not implemented");
    }
}
