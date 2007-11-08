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
package com.sun.enterprise.admin.wsmgmt.repository.spi;

import com.sun.enterprise.admin.wsmgmt.repository.impl.*;

/**
 * This SPI mechanism allows RepositoryProvider instance. This repority provider
 * can be queried for existence of web services.  
 * <br>
 * Because the methods in the RepositoryFactory are all static, there is only 
 * one RepositoryFactory per Java VM at any one time. This ensures that there is
 * a single source from which RepositoryProvider implementation is obtained. 
 * <br>
 * Registering RepositoryProvider
 * <br>
 * The name of the provider is supplied on the command line. If no provider is
 * specified, default implementation is assumed. Default provider class is
 * com.sun.enterprise.admin.repository.impl.ApplicationServerRepositoryProvider.
 * For example the following 
 * -Drepository.provider.classname=com.sun.web.WebServerRepositoryProvider
 * would change the provider class to com.sun.web.WebServerRepositoryProvider.
 */
public class RepositoryFactory {

    /**
     * Make RepositoryFactory private, only one sigleton object is available.
     */
    private RepositoryFactory () {
    }

    /**
     * Returns the RepositoryFactory singleton.
     *
     * @return the RepositoryFactory instance
     */
    public static RepositoryFactory  getRepositoryFactory() {
        return new RepositoryFactory();
    }

    /**
     * Returns the RepositoryProvider instance. If 
     * -Drepository.provider.classname is defined, that class is loaded and
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
    public static RepositoryProvider getRepositoryProvider() 
      throws InstantiationException, IllegalAccessException, ClassCastException,
        ClassNotFoundException {
       String implName = System.getProperty(REPOSITORY_PROVIDER_NAME);
       if ( implName == null ) {
            return new AppServRepositoryProvider();
       } else {
            Class repClass = Class.forName(implName);
            Object o = repClass.newInstance();
            return (RepositoryProvider)o;

       }
    }

    /**
     * Returns the WebServiceInfoProvider instance. If 
     * -Dwebserviceinfo.provider.classname is defined, that class is loaded and
     * returned as the repository provider. If there is an error finding or 
     * loading the class, the default provider class is returned.
     *
     * @return WebServiceInfoProvider implementation
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
    public static WebServiceInfoProvider getWebServiceInfoProvider() 
     throws InstantiationException, IllegalAccessException, ClassCastException,
     ClassNotFoundException {
       String implName = System.getProperty(WEBSERVICE_INFO_PROVIDER_NAME);
       if ( implName == null ) {
            //return new AppServWebServiceInfoProvider();
            Class repClass = Class.forName(AS_DEFAULT_PROVIDER);
            return (WebServiceInfoProvider) repClass.newInstance();
       } else {
            Class repClass = Class.forName(implName);
            return (WebServiceInfoProvider) repClass.newInstance();
       }
    }

    /** Environment property name to customize Repository Provider */
    public static final String REPOSITORY_PROVIDER_NAME = 
        "repository.provider.classname";

    /** Environment property name to customize Web Service Info Provider */
    public static final String WEBSERVICE_INFO_PROVIDER_NAME = 
        "webservice_info.provider.classname";
    
    private static final String AS_DEFAULT_PROVIDER = 
        "com.sun.enterprise.tools.common.AppServWebServiceInfoProvider";
}
