
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
package com.sun.enterprise.ee.admin.mbeanapi;



import com.sun.enterprise.config.ConfigException;

import javax.management.MBeanException;

import java.util.Properties;


/**
 * Interface PropertyConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface PropertyConfigMBean {

    /**
     * Method listPropertiesAsString lists the system properties of the target.
     * System properties can be defined at the following levels domain,
     * configuration, cluster, server. This forms an inheritance chain in 
     * which server has the highest precedence and will overwrite identically 
     * named properties defined for the cluster, configuration, or domain.
     *
     * @param targetName The target name can be one of the following: "domain" --
     * lists system properties of the domain, configuration-name -- lists system
     * properties of the configuration (and optionally those inherited from the 
     * domain), cluster-name -- lists system properties of the cluster (and 
     * optionally those inherited from the domain and the cluster's configuration), 
     * server-name -- lists system properties of the clustered or unclustered server
     * instance (and optionally those inherited from the domain, server's configuration,
     * and clustered server's cluster if the server is clustered).
     * @param inherit when true, properties inherited are returned. When false 
     * only properties defined at the specified target are displayed.
     *
     * @return the list of system properties for the target (and optionally its 
     * inherited properties).
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    String[] listSystemPropertiesAsString(String targetName, boolean inherit)
        throws ConfigException, MBeanException;

    /**
     * Method listProperties returns the system properties of the target.
     * System properties can be defined at the following levels domain,
     * configuration, cluster, server. This forms an inheritance chain in 
     * which server has the highest precedence and will overwrite identically 
     * named properties defined for the cluster, configuration, or domain.
     *
     * @param targetName The target name can be one of the following: "domain" --
     * lists system properties of the domain, configuration-name -- returns system
     * properties of the configuration (and optionally those inherited from the 
     * domain), cluster-name -- returns system properties of the cluster (and 
     * optionally those inherited from the domain and the cluster's configuration), 
     * server-name -- returns system properties of the clustered or unclustered server
     * instance (and optionally those inherited from the domain, server's configuration,
     * and clustered server's cluster if the server is clustered).
     * @param inherit when true, properties inherited are returned. When false 
     * only properties defined at the specified target are displayed.
     *
     * @return the of system properties for the target (and optionally its 
     * inherited properties).
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    Properties listSystemProperties(String targetName, boolean inherit)
        throws ConfigException, MBeanException;

    /**
     * Method createProperties creates or overwrites system properties of 
     * the specified target. System properties can be defined at the following levels domain,
     * configuration, cluster, server. This forms an inheritance chain in 
     * which server has the highest precedence and will overwrite identically 
     * named properties defined for the cluster, configuration, or domain.     
     *
     * @param props The system properties to define for the cluster.    
     * @param targetName The target name can be one of the following: "domain" --
     * creates system properties of the domain, configuration-name -- creates system
     * properties of the configuration, cluster-name -- creates system properties 
     * of the cluster, server-name -- creates system properties of the clustered or 
     * unclustered server instance.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    void createSystemProperties(Properties props, String targetName)
        throws ConfigException, MBeanException;

    /**
     * Method deleteProperty deletes an existing system property of 
     * the specified target. System properties can be defined at the following levels domain,
     * configuration, cluster, server. This forms an inheritance chain in 
     * which server has the highest precedence and will overwrite identically 
     * named properties defined for the cluster, configuration, or domain.     
     *
     * @param propertyName The name of the system property to delete.
     * @param targetName The target name can be one of the following: "domain" --
     * deletes a system property of the domain, configuration-name -- deletes a system
     * property of the configuration, cluster-name -- deletes a system property 
     * of the cluster, server-name -- deletes a system properties of the clustered or 
     * unclustered server instance.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    void deleteSystemProperty(String propertyName, String targetName)
        throws ConfigException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
