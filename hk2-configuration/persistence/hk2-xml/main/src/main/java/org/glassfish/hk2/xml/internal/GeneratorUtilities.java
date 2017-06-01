/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.api.annotations.PluralOf;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;
import org.glassfish.hk2.xml.internal.alt.clazz.AnnotationAltAnnotationImpl;

/**
 * @author jwells
 *
 */
public class GeneratorUtilities {
    private final static String XML_VALUE_LOCAL_PART = "##XmlValue";
    public final static String XML_ANY_ATTRIBUTE_LOCAL_PART = "##XmlAnyAttribute";
    
    public static QName convertXmlRootElementName(AltAnnotation root, AltClass clazz) {
        String namespace = root.getStringValue("namespace");
        
        String rootName = root.getStringValue("name");
        
        if (!"##default".equals(rootName)) return QNameUtilities.createQName(namespace, rootName);
        
        String simpleName = clazz.getSimpleName();
        
        char asChars[] = simpleName.toCharArray();
        StringBuffer sb = new StringBuffer();
        
        boolean firstChar = true;
        boolean lastCharWasCapital = false;
        for (char asChar : asChars) {
            if (firstChar) {
                firstChar = false;
                if (Character.isUpperCase(asChar)) {
                    lastCharWasCapital = true;
                    sb.append(Character.toLowerCase(asChar));
                }
                else {
                    lastCharWasCapital = false;
                    sb.append(asChar);
                }
            }
            else {
                if (Character.isUpperCase(asChar)) {
                    if (!lastCharWasCapital) {
                        sb.append('-');
                    }
                    
                    sb.append(Character.toLowerCase(asChar));
                    
                    lastCharWasCapital = true;
                }
                else {
                    sb.append(asChar);
                    
                    lastCharWasCapital = false;
                }
            }
        }
        
        String localPart = sb.toString();
        
        return QNameUtilities.createQName(namespace, localPart);
    }
    
    private static boolean isSpecifiedReference(AltMethod method) {
        AltAnnotation customAnnotation = method.getAnnotation(XmlIDREF.class.getName());
        return (customAnnotation != null);
    }
    
    private static void checkOnlyOne(AltClass convertMe, AltMethod method, AltAnnotation aAnnotation, AltAnnotation bAnnotation) {
        if (aAnnotation != null && bAnnotation != null) {
            throw new IllegalArgumentException("The method " + method + " of " + convertMe + " has both the annotation " + aAnnotation + " and the annotation " +
              bAnnotation + " which is illegal");
        }
    }
    
