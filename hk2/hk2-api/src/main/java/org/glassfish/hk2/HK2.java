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

package org.glassfish.hk2;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.glassfish.hk2.spi.HK2Provider;
import sun.misc.Service;

import java.util.Iterator;

/**
 * Entry point to the HK2 services runtime
 */
public class HK2 {


    final HK2Provider provider;


    public static HK2 get() {
        try {
            HK2Provider provider=null;
            Iterable<? extends HK2Provider> loader = ServiceLoader.lookupProviderInstances(HK2Provider.class);
            if (loader!=null) {
                Iterator<? extends HK2Provider> iterator = loader.iterator();
                if (iterator!=null && iterator.hasNext()) {
                    provider=iterator.next();
                }
            }
            // todo : we need to revisit when ServiceLoader can take care of META-INF/Services in non OSGi env.
            if (provider==null) {
                Iterator<HK2Provider> providers = java.util.ServiceLoader.load(HK2Provider.class).iterator();
                if (providers!=null && providers.hasNext()) {
                    provider = providers.next();
                } else {
                    // last ditch attempt in case we are loaded in the same classloader.
                    Class<? extends HK2Provider> providerType = (Class<? extends HK2Provider>)
                            Class.forName("org.jvnet.hk2.component.HK2ProviderImpl");
                    if (providerType==null) {
                        throw new RuntimeException("Cannot find HK2Provider implementation");
                    }
                    provider = providerType.newInstance();
                }
            }
            return new HK2(provider);
        } catch (Exception e) {
            // todo : better handling
            throw new RuntimeException(e);
        }
    }

    public static HK2 get(HK2Provider provider) {
        return new HK2(provider);
    }

    public HK2(HK2Provider provider) {
        this.provider = provider;
    }

    public Services create(Services parent, Class<? extends Module>... moduleTypes) {
        return provider.create(parent, moduleTypes);
    }

    public Services create(Services parent, Module... modules) {
        return provider.create(parent, modules);
    }
}
