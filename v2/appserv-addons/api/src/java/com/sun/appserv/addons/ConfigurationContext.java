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

package com.sun.appserv.addons;

import java.io.File;
import com.sun.appserv.management.DomainRoot;
import java.util.Map;

/**
 * A context class describing the context in which Configurator is being
 * executed. 
 *
 * @see com.sun.appserv.addons.InstallationContext
 */
public class ConfigurationContext {

    /**
     * <p>DAS         - Configurator is being executed to configure DAS.
     * <p>INSTANCE    - Configurator is being executed in DAS to configure 
     *                  an instance 
     */
    public enum ConfigurationType {DAS, INSTANCE}

    private InstallationContext ic = null;
    private File domainDir = null;
    private ConfigurationType type = null;
    private DomainRoot domainRoot = null;
    private String adminUser = null;
    private String adminPassword = null;
    private Map map = null;

    /**
     * Set an instance of <code>InstallationContext</code> 
     *
     * @param ic <code>InstallationContext</code>
     */
    public void setInstallationContext(InstallationContext ic) {
        this.ic = ic;
    }
     
    /**
     * Provide an instance of <code>InstallationContext</code>
     *
     * @return <code>InstallationContext</code>
     */
    public InstallationContext getInstallationContext() {
        return this.ic;
    }

    /**
     * Set the location of domain. 
     *
     * @param domain <code>java.io.File</code> object
     */
    public void setDomainDirectory(File domain) {
        this.domainDir = domain;   
    }

    /**
     * Return the location of domain. Will be null, when the 
     * <code>ConfigurationType</code> is REMOTEINSTANCE 
     *
     * @return <code>java.io.File</code> object
     */
    public File getDomainDirectory() {
        return this.domainDir;
    }

    /**
     * Set the configuration type.
     *
     * @param type DAS or INSTANCE.
     */
    public void setConfigurationType(ConfigurationType type) {
        this.type = type;
    }

    /**
     * Return the <code>ConfigurationType</code>
     *
     * @return <code>ConfigurationType</code>
     */
    public ConfigurationType getConfigurationType() {
        return this.type;
    }

    /**
     * Set the AMX <code>DomainRoot</code> object.
     * This will be set only when <code>ConfigurationType</code>
     * is INSTANCE.
     *
     * @param root DomainRoot.
     */
    public void setAMXDomainRoot(DomainRoot root) {
        this.domainRoot = root;
    }

    /**
     * Return the <code>DomainRoot</code> object. When 
     * <code>ConfigurationType</code> is DAS this object 
     * will return null.
     * When <code>ConfigurationType</code> is INSTANCE
     * this method will return a valid <code>DomainRoot</code>
     * object. Any domain.xml updates at the time of 
     * <code>Configurator.configureInstances</code> should be 
     * done using AMX.
     *
     * @retun a <code>DomainRoot</code> object or null.
     */
    public DomainRoot getAMXDomainRoot() {
        return domainRoot;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Useful for setting user defined properties
     * and passing them to the addon infrastructure
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Returns user defined properties as map
     * @return <code>Map</code>
     */
    public Map getMap() {
        return map;
    }

}
