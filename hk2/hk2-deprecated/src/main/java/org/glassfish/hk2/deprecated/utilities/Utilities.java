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

package org.glassfish.hk2.deprecated.utilities;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.internal.IndexedFilterImpl;
import org.glassfish.hk2.utilities.AliasDescriptor;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.deprecated.internal.InhabitantImpl;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Core utilities.
 *
 * @author tbeerbower
 */
@Deprecated
public class Utilities {

    /**
     * Creates an {@link Inhabitant} from an {@link ActiveDescriptor}.
     *
     * @param fromMe  the {@link Descriptor} to turn into an {@link Inhabitant}
     * @param locator the locator to use for the {@link Inhabitant}
     *
     * @return an {@link Inhabitant}
     */
    public static <T> Inhabitant<T> getInhabitantFromActiveDescriptor(ActiveDescriptor<T> fromMe,
                                                                      ServiceLocator locator) {
        if (fromMe == null) return null;

        org.glassfish.hk2.api.Descriptor original = fromMe.getBaseDescriptor();
        if (original != null && (original instanceof Inhabitant)) {
            return (Inhabitant<T>) original;
        }

        return new InhabitantImpl<T>(fromMe, locator);
    }
}
