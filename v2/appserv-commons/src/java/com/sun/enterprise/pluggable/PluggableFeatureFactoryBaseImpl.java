/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.pluggable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract base class for implementing a PluggableFeatureFactory interface i * using dynamic proxies. This class does not directly implement the interface
 * PluggableFeatureFactory, but an proxy instance implementing the interface
 * can be obtained by a call to the static method getInstance(). In reality,
 * this class implements InvocationHnalder interface used to handle method
 * invocations on a dynamic proxy object.
 */
public abstract class PluggableFeatureFactoryBaseImpl implements InvocationHandler {

    /**
     * Reference to a logger used to log exceptions.
     */
    private Logger _logger;

    /**
     * A property object that keeps interface names and the names of
     * corresponding implementation classes.
     */
    private Properties _featureImplClasses;

    /**
     * Private constructor. The public instances of this object are not
     * available. The instance of this class is however used as invocation
     * handler for dynamic proxy returned by static method getInstance().
     */
    protected PluggableFeatureFactoryBaseImpl(Logger logger) {
        _logger = logger;
    }

    /**
     * Handle a invocation on a proxy object. This implementation looks for
     * a property key matching the return type of the method, then takes the
     * value of the property, assumes it to be a class name, assumes that the
     * class has a default public constructor and then creates an instance of
     * that class and returns it.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String featureName = findFeatureFromMethod(method);
        String className = _featureImplClasses.getProperty(featureName); 
        Object featureImpl = Class.forName(className).newInstance();
        return featureImpl;
    }

    protected abstract String getDefaultFeatureFactoryPropertyName();

    /**
     * Get an instance of a pluggable feature factory using the system
     * property com.sun.appserv.pluggable.features. The value of the system
     * property is expected to be the name of a class that extends
     * java.util.Properties and defines one property for every supported
     * pluggable feature. This method calls getInstance(String) if the
     * value of com.sun.appserv.pluggable.features is not null.
     *
     * The property com.sun.appserv.pluggable.features is defined as a
     * constant in the interface PluggableFeatureFactory and the implementation
     * uses that (PluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME)
     *
     * @return the return value from call to getInstance(String) if the
     *     system property is defined, null otherwise.
     * @see getInstance(String propClassName)
     */
    public Object getInstance() {
        String propClassName = System.getProperty(
                getDefaultFeatureFactoryPropertyName());
        return getInstance(propClassName);
    }

    /**
     * Get an instance of a pluggable feature factory using specified property
     * class name. The method expects name of a class that extends
     * java.util.Properties and has a default (null or no argument) constructor.
     * This method will create an instance of specified class and then
     * call getInstance(Properties). If an instance of specified class name can
     * not be created, the method logs the exception to the logger specified
     * by setLogger() method, or to System.err (if no logger was set).
     *
     * @param propClassName name of a class that extends Properties and contains
     *     a property for every supported pluggable feature.
     *
     * @return the return value from getInstance(Properties) if propClassName
     *     is not null and an instance of the class represented by propClassName
     *     was successfully created, null otherwise.
     * @see getInstance(java.util.Properties props)
     */
    public Object getInstance(String propClassName) {
        if (propClassName == null) {
            return null;
        }
        Properties props = null;
        try {
            props = (Properties)Class.forName(propClassName).newInstance();
        } catch (Exception ex) {
            String msg = "Error loading pluggable features class "
                    + propClassName;
            if (_logger != null) {
                _logger.log(Level.WARNING, msg, ex);
            } else {
                System.err.println(msg + "\nStack Trace:");
                ex.printStackTrace();
            }
        }
        return getInstance(props);
    }

    /**
     * Get an instance of a pluggable feature factory. The method expects
     * a property object as parameter that has one property for every supported
     * pluggable feature. The property name is the name of the interface
     * (without package name) and property value is the fully qualified name of
     * the class that implements the interface. The implementing class must
     * have a default public constructor.
     *
     * @param props properties defining name of feature and implementing
     *     classes. Name of the feature is the class name of the interface
     *     defining the feature.
     * @return a proxy object implementing the interface
     *     PluggableFeatureFactory. If specified parameter props is null, 
     *     the method returns null.
     */
    protected abstract Object createFeatureFactory(InvocationHandler handler);

    public Object getInstance(Properties props) {
        if (props == null) {
            return null;
        }
        _featureImplClasses = props;
        return createFeatureFactory(this);
    }

    /**
     * Find feature name from a method. For the interface
     * PluggableFeatureFactory, a feature name is name of the interface
     * defining the feature. The return type of any method in the interface
     * PluggableFeatureFactory is the interface defining the feature (Note
     * that the interface only contains getter methods). For example, if one
     * of the pluggable features is defined by the interface
     * com.sun.enterprise.server.pluggable.CoolStuff, the corresponding
     * feature name is CoolStuff.
     */
    private String findFeatureFromMethod(Method method) {
        Class returnType = method.getReturnType();
        return Utils.getNQClassName(returnType);
    }

}
