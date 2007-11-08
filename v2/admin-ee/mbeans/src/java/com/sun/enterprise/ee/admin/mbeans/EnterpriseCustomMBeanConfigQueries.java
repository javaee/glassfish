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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.mbeans.custom.BasicCustomMBeanConfigQueries;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConfigQueries;
import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import java.util.List;
import javax.management.ObjectName;

public class EnterpriseCustomMBeanConfigQueries extends BasicCustomMBeanConfigQueries implements CustomMBeanConfigQueries {

    private final TargetType[] vTargets;
    /** Creates a new instance of EnterpriseCustomMBeanConfigQueries */
    public EnterpriseCustomMBeanConfigQueries() {
        super();
        vTargets    = new TargetType[4]; //domain, DAS, standalone server, cluster
        vTargets[0] = TargetType.DOMAIN;
        vTargets[1] = TargetType.DAS;
        vTargets[2]  = TargetType.SERVER;
        vTargets[3]  = TargetType.CLUSTER;
    }

    public boolean existsMBean(String target, String name) throws CustomMBeanException {
        return ( false) ; //TODO
    }

    public boolean isMBeanEnabled(String target, String name) throws CustomMBeanException {
        return ( false); //TODO
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target) throws CustomMBeanException {
        /* note that all the servers in a cluster should have exactly same references to mbeans, hence getting a single server
         in a cluster is good enough.
         * <servers>
         *  <server name="s1">
         *   <application-ref ref="fooMBean" .../>
         *  </server>
         *  <server name="s2">
         *   <application-ref ref="fooMBean" .../>
         *  </server>
         * </servers>
         * <clusters>
         *  <cluster name="s1s2" ...>
         *   <server-ref>s1</server-ref>
         *   <server-ref>s2</server-ref>
         *  </cluster>
         * </clusters>
         */
        List<ObjectName> ons = null;
        Target t = null;
        try {
            t = TargetBuilder.INSTANCE.createTarget(target, vTargets, target, this.acc);
        } catch(ConfigException e) {
            throw new CustomMBeanException(e);
        }
        if (TargetType.DAS.equals(t.getType()) || TargetType.SERVER.equals(t.getType())) {
            if (TargetType.SERVER.equals(t.getType())) {
                EnterpriseCustomMBeanOperations.checkClusteredInstance(this.acc, t.getName());
            }
            ons = super.listMBeanConfigObjectNamesForServer(t.getName());
        }
        else if (TargetType.DOMAIN.equals(t.getType())) {
            // just list the mbean definitions
            ons = this.getMBeanDefinitionObjectNamesInDomain();
        }
        else {//has to be a cluster
            String nameOfAServerInCluster = null;
            try {
                Server[] servers= ServerHelper.getServersInCluster(this.acc, target);
                nameOfAServerInCluster = servers[0].getName();
                ons = super.listMBeanConfigObjectNamesForServer(nameOfAServerInCluster);
            } catch(ConfigException e) {
                throw new CustomMBeanException(e);
            }
        }
        return ( ons ) ;
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target, int type, boolean state) throws CustomMBeanException {
        return ( null ); //TODO
    }

    public List<String> listMBeanNames(String target) throws CustomMBeanException {
        return ( super.listMBeanNames(target) );
    }
    
    ///// Private Methods
    private List<ObjectName> getMBeanDefinitionObjectNamesInDomain() throws CustomMBeanException {
        try {
            final List<Mbean> mbeans = ServerBeansFactory.getAllMBeanDefinitions(this.acc);
            return ( super.mbeans2ConfigMBeanObjectNames(mbeans) );
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
    }
}