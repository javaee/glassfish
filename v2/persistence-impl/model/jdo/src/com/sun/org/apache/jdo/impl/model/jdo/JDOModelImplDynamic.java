/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.model.jdo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.impl.model.jdo.xml.JDOHandler;
import com.sun.org.apache.jdo.impl.model.jdo.xml.JDOHandlerImpl;
import com.sun.org.apache.jdo.impl.model.jdo.xml.JDOParser;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOPackage;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.StringHelper;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * A JDOModel instance bundles a number of JDOClass instances used by an 
 * application. It provides factory methods to create and retrieve JDOClass 
 * instances. A fully qualified class name must be unique within a JDOModel 
 * instance. The model supports multiple classes having the same fully qualified 
 * name by different JDOModel instances.
 * <p>
 * The dynamic JDOModel implementation does not store any internally
 * calculated values. It is intended to be used in an environment
 * where JDO metatdata are likely to be changed (e.g. at development
 * time).
 * <br> 
 * There are two exceptions:
 * <ul>
 * <li>JDOModelImplDynamic caches JDOClass instances. This means a
 * second lookup of the same class will return the same JDOClass
 * instance.
 * <li>JDOModelImplDynamic manages a list of xml resources (.jdo
 * files) that are processed already, to avoid reading the same
 * resource again.
 * <p>
 * TBD:
 * <ul>
 * <li> Other implementations of JavaModel interface: Development
 * <li> Check synchronization.
 * <li> Check non validating XML parsing
 * <li> Open issue: a .jdo file might contain XML for multiple classes. 
 * Today all the metadata is stored in the same jdoModel instance, but at runtime
 * class loading determines the correct jdoModel instance. 
 * Either we need to be able to change the declaringModel of a JDOClass instance. 
 * Or reading a .jdo only load metadata for the requested class, so all the other 
 * metadata is ignored.
 * </ul>
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 2.0
 */
public class JDOModelImplDynamic extends JDOElementImpl implements JDOModel {

    /** 
     * Map of JDOPackage managed by this JDOModel instance,
     * key is the package name.
     */
    private Map jdoPackages = new HashMap();

    /** 
     * Map of JDOClass objects managed by this JDOModel instance, 
     * key is the fully qualified class name. 
     */
    private Map jdoClasses = new HashMap();

    /**
     * Set of names of XML resources that are processed already 
     * (see {link #lookupXMLMetadata(String className)}.
     */
    private Set processedResources = new HashSet();

    /** The JavaModel used to get type info. */
    private JavaModel javaModel;

    /** The default for loadXMLMetadata. */ 
    private final boolean loadXMLMetadataDefault;
    
    /** */
    private final UnresolvedRelationshipHelper unresolvedRelationshipHelper = 
        new UnresolvedRelationshipHelper();

    /** I18N support */
    protected final static I18NHelper msg =
        I18NHelper.getInstance(JDOModelImplDynamic.class);

    /** XML Logger */
    protected static Log xmlLogger = 
        LogFactory.getFactory().getInstance("com.sun.org.apache.jdo.impl.model.jdo.xml"); // NOI18N

    /** Logger */
    protected static Log logger =
        LogFactory.getFactory().getInstance("com.sun.org.apache.jdo.impl.model.jdo"); // NOI18N

    /** 
     * Constructor. 
     * JDOModel instances are created using the JDOModelFactory only.
     */
    protected JDOModelImplDynamic(
        JavaModel javaModel, boolean loadXMLMetadataDefault) {
        super();
        setJavaModel(javaModel);
        this.loadXMLMetadataDefault = loadXMLMetadataDefault;
        try {
            javaModel.setJDOModel(this);
        }
        catch (ModelException ex) {
            throw new ModelFatalException(msg.msg("ERR_CannotSetJDOModel"), ex); //NOI18N
        }
    }

    /** 
     * The method returns a JDOClass instance for the specified package name.
     * If this JDOModel contains the corresponding JDOPackage instance,
     * the existing instance is returned. Otherwise, it creates a new JDOPackage
     * instance and returns the new instance.
     * @param packageName the name of the JDOPackage instance 
     * to be returned
     * @return a JDOPackage instance for the specified package name
     * @exception ModelException if impossible
     */
    public JDOPackage createJDOPackage(String packageName) 
        throws ModelException {
        JDOPackage jdoPackage = getJDOPackage(packageName);
        if (jdoPackage == null) {
            jdoPackage = new JDOPackageImpl();
            jdoPackage.setName(packageName);
            jdoPackage.setDeclaringModel(this);
            jdoPackages.put(packageName, jdoPackage);
        }
        return jdoPackage;
    }
    
    /** 
     * The method returns the JDOPackage instance for the specified package 
     * name, if present. The method returns <code>null</code> if it cannot 
     * find a JDOPackage instance for the specified name. 
     * @param packageName the name of the JDOPackage instance 
     * to be returned
     * @return a JDOPackage instance for the specified package name 
     * or <code>null</code> if not present
     */
    public JDOPackage getJDOPackage(String packageName) {
        return (JDOPackage)jdoPackages.get(packageName);
    }

    /**
     * Returns the collection of JDOPackage instances declared by this JDOModel 
     * in the format of an array.
     * @return the packages declared by this JDOModel
     */
    public JDOPackage[] getDeclaredPackages() {
        return (JDOPackage[])jdoPackages.values().toArray(
            new JDOPackage[jdoPackages.size()]);
    }

    /**
     * The method returns a JDOClass instance for the specified fully qualified
     * class name. If this JDOModel contains the corresponding JDOClass instance,
     * the existing instance is returned. Otherwise, it creates a new JDOClass 
     * instance, sets its declaringModel and returns the new instance.
     * <p>
     * Whether this method reads XML metatdata or not is determined at
     * JDOModel creation time (see flag <code>loadXMLMetadataDefault</code> 
     * in {@link JDOModelFactory#getJDOModel(JavaModel javaModel, boolean
     * loadXMLMetadataDefault)}). Invoking this method is method is equivalent
     * to <code>createJDOClass(className, loadXMLMetadataDefault)</code>.
     * @param className the fully qualified class name of the JDOClass
     * instance to be returned
     * @return a JDOClass instance for the specified class name
     * @exception ModelException if impossible
     */
    public JDOClass createJDOClass(String className) throws ModelException {
        return createJDOClass(className, loadXMLMetadataDefault);
    }

    /**
     * The method returns a JDOClass instance for the specified fully qualified
     * class name. If this JDOModel contains the corresponding JDOClass instance,
     * the existing instance is returned. Otherwise, if the flag loadXMLMetadata
     * is set to <code>true</code> the method tries to find the JDOClass 
     * instance by reading the XML metadata. If it could not be found the method
     * creates a new JDOClass instance, sets its declaringModel and returns the 
     * instance.
     * @param className the fully qualified class name of the JDOClass instance 
     * to be returned
     * @param loadXMLMetadata indicated whether to read XML metadata or not
     * @return a JDOClass instance for the specified class name
     * @exception ModelException if impossible
     */
    public synchronized JDOClass createJDOClass(String className, 
                                                boolean loadXMLMetadata)
        throws ModelException {
        JDOClass jdoClass = getJDOClass(className, loadXMLMetadata);
        if (jdoClass == null) {
            if (logger.isDebugEnabled())
                logger.debug("JDOModel.createJDOClass: " + //NOI18N
                             "create new JDOClass instance " + className); //NOI18N
            jdoClass = newJDOClassInstance(className);
            jdoClass.setDeclaringModel(this);
            jdoClasses.put(className, jdoClass);
            // create the corresponding JDOPackage
            jdoClass.setJDOPackage(createJDOPackage(getPackageName(className)));
        }
        return jdoClass;
    }

    /**
     * The method returns the JDOClass instance for the specified fully 
     * qualified class name if present. The method returns <code>null</code> 
     * if it cannot find a JDOClass instance for the specified name. 
     * <p>
     * Whether this method reads XML metatdata or not is determined at
     * JDOModel creation time (see flag <code>loadXMLMetadataDefault</code> 
     * in {@link JDOModelFactory#getJDOModel(JavaModel javaModel, boolean 
     * loadXMLMetadataDefault)}). Invoking this method is method is equivalent
     * to <code>createJDOClass(className, loadXMLMetadataDefault)</code>.
     * @param className the fully qualified class name of the JDOClass
     * instance to be returned
     * @return a JDOClass instance for the specified class name 
     * or <code>null</code> if not present
     */
    public JDOClass getJDOClass(String className) {
        return getJDOClass(className, loadXMLMetadataDefault);
    }
    
