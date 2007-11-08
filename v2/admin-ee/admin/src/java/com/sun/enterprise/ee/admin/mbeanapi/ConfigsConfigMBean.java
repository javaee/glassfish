
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
import javax.management.ObjectName;

import java.util.Properties;


/**
 * Interface ConfigsConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ConfigsConfigMBean {

    /**
     * Method copyConfiguration makes copy of the named configuration.
     *
     * @param sourceConfigName the name of the configuration to be 
     * copied.
      *@param newConfigName the name of the new configuration
     * @param props system properties which are added to the newly created 
     * configuration or which overwrite those defined in the cloned 
     * configuration
     *
     * @return the JMX object name of the newly created configuration.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    ObjectName copyConfiguration(
        String sourceConfigName, String newConfigName, Properties props)
            throws ConfigException, MBeanException;

    /**
     * Method deleteConfiguration deletes the named configuration. A configuration must
     * be referenced by no clusters or unclustered server instances
     * before it can be deleted; furthermore, the default configuration
     * (named "default-config") cannot be deleted.
     *
     *
     * @param configName the name of the configuration to delete.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    void deleteConfiguration(String configName)
        throws ConfigException, MBeanException;

    /**
     * Method listConfigurations lists configurations of the specified 
     * target.
     *
     *
     * @param targetName The target can be one of the following: "domain" -- lists
     * all configurations in the domain, cluster-name -- lists the configuration
     * for the specified cluster, server-name -- lists the configuration of the 
     * clustered or unclustered server instance.
     *
     * @return The JMX object names of all the configurations for the given 
     * target.
     *
     * @throws ConfigException
     * @throws MBeanException
     *
     */
    ObjectName[] listConfigurations(String targetName)
        throws ConfigException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
