/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.hk2Config.test.customizers;

import java.beans.PropertyVetoException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phyla;
import org.glassfish.hk2.xml.hk2Config.test.beans.Phylum;
import org.glassfish.hk2.xml.hk2Config.test.beans.PropertyValue;
import org.glassfish.hk2.xml.hk2Config.test.beans.pv.NamedPropertyValue;

/**
 * @author jwells
 *
 */
@Singleton @Named
public class PhylaCustomizer {
    @Inject
    private Phyla phyla;
    
    @Inject
    private XmlService xmlService;
    
    public List<Phylum> getPhylumByType(Class<?> type) {
        List<Phylum> retVal = new LinkedList<Phylum>();
        List<Phylum> phylum = phyla.getPhylum();
        for (Phylum one : phylum) {
            if (type.isAssignableFrom(one.getClass())) {
                retVal.add(one);
            }
        }
        
        return retVal;
    }

    public Phylum getPhylumByName(Phyla phyla, String name) {
        XmlHk2ConfigurationBean configBean = (XmlHk2ConfigurationBean) phyla;
        
        Object retVal = configBean._lookupChild("phylum", name);
        
        return (Phylum) retVal;
    }

    public Phylum createPhylum(Map<String, PropertyValue> properties) throws PropertyVetoException {
        Phylum retVal = xmlService.createBean(Phylum.class);
        
        boolean nameSet = false;
        for (Map.Entry<String, PropertyValue> entry : properties.entrySet()) {
            String key = entry.getKey();
            PropertyValue prop = entry.getValue();
            
            if ("name".equals(key)) {
                nameSet = true;
                NamedPropertyValue npv = (NamedPropertyValue) prop;
                retVal.setName(npv.getName());
            }
            
        }
        
        if (!nameSet) {
            throw new RuntimeException("Name not set in createPhylum " + properties);
        }
        
        phyla.addPhylum(retVal);
        return retVal;
    }

    public Phylum deletePhylum(Phylum removeMe) {
        return phyla.removePhylum(removeMe);
    }

}
