/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.config.types;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.jvnet.hk2.config.HK2DomConfigUtilities;

/**
 * @author jwells
 *
 */
public class HK2DomConfigTypesUtilities {
    private final static String PROPERTY_GENERATED_INJECTOR_CLASS = "org.jvnet.hk2.config.types.PropertyInjector";
    /**
     * This method enables the HK2 Dom based XML configuration parsing for
     * systems that do not use HK2 metadata files or use a non-default
     * name for HK2 metadata files, along with support for the types
     * provided in this module.  This method is idempotent, so that
     * if the services already are available in the locator they will
     * not get added again
     * 
     * @param locator The non-null locator to add the hk2 dom based
     * configuration services to
     */
    public static void enableHK2DomConfigurationConfigTypes(ServiceLocator locator) {
        if (locator.getBestDescriptor(BuilderHelper.createContractFilter(PROPERTY_GENERATED_INJECTOR_CLASS)) != null) return;
        
        HK2DomConfigUtilities.enableHK2DomConfiguration(locator);
        
        Class<?> propertyInjectorClass = GeneralUtilities.loadClass(HK2DomConfigTypesUtilities.class.getClassLoader(), PROPERTY_GENERATED_INJECTOR_CLASS);
        if (propertyInjectorClass == null) return;
        
        ServiceLocatorUtilities.addClasses(locator, propertyInjectorClass);
    }

}
