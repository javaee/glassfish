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

import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


public class BasicCustomMBeanConfigQueries implements CustomMBeanConfigQueries {
    
    protected final ConfigContext acc;
    /** Creates a new instance of BasicCustomMBeanConfigQueries */
    public BasicCustomMBeanConfigQueries() {
        this.acc = MBeanRegistryFactory.getAdminContext().getAdminConfigContext();
    }

    public boolean existsMBean(String target, String name) throws CustomMBeanException {
        final List<String> names = this.listMBeanNames(target);
        boolean exists = false;
        for (String aName : names) {
            if (aName.equals(name)) {
                exists = true;
                break;
            }
        }
        return ( exists );
    }

    public boolean isMBeanEnabled(String target, String name) throws CustomMBeanException {
        boolean enabled = false;
        if (!existsMBean(target, name)) {
            final String msg = CMBStrings.get("MBeanNotFound", name, target);
            throw new CustomMBeanException(msg);
        }
        final List<ObjectName> ons      = this.listMBeanConfigObjectNames(target);
        //get the attribute called "enabled" on these ObjectNames.
        try {
            final MBeanServerConnection mbsc        = MBeanServerFactory.getMBeanServer(); // best to be prepared toward remoteness
            final String noe                        = ServerTags.ENABLED;
            for (ObjectName on : ons) {
                final String s = (String) mbsc.getAttribute(on, noe);
                if (Boolean.valueOf(s).booleanValue()) {
                    enabled = true;
                    break;
                }
            }
            return ( enabled );
        } catch (final Exception e) {
            throw new CustomMBeanException (e);
        }
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target) throws CustomMBeanException {
        Target t = null;
        try {
            t = TargetBuilder.INSTANCE.createTarget(target, this.acc);
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
        return ( this.listMBeanConfigObjectNamesForServer(t.getName()) );
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target, int type, boolean state) throws CustomMBeanException {
        throw new UnsupportedOperationException(CMBStrings.get("NYI", "com.sun.enterprise.admin.mbeans.custom.BasicCustomMBeanConfigQueries.listMBeanConfigObjectNames"));
    }

    public List<String> listMBeanNames(String target) throws CustomMBeanException {
        final List<ObjectName> ons      = this.listMBeanConfigObjectNames(target);
        final List<String> names        = new ArrayList<String> ();
        //get the attribute called "name" on these ObjectNames.
        try {
            final MBeanServerConnection mbsc        = MBeanServerFactory.getMBeanServer(); // best to be prepared toward remoteness
            final String noa                        = ServerTags.NAME;
            for (ObjectName on : ons) {
                final String voa                        = (String) mbsc.getAttribute(on, noa);
                names.add(voa);
            }
            return ( names );
        } catch(final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
    
    /** Method to get the ObjectNames of the Config MBeans that correspond to custom-mbean-definitions
     * that are referenced from a server instance. This method does bulk of the work in this class.
     */
    protected List<ObjectName> listMBeanConfigObjectNamesForServer(final String s) throws RuntimeException {
        //when here, assume that the server exists, with the name "s"
        try {
            final List<Mbean> refdMbeans    = ServerBeansFactory.getReferencedMBeans(acc, s);
            return ( this.mbeans2ConfigMBeanObjectNames(refdMbeans) );
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    protected List<ObjectName> mbeans2ConfigMBeanObjectNames(List <Mbean> mbeans) throws RuntimeException {
        try {
            final List<ObjectName> ons = new ArrayList<ObjectName> ();
            for (Mbean m : mbeans) {
                final String domain = AdminService.PRIVATE_MBEAN_DOMAIN_NAME;
                final ObjectName on = MBeanRegistryFactory.getAdminMBeanRegistry().getObjectNameForConfigBean(m, domain);
                ons.add(on);
            }
            return ( ons ) ;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