    public static NameInformation getXmlNameMap(AltClass convertMe) {
        Map<String, XmlElementData> xmlNameMap = new LinkedHashMap<String, XmlElementData>();
        Set<String> unmappedNames = new LinkedHashSet<String>();
        Map<String, String> addMethodToVariableMap = new LinkedHashMap<String, String>();
        Map<String, String> removeMethodToVariableMap = new LinkedHashMap<String, String>();
        Map<String, String> lookupMethodToVariableMap = new LinkedHashMap<String, String>();
        Set<String> referenceSet = new LinkedHashSet<String>();
        Map<String, List<XmlElementData>> aliasMap = new LinkedHashMap<String, List<XmlElementData>>();
        XmlElementData valueData = null;
        XmlElementData xmlAnyAttributeData = null;
        
        boolean hasAnElement = false;
        for (AltMethod originalMethod : convertMe.getMethods()) {
            String setterVariable = Utilities.isSetter(originalMethod);
            if (setterVariable == null) {
                setterVariable = Utilities.isGetter(originalMethod);
                if (setterVariable == null) continue;
            }
            
            if (isSpecifiedReference(originalMethod)) {
                referenceSet.add(setterVariable);
            }
            
            AltAnnotation pluralOf = null;
            AltAnnotation xmlElement = originalMethod.getAnnotation(XmlElement.class.getName());
            AltAnnotation xmlElements = originalMethod.getAnnotation(XmlElements.class.getName());
            AltAnnotation xmlElementWrapper = originalMethod.getAnnotation(XmlElementWrapper.class.getName());
            AltAnnotation xmlAttribute = originalMethod.getAnnotation(XmlAttribute.class.getName());
            AltAnnotation xmlValue = originalMethod.getAnnotation(XmlValue.class.getName());
            AltAnnotation xmlAnyAttribute = originalMethod.getAnnotation(XmlAnyAttribute.class.getName());
            
            String xmlElementWrapperName = (xmlElementWrapper == null) ? null : xmlElementWrapper.getStringValue("name");
            if (xmlElementWrapperName != null && xmlElementWrapperName.isEmpty()) {
                xmlElementWrapperName = setterVariable;
            }
            
            checkOnlyOne(convertMe, originalMethod, xmlElement, xmlElements);
            checkOnlyOne(convertMe, originalMethod, xmlElement, xmlAttribute);
            checkOnlyOne(convertMe, originalMethod, xmlElements, xmlAttribute);
            checkOnlyOne(convertMe, originalMethod, xmlElement, xmlValue);
            checkOnlyOne(convertMe, originalMethod, xmlElements, xmlValue);
            checkOnlyOne(convertMe, originalMethod, xmlAttribute, xmlValue);
            checkOnlyOne(convertMe, originalMethod, xmlElement, xmlAnyAttribute);
            checkOnlyOne(convertMe, originalMethod, xmlElements, xmlAnyAttribute);
            checkOnlyOne(convertMe, originalMethod, xmlAttribute, xmlAnyAttribute);
            checkOnlyOne(convertMe, originalMethod, xmlValue, xmlAnyAttribute);
            
            if (xmlElements != null) {
                hasAnElement = true;
                
                // First add the actual method so it is known to the system
                pluralOf = originalMethod.getAnnotation(PluralOf.class.getName());
                    
                String defaultValue = Generator.JAXB_DEFAULT_DEFAULT;
                    
                xmlNameMap.put(setterVariable, new XmlElementData("", setterVariable, setterVariable, defaultValue, Format.ELEMENT, null, true, xmlElementWrapperName));
                    
                String aliasName = setterVariable;
                    
                AltAnnotation allXmlElements[] = xmlElements.getAnnotationArrayValue("value");
                List<XmlElementData> aliases = new ArrayList<XmlElementData>(allXmlElements.length);
                aliasMap.put(setterVariable, aliases);
                    
                for (AltAnnotation allXmlElement : allXmlElements) {
                    defaultValue = allXmlElement.getStringValue("defaultValue");
                    
                    String allXmlElementNamespace = allXmlElement.getStringValue("namespace");
                    String allXmlElementName = allXmlElement.getStringValue("name");
                    AltClass allXmlElementType = (AltClass) allXmlElement.getAnnotationValues().get("type");
                    String allXmlElementTypeName = (allXmlElementType == null) ? null : allXmlElementType.getName() ;
                    boolean allXmlElementTypeInterface = (allXmlElementType == null) ? true : allXmlElementType.isInterface();
                        
                    if (Generator.JAXB_DEFAULT_STRING.equals(allXmlElementName)) {
                        throw new IllegalArgumentException("The name field of an XmlElement inside an XmlElements must have a specified name");
                    }
                    else {
                        aliases.add(new XmlElementData(
                                allXmlElementNamespace,
                                allXmlElementName,
                                aliasName,
                                defaultValue,
                                Format.ELEMENT,
                                allXmlElementTypeName,
                                allXmlElementTypeInterface,
                                xmlElementWrapperName));
                    }
                }
            }
            else if (xmlElement != null) {
                hasAnElement = true;
                
                // Get the pluralOf from the method
                pluralOf = originalMethod.getAnnotation(PluralOf.class.getName());
                    
                String defaultValue = xmlElement.getStringValue("defaultValue");
                
                String namespace = xmlElement.getStringValue("namespace");
                String name = xmlElement.getStringValue("name");
                    
                if (Generator.JAXB_DEFAULT_STRING.equals(name)) {
                    xmlNameMap.put(setterVariable, new XmlElementData(
                            namespace,
                            setterVariable,
                            setterVariable,
                            defaultValue,
                            Format.ELEMENT,
                            null,
                            true,
                            xmlElementWrapperName));
                }
                else {
                    xmlNameMap.put(setterVariable, new XmlElementData(
                            namespace,
                            name,
                            name,
                            defaultValue,
                            Format.ELEMENT,
                            null,
                            true,
                            xmlElementWrapperName));
                }
            }
            else if (xmlAttribute != null) {
                String namespace = xmlAttribute.getStringValue("namespace");
                String name = xmlAttribute.getStringValue("name");
                
                if (Generator.JAXB_DEFAULT_STRING.equals(name)) {
                    xmlNameMap.put(setterVariable, new XmlElementData(
                            namespace,
                            setterVariable,
                            setterVariable,
                            Generator.JAXB_DEFAULT_DEFAULT,
                            Format.ATTRIBUTE,
                            null,
                            true,
                            xmlElementWrapperName));
                }
                else {
                    xmlNameMap.put(setterVariable, new XmlElementData(
                            namespace,
                            name,
                            name,
                            Generator.JAXB_DEFAULT_DEFAULT,
                            Format.ATTRIBUTE,
                            null,
                            true,
                            xmlElementWrapperName));
                }
            }
            else if (xmlValue != null) {
                if (valueData != null) {
                    throw new IllegalArgumentException("There may be only one XmlValue method on " + convertMe);
                }
                
                valueData = new XmlElementData(
                        XmlService.DEFAULT_NAMESPACE,
                        XML_VALUE_LOCAL_PART,
                        XML_VALUE_LOCAL_PART,
                        null,
                        Format.VALUE,
                        null,
                        false,
                        xmlElementWrapperName);
                xmlNameMap.put(setterVariable, valueData);
            }
            else if (xmlAnyAttribute != null) {
                if (xmlAnyAttributeData != null) {
                    throw new IllegalArgumentException("There may be only one XmlAnyAttribute method on " + convertMe);
                }
                
                xmlAnyAttributeData = new XmlElementData(
                        XmlService.DEFAULT_NAMESPACE,
                        XML_ANY_ATTRIBUTE_LOCAL_PART,
                        XML_ANY_ATTRIBUTE_LOCAL_PART,
                        null,
                        Format.ATTRIBUTE,
                        null,
                        false,
                        xmlElementWrapperName);
                xmlNameMap.put(setterVariable, xmlAnyAttributeData);       
            }
            else {
                unmappedNames.add(setterVariable);
            }
            
            if (pluralOf == null) pluralOf = new AnnotationAltAnnotationImpl(new PluralOfDefault(), null);
            
            String unDecapitalizedVariable = originalMethod.getName().substring(3);
            
            addMethodToVariableMap.put(getMethodName(MethodType.ADD, unDecapitalizedVariable, pluralOf), setterVariable);
            removeMethodToVariableMap.put(getMethodName(MethodType.REMOVE, unDecapitalizedVariable, pluralOf), setterVariable);
            lookupMethodToVariableMap.put(getMethodName(MethodType.LOOKUP, unDecapitalizedVariable, pluralOf), setterVariable);
        }
        
        if (valueData != null && hasAnElement) {
            throw new IllegalArgumentException("A bean cannot both have XmlElements and XmlValue methods in " + convertMe);
        }
        
        Set<String> noXmlElementNames = new LinkedHashSet<String>();
        for (String unmappedName : unmappedNames) {
            if (!xmlNameMap.containsKey(unmappedName)) {
                noXmlElementNames.add(unmappedName);
            }
        }
        
        return new NameInformation(xmlNameMap, noXmlElementNames,
                addMethodToVariableMap,
                removeMethodToVariableMap,
                lookupMethodToVariableMap,
                referenceSet,
                aliasMap,
                valueData);
    }
    