    /**
     * The method returns the JDOClass instance for the specified fully 
     * qualified class name if present. If the flag loadXMLMetadata is set 
     * to <code>true</code> the method tries to find the JDOClass instance by 
     * reading the XML metadata. The method returns null if it cannot find a 
     * JDOClass instance for the specified name.
     * @param className the fully qualified class name of the JDOClass instance 
     * to be returned
     * @param loadXMLMetadata indicate whether to read XML metatdata or not
     * @return a JDOClass instance for the specified class name
     * or <code>null</code> if not present
     * @exception ModelException if impossible
     */
    public synchronized JDOClass getJDOClass(String className, 
                                             boolean loadXMLMetadata) {

        boolean trace = logger.isTraceEnabled();

        // check whether the class is known to be non PC
        if (isKnownNonPC(className)) {
            if (trace)
                logger.trace("JDOModel.getJDOClass: " + className + //NOI18N
                             " known to be non-persistence-capable"); //NOI18N
            return null;
        }

        JDOClass jdoClass = (JDOClass)jdoClasses.get(className);
        if (trace)
            logger.trace("JDOModel.getJDOClass: " + className + //NOI18N
                         ((jdoClass != null) ? " cached" : " not cached")); //NOI18N

        // check for XML metatdata
        if (loadXMLMetadata) {
            if (jdoClass == null)
                jdoClass = lookupXMLMetadata(className);
            else if (!jdoClass.isXMLMetadataLoaded())
                jdoClass = lookupXMLMetadata(jdoClass);

            if (jdoClass == null) {
                // we loaded XML metadata, but there is no metadata
                // for this class => known to be non persistence-capable
                knownNonPC(className);
            }
        }

        return jdoClass;
    }

    /**
     * The method returns the JDOClass instance for the specified short name
     * (see {@link JDOClass#getShortName()}) or <code>null</code> if it cannot
     * find a JDOClass instance with the specified short name. 
     * <p>
     * The method searches the list of JDOClasses currently managed by this
     * JDOModel instance. It does not attempt to load any metadata if it
     * cannot find a JDOClass instance with the specified short name. The
     * metadata for a JDOClass returned by this method must have been loaded
     * before by any of the methods
     * {@link #createJDOClass(String className)},
     * {@link #createJDOClass(String className, boolean loadXMLMetadataDefault)},
     * {@link #getJDOClass(String className)}, or
     * {@link #getJDOClass(String className, boolean loadXMLMetadataDefault)}.
     * @param shortName the short name of the JDOClass instance to be returned
     * @return a JDOClass instance for the specified short name 
     * or <code>null</code> if not present
     */
    public synchronized JDOClass getJDOClassForShortName(String shortName) {
        if (StringHelper.isEmpty(shortName))
            return null;

        for (Iterator i = jdoClasses.values().iterator(); i.hasNext();) {
            JDOClass jdoClass = (JDOClass)i.next();
            if (shortName.equals(jdoClass.getShortName()))
                // found => return
                return jdoClass;
        }

        return null;
    }

    /**
     * Returns the collection of JDOClass instances declared by this JDOModel 
     * in the format of an array.
     * @return the classes declared by this JDOModel
     */
    public synchronized JDOClass[] getDeclaredClasses()  {
        return (JDOClass[])jdoClasses.values().toArray(
            new JDOClass[jdoClasses.size()]);
    }

    /**
     * Returns the JavaModel bound to this JDOModel instance.
     * @return the JavaModel
     */
    public JavaModel getJavaModel() {
        return javaModel;
    }
    
    /**
     * Sets the JavaModel for this JDOModel instance.
     * @param javaModel the JavaModel
     */
    public void setJavaModel(JavaModel javaModel) {
        this.javaModel = javaModel;
    }
    
    /**
     * Returns the parent JDOModel instance of this JDOModel.
     * @return the parent JDOModel
     */
    public JDOModel getParent() {
        if (javaModel != null) {
            JavaModel parentJavaModel = javaModel.getParent();
            if (parentJavaModel != null)
                return parentJavaModel.getJDOModel();
        }
        return null; 
    }

