/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * EnvironmentFactory.java
 *
 * Created on January 9, 2004, 11:42 AM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.pluggable.ConfigEnvironment;

/**
 * provides a implementation of factory. Users can
 * extend this class and implement required methods and set their
 * class in environment variable ENVIRONMENT_FACTORY_CLASS. The
 * statis create method in this class reads the variable and
 * instantiates the class. Note that the implemented factory
 * needs a no arg constructor
 * @author  sridatta
 */
public class AppserverConfigEnvironmentFactory 
      extends com.sun.enterprise.config.pluggable.EnvironmentFactory {
 
    public ConfigEnvironment getConfigEnvironment() {
        ConfigEnvironment ce = new ConfigEnvironmentImpl();
        ce.setConfigBeanInterceptor(new ServerBeanInterceptor());
        return ce;
    }
}
