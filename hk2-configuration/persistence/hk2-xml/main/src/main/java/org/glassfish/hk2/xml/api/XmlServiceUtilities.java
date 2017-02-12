/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.api;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.ManagerUtilities;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.xml.internal.DomXmlParser;
import org.glassfish.hk2.xml.internal.XmlServiceImpl;
import org.glassfish.hk2.xml.jaxb.internal.JAXBXmlParser;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

/**
 * Useful utilities for initializing the HK2 XmlService
 * 
 * @author jwells
 */
public class XmlServiceUtilities {

    /**
     * Enables Hk2 XmlServices in the given locator.  Will
     * also enable the HK2 Configuration Hub if the hub has
     * not already been started.  This operation is idempotent
     * in that if the named XmlService is already available in the
     * given locator then this method does nothing.
     * 
     * There will be one xml service added per parser that is found
     * in the locator at the time this is called.  The two provided
     * providers will also be added if they are not in the locator
     * already.  The parsers must have names, and both the name
     * and the rank from the underlying parsers will be given
     * to the XmlServices added
     * 
     * @param locator The non-null locator to which to add
     * the {@link XmlService}
     */
    public static void enableXmlService(ServiceLocator locator) {
        ManagerUtilities.enableConfigurationHub(locator);
        
        ServiceLocatorUtilities.addClasses(locator, true, JAXBXmlParser.class, DomXmlParser.class);
        
        List<ActiveDescriptor<?>> allParsers = locator.getDescriptors(BuilderHelper.createContractFilter(XmlServiceParser.class.getName()));
        for (ActiveDescriptor<?> parserDescriptor : allParsers) {
            String name = parserDescriptor.getName();
            if (name == null) continue;
            
            ActiveDescriptor<?> found = locator.getBestDescriptor(BuilderHelper.createNameAndContractFilter(XmlService.class.getName(), name));
            if (found != null) continue;
            
            DescriptorImpl di = BuilderHelper.createDescriptorFromClass(XmlServiceImpl.class);
            di.setName(name);
            di.setRanking(parserDescriptor.getRanking());
            
            ServiceLocatorUtilities.addOneDescriptor(locator, di, false);
        }
    }
    
    /**
     * This will enable all of the same xml parsers as
     * {@link #enableXmlService(ServiceLocator)} but will
     * set the rank of the stream-based xml parser to be
     * higher than that of the JAXB based one
     * 
     * @param locator The non-null locator to which to add
     * the {@link XmlService}
     */
    public static void enableDomXmlService(ServiceLocator locator) {
        enableXmlService(locator);
        
        ActiveDescriptor<?> found = locator.getBestDescriptor(BuilderHelper.createNameAndContractFilter(XmlService.class.getName(),
                XmlServiceParser.STREAM_PARSING_SERVICE));
        if (found == null) return;
        
        found.setRanking(2);
    }
}
