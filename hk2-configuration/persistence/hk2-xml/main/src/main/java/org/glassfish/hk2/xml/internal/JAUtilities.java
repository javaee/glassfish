/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.XmlRootElement;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.HybridCacheEntry;
import org.glassfish.hk2.utilities.cache.LRUHybridCache;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.glassfish.hk2.xml.api.annotations.DefaultChild;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;
import org.glassfish.hk2.xml.internal.alt.clazz.ClassAltClassImpl;
import org.glassfish.hk2.xml.internal.alt.clazz.MethodAltMethodImpl;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class JAUtilities {
    private final static String ID_PREFIX = "XmlServiceUID-";
    
    private final static boolean DEBUG_METHODS = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.methods", "false"));
    private final static boolean DEBUG_PREGEN = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.pregenerated", "false"));
    /* package */ final static boolean DEBUG_GENERATION_TIMING = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.generationtime", "false"));
    
    public final static String GET = "get";
    public final static String SET = "set";
    public final static String IS = "is";
    public final static String LOOKUP = "lookup";
    public final static String ADD = "add";
    public final static String REMOVE = "remove";
    public final static String JAXB_DEFAULT_STRING = "##default";
    public final static String JAXB_DEFAULT_DEFAULT = "\u0000";
    
    private final static String NO_CHILD_PACKAGE = "java.";
    
    private final ClassReflectionHelper classReflectionHelper;
    private final HashMap<Class<?>, UnparentedNode> interface2NodeCache = new HashMap<Class<?>, UnparentedNode>();
    private final HashMap<Class<?>, UnparentedNode> proxy2NodeCache = new HashMap<Class<?>, UnparentedNode>();
    private final ClassPool defaultClassPool = ClassPool.getDefault(); // TODO:  We probably need to be more sophisticated about this
    private final CtClass superClazz;
    private int numGeneratedClasses = 0;
    private int numFoundClasses = 0;
    
    private final LRUHybridCache<Class<?>, Model> interface2ModelCache;
    
    private final AtomicLong idGenerator = new AtomicLong();
    
    /* package */ JAUtilities(ClassReflectionHelper classReflectionHelper) {
        this.classReflectionHelper = classReflectionHelper;
        try {
            superClazz = defaultClassPool.get(BaseHK2JAXBBean.class.getName());
        }
        catch (NotFoundException e) {
            throw new MultiException(e);
        }
        
        interface2ModelCache = new LRUHybridCache<Class<?>, Model>(Integer.MAX_VALUE - 1, new Computer(this));
    }
    
    /**
     * Gets the XmlService wide unique identifier
     * @return A unique identifier for unkeyed multi-children
     */
    public String getUniqueId() {
        return ID_PREFIX + idGenerator.getAndAdd(1L);
    }
    
    public synchronized UnparentedNode getNode(Class<?> type) {
        UnparentedNode retVal = proxy2NodeCache.get(type);
        if (retVal == null) return interface2NodeCache.get(type);
        return retVal;
    }
    
    /**
     * Goes from interface name (fully qualified) to the associated Model
     * 
     * @param iFace Fully qualified interface name (not proxy)
     * @return The Model for the interface
     */
    public Model getModel(Class<?> iFace) {
        HybridCacheEntry<Model> entry = interface2ModelCache.compute(iFace);
        return entry.getValue();
    }
    
    public synchronized UnparentedNode convertRootAndLeaves(Class<?> root) {
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        LinkedHashSet<Class<?>> needsToBeConverted = new LinkedHashSet<Class<?>>();
        
        getAllToConvert(root, needsToBeConverted, new HashSet<Class<?>>(), helper);
        
        // Just for fun, lets fill in the model cache here, to flush out any errors
        for (Class<?> needsToBeConvertedClass : needsToBeConverted) {
            interface2ModelCache.compute(needsToBeConvertedClass);
        }
        
        long elapsedTime = 0L;
        if (DEBUG_METHODS || DEBUG_GENERATION_TIMING) {
            elapsedTime = System.currentTimeMillis();
            Logger.getLogger().debug("Converting " + needsToBeConverted.size() + " nodes for root " + root.getName());
        }
        
        needsToBeConverted.removeAll(interface2NodeCache.keySet());
        
        LinkedList<UnparentedNode> contributions = new LinkedList<UnparentedNode>();
        for (Class<?> convertMe : needsToBeConverted) {
            UnparentedNode converted;
            try {
                converted = convert(convertMe, helper);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable e) {
                throw new MultiException(e);
            }
            
            interface2NodeCache.put(convertMe, converted);
            contributions.add(converted);
        }
        
        for (UnparentedNode node : contributions) {
            for (ParentedNode child : node.getAllChildren()) {
                if (child.getChild().isPlaceholder()) {
                    UnparentedNode nonPlaceholder = interface2NodeCache.get(child.getChild().getOriginalInterface());
                    if (nonPlaceholder == null) {
                        throw new RuntimeException("The child of type " + child.getChild().getOriginalInterface().getName() +
                                " is unknown for " + node);  
                    }
                    
                    child.setChild(nonPlaceholder);
                }
            }
        }
        
        helper.dispose();
        
        if (DEBUG_METHODS || DEBUG_GENERATION_TIMING) {
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            Logger.getLogger().debug("Converted " + needsToBeConverted.size() + " nodes.  " +
              "There were " + numFoundClasses + " pre-generated, and " + numGeneratedClasses +
              " dynamically generated.  Analysis took " + elapsedTime + " milliseconds");
        } 
        return interface2NodeCache.get(root);
    }
    
    private UnparentedNode convert(Class<?> convertMe, ClassReflectionHelper helper) throws Throwable {
        Logger.getLogger().debug("XmlService converting " + convertMe.getName());
        UnparentedNode retVal = new UnparentedNode(convertMe);
        
        String targetClassName = Utilities.getProxyNameFromInterfaceName(convertMe.getName());
        CtClass foundClass = defaultClassPool.getOrNull(targetClassName);
        
        if (foundClass == null) {
            numGeneratedClasses++;
            
            long elapsedTime = 0;
            if (DEBUG_PREGEN) {
                elapsedTime = System.nanoTime();
                
                Logger.getLogger().debug("Dynamically generating impl for " + targetClassName);
            }
            
            CtClass generated = Generator.generate(
                    new ClassAltClassImpl(convertMe, helper), superClazz, defaultClassPool);
            
            generated.toClass(convertMe.getClassLoader(), convertMe.getProtectionDomain());
            
            if (DEBUG_PREGEN) {
                elapsedTime = System.nanoTime() - elapsedTime;
                
                Logger.getLogger().debug("Generating impl for " + targetClassName + " took " + elapsedTime + " nanoseconds");
            }
            
            foundClass = defaultClassPool.getOrNull(targetClassName);
        }
        else {
            if (DEBUG_PREGEN) {
                Logger.getLogger().debug("Found pregenerated impl for " + targetClassName);   
            }
            
            numFoundClasses++;
        }
        
        Class<?> proxy = convertMe.getClassLoader().loadClass(targetClassName);
         
        XmlRootElement xre = convertMe.getAnnotation(XmlRootElement.class);
        if (xre != null) {        
            String rootName = Utilities.convertXmlRootElementName(xre, convertMe);
            retVal.setRootName(rootName);
        }
        
        ClassAltClassImpl altConvertMe = new ClassAltClassImpl(convertMe, helper);
        NameInformation xmlNameMap = Generator.getXmlNameMap(altConvertMe);

        MethodInformation foundKey = null;
        for (AltMethod altMethodRaw : altConvertMe.getMethods()) {
            MethodAltMethodImpl altMethod = (MethodAltMethodImpl) altMethodRaw;
            
            MethodInformation mi = Generator.getMethodInformation(altMethod, xmlNameMap);
                
            if (DEBUG_METHODS) {
                Logger.getLogger().debug("Analyzing method " + mi + " of " + convertMe.getSimpleName());
            }
                
            if (mi.isKey()) {
                if (foundKey != null) {
                    throw new RuntimeException("Class " + convertMe.getName() + " has multiple key properties (" + altMethodRaw.getName() +
                            " and " + foundKey.getOriginalMethod().getName());
                }
                foundKey = mi;
                    
                retVal.setKeyProperty(mi.getRepresentedProperty());
            }
                
            boolean getterOrSetter = false;
            UnparentedNode childType = null;
            if (MethodType.SETTER.equals(mi.getMethodType())) {
                getterOrSetter = true;
                
                if (mi.getBaseChildType() != null) {
                    if (!interface2NodeCache.containsKey(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass())) {
                        // Must use a placeholder
                        childType = new UnparentedNode(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass(), true);
                    }
                    else {
                        childType = interface2NodeCache.get(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass());
                    }
                }
            }
            else if (MethodType.GETTER.equals(mi.getMethodType())) {
                getterOrSetter = true;
                
                if (mi.getBaseChildType() != null) {
                    if (!interface2NodeCache.containsKey(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass())) {
                        // Must use a placeholder
                        childType = new UnparentedNode(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass(), true);
                    }
                    else {
                        childType = interface2NodeCache.get(((ClassAltClassImpl) mi.getBaseChildType()).getOriginalClass());
                    }
                }
            }
                
            if (getterOrSetter) {
                if (childType != null) {
                    Map<String, String> defaultChild = null;
                    AltAnnotation defaultChildAnnotation= mi.getOriginalMethod().getAnnotation(DefaultChild.class.getName());
                    if (defaultChildAnnotation != null) {
                        String[] defaultStrings = defaultChildAnnotation.getStringArrayValue("value");
                        
                        defaultChild = convertDefaultChildValueArray(defaultStrings);
                    }
                        
                    retVal.addChild(mi.getRepresentedProperty(),
                            Generator.getChildType(mi.isList(), mi.isArray()),
                            childType,
                            defaultChild,
                            mi.getDefaultValue());
                }
                else {
                    Class<?> expectedType = null;
                    AltClass gsType = mi.getGetterSetterType();
                    if (gsType instanceof ClassAltClassImpl) {
                        expectedType = ((ClassAltClassImpl) gsType).getOriginalClass();
                    }
                    
                    retVal.addNonChildProperty(mi.getRepresentedProperty(), mi.getDefaultValue(), expectedType);
                }
            }
        }
            
        retVal.setTranslatedClass(proxy);
        proxy2NodeCache.put(proxy, retVal);
            
        return retVal;
    }
    
    private static Map<String, String> convertDefaultChildValueArray(String[] values) {
        LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
        if (values == null) return retVal;
        for (String value : values) {
            value = value.trim();
            if ("".equals(value)) continue;
            if (value.charAt(0) == '=') {
                throw new AssertionError("First character of " + value + " may not be an =");
            }
            
            int indexOfEquals = value.indexOf('=');
            if (indexOfEquals < 0) {
                retVal.put(value, null);
            }
            else {
                String key = value.substring(0, indexOfEquals);
                
                String attValue;
                if (indexOfEquals >= (value.length() - 1)) {
                    attValue = null;
                }
                else {
                    attValue = value.substring(indexOfEquals + 1);
                }
                
                retVal.put(key, attValue);
            }
        }
        
        return retVal;
    }
    
    private static void getAllToConvert(Class<?> toBeConverted,
            LinkedHashSet<Class<?>> needsToBeConverted,
            Set<Class<?>> cycleDetector,
            ClassReflectionHelper helper) {
        if (needsToBeConverted.contains(toBeConverted)) return;
        
        if (cycleDetector.contains(toBeConverted)) return;
        cycleDetector.add(toBeConverted);
        
        try {
            // Find all the children
            for (Method method : toBeConverted.getMethods()) {
                if (Generator.isGetter(new MethodAltMethodImpl(method, helper)) == null) continue;
                
                Class<?> returnClass = method.getReturnType();
                if (returnClass.isInterface() && !(List.class.equals(returnClass))) {
                    // The assumption is that this is a non-instanced child
                    if (returnClass.getName().startsWith(NO_CHILD_PACKAGE)) continue;
                    
                    getAllToConvert(returnClass, needsToBeConverted, cycleDetector, helper);
                    
                    continue;
                }
                
                if (returnClass.isArray()) {
                    Class<?> aType = returnClass.getComponentType();
                    
                    if (aType.isInterface()) {
                        getAllToConvert(aType, needsToBeConverted, cycleDetector, helper);
                        
                        continue;
                    }
                }
                
                Type retType = method.getGenericReturnType();
                if (retType == null || !(retType instanceof ParameterizedType)) continue;
                
                Class<?> returnRawClass = ReflectionHelper.getRawClass(retType);
                if (returnRawClass == null || !List.class.equals(returnRawClass)) continue;
                
                Type listReturnType = ReflectionHelper.getFirstTypeArgument(retType);
                if (Object.class.equals(listReturnType)) continue;
                
                Class<?> childClass = ReflectionHelper.getRawClass(listReturnType);
                if (childClass == null || Object.class.equals(childClass)) continue;
                
                getAllToConvert(childClass, needsToBeConverted, cycleDetector, helper);
            }
            
            needsToBeConverted.add(toBeConverted);
        }
        finally {
            cycleDetector.remove(toBeConverted);
        }
    }
    
    private CtClass getBaseClass() {
        return superClazz;
    }
    
    private ClassPool getClassPool() {
        return defaultClassPool;
    }
    
    private final class Computer implements Computable<Class<?>, HybridCacheEntry<Model>> {
        private final JAUtilities jaUtilities;
        
        private Computer(JAUtilities jaUtilities) {
            this.jaUtilities = jaUtilities;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.cache.Computable#compute(java.lang.Object)
         */
        @Override
        public HybridCacheEntry<Model> compute(Class<?> key) {
            String iFaceName = key.getName();
            String proxyName = Utilities.getProxyNameFromInterfaceName(iFaceName);
            
            Class<?> proxyClass = GeneralUtilities.loadClass(key.getClassLoader(), proxyName);
            if (proxyClass == null) {
                // Generate the proxy
                try {
                  CtClass generated = Generator.generate(new ClassAltClassImpl(key, classReflectionHelper),
                        jaUtilities.getBaseClass(),
                        jaUtilities.getClassPool());
                  
                  proxyClass = generated.toClass(key.getClassLoader(), key.getProtectionDomain());
                  
                  
                }
                catch (RuntimeException re) {
                    throw re;
                }
                catch (Throwable th) {
                    throw new RuntimeException(th);
                }
            }
            
            Method getModelMethod;
            try {
                getModelMethod = proxyClass.getMethod(Generator.STATIC_GET_MODEL_METHOD_NAME, new Class<?>[0]);
            }
            catch (NoSuchMethodException e) {
                throw new AssertionError("This proxy must have been generated with an old generator, it has no __getModel method");
            }
            
            Model retVal;
            try {
                retVal = (Model) ReflectionHelper.invoke(null, getModelMethod, new Object[0], false);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
            
            if (retVal == null) {
                throw new AssertionError("The __getModel method on " + proxyClass.getName() + " returned null");
            }
            
            retVal.setJAUtilities(jaUtilities, key.getClassLoader());
            
            return interface2ModelCache.createCacheEntry(key, retVal, false);
        }
        
    }
}
