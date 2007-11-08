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

package com.sun.enterprise.admin.server.core.mbean.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.io.InputStream;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//Config imports
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;

//Other imports
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ServerManager;

//Admin imports
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.exception.PortInUseException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
/* New for 8.0 */
import com.sun.enterprise.admin.server.core.mbean.config.Domain2ServerTransformer;
import com.sun.enterprise.server.Constants;
/* New for 8.0 */

/**
    The MBean that represents the Admin Server Instance for iAS SE. In
    other words it represents the <strong> management interface </strong> of
    the Admin Server Instance.
    ObjectName of this MBean is ias:type=server-instance, name=admin-server
*/

public class ManagedAdminServerInstance extends ConfigMBeanBase
{
    private static final String[][] MAPLIST  = 
    {
   // REMOVED FROM DTD from server element
   //     {ConfigAttributeName.Server.kLocale , 
   //      ConfigMBeanBase.ATTRIBUTE + ServerTags.LOCALE}
    };

    private static final String[]   ATTRIBUTES  = 
    {
   //     ConfigAttributeName.Server.kLocale  + ", String,        RW"
    };

    private static final String[]   OPERATIONS  = 
    {
        "applyConfigChanges(), ACTION", 
        "useManualConfigChanges(), ACTION",
        "getHostAndPort(),  INFO",
        "isApplyNeeded(), INFO",
//	"getCertNicknames(), INFO",
        "isRestartNeeded(), INFO"
    };

    private static final Logger _logger = Logger.getLogger(
            AdminConstants.kLoggerName);

    private ManagedServerInstance mDelegate;

    private boolean mRestartNeeded = false;

