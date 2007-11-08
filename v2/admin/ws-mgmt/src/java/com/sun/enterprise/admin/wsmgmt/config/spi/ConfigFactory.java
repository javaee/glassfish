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
package com.sun.enterprise.admin.wsmgmt.config.spi;


/**
 * This SPI mechanism allows getting web services Management configuration. Thi
 * provider can be implemented using any configuration storage mechanism 
 * underneath. 
 */
public class ConfigFactory {

    /**
     * Make ConfigFactory private, only one sigleton object is available.
     */
    private ConfigFactory () {
    }

    /**
     * Returns the ConfigFactory singleton.
     *
     * @return the ConfigFactory instance
     */
    public static ConfigFactory  getConfigFactory() {
        return new ConfigFactory();
    }

    /**
     * Returns the ConfigProvider instance. If 
     * -Dconfig.provider.classname is defined, that class is loaded and
     * returned as the repository provider. If there is an error finding or 
     * loading the class, the default provider class is returned.
     *
     * @return RepositoryProvider implementation
     * @throws IllegalAccessException - if the class or its nullary constructor 
     *                                  is not accessible. 
     *         InstantiationException - if this Class represents an abstract 
     *                                  class,an interface, an array class, a 
     *                                  primitive type, or void; or if the class     *                                  has no nullary constructor; or if the 
     *                                  instantiation fails for some other 
     *                                  reason. 
     *         ClassCastException     - if the provider implementation specified
     *                                  by -D does not implement the com.sun.
     *                                  enterprise.admin.wsmgmt.repository.spi.
     *                                  RepositoryProvider interface.
     *         ClassNotFoundException - if the provider implementation specified     *                                  by -D does could not be found by the 
     *                                  class loader.
     */
    public ConfigProvider getConfigProvider() 
      throws InstantiationException, IllegalAccessException, ClassCastException,
        ClassNotFoundException {
       String implName = System.getProperty(CONFIG_PROVIDER_NAME);
       if ( implName == null ) {
            Class repClass = Class.forName(CONFIG_DEFAULT_PROVIDER);
            Object o = repClass.newInstance();
            return (ConfigProvider)o;
       } else {
            Class repClass = Class.forName(implName);
            Object o = repClass.newInstance();
            return (ConfigProvider)o;
       }
    }

    /** Environment property name to customize Repository Provider */
    public static final String CONFIG_PROVIDER_NAME = 
        "config.provider.classname";

    public static final String CONFIG_DEFAULT_PROVIDER = 
        "com.sun.enterprise.admin.wsmgmt.config.impl.AppServConfigProvider";
}