    /**
     * This method returns the JDOClass instance that defines the specified type
     * as its objectId class. In the case of an inheritance hierarchy it returns 
     * the top most persistence-capable class of the hierarchy (see 
     * {@link JDOClass#getPersistenceCapableSuperclass}).
     * @param objectIdClass the type representation of the ObjectId class
     * @return the JDOClass defining the specified class as ObjectId class
     */
    public JDOClass getJDOClassForObjectIdClass(JavaType objectIdClass) {
        // Note, method getJDOClassForObjectIdClass is not synchronized to
        // avoid a deadlock with PC class registration.
        if (logger.isTraceEnabled())
            logger.trace("JDOModel.getJDOClassForObjectIdClass: " + //NOI18N
                         "check objectIdClass " +objectIdClass); //NOI18N
                        
        if (objectIdClass == null)
            return null;

        JDOClass jdoClass = null;
        String objectIdClassName = objectIdClass.getName();
        // check all JDOClasses for this JDOModel instance
        List classesToActivate = new ArrayList();
        while (true) {
            try {
                for (Iterator i = jdoClasses.values().iterator(); i.hasNext();) {
                    JDOClass next = (JDOClass)i.next();
                    if (next.isXMLMetadataLoaded()) {
                        // XML metadata is loaded => check the objectIdClass
                        if (objectIdClassName.equals(
                                next.getDeclaredObjectIdClassName())) {
                            // found => return
                            return next;
                        }
                    }
                    else {
                        // XML metadata is NOT loaded => 
                        // store the class for later processing.
                        // Do not load the XML metadata here. This might create
                        // new JDOClass instances in this model for other pc
                        // classes listed in the same .jdo file. This would
                        // change the jdoClasses map while its values are
                        // iterated => ConcurrentModificationException
                        classesToActivate.add(next);
                    }
                }
                // No ConcurrentModificationException => break the loop 
                break;
            }
            catch (ConcurrentModificationException ex) {
                // ConcurrentModificationException means a JDOClass was
                // added to the JDOModel in parallel => 
                // start the loop again.
            }
        }
                
        // None of the activated JDOClasses knows the objectIdClass =>
        // activate the classes that were registered but not activated 
        // and check these classes
        for (Iterator i = classesToActivate.iterator(); i.hasNext();) {
            JDOClass next = (JDOClass)i.next();
            lookupXMLMetadata(next);

            if (objectIdClass.equals(next.getDeclaredObjectIdClassName())) {
                // found => return
                return next;
            }
        }

        // objectIdClass not found in this model => return null
        return null;
    }

    //========= Internal helper methods ==========
    
    /** Returns a new instance of the JDOClass implementation class. */
    protected JDOClass newJDOClassInstance(String name) {
        return new JDOClassImplDynamic(name);
    }

    /**
     * Checks whether the type with the specified name does NOT denote a
     * persistence-capable class.
     * @param typeName name of the type to be checked
     * @return <code>true</code> if types is a name of a primitive type; 
     * <code>false</code> otherwise
     */
    protected boolean isKnownNonPC(String typeName) {
        // Any class from packages java and javax are supposed to be non-pc.
        return typeName.startsWith("java.") || //NOI18N
               typeName.startsWith("javax."); //NOI18N
    }

    /** 
     * Hook called when a class is known to be non persistence
     * capable. Subclasses might want to keep track of classes marked
     * as non pc classes and use this in the implementation of
     * {link #isKnownNonPC(String typeName)}.
     * @param className the name of the non-pc class
     */
    protected void knownNonPC(String className) {
    }

    /**
     * The method seaches JDO metadata for the class with the specified
     * class name. Chapter 18 of the JDO specification defines the search
     * order. The method skips resources that have been tried to load
     * before, no matter whether the resource currently exist. 
     * The method returns the populated JDOClass instance, if there is XML
     * metadata available for the specified class name. Otherwise it
     * returns <code>null</code>. 
     * <p>
     * The purpose of this method is to populate an existing JDOClass
     * instance with the JDO metadata. It throws an exception if there is
     * no jdo file for this class. The specified jdoClass must not be
     * <code>null</code>; otherwise a NullPointerException is thrown.
     * @param jdoClass the non-activated JDOClass instance.
     * @return the JDOClass instance for the specified class name or
     * <code>null</code> if there is no XML metadata.
     * @exception ModelFatalException indicates a problem while parsing the
     * XML file or a missing XML file.
     * @exception NullPointerException the specified jdoClass is
     * <code>null</code>.
     */
    private JDOClass lookupXMLMetadata(JDOClass jdoClass)
        throws ModelFatalException, NullPointerException {
        String className = jdoClass.getName();
        JDOClass activated = lookupXMLMetadata(className);
        if (activated == null) {
            throw new ModelFatalException(msg.msg(
                "EXC_MissingJDOMetadata", className)); //NOI18N
        }
        else if (activated != jdoClass) {
            throw new ModelFatalException(msg.msg(
                "ERR_MultipleJDOClassInstances", className)); //NOI18N
        }
        return jdoClass;
    }

