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
package org.glassfish.hk2.configuration.persistence.properties.internal;

import java.util.HashMap;
import java.util.Properties;

import org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
public class PropertyFileHandleImpl implements PropertyFileHandle {
    private final static String SEPARATOR = ".";
    
    private HashMap<String, String> lastRead = new HashMap<String, String>();
    
    private final String specificType;
    private final String defaultType;
    private final String defaultInstanceName;
    
    private static String emptyNull(String input) {
        if (input == null) return null;
        input = input.trim();
        if (input.length() <= 0) return null;
        return input;
    }
    
    /* package */ PropertyFileHandleImpl(String specificType, String defaultType, String defaultInstanceName) {
        this.specificType = emptyNull(specificType);
        this.defaultType = emptyNull(defaultType);
        this.defaultInstanceName = emptyNull(defaultInstanceName);
    }
    
    private TypeData extractData(String rawString) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#readProperties(java.util.Properties)
     */
    @Override
    public void readProperties(Properties properties) {
        if (properties == null) throw new IllegalArgumentException();
        
        for (Object fullKey : properties.keySet()) {
            if (!(fullKey instanceof String)) continue;
            
            String sFullKey = (String) fullKey;
            
            int firstDotIndex = sFullKey.indexOf(SEPARATOR);
            
        }

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getSpecificType()
     */
    @Override
    public String getSpecificType() {
        return specificType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getDefaultType()
     */
    @Override
    public String getDefaultType() {
        return defaultType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getDefaultInstanceName()
     */
    @Override
    public String getDefaultInstanceName() {
        return defaultInstanceName;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }
    
    private static class TypeData {
        private final String typeName;
        private final String instanceName;
        private final String paramName;
        private final int hashCode;
        private String value;
        
        private TypeData(String typeName,
                String instanceName,
                String paramName,
                String value) {
            this.typeName = typeName;
            this.instanceName = instanceName;
            this.paramName = paramName;
            this.value = value;
            
            hashCode = typeName.hashCode() ^ instanceName.hashCode() ^ paramName.hashCode();
        }
        
        public int hashCode() {
            return hashCode;
        }
        
        private void setValue(String value) {
            this.value = value;
        }
        
        private String getValue() {
            return value;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof TypeData)) return false;
            TypeData other = (TypeData) o;
            
            return (typeName.equals(other.typeName) &&
                    instanceName.equals(other.instanceName) &&
                    paramName.equals(other.paramName));
        }
    }

}
