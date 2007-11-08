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

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.mbeans.custom.BasicCustomMBeanOperations;
import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;
import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanOperationsMBean;
import com.sun.enterprise.admin.mbeans.custom.MBeanValidator;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import java.util.logging.Logger;
import javax.management.ObjectName;
import com.sun.enterprise.util.i18n.StringManager;


/** Enterprise implementation to create and delete the MBeans and references to them.
 * The single server based implementation is derived from the base class, for most of the
 * methods of this class.
*/
public class EnterpriseCustomMBeanOperations extends BasicCustomMBeanOperations implements CustomMBeanOperationsMBean {
    private final TargetType[] vTargets;
    private final static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    /** Creates a new instance of EnterpriseCustomMBeanOperations */
    public EnterpriseCustomMBeanOperations() {
        super();
        vTargets    = new TargetType[4]; //domain, DAS, standalone server, cluster
        vTargets[0] = TargetType.DOMAIN;
        vTargets[1] = TargetType.DAS;
        vTargets[2]  = TargetType.SERVER;
        vTargets[3]  = TargetType.CLUSTER;
    }

    /** Creates the MBean. The valid targets while creating an MBean are: 
     * <ul> 
     * <li> The domain itself. This means user is trying to create the MBean definition with an 
     * intent to create an application reference later on. A target whose value is null represents domain by default. 
     * This is not to say that <i> null </i> is a separate target, but to say that domain target can be represent by a null reference. </li>
     * <li> The DAS. This results in creating the MBean Definition and an implicit reference to it from the server representing DAS. </li>
     * <li> A cluster. This results in creating MBean Definition and creating references from <i> all the servers comprising the cluster </i>. </li>
     * <li> A standalone server. This results in creating MBean Definition and creating references from <i> the server</li>. 
     * </ul>
     * @param target String representing the target, could be null.
     * @param params a Map<String, String> that could contain NAME, OBJECT_NAME of the MBean.
     * @param attributes a Ma<String, String> that could contain the settable attributes of the MBean.
     */
    public String createMBean(final String target, final Map<String, String> params, final Map<String, String> attributes) throws CustomMBeanException {
        boolean mbeanDefinitionCreated = false;
        try {
            final Target t = TargetBuilder.INSTANCE.createTarget(target, vTargets, target, this.acc);
            final TargetType pType = t.getType();
            final String newTarget = t.getName();
            //note: mutates the passed params for "NAME_KEY"
            mbeanDefinitionCreated = createMBeanDefinitionIfAbsent(newTarget, params, attributes);
            final String name = params.get(CustomMBeanConstants.NAME_KEY);
            if (TargetType.DOMAIN.equals((pType))) {
                //this should create the MBean definition only
                //TODO NO CHECK AGAINST AN EXISTING MBEAN DEFINITION THAT IS NOT REFERENCED ...
            }
            else if (TargetType.DAS.equals(pType) || TargetType.SERVER.equals(pType)) {
                if (TargetType.SERVER.equals(pType)) {
                    EnterpriseCustomMBeanOperations.checkClusteredInstance(this.acc, newTarget);
                }
                this.createMBeanReferenceForServer(newTarget, name);
            }
            else if (TargetType.CLUSTER.equals(pType)) {
                // this may throw a ConfigException if the cluster config is corrupt.  If that happens it will
                // be processed by the catch(Exception e) below.
                // Conversely, if the ref already exists in the cluster than we don't want to go into
                // that catch clause (the mbean is SUPPOSED to stay registered) -- and we don't!
                if(ServerBeansFactory.isReferencedMBeanInCluster(this.acc, newTarget, name))
                    throw new CustomMBeanException(CMBStrings.get("cmb.ee.local.MbeanAlreadyExistsInCluster", name, newTarget));
                
                this.createMBeanReferencesInCluster(newTarget, name);
            }
            else {
                throw new RuntimeException(CMBStrings.get("cmb.ee.local.targetProcessBad", target));
            }
            return ( name );
        } catch (final CustomMBeanException cmbe) {
            throw cmbe;
        }
        catch (final Exception e) {
            //protect against the case where the definition was created, but reference creation failed
            if (mbeanDefinitionCreated) {
                try {
                    ServerBeansFactory.removeMbeanDefinition(this.acc, params.get(CustomMBeanConstants.NAME_KEY));
                } catch (final Exception ee) {throw new RuntimeException(ee); }
            }
            throw new CustomMBeanException(e);
        }
    }

    public String createMBean(final String target, final Map<String, String> params) throws CustomMBeanException {
        final Map<String, String> ea = Collections.emptyMap();
        return ( this.createMBean(target, params, ea) );
    }

