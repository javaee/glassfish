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
package org.glassfish.hk2.configuration.hub.xml.dom.integration;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.ManagerUtilities;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.internal.ConfigListener;
import org.glassfish.hk2.configuration.hub.xml.dom.integration.internal.MapTranslator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * @author jwells
 *
 */
public class XmlDomIntegrationUtilities {
    /**
     * If there is no key associated with a configured bean then the
     * instance name of that bean will be this string
     */
    public final static String DEFAULT_INSTANCE_NAME = "HK2_CONFIG_DEFAULT";
    
    /**
     * The name of the field that will be filled in if the config bean
     * is of type PropertyBag
     */
    public final static String PROPERTIES = "properties";
    
    /**
     * This enables the XmlDomIntegration layer of the system.
     * It is idempotent
     * 
     * @param locator The non-null locator to add hk2-config integration to
     */
    public final static void enableHk2ConfigDomIntegration(ServiceLocator locator) {
        ManagerUtilities.enableConfigurationHub(locator);
        
        if (locator.getService(ConfigListener.class) != null) return;
        
        ServiceLocatorUtilities.addClasses(locator, ConfigListener.class);
    }
    
    /**
     * This adds in an implementation of {@link XmlDomTranslationService} that
     * converts the hk2-config bean into a bean-like map.  The type and
     * instance names remain the same.  This method is idempotent.
     * <p>
     * If the incoming bean implements PropertyBag then an extra field
     * will be added to the map of type Properties with the name
     * &quot;properties&quot; which contains the properties associated with
     * the bean
     * 
     * @param locator The locator to add the translation service to
     */
    public final static void enableMapTranslator(ServiceLocator locator) {
        if (locator.getService(MapTranslator.class) != null) return;
        
        ServiceLocatorUtilities.addClasses(locator, MapTranslator.class);
    }

}
