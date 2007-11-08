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

/*
 * $Id: IDomainMBean.java,v 1.4 2007/05/05 05:24:08 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeanapi;

import javax.management.MBeanException;
import javax.management.AttributeList;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.config.ConfigException;

public interface IDomainMBean 
{
    public AttributeList getDefaultCustomProperties(String mbeanTypeName, AttributeList attributeList)
        throws MBeanException;
    
    public AttributeList getDefaultAttributeValues(String mbeanTypeName, String attrNames[])
        throws MBeanException;
     
    public String getConfigDir()
        throws MBeanException; 
    
    /**
     * Returns the name of this domain.
     *
     * @return   domain name
     */
    public String getName() throws MBeanConfigException, MBeanException; 
    
    /** Adds a cluster management support for this domain. Implementation should
        make sure that the support does not already exist. Necessary changes are
        made to the configuration of the domain. These changes include but are not
        limited to: <ul>
        <li> Provision of Pluggable Feature Factory that supports clusters </li>
        <li> Provision of appropriate MBeanServer Builder </li>
        <li> Provision of a template configuration </li>
        </ul>
        The behavior of the existing domain should remain unchanged as far as
        the deployed application, resources, any other user customizations and
        configuration are concerned. It should only add a cluster support. Some
        change in the DAS's (domain's runtime incarnation) is permissible.
        @param profile a String representing the profile from which the default
               (template) config is taken. This config must not be null. It should
               exist in the <install-dir>/lib/install/templates location as a folder
               and must have a domain.xml template. By default, clients should 
               use "cluster".
        @throws ConfigException in case the profile folder did not exist, or did not have
               a valid domain.xml with proper &lt;config&gt; element named "default-config"
    */
    public void addClusteringSupportUsingProfile(final String profile) throws ConfigException;
}