    /**
     * The method seaches JDO metadata for the class with the specified
     * class name. Chapter 18 of the JDO specification defines the search
     * order. The method skips resources that have been tried to load
     * before, no matter whether the resource currently exist. 
     * The method returns the populated JDOClass instance, if there is XML
     * metadata available for the specified class name. Otherwise it
     * returns <code>null</code>. 
     * @param className the name of the class to check for XML metadata.
     * @return the JDOClass instance for the specified class name or
     * <code>null</code> if there is no XML metadata.
     */
    private JDOClass lookupXMLMetadata(String className) {
        boolean debug = xmlLogger.isDebugEnabled();
        JDOClass jdoClass = null;
        
        if (debug)
            xmlLogger.debug("JDOModel.lookupXMLMetadata:" + // NOI18N
                            " lookup XML for class " + className); // NOI18N
        // Iterate possible resources to find JDO metadata for className
        Iterator i = new MetadataResourceNameIterator(className);
        while((jdoClass == null) && i.hasNext()) {
            String resource = (String)i.next();
            if (processedResources.contains(resource)) {
                if (debug)
                    xmlLogger.debug("  XML " + resource + //NOI18N
                                    " processed already"); //NOI18N
                // processed already => skip
                continue;
            }
            else {
                // add resource to the list of processed resources
                // Note, this adds the resource no matter whether the
                // resource existst or not. This implies this JDOModel 
                // instance will not check this resource again, 
                // even if it exists, again.
                processedResources.add(resource);
                // load resource
                jdoClass = loadXMLResource(resource, className);
            }
        }
        if (debug)
            xmlLogger.debug("JDOModel.lookupXMLMetadata: " +  //NOI18N
                            ((jdoClass!=null)?"found":"no") + //NOI18N
                            " JDO metadata for class " + className); //NOI18N
        return jdoClass;
    }

    /**
     * Load the specified resource assuming it contains JDO metadata for
     * one or more persistence capable classes. 
     * The method returns the populated JDOClass instance, if the specified
     * resource has XML metadata for the specified class name. Otherwise it
     * returns <code>null</code>. 
     * @param resource resource to load
     * @param className the name of the class to check for XML metadata.
     * @return the JDOClass instance for the specified class name or
     * <code>null</code> if the specified resource has no XML metadata.
     */
    private JDOClass loadXMLResource(String resource, String className) {
        boolean debug = xmlLogger.isDebugEnabled();
        JDOClass jdoClass = null;
        InputStream stream = javaModel.getInputStreamForResource(resource);
        if (stream == null) {
            if (debug)
                xmlLogger.debug("  XML " + resource + " not available"); //NOI18N
        }
        else {
            // resource exists => parse it
            // Store all pc classes specified in this resource in this
            // JDOModel instance => pass this to the handler
            JDOHandler handler = new JDOHandlerImpl(this);
            JDOParser parser = new JDOParser(handler);
            try {
                if (debug)
                    xmlLogger.debug("  XML " + resource +  //NOI18N
                                    " found, start parsing ..."); //NOI18N
                parser.parse(new InputSource(new InputStreamReader(stream)));
            }
            catch (SAXException ex) {
                throw new ModelFatalException(
                    msg.msg("EXC_XMLError", resource), ex); //NOI18N
            }
            catch (ParserConfigurationException ex) {
                throw new ModelFatalException(
                    msg.msg("EXC_XMLError", resource), ex); //NOI18N
            }
            catch (IOException ex) {
                throw new ModelFatalException(
                    msg.msg("EXC_XMLError", resource), ex); //NOI18N
            }
            finally {
                try { stream.close(); }
                catch (IOException ex) { 
                    // ignore close exception, stream will be nullified anyway 
                }
            }
            stream = null;
            
            // post process loaded JDOClasses
            Collection newJDOClasses = handler.handledJDOClasses();
            if (debug)
                xmlLogger.debug("  XML " + resource + //NOI18N
                                " has JDO metadata for class(es) " + //NOI18N
                                newJDOClasses);
            for (Iterator i = newJDOClasses.iterator(); i.hasNext();) {
                JDOClass next = (JDOClass)i.next();
                if (className.equals(next.getName())) {
                    jdoClass = next;
                }
                checkSuperclass(next);
            }
            if (jdoClass == null)
                if (debug)
                    xmlLogger.debug("  XML " + resource + //NOI18N
                                    " does not have JDO metadata for class " + //NOI18N
                                    className); 
        }
        return jdoClass;
    }