    public void deleteMBeanRef(final String target, final String ref) throws CustomMBeanException {
        throw new UnsupportedOperationException(CMBStrings.get("InternalError", "Not implemented yet."));
    }
    
    public String deleteMBean(final String target, final String name) throws CustomMBeanException {
        if (name == null)
            throw new IllegalArgumentException(CMBStrings.get("InternalError", "null argument"));
        try {
            final Target t = TargetBuilder.INSTANCE.createTarget(target, vTargets, target, this.acc);
            final TargetType pType = t.getType();
            if (TargetType.DAS.equals(pType) || TargetType.SERVER.equals(pType)) {
                if (TargetType.SERVER.equals(pType)) {
                    EnterpriseCustomMBeanOperations.checkClusteredInstance(this.acc, t.getName());
                }
                this.deleteMBeanFromServer(t.getName(), name);
            }
            else if (TargetType.DOMAIN.equals(t.getType())) {
                this.checkReferencingServers(name); // throws exception if any references
                ServerBeansFactory.removeMbeanDefinition(this.acc, name);
            }
            else if (TargetType.CLUSTER.equals(t.getType())) {
                deleteMBeanFromCluster(target, name);
            }
            else {
                throw new RuntimeException(CMBStrings.get("cmb.ee.local.targetProcessBad", target));
            }
            return ( name );
        } catch (final Exception e) {
            throw new CustomMBeanException(e);
        }
    }

    public void createMBeanRef(final String target, final String ref) throws CustomMBeanException {
        throw new UnsupportedOperationException(CMBStrings.get("InternalError", "Not implemented yet."));
    }

    public String createMBean(final String target, final String className) throws CustomMBeanException {
        final String cnk                        = CustomMBeanConstants.IMPL_CLASS_NAME_KEY;
        final String nk                         = CustomMBeanConstants.NAME_KEY;
        final Map<String, String> tmp           = new HashMap<String, String> ();
        tmp.put(nk, className);
        tmp.put(cnk, className); // name and class-name are same in this case
        final Map<String, String> attributes    = Collections.emptyMap();
        return ( this.createMBean(target, tmp, attributes) );
    }
    