    public ManagedAdminServerInstance() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
        this.initialize(ObjectNames.kServerInstance, new String[]{ServerManager.ADMINSERVER_ID});
    }

    /**
     * Copies the file from backup directory to the real config directory
     * so that the configuration is stored on disk. There is no guarantee of
     * any transactional support. In case of admin server instance only
     * server.xml file is edited in the backup directory, so this method only
     * copies server.xml. (The other conf files for admin instance are edited
     * directly in live configuration area). As there is no dynamic
     * reconfiguration for admin server, the admin instance always requires
     * restart for new settings to take affect.
     * @return True means requires restart.
     */
    public boolean applyConfigChanges() throws ServerInstanceException
    {
        try {
            InstanceEnvironment ie = new InstanceEnvironment(
                    ServerManager.ADMINSERVER_ID);
            // Force write of server.xml changes because the check to ensure
            // that manual edits are not lost (the method canApplyConfigChanges)
            // is already invoked from MBeanServer just before invoking this
            // (applyConfigChanges) method.
            //ie.applyServerXmlChanges(true);
            initDelegate();
            ConfigContext ctx = mDelegate.getConfigContext(
                    ServerManager.ADMINSERVER_ID);
            ctx.resetConfigChangeList();
            mRestartNeeded = true;
            /* New for 8.0 - temporary - gen server.xml b4 notif */
            /* This call most likely goes away */
            if (ManagedServerInstance.PORT_DOMAIN_TO_SERVER) {
                final String domainXMLPath = ie.getConfigFilePath();
                final String serverXMLPath = System.getProperty(Constants.IAS_ROOT) 
                    + "/" 
                    + ie.getName()
                    + "/config/" 
                    + ie.kServerXMLFileName;
                new Domain2ServerTransformer(domainXMLPath, serverXMLPath).transform();
            }
            /* New for 8.0 - temporary - gen server.xml b4 notif */
        } catch (ConfigException ce) {
            _logger.log(Level.INFO, "mbean.config.admin.apply_failed",
                    ce.getLocalizedMessage());
            _logger.log(Level.FINEST, ce.getMessage(), ce);
            throw new ServerInstanceException(ce.getLocalizedMessage());
        }
        return mRestartNeeded;
    }

    /**
     * Can config changes be applied. This method returns <code>true</code>
     * <UL>
     * <LI>if live server.xml has not been manually edited or</LI>
     * <LI>if live server.xml has been manully edited but no other changes
     * have been made through the GUI or CLI to backup server.xml.</LI></UL>.
     * This method is explicitly coded to not throw any exception and will
     * return false in case there is any exception in processing.
     * @return true if config changes can be applied, false otherwise
     */
    public boolean canApplyConfigChanges()
    {
        boolean canApply = false;
        try {
            initDelegate();
            //InstanceEnvironment ie = new InstanceEnvironment(
                   // ServerManager.ADMINSERVER_ID);
            /*
            if (ie.hasHotXmlChanged()) {
                if (ie.canReloadManualXmlChanges()) {
                    ie.useManualServerXmlChanges();
                    mDelegate.reloadAfterChange(ie);
                    canApply = true;
                }
            } else {
             */
                canApply = true;
                /*
            }
                 **/
        } catch (Exception ex) {
            _logger.log(Level.INFO, "mbean.config.admin.canapply_failed",
                    ex.getLocalizedMessage());
            _logger.log(Level.FINEST, ex.getLocalizedMessage(), ex);
        }
        return canApply;
    }

    /**
     * Use manual config changes. This method is invoked if the user decides
     * to keep the hand edits to server.xml.
     * @return true if restart is required.
     */
    public boolean useManualConfigChanges() throws ServerInstanceException
    {
        try {
            initDelegate();
            InstanceEnvironment ie =
                    new InstanceEnvironment(ServerManager.ADMINSERVER_ID);
            // copy server.xml from hot to back
            //ie.useManualServerXmlChanges();
            // Reinitialize mbeans
            mDelegate.reloadAfterChange(ie);
            // Set restart needed to true because we do not know whether admin
            // server is already running with the changes in server.xml -- the
            // policy is say restart is needed unless you know otherwise.
            mRestartNeeded = true;
        } catch (ConfigException ce) {
            _logger.log(Level.INFO, "mbean.config.admin.usemanual_failed",
                    ce.getMessage());
            _logger.log(Level.FINEST, ce.getMessage(), ce);
            throw new ServerInstanceException(ce.getLocalizedMessage());
        }
        return mRestartNeeded;
    }

    /**
     * Is apply changes required. This method added to sync up with
     * ManagedServerInstance. SOM uses same object for admin server mbean
     * non admin server mbean. Therefore the method signatures should be same.
     * @param check not used
     */
    public boolean isApplyNeeded(boolean check) throws ServerInstanceException {
        return isApplyNeeded();
    }

    /**
     * Is apply changes required. This method returns true if any changes
     * have been made to server.xml that have not been applied yet.
     * @return true if there are changes to backup server.xml and that have
     *    not been applied to live server.xml
     */
    public boolean isApplyNeeded() throws ServerInstanceException
    {
        boolean applyNeeded = false;
        try
        {
            initDelegate();
            ConfigContext ctx = mDelegate.getConfigContext(
                    ServerManager.ADMINSERVER_ID);
            applyNeeded = ctx.isChanged();
        } catch (ConfigException ce) {
            _logger.log(Level.INFO, "mbean.config.admin.applyneeded_failed",
                    ce.getMessage());
            _logger.log(Level.FINEST, ce.getMessage(), ce);
            throw new ServerInstanceException(ce.getLocalizedMessage());
        }
        return applyNeeded;
    }

    /**
     * Is restart needed. For admin server instance, there is no dynamic
     * reconfiguration, so restart is needed whenever any change is applied.
     * @return true if restart is required.
     */
    public boolean isRestartNeeded() throws ServerInstanceException
    {
        return mRestartNeeded;
    }

    /**
     */
    public HostAndPort getHostAndPort() throws ServerInstanceException
    {
        return getHttpListenerHostPort();
    }

    private synchronized void initDelegate() throws ServerInstanceException
    {
        if (mDelegate != null)
        {
            return;
        }
        try
        {
            HostAndPort hp = getHttpListenerHostPort();
            mDelegate = new ManagedServerInstance(ServerManager.ADMINSERVER_ID, 
                                                  hp, false);
        }
        catch (ServerInstanceException sie)
        {
            throw sie;
        }
        catch (PortInUseException piue)
        {
        }
        catch (Exception e)
        {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
    }

    /**
     * Returns the host & port of the http listener 'http-listener-1'.
     * (host by default is localhost).
     */
    private HostAndPort getHttpListenerHostPort() 
        throws ServerInstanceException
    {
        HostAndPort hAndp = null;
        try
        {
//ms1            Server          server  = (Server) super.getBaseConfigBean();
            Config          config  = (Config) super.getConfigBeanByXPath(ServerXPathHelper.XPATH_CONFIG);
            HttpService     https   = config.getHttpService();
            
            HttpListener[] hlArray = https.getHttpListener();
            //check not needed since there should always be atleast 1 httplistener
            //if you don't find one, use first one.
            HttpListener ls = hlArray[0];
            //default is the first one that is enabled.
            for(int i = 0;i<hlArray.length;i++) {
                if(hlArray[i].isEnabled()) {
                    ls = hlArray[i];
                    break;
                }
            }
            
            String          port    = ls.getPort();
            int             intPort = Integer.parseInt (port);
            hAndp = new HostAndPort("localhost", intPort);
        }
        catch (Exception e)
        {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return hAndp;
    }


}
