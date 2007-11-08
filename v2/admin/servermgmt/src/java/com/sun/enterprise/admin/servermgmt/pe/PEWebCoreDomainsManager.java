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

package com.sun.enterprise.admin.servermgmt.pe;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;

import com.iplanet.ias.installer.core.installConfig;
import com.iplanet.ias.installer.core.ServerConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;

public class PEWebCoreDomainsManager extends DomainsManager
{
    private static final String SERVER_ID = "server";

    public PEWebCoreDomainsManager()
    {
    }

    public void createDomain(String domainName, Map domainConfig) 
        throws DomainException
    {
        // validate(domainConfig);

        // construct the ServerConfig obj, since validation succeeded
        ServerConfig sc = createServerConfig(domainConfig);
        
        // create the actual domain directory, under the domain root
        File domainDir = new File(sc.domainRoot); 
        domainDir.mkdirs();

        // Assuming that the native libraries have been loaded by the installer
        installConfig instconf = new installConfig();
        
        // invoke the createDomain on installConfig
        instconf.createServerInstance(sc);
    }

    private void validate(Map domainConfig) throws DomainException
    {
        final String domainRoot = 
            (String)domainConfig.get(DomainConfig.K_DOMAIN_ROOT);
        if ((domainRoot == null) || (domainRoot.length() == 0))
        {
            throw new DomainException("Invalid domain root");
        }
        /* Do we need this?
        if (!(new File(domainRoot).exists()))
        {
            throw new DomainException("Domain root doesnot exist");
        }
         **/
    }

    public void deleteDomain(String domainName, Map domainConfig) 
        throws DomainException
    {
        String domainRoot = (String) domainConfig.get(DomainConfig.K_DOMAIN_ROOT);
        try {
            installConfig instconf = new installConfig();
            instconf.deleteDomain(domainName, domainRoot);
        } catch(java.io.IOException ioe) {
            throw new DomainException("Error deleting domain: "+domainName);
        }
    }

    public void startDomain(String domainName, Map domainConfig) 
        throws DomainException
    {
    }

    public void stopDomain(String domainName, Map domainConfig) 
        throws DomainException
    {
    }

    /**
     * Lists all the domains.
     */
    public String[] listDomains()
    {
        String[] domains = new String[0];
        return domains;
    }

    public InstancesManager getInstancesManager(String domainName)
    {
        throw new UnsupportedOperationException("Not Supported for PE");
    }

    public boolean isDomainExists(String domainName, String domainRoot)
    {
        return new File(domainRoot, domainName).exists();
    }

    private ServerConfig createServerConfig(Map domainConfig)
    {
        ServerConfig sc = new ServerConfig();
        sc.defaultLocale = (String) domainConfig.get(DomainConfig.K_DEFAULTLOCALE);
        sc.icuLib = (String) domainConfig.get(DomainConfig.K_ICULIB);
        sc.webServicesLib = (String) domainConfig.get(DomainConfig.K_WEBSVCSLIB);
        sc.perlRoot = (String) domainConfig.get(DomainConfig.K_PERLROOT);
        sc.serverID = PEWebCoreDomainsManager.SERVER_ID;
        sc.serverPort = ((Integer) domainConfig.get(DomainConfig.K_INSTANCE_PORT)).intValue();
        sc.adminPort = ((Integer) domainConfig.get(DomainConfig.K_ADMIN_PORT)).intValue();
        sc.serverRoot = (String) domainConfig.get(DomainConfig.K_INSTALL_ROOT);
        sc.domainRoot = (String) domainConfig.get(DomainConfig.K_DOMAIN_ROOT);
        sc.serverUser = (String) domainConfig.get(DomainConfig.K_SVRUSER);
        sc.serverUserFlag = ((Boolean) domainConfig.get(DomainConfig.K_SVRUSERFLAG)).booleanValue();
        sc.serverName = (String) domainConfig.get(DomainConfig.K_SVRNAME);
        sc.domainName = (String) domainConfig.get(DomainConfig.K_DOMAIN_NAME);
        sc.mailHost = (String) domainConfig.get(DomainConfig.K_MAIL_HOST);
        sc.docRoot = sc.domainRoot + File.separator + "docroot";

        sc.jmsPort = ((Integer) domainConfig.get(DomainConfig.K_JMS_PORT)).intValue();
        sc.jmsUser = (String) domainConfig.get(DomainConfig.K_JMS_USER);
        sc.jmsPasswd = (String) domainConfig.get(DomainConfig.K_JMS_PASSWORD);
        sc.imqBin = (String) domainConfig.get(DomainConfig.K_IMQ_BIN);
        sc.imqLib = (String) domainConfig.get(DomainConfig.K_IMQ_LIB);
        sc.javaHome = (String) domainConfig.get(DomainConfig.K_JAVA_HOME);
        sc.orbListenerPort = ((Integer) domainConfig.get(DomainConfig.K_ORB_LISTENER_PORT)).intValue();
        sc.configRoot = (String) domainConfig.get(DomainConfig.K_CFG_ROOT);
        sc.adminName = (String) domainConfig.get(DomainConfig.K_USER);
        sc.adminPasswd = (String) domainConfig.get(DomainConfig.K_PASSWORD);
        sc.ldapUser = (String) domainConfig.get(DomainConfig.K_LDAP_USER);
        sc.ldapURL = (String) domainConfig.get(DomainConfig.K_LDAP_URL);
        sc.ldapPasswd = (String) domainConfig.get(DomainConfig.K_LDAP_PASSWD);
        sc.ldapDN = (String) domainConfig.get(DomainConfig.K_LDAP_DN);
        
        return sc;
    }
}
