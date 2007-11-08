
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



import com.sun.enterprise.admin.servermgmt.InstanceException;

import javax.management.MBeanException;

import java.util.Properties;

import javax.management.ObjectName;


/**
 * Interface ConfigConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface ConfigConfigMBean {

    /**
     * Method delete deletes the configuration. A configuration must
     * be referenced by no clusters or unclustered server instances
     * before it can be deleted; furthermore, the default configuration
     * (named "default-config") cannot be deleted.
     *
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void delete() throws InstanceException, MBeanException;

    /**
     * Method copy makes copy of the configuration.
     *
     * @param newConfigName the name of the new configuration
     * @param props system properties which are added to the newly created 
     * configuration or which overwrite those defined in the cloned 
     * configuration.
     *
     * @return the JMX object name of the newly created configuration.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    ObjectName copy(String newConfigName, Properties props)
        throws InstanceException, MBeanException;

    /**
     * Method listSystemProperties lists the system properties of the configuration.
     * Configuration level system properties take precedence over system properties
     * with the same name defined at the domain level, but can be 
     * overwritten by system properties at the cluster or server instance level.
     *
     * @param inherit when true, system properties inherited from the domain
     * and from the cluster's configuration are displayed; otherwise only the
     * system properties of the configuration are displayed.
     *
     * @return the list of system properties for the configuratiojn.
     *
     *
     * @param inherit
     *
     * @return
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    Properties listSystemProperties(boolean inherit)
        throws InstanceException, MBeanException;

    /**
     * Method createSystemProperties create system or overwrites properties 
     * of the configuration.     
     * Configuration level system properties take precedence over system properties
     * with the same name defined at the domain level, but can be 
     * overwritten by system properties at the cluster or server instance level.
     *
     * @param props The system properties to define for the configuration.     
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void createSystemProperties(Properties props)
        throws InstanceException, MBeanException;

    /**
     * Method deleteSystemProperty deletes the named system property of the 
     * configuration. 
     * Configuration level system properties take precedence over system properties
     * with the same name defined at the domain level, but can be 
     * overwritten by system properties at the cluster or server instance level.
     *    
     * @param propertyName The name of the system property to delete. This property
     * must exist, or an exception is thrown.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void deleteSystemProperty(String propertyName)
        throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
