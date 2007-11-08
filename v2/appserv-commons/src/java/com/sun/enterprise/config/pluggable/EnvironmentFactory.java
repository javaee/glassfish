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

/*
 * EnvironmentFactory.java
 *
 * Created on January 9, 2004, 11:42 AM
 */

package com.sun.enterprise.config.pluggable;

import com.sun.enterprise.config.ConfigRuntimeException;
import com.sun.enterprise.config.impl.ConfigEnvironmentImpl;
import com.sun.enterprise.config.impl.DefaultConfigBeanInterceptor;
import com.sun.enterprise.config.util.LocalStringsHelper;
import com.sun.enterprise.config.util.LoggerHelper;
/**
 * provides a implementation of factory. Users can
 * extend this class and implement required methods and set their
 * class in environment variable ENVIRONMENT_FACTORY_CLASS. The
 * statis create method in this class reads the variable and
 * instantiates the class. Note that the implemented factory
 * needs a no arg constructor
 * @author  sridatta
 */
public class EnvironmentFactory {
    
   static final String ENVIRONMENT_FACTORY_CLASS = 
      "com.sun.enterprise.config.config_environment_factory_class";
        
    private static EnvironmentFactory _ENV = null;
    
    /** Creates a new instance of EnvironmentFactory */
    public EnvironmentFactory() {
    }
    
    public static synchronized EnvironmentFactory getEnvironmentFactory() {
        if(_ENV == null) {
           _ENV = createEnvironmentFactory();
        } 
        return _ENV; 
    }
        
    private static EnvironmentFactory createEnvironmentFactory() {
                                            
        String factoryClassName = System.getProperty(ENVIRONMENT_FACTORY_CLASS);
        
        Class factoryClass;
        try {
            if(factoryClassName!= null && !"".equals(factoryClassName)) {
                factoryClass = Class.forName(factoryClassName);
            } else {
                factoryClass = EnvironmentFactory.class;
            }
        }catch(Exception e) {
            throw new ConfigRuntimeException(
                    "error_loading_environment_factory_class",
                    LocalStringsHelper.
                        getString("error_loading_environment_factory_class"),
                    e);
        }
        LoggerHelper.fine(
            "com.sun.enterprise.config.pluggable.EnvironmentFactory.getEnvironmentFactory():" +
                    "Factory Class is " + factoryClass);
        
        EnvironmentFactory result = null;
        try {
            result = (EnvironmentFactory) factoryClass.newInstance();
        } catch(Exception e) {
            throw new ConfigRuntimeException(
                "error_creating_environment_factory", 
                LocalStringsHelper.
                        getString("error_creating_environment_factory"),
                e);
        }
        
        return result;
    }
    
    /**
     * creates a new configEnvironmentImpl with 
     * defaults
     */
    public ConfigEnvironment getConfigEnvironment() {
        ConfigEnvironment ce = new ConfigEnvironmentImpl();
        ce.setConfigBeanInterceptor(new DefaultConfigBeanInterceptor());
        return ce;
    }
}
