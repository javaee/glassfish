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
import java.util.logging.Logger;

/**
 * Entry point to the HK2 services runtime
 */
public class HK2 {


    final HK2Provider provider;

    private HK2(HK2Provider provider) {
        this.provider = provider;
    }

    /**
     * Entry point to the HK2 public APIs, will initialize the implementation and return
     * a valid instance that can be used to configure modules bindings.
     *
     * @return a HK2 instance to configure {@link Module} types or instances with or null if
     * an implementation cannot be found using the META-INF/services and {@link java.util.ServiceLoader}
     * mechanism.
     *
     * @throws RuntimeException if an implementation cannot be located.
     */
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
                    Logger.getLogger(HK2.class.getName()).severe("Cannot find an implementation of the HK2 public API.");
                    return null;
                }
            }
            return new HK2(provider);
        } catch (Exception e) {
            // todo : better handling
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link Services} instances to register and lookup services to and from.
     *
     * {@link Module} will be instantiated in the order of the their declaration unless some
     * modules have explicit dependencies on other modules through a {@link org.jvnet.hk2.annotations.Inject}
     * annotation.
     *
     * {@link Module} are components and will follow normal HK2 activation and injection
     * procedure prior to the {@link Module#configure(BinderFactory)} call.
     *
     * @param parent the parent {@link Services} instances to delegate lookup to.
     * @param moduleTypes array of {@link Module} types that will be configured with in the
     * returned {@link Services} instance.
     * @return a {@link Services} instance configured with all the {@link Module} services
     */
    public Services create(Services parent, Class<? extends Module>... moduleTypes) {
        return provider.create(parent, moduleTypes);
    }

    /**
     * Creates a new {@link Services} instances to register and lookup services to and from.
     *
     * Module instances will not be injected before the {@link Module#configure(BinderFactory)} call.
     *
     * @param parent the parent {@link Services} instances to delegate lookup to.
     * @param modules array of {@link Module} that will be configured with in the
     * returned {@link Services} instance.
     * @return a {@link Services} instance configured with all the {@link Module} services
     */
    public Services create(Services parent, Module... modules) {
        return provider.create(parent, modules);
    }
}