    private static String getMethodName(MethodType methodType, String unDecapitalizedVariable, AltAnnotation instructions) {
        String retVal;
        
        switch (methodType) {
            case ADD:
                retVal = instructions.getStringValue("add");
                break;
            case REMOVE:
                retVal = instructions.getStringValue("remove");
                break;
            case LOOKUP:
                retVal = instructions.getStringValue("lookup");
                break;
            default:
                throw new AssertionError("Only ADD, REMOVE and LOOKUP supported");
        }
        
        if (!PluralOf.USE_NORMAL_PLURAL_PATTERN.equals(retVal)) {
            // We got the specific name for the method, overrides any algorithm
            return retVal;
        }
        
        String pluralOf = instructions.getStringValue("value");
        if (!PluralOf.USE_NORMAL_PLURAL_PATTERN.equals(pluralOf)) {
            // We got a specific name for the singular, use it
            switch (methodType) {
            case ADD:
                return JAUtilities.ADD + pluralOf;
            case REMOVE:
                return JAUtilities.REMOVE + pluralOf;
            case LOOKUP:
                return JAUtilities.LOOKUP + pluralOf;
            default:
                throw new AssertionError("Only add, remove and lookup supported");
            }
        }
        
        // Do the algorithm
        if (unDecapitalizedVariable.endsWith("s")) {
            unDecapitalizedVariable = unDecapitalizedVariable.substring(0, unDecapitalizedVariable.length() - 1);
        }
        
        switch (methodType) {
        case ADD:
            return JAUtilities.ADD + unDecapitalizedVariable;
        case REMOVE:
            return JAUtilities.REMOVE + unDecapitalizedVariable;
        case LOOKUP:
            return JAUtilities.LOOKUP + unDecapitalizedVariable;
        default:
            throw new AssertionError("Only add, remove and lookup supported");
        }
        
    }
    
    private static final class PluralOfDefault extends AnnotationLiteral<PluralOf> implements PluralOf {
        private static final long serialVersionUID = 4358923840720264176L;

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#value()
         */
        @Override
        public String value() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#add()
         */
        @Override
        public String add() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#remove()
         */
        @Override
        public String remove() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#lookup()
         */
        @Override
        public String lookup() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }
        
    }

}
