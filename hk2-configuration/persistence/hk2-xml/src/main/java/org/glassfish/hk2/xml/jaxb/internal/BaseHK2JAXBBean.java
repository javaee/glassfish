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
package org.glassfish.hk2.xml.jaxb.internal;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;

/**
 * @author jwells
 *
 */
public class BaseHK2JAXBBean implements XmlHk2ConfigurationBean {
    private final static boolean DEBUG_GETS_AND_SETS = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.getsandsets", "false"));
    
    private final ConcurrentHashMap<String, Object> beanLikeMap = new ConcurrentHashMap<String, Object>();
    
    public void _setProperty(String propName, Object propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService setting property " + propName + " to " + propValue + " in " + this);
        }
        
        beanLikeMap.put(propName, propValue);
    }
    
    public void _setProperty(String propName, byte propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Byte) propValue);
    }
    
    public void _setProperty(String propName, char propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Character) propValue);
    }
    
    public void _setProperty(String propName, short propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Short) propValue);
    }
    
    public void _setProperty(String propName, int propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Integer) propValue);
    }
    
    public void _setProperty(String propName, float propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Float) propValue);
    }
    
    public void _setProperty(String propName, long propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Long) propValue);
    }
    
    public void _setProperty(String propName, double propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Double) propValue);
    }
    
    public Object _getProperty(String propName) {
        Object retVal = beanLikeMap.get(propName);
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService getting property " + propName + "=" + retVal + " in " + this);
        }
        
        return retVal;
    }
    
    public boolean _getPropertyZ(String propName) {
        return (Boolean) _getProperty(propName);
    }
    
    public byte _getPropertyB(String propName) {
        return (Byte) _getProperty(propName);
    }
    
    public char _getPropertyC(String propName) {
        return (Character) _getProperty(propName);
    }
    
    public short _getPropertyS(String propName) {
        return (Short) _getProperty(propName);
    }
    
    public int _getPropertyI(String propName) {
        return (Integer) _getProperty(propName);
    }
    
    public float _getPropertyF(String propName) {
        return (Float) _getProperty(propName);
    }
    
    public long _getPropertyJ(String propName) {
        return (Long) _getProperty(propName);
    }
    
    public double _getPropertyD(String propName) {
        return (Double) _getProperty(propName);
    }

    public boolean _hasProperty(String propName) {
        return beanLikeMap.containsKey(propName);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#getBeanLikeMap()
     */
    @Override
    public Map<String, Object> getBeanLikeMap() {
        HashMap<String, Object> intermediateCopy = new HashMap<String, Object>();
        
        for (Map.Entry<String, Object> entrySet : beanLikeMap.entrySet()) {
            if (entrySet.getValue() != null && List.class.isAssignableFrom(entrySet.getValue().getClass())) {
                // Is a child
                intermediateCopy.put(entrySet.getKey(), Collections.unmodifiableList((List<?>) entrySet.getValue()));
            }
            else {
                intermediateCopy.put(entrySet.getKey(), entrySet.getValue());
            }
        }
        
        return Collections.unmodifiableMap(intermediateCopy);
    }
    
    @Override
    public String toString() {
        return "BaseHK2JAXBBean(" + System.identityHashCode(this) + ")";
    }
}