    /**
     * Updates the pcSuperclass property of the specified JDOClass and all its 
     * superclasses. 
     * @param jdoClass the class to be checked
     */
    private void checkSuperclass(JDOClass jdoClass) {
        if (jdoClass != null) {
            JDOClass superclass = jdoClass.getPersistenceCapableSuperclass();
            checkSuperclass(superclass);
        }
    }

    /** Returns the package name of a class name */
    private String getPackageName(String className) {
        int index = className.lastIndexOf('.');
        return (index == -1) ? "" : className.substring(0, index); //NOI18N
    }
    
    /**
     * Returns the UnresolvedRelationshipHelper instance for this JDOModel
     * instance. 
     * @return the current UnresolvedRelationshipHelper
     */
    UnresolvedRelationshipHelper getUnresolvedRelationshipHelper() {
        return unresolvedRelationshipHelper;
    }

    /**
     * This Iterator implementation iterates resource names of possible JDO
     * metadata files for the specified class name. Chapter 18 of the JDO
     * specification defines the search order as follows: 
     * META-INF/package.jdo, WEB-INF/package.jdo, package.jdo, 
     * <package>/.../<package>/package.jdo, and <package>/<class>.jdo.
     */
    private static class MetadataResourceNameIterator 
        implements Iterator {
        /** Suffix of a JDO metadata file. */
        private static final String JDO_SUFFIX = ".jdo"; //NOI18N

        /** The name of a package JDO metadata file. */
        private static final String PACKAGE_JDO = "package" + JDO_SUFFIX; //NOI18N

        /** List of constant package JDO metadata file names. */
        private static final String[] constantResources = { 
            "META-INF/" + PACKAGE_JDO, //NOI18N
            "WEB-INF/" + PACKAGE_JDO,  //NOI18N
            PACKAGE_JDO 
        };

        /** Indicates whether this iterator has more elements. */
        private boolean hasNext = true;

        /** The class name as resource name. */
        private final String prefix;

        /** Current index in the list of constant package JDO metadata file names. */
        private int constantResourcesIndex = 0;

        /** Current index in the prefix. */
        private int fromIndex = 0;

        /** Constructor. */
        public MetadataResourceNameIterator(String className) {
            this.prefix = className.replace('.', '/');
        }
        
        /**
         * Returns <code>true</code> if the iteration has more elements.
         * @return <code>true</code> if the iterator has more elements.
         */
        public boolean hasNext() {
            return hasNext;
        }
        
        /**
         * Returns the next resource name.
         * @return the next resource name.
         * @exception NoSuchElementException iteration has no more elements. 
         */
        public Object next() {
            String resource = null;
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            else if (constantResourcesIndex < constantResources.length) {
                // Use a constant resource name, if there is one left in
                // the iteration.
                resource = constantResources[constantResourcesIndex];
                constantResourcesIndex++;
            }
            else {
                // No constant resource name left
                // build resource names from the package of the class name
                // Check for the next package, fromIndex is the index of
                // the '/' used in the previous iteration.
                int index = prefix.indexOf('/', fromIndex);
                if (index != -1) {
                    // string needs to include '/' => use index+1
                    resource = prefix.substring(0, index + 1) + PACKAGE_JDO;
                    fromIndex = index + 1;
                }
                else {
                    // no more package jdo files left => use class .jdo file 
                    resource = prefix + JDO_SUFFIX;
                    // the class jdo file is the last resource to be checked 
                    hasNext = false;
                }
            }
            return resource;
        }

        /**
         * This Iterator does not implement this method.
         * @exception UnsupportedOperationException if the
         * <code>remove</code> operation is not supported by this
         * Iterator. 
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}
