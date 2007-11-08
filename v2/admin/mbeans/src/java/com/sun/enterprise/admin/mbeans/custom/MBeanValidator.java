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

package com.sun.enterprise.admin.mbeans.custom;

import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;
import com.sun.enterprise.admin.server.core.CustomMBeanRegistration;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/** A class to validate if nothing changes, it would be possible to register given custom MBean 
 * in a JMX MBeanServer. It is used to decide, with a reasonable confidence, whether
 * the MBean would be <i> registration-capable </i> when time comes to actually register it.
 * This is to ensure (statically) that at the runtime, there are no unforeseen problems with 
 * the registration. If this class indicates that an MBean could be registered, it should
 * imply that the MBean that user is trying to create is <i> good </i>. This approach is
 * selected to avoid any duplication of work in <code> validating </code> an MBean. A JMX
 * MBeanServer is the best judge to decide if an MBean implementation class and its management
 * interface is something that can be an MBean. In other words, it is simulating the actual
 * MBean registration by doing a dummy registration with all the bells and whistles. It is important
 * the the callers of this class carefully destroy the instance of this class. For a given MBean it
 * always makes sense to create an instance of this class, try registration, unregistration and then
 * cleanup serially.
 * @since SJSAS9.0
*/
public final class MBeanValidator {
    
    private final MBeanServer mbs;
    /** Creates a new instance of MBeanValidator */
    public MBeanValidator() {
        mbs = MBeanServerFactory.newMBeanServer();
    }
    public ObjectName registerTestMBean(final Map<String, String> params, final Map<String, String> attributes) throws RuntimeException {
        try {
            CustomMBeanRegistration cmr = new CustomMBeanRegistrationImpl(mbs);
            final Mbean m = toMbean(params, attributes, true);
            final ObjectName ron = cmr.registerMBean(m);
            return ( ron );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    public void unregisterTestMBean(final ObjectName ron) throws RuntimeException {
        try {
            if (mbs.isRegistered(ron))
                mbs.unregisterMBean(ron);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    /** A convenience method to create a config bean @{link Mbean} corresponding a the given
     * map of input parameters. It is not guaranteed that this Mbean passes validation.
     * @param params a Map consisting of name, class-name, object-name etc. of the Mbean
     * @param attributes a Map consisting of name and value of the MBean attribute which will be added as <property> sub-elements in <mbean>.
     * @param enabledNotUsed a vestigial parameter.  <b>This parameter has no effect</b> It is still here because this is a public method that may have callers anywhere at all.
     */
    public static final Mbean toMbean(final Map<String, String> params, final Map<String, String> attributes, final boolean enabledNotUsed) {
        final Mbean cmb             = new Mbean();
        final String name           = params.get(CustomMBeanConstants.NAME_KEY);
        final String cName          = params.get(CustomMBeanConstants.IMPL_CLASS_NAME_KEY);
        final String on             = params.get(CustomMBeanConstants.OBJECT_NAME_KEY);
        final String ot             = params.get(CustomMBeanConstants.OBJECT_TYPE_KEY);
        final String enabledString  = params.get(CustomMBeanConstants.ENABLED_KEY);
        
        boolean enabled = true;
        
        if(enabledString != null)
            enabled = Boolean.valueOf(enabledString);

        cmb.setName(name);
        cmb.setImplClassName(cName);
        cmb.setObjectName(on);
        cmb.setObjectType(ot);
        cmb.setEnabled(enabled);
        cmb.setElementProperty(map2Properties(attributes));
        return ( cmb );
    }
    
    public static ObjectName formDefaultObjectName(final Map<String, String> params) throws MalformedObjectNameException {
        final String domain = CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN;
        //form the mandatory class-name property, can be safely assumed to be available here.
        final Map<String, String> onProperties = new HashMap<String, String> ();
        if (params.containsKey(CustomMBeanConstants.IMPL_CLASS_NAME_KEY))
            onProperties.put(CustomMBeanConstants.IMPL_CLASS_NAME_KEY, params.get(CustomMBeanConstants.IMPL_CLASS_NAME_KEY));
        if (params.containsKey(CustomMBeanConstants.NAME_KEY))
            onProperties.put(CustomMBeanConstants.NAME_KEY, params.get(CustomMBeanConstants.NAME_KEY));
        final ObjectName on = new ObjectName(domain, new Hashtable<String, String>(onProperties));
        return ( on );
    }
    
    private static ElementProperty[] map2Properties(final Map<String, String> attributes) {
        final ElementProperty[] props = new ElementProperty[attributes.size()];
        int i = 0;
        for (String n : attributes.keySet()) {
            final ElementProperty prop = new ElementProperty();
            prop.setName(n);
            prop.setValue(attributes.get(n));
            props[i] = prop;
            i++;
        }
        return ( props ) ;
    }
    
}
