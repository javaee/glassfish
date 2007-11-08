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
package com.sun.enterprise.server.pluggable;

import com.sun.enterprise.pluggable.PluggableFeatureFactoryBaseImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

/**
 * An implementation of PluggableFeatureFactory interface using dynamic
 * proxies. This class does not directly implement the interface
 * PluggableFeatureFactory, but an proxy instance implementing the interface
 * can be obtained by a call to the static method getInstance(). In reality,
 * this class implements InvocationHnalder interface used to handle method
 * invocations on a dynamic proxy object.
 */
public class PluggableFeatureFactoryImpl extends PluggableFeatureFactoryBaseImpl {
    /**
     * Name of the property class containing feature name and feature
     * implementation class names.
     */
    private static final String DEFAULT_FEATURES_PROPERTY_CLASS =
            "com.sun.enterprise.server.pluggable.PEPluggableFeatureImpl";

    private static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /**
     * Private constructor. The public instances of this object are not
     * available. The instance of this class is however used as invocation
     * handler for dynamic proxy returned by static method getInstance().
     */
    public PluggableFeatureFactoryImpl(Logger logger) {
        super(logger);
    }

    public static PluggableFeatureFactory getFactory() {
        String featurePropClass = System.getProperty(
                PluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME,
                DEFAULT_FEATURES_PROPERTY_CLASS);
        _logger.log(Level.FINER, "featurePropClass: " + featurePropClass);
        PluggableFeatureFactoryImpl featureFactoryImpl = 
            new PluggableFeatureFactoryImpl(_logger);
        PluggableFeatureFactory featureFactory = (PluggableFeatureFactory)
            featureFactoryImpl.getInstance(featurePropClass);
        if (featureFactory == null) {	    
            _logger.log(Level.WARNING,
                    "j2eerunner.pluggable_feature_noinit", featurePropClass);
        }
        return featureFactory;
    }

    protected String getDefaultFeatureFactoryPropertyName() {
        return System.getProperty(
            PluggableFeatureFactory.PLUGGABLE_FEATURES_PROPERTY_NAME);
    }

    protected Object createFeatureFactory(InvocationHandler handler) {
        return Proxy.newProxyInstance(
                PluggableFeatureFactory.class.getClassLoader(),
                new Class[] { PluggableFeatureFactory.class },
                handler);
    }
}