    /*package private */
    static void checkClusteredInstance(final ConfigContext cc, final String server) throws RuntimeException {
        try {
            if (ServerHelper.isServerClustered(cc, server)) {
                final String cluster = ClusterHelper.getClusterForInstance(cc, server).getName();
                final String msg = CMBStrings.get("cmb.ee.local.badTarget.ClusteredServer", server, cluster);
                throw new IllegalArgumentException(msg);
            }
            
        } catch (final IllegalArgumentException iae) {
            throw iae;
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
    private boolean createMBeanDefinitionIfAbsent(final String target, final Map<String, String> params, final Map<String, String> attributes) throws Exception {
        boolean definitionCreated = false;
        final Map<String, String> mm = super.checkAndModifyParamsForName(params);
        final ObjectName onPostReg = super.selectObjectName(mm, attributes);
        final String name = mm.get(CustomMBeanConstants.NAME_KEY);
        //mutate the passed params
        params.put(CustomMBeanConstants.NAME_KEY, name);
        params.put(CustomMBeanConstants.OBJECT_NAME_KEY, onPostReg.toString());
        if (ServerBeansFactory.getMBeanDefinition(this.acc, name) == null) {
            checkObjectNameUniquenessForSameTarget(target, onPostReg);
            final Mbean md = MBeanValidator.toMbean(params, attributes, true);
            ServerBeansFactory.addMbeanDefinition(this.acc, md);
            definitionCreated = true;
        }
        else {
            final String msg = CMBStrings.get("cmb.ee.defExists", name);
            logger.info(msg);
        }
        return ( definitionCreated );
    }
    private void checkObjectNameUniquenessForSameTarget(final String target, final ObjectName on) throws Exception {
        final Target t = TargetBuilder.INSTANCE.createTarget(target, vTargets, target, this.acc);
        final TargetType pType = t.getType();
        final String msg = CMBStrings.get("cmb.ee.objNameExists", on.toString(), target);
        logger.info(msg);
        
        if (TargetType.DOMAIN.equals(pType)) {
            final List<Mbean> mds = ServerBeansFactory.getAllMBeanDefinitions(this.acc);
            for (final Mbean md : mds) {
                final String ons = md.getObjectName();
                final ObjectName ton = new ObjectName(ons);
                if (ton.equals(on)) {
                    throw new RuntimeException(msg); //TODO
                }
            }
        }
        /* 6320507 -- added check for TargetType.SERVER
         * for some unknown reason a stand-alone server instance's type 
         * is SERVER, *not* STANDALONE_SERVER
         */
        else if (TargetType.DAS.equals(pType) || TargetType.STANDALONE_SERVER.equals(pType) 
            || TargetType.SERVER.equals(pType)) {
            if (super.onExists(t.getName(), on)) {
                throw new RuntimeException(msg);
            }
        }
        else if (TargetType.CLUSTER.equals(pType)) {
            final Server[] ss = ServerHelper.getServersInCluster(this.acc, target);
            //should be at least 1
            if (ss.length < 1) {
                throw new RuntimeException(CMBStrings.get("cmb.ee.local.emptyCluster", target));
            }
            //now select any server, as they will have exactly same references for mbeans
            final String s = ss[0].getName();
            if (super.onExists(s, on)) {
                throw new RuntimeException(msg);
            }
        }
    }
    private void deleteMBeanFromServer(final String sName, final String name) throws Exception {
        //first delete the reference to this mbean definition from DAS
        if (ServerBeansFactory.isReferencedMBean(this.acc, sName, name)) {
            ServerBeansFactory.removeMbeanReference(this.acc, name, sName);
        }
        else {
            final String msg = CMBStrings.get("cmb.ee.local.notReferenced", name, sName);
            throw new CustomMBeanException(msg);
        }
        //now delete the mbean definition itself, it there are no more references
        deleteMBeanDefinitionIfNoMoreReferences(name);
    }
    private void deleteMBeanDefinitionIfNoMoreReferences(final String name) throws Exception {
        final Server[] ss = ServerBeansFactory.getServersReferencingMBeanDefinition(this.acc, name);
        if (ss.length == 0) {
            ServerBeansFactory.removeMbeanDefinition(this.acc, name);
        }        
    }
    
    private void checkReferencingServers(final String mbeanName) throws Exception {
        final Server[] ss = ServerBeansFactory.getServersReferencingMBeanDefinition(this.acc, mbeanName);
        if (ss.length != 0) {
            final StringBuilder sb = new StringBuilder();
            for (final Server s : ss) {
                sb.append(s.getName()).append("  ");
            }
            final String msg = CMBStrings.get("cmb.ee.local.referencedByServers", mbeanName, sb.toString());
            throw new RuntimeException(msg);
        }
    }
    private void createMBeanReferencesInCluster(String cluster, String name) throws RuntimeException {
        Server[] ss = null;
        try {
            ss = ServerHelper.getServersInCluster(this.acc, cluster);
            // we know that the mbean has to be created in a cluster now
            for (Server s : ss) {
                this.createMBeanReferenceForServer(s.getName(), name);
            }
            //finally add ref to cluster itself
            ServerBeansFactory.addClusterMbeanReference(this.acc, name, cluster);
        } catch (final Exception e) {
            //cleanup the created references because the cluster configuration has got to be uniform
                for (Server s : ss) {
                    try {
                            this.deleteMBeanReferenceFromServer(s.getName(), name);
                    } catch(final Exception ee) {
                        ee.printStackTrace(); // can't do anything, move on with a heavy heart
                    }
                }
                try {
                    ServerBeansFactory.removeClusterMbeanReference(this.acc, name, cluster);
                } catch(final Exception ee) {
                    ee.printStackTrace(); // can't do anything, move on with a heavy heart
                }
            throw new RuntimeException(e);
        }
    }
    
    private void createMBeanReferenceForServer(final String target, final String ref) throws Exception {
        Mbean m = null;
        m = ServerBeansFactory.getMBeanDefinition(this.acc, ref);
        if (m == null) {
            final String msg = CMBStrings.get("cmb.ee.local.mustCreateDefinition", ref);
            throw new CustomMBeanException(msg);
        }
        if (! ServerBeansFactory.isReferencedMBean(this.acc, target, ref) ) {
            ServerBeansFactory.addMbeanReference(this.acc, ref, target);
        }
        else {
            final String msg = CMBStrings.get("cmb.ee.local.refExists", target, ref);
            throw new CustomMBeanException(msg);
        }
    }
    
    private void deleteMBeanFromCluster(final String cluster, final String name) throws Exception {
        ServerBeansFactory.removeClusterMbeanReference(this.acc, name, cluster);
        Server[] ss     = null;
        ss = ServerHelper.getServersInCluster(this.acc, cluster);
        for (Server s : ss) {
            this.deleteMBeanFromServer(s.getName(), name);
        }
    }
    
    private void deleteMBeanReferenceFromServer(final String server, final String name) throws RuntimeException {
        try {
            if (ServerBeansFactory.isReferencedMBean(this.acc, server, name)) {
                ServerBeansFactory.removeMbeanReference(this.acc, name, server);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}