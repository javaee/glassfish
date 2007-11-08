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

package com.sun.enterprise.admin.server.core;
import com.sun.enterprise.config.serverbeans.Mbean;
import java.lang.reflect.Constructor;
import javax.management.MBeanServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.List;

class CustomMBeanRegistrationHelper {
    private final MBeanServer mbs;
    private final ConfigContext cc;
    private final String myName;
    private final CustomMBeanRegistration cmr;
    CustomMBeanRegistrationHelper(final MBeanServer mbs, final ConfigContext cc) throws Exception {
        this.mbs     = mbs;
        this.cc     = cc;
        myName      = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        cmr         = mbeanRegistrationFactory();
    }
    
    void registerMBeans() throws Exception {
        final List<Mbean> m2r = getMBeans2Register();
        cmr.registerMBeans(m2r);
    }
    
    ///// private methods /////
    private CustomMBeanRegistration mbeanRegistrationFactory() throws Exception {
        final String CUSTOM_REGRISTRATION_IMPL_CLASS = "com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl";
        final Class c           = Class.forName(CUSTOM_REGRISTRATION_IMPL_CLASS);
        final Class[] pts       = new Class[]{javax.management.MBeanServer.class};
        final Constructor ctor  = c.getConstructor(pts);
        final Object[] aps      = new Object[]{mbs};
        return ( (CustomMBeanRegistration) ctor.newInstance(aps) );
    }
    
    private List<Mbean> getMBeans2Register() throws Exception {
        return ( ServerBeansFactory.getFullyEnabledUserDefinedMBeans(cc, myName) );
    }
    ///// private methods /////
}