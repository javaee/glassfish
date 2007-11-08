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
package com.sun.enterprise.admin.monitor;


/**
 * This SPI mechanism allows loaders to call web services Management module to
 * initialize web service endpoints for monitoring and management. JSR 77 and
 * Monitoring MBeans are created at this time.
 * <br>
 */
public class WSMonitorLifeCycleFactory {

    /**
     * WSMonitorLifeCycleFactory is private. Only one sigleton object is available
     * is available through getInstance method.
     */
    private WSMonitorLifeCycleFactory () {
    }

    /**
     * Returns the WSMonitorLifeCycleFactory singleton.
     *
     * @return the WSMonitorLifeCycleFactory instance
     */
    public static WSMonitorLifeCycleFactory  getInstance() {
        return lcf;
    }

    /**
     * Returns the WSLifeCycleProvider instance. If 
     * -Dwsmgmt.lifecycle.provider.classname is defined,that class is loaded and
     * returned as the WSLifeCycle provider. If there is an error finding or 
     * loading the class, the default provider class is returned.
     *
     * @return WSMonitorLifeCycleProvider implementation
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
    public WSMonitorLifeCycleProvider getWSMonitorLifeCycleProvider() 
      throws InstantiationException, IllegalAccessException, ClassCastException,
        ClassNotFoundException {
       String implName = System.getProperty(WSMGMT_PROVIDER_NAME);
       if ( implName == null ) {
            Class repClass = Class.forName(WSMGMT_DEFAULT_PROVIDER);
            Object o = repClass.newInstance();
            return (WSMonitorLifeCycleProvider)o;
       } else {
            Class repClass = Class.forName(implName);
            Object o = repClass.newInstance();
            return (WSMonitorLifeCycleProvider)o;
       }
    }

    /** Environment property name to customize Repository Provider */
    public static final String WSMGMT_PROVIDER_NAME = 
        "wsmgmt.lifecycle.provider.classname";

    public static final String WSMGMT_DEFAULT_PROVIDER = 
        "com.sun.enterprise.admin.wsmgmt.lifecycle.AppServWSMonitorLifeCycleProvider";

    // PRIVATE VARS
    static WSMonitorLifeCycleFactory lcf = new WSMonitorLifeCycleFactory();
}
