/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information about the name to XmlElement mappings and
 * about children with no XmlElement at all
 * 
 * @author jwells
 *
 */
public class NameInformation {
    private final Map<String, XmlElementData> nameMapping;
    private final Set<String> noXmlElement;
    private final Map<String, String> addMethodToVariableName;
    private final Map<String, String> removeMethodToVariableName;
    private final Map<String, String> lookupMethodToVariableName;
    private final Set<String> referenceSet;
    private final Map<String, List<XmlElementData>> aliases;
    private final XmlElementData valueData;
    
    NameInformation(Map<String, XmlElementData> nameMapping,
            Set<String> unmappedNames,
            Map<String, String> addMethodToVariableName,
            Map<String, String> removeMethodToVariableName,
            Map<String, String> lookupMethodToVariableName,
            Set<String> referenceSet,
            Map<String, List<XmlElementData>> aliases,
            XmlElementData valueData) {
        this.nameMapping = nameMapping;
        this.noXmlElement = unmappedNames;
        this.addMethodToVariableName = addMethodToVariableName;
        this.removeMethodToVariableName = removeMethodToVariableName;
        this.lookupMethodToVariableName = lookupMethodToVariableName;
        this.referenceSet = referenceSet;
        this.aliases = aliases;
        this.valueData = valueData;
    }
    
    String getNameMap(String mapMe) {
        if (mapMe == null) return null;
        if (!nameMapping.containsKey(mapMe)) return mapMe;
        return nameMapping.get(mapMe).getName();
    }
    
    public List<XmlElementData> getAliases(String variableName) {
        return aliases.get(variableName);
    }
    
    String getDefaultNameMap(String mapMe) {
        if (mapMe == null) return Generator.JAXB_DEFAULT_DEFAULT;
        if (!nameMapping.containsKey(mapMe)) return Generator.JAXB_DEFAULT_DEFAULT;
        return nameMapping.get(mapMe).getDefaultValue();
    }
    
    String getXmlWrapperTag(String mapMe) {
        if (mapMe == null) return null;
        if (!nameMapping.containsKey(mapMe)) return null;
        return nameMapping.get(mapMe).getXmlWrapperTag();
    }
    
    boolean hasNoXmlElement(String variableName) {
        if (variableName == null) return true;
        return noXmlElement.contains(variableName);
    }
    
    boolean isReference(String variableName) {
        if (variableName == null) return false;
        return referenceSet.contains(variableName);
    }
    
    Format getFormat(String variableName) {
        if (variableName == null) return Format.ATTRIBUTE;
        if ((valueData != null) && valueData.getName().equals(variableName)) return Format.VALUE;
        if (!nameMapping.containsKey(variableName)) return Format.ATTRIBUTE;
        return nameMapping.get(variableName).getFormat();
    }
    
    String getAddVariableName(String methodName) {
        return addMethodToVariableName.get(methodName);
    }
    
    String getRemoveVariableName(String methodName) {
        return removeMethodToVariableName.get(methodName);
    }
    
    String getLookupVariableName(String methodName) {
        return lookupMethodToVariableName.get(methodName);
    }
    
    public XmlElementData getValueData() {
        return valueData;
    }
}