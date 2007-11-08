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
package com.sun.enterprise.ee.synchronization.impl;

import java.io.File;
import java.io.IOException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import com.sun.enterprise.admin.common.JMXFileTransfer;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.servermgmt.InstanceException;

import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.ee.synchronization.SynchronizationException;
import com.sun.enterprise.ee.synchronization.api.SynchronizationClient;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.util.JMXConnectorConfig;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.servermgmt.InstanceConfig;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;

import com.sun.enterprise.security.store.IdentityManager;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Utility to sync a file, between server instance(s) and DAS. This utility 
 * allows synchornization of files. This API design to similar to FTP service
 * client. It provides upload or download functionality.
 *
 * Server name of any instance can be obtained from domain.xml 
 * (xpath is domain/servers/server@name).
 *
 * Notes: To connect to DAS, please use server name of DAS.
 *
 * Please refer to more inline documentation for default values and more 
 * description of API methods.
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class SynchronizationClientImpl implements SynchronizationClient
{

    /**
     * Connects to the server instance or DAS, by the given name
     *
     * @param instanceName  remote server name
     */
    public SynchronizationClientImpl(java.lang.String instanceName) 
            throws SynchronizationException
    {

        AdminService aService = AdminService.getAdminService();
        if ( aService != null && aService.isDas() ) {
            try {
                jmxFileTrans = new JMXFileTransfer (InstanceRegistry.
                            getInstanceConnection(instanceName));

                // since initializing with a connection already
                isConnectNeeded = false;
                jmxFileTrans.setTargetServer(instanceName);
            } catch (InstanceException ie) {
                throw new SynchronizationException(
                   _strMgr.getString("synchronization.api.conn_reg_get_failed",
                            instanceName),ie);
            } catch (IOException ioe) {
                throw new SynchronizationException(
                    _strMgr.getString("synchronization.api.jmx_ctor_failed",
                            instanceName),ioe);
            }
        } else { // A server connecting to DAS or another server.            
            String url =null;

            String user = IdentityManager.getUser();
            if (user == null ) {
                String msg =_localStrMgr.getString("ClientUserNotFound",
                    instanceName);
                throw new RuntimeException(msg);
            }
                    
            String password = IdentityManager.getPassword();
            if (password == null) {
                String msg =_localStrMgr.getString("PassWordNotFound",
                    instanceName);
                throw new RuntimeException(msg);
            }                        
            
            // connecting to DAS. Using DAS property reader
            if ( SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME.compareTo(
                    instanceName) == 0 ) {
               DASPropertyReader dpr = new DASPropertyReader(
                            new InstanceConfig());

                try {
                    dpr.read();
                } catch (IOException ioe) {
                    String msg = _localStrMgr.getString("DASPropReadNotFound");
                    throw new SynchronizationException(msg);
                }
                url = dpr.getJMXURL();                
            }
            else {
                try {
                    ConfigContext cfgCtx = ApplicationServer.getServerContext().
                                                getConfigContext();
                    assert(cfgCtx != null);
                    JMXConnectorConfig jmxConfig = ServerHelper.
                        getJMXConnectorInfo(cfgCtx, instanceName);
                    if ( jmxConfig == null ) {
                        String msg = _localStrMgr.getString("JMXInfoNotFound",
                            instanceName);
                        throw new RuntimeException(msg);
                    }                                       


		    /* Need a better way of handling connector configuration */
                    final JMXServiceURL theUrl = 
                    JmxServiceUrlFactory.forRmiWithJndiInAppserver(
                        jmxConfig.getHost(), 
                        Integer.parseInt(jmxConfig.getPort()));
                    url = theUrl.toString();
                } catch (ConfigException ce) {
                    throw new SynchronizationException(
                    _strMgr.getString(
                        "synchronization.api.jmx_config_ctor_failed",
                      instanceName),ce);
                }
            }
            try {
                JMXServiceURL jmxurl = new JMXServiceURL(url);
                jmxFileTrans=new JMXFileTransfer(jmxurl, user, password, false);
                isConnectNeeded = true;
                assert(jmxFileTrans != null);
                jmxFileTrans.setTargetServer(instanceName);
            } catch (IOException ie) {
               throw new SynchronizationException(
                    _strMgr.getString("synchronization.api.jmx_ctor_failed",
                            instanceName),ie);
            }
        }
    }
    
    /**
     * Gets the file from the target server, in to the current node's
     * file system. 
     *
     * @param  remoteFile          Name of remoteFile to be downloaded.This file
     *                             name is relative to install root dir. Only
     *                             files under install root are allowed.
     * @param  localFile           Local file's name, where downloaded file must
     *                             be saved.If this does not exist,it is created
     *
     * @throws SynchronizatioException      This exception is thrown in all 
     *                                      error cases
     */
    public void get(java.lang.String remoteFile, java.io.File localFile) 
            throws  SynchronizationException {

        get(remoteFile, localFile, false);
    }

    /**
     * This is intended for internal synchronization use. This allows 
     * retrival of absolute file from remote host.
     *
     * @param  remoteFile          name of remoteFile to be downloaded
     * @param  localFile           local file name
     *
     * @throws SynchronizatioException   if an error during synchronization
     */
    public void getAbsolute(java.lang.String remoteFile, java.io.File localFile)
            throws  SynchronizationException {

        get(remoteFile, localFile, true);
    }

    /**
     * Implementation of get method.
     *
     * @param  remoteFile          name of remoteFile to be downloaded
     * @param  localFile           local file name
     * @param  allowAbsolutePath   if true, absolute path in remote 
     *                             host is allowed
     *
     * @throws SynchronizatioException   if an error during synchronization
     */
    private void get(java.lang.String remoteFile, java.io.File localFile, 
            boolean allowAbsolutePath) throws  SynchronizationException {

        if ( (jmxFileTrans == null ) 
                || (jmxFileTrans.getMBeanServerConnection() == null)) { 
                
            throw new SynchronizationException(
                _strMgr.getString("synchronization.api.connect_error"));
        }

        String relFile =  getRelativeDir(remoteFile);

        if (relFile == null ) {
            throw new SynchronizationException(_localStrMgr.getString(
                "NotValidParamString", remoteFile));
        } else {
            remoteFile = relFile;
        }

        // allows absolute path only if the flag is set to true
        if (!allowAbsolutePath) {
            if ( new File(remoteFile).isAbsolute() ) {
                 throw new SynchronizationException(
                   _strMgr.getString("synchronization.api.no_absolute",
                   remoteFile));
            }
        }

        try {
            if (!localFile.exists() )
                localFile.createNewFile();
        } catch( IOException ioe) {
            throw new SynchronizationException( _strMgr.getString(
                        "synchronization.api.create_local_file_failed",
                            localFile.getName()), ioe);
        }
        try {
            jmxFileTrans.mcDownloadFile(remoteFile, localFile);
        } catch( IOException e) {
            throw new SynchronizationException( _strMgr.getString(
                        "synchronization.api.transfer_exception",
                            remoteFile), e);
        }
    }

    public void get(java.lang.String remoteFile, java.lang.String localFile)
        throws SynchronizationException {

        String resolvedFile = RelativePathResolver.resolvePath(localFile);

        if (resolvedFile == null) {
            String msg = _localStrMgr.getString("NotValidResolveString",
                                localFile);
            throw new SynchronizationException(msg); 
        }

        File f = new File(resolvedFile);
        get(remoteFile, f);
    }

    /**
     * Uploads the file from  in to the current node's file system. 
     *
     * @param  localFile          Name of localFile to be uploaded
     * @param  remoteDir          Either temp dir or any sub directory of
     *                              install root
     *                            Pass null to upload to tmp directory
     *                            Absolute paths are not allowed
     *                            only relative path (relative to instanceRoot)
     *                            is allowed.
     *
     * @return String             Upload location of the file
     *
     * @throws SynchronizatioException      This exception is thrown in all          *                                      error cases
     */
    public String put(java.io.File localFile, String remoteDir) 
            throws  SynchronizationException{

        String relFile =  getRelativeDir(remoteDir);

        if (relFile == null ) {
            throw new SynchronizationException(_localStrMgr.getString(
                "NotValidParamString", remoteDir));
        } else {
            remoteDir = relFile;
        }

        if ( new File(remoteDir).isAbsolute() )
             throw new SynchronizationException(
               _strMgr.getString("synchronization.api.no_absolute",remoteDir));

        if ( (jmxFileTrans == null ) || (jmxFileTrans.getMBeanServerConnection()                                             == null))
                throw new SynchronizationException(
                    _strMgr.getString("synchronization.api.connect_error"));
        try {

            return jmxFileTrans.uploadFile(localFile, remoteDir);
        } catch(IOException e) {
            throw new SynchronizationException( _strMgr.getString(
                        "synchronization.api.transfer_exception",
                            remoteDir), e);
        }

    }

    public String put(java.lang.String localFile, String remoteDir) 
         throws SynchronizationException {

        String resolvedFile = RelativePathResolver.resolvePath(localFile);

        if (resolvedFile == null) {
            String msg = _localStrMgr.getString("NotValidResolveString",
                            localFile);
            throw new SynchronizationException(msg); 
        }
        File f = new File(resolvedFile);
        return put(f, remoteDir);
    }

    /**
     * Connects to the remote side. Uses the authentication information
     * passed in the constructor
     */
    public void connect() throws java.io.IOException{
        if (isConnectNeeded) {
            jmxFileTrans.setConnection();
            isConnectNeeded = false;
        }
    }

    /**
     * Closes the connection to the remote side.
     */
    public void disconnect() throws java.io.IOException {
        jmxFileTrans.setMBeanServerConnection(null);
    }

     /**
      * The following function removes the prefix ${com.sun.aas.instanceRoot}
      * from a given string. It is useful for getting relative paths.
      *
      * @param absolute      Parameterized path
      *
      * @return String       Relative path of the file/directory
      */
    String getRelativeDir(String absolute) {
        // It is not a string starting with a parameter
        if ( absolute.charAt(0) != '$' )
            return absolute;

        String prefix = absolute.substring(0,27);
        if ( (prefix != null ) && ( prefix.compareTo(
           "${"+SystemPropertyConstants.INSTANCE_ROOT_PROPERTY+"}")  == 0)) {
           // 27th character is /, 28 is relative path after 
           // ${com.sun.aas.instanceRoot}
           return absolute.substring(28);
        }
        else 
            return null;
    }

    // ---- INSTANCE VARIABLES -- PRIVATE --------------------------------
    private boolean isConnectNeeded = false;
    private JMXFileTransfer jmxFileTrans = null;
    private static Logger _logger = Logger.getLogger(EELogDomains.
                                        SYNCHRONIZATION_LOGGER);
    StringManagerBase _strMgr = StringManagerBase.getStringManager(_logger.
                                        getResourceBundleName());
    private static final StringManager _localStrMgr =
               StringManager.getManager(SynchronizationClientImpl.class);
}
