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
package org.glassfish.hk2.utilities;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * This is a {@link PopulatorPostProcessor} that adds an {@link HK2Loader}
 * based on a ClassLoader.  This is useful for those writing ClassLoader
 * based plugins that need to ensure their descriptors are loaded with
 * the given ClassLoader
 * 
 * @author jwells
 *
 */
public class ClassLoaderPostProcessor implements PopulatorPostProcessor {
    private final HK2Loader loader;
    private final boolean force;
    
    /**
     * Creates a {@link PopulatorPostProcessor} that will set the HK2Loader
     * of descriptors added with
     * {@link org.glassfish.hk2.api.Populator#populate(org.glassfish.hk2.api.DescriptorFileFinder, PopulatorPostProcessor...)}
     * 
     * @param classloader The classloader to use when classloading the added services
     * @param force If true then this will overwrite any value in the descriptor.  If false then if
     * the descriptor will only be changed if the HK2Loader field of the descriptor is not
     * already set 
     */
    public ClassLoaderPostProcessor(ClassLoader classloader, boolean force) {
        loader = new HK2LoaderImpl(classloader);
        this.force = force;
    }
    
    /**
     * Creates a {@link PopulatorPostProcessor} that will set the HK2Loader
     * of descriptors added with
     * {@link org.glassfish.hk2.api.Populator#populate(org.glassfish.hk2.api.DescriptorFileFinder, PopulatorPostProcessor...)}.
     * The HK2Loader field of services will only be changed if they have not already
     * been set
     * 
     * @param classloader The classloader to use when classloading the added services 
     */
    public ClassLoaderPostProcessor(final ClassLoader classloader) {
        this(classloader, false);
    }

    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator,
            DescriptorImpl descriptorImpl) {
        if (force) {
            // Doesn't matter what the old loader was, replace with the new one
            descriptorImpl.setLoader(loader);
            return descriptorImpl;
        }
        
        if (descriptorImpl.getLoader() != null) {
            // loader already set, force is false, do nothing
            return descriptorImpl;
        }
        
        // loader is null so set to our loader
        descriptorImpl.setLoader(loader);
        return descriptorImpl;
    }

}
