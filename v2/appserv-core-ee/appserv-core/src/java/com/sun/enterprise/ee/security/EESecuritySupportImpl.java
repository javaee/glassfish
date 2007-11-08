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
package com.sun.enterprise.ee.security;

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.synchronization.api.SecurityServiceMgr;
import com.sun.enterprise.ee.synchronization.api.SynchronizationContext;
import com.sun.enterprise.ee.synchronization.api.SynchronizationFactory;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.security.SecuritySupportImpl;
import com.sun.logging.LogDomains;

import sun.security.pkcs11.SunPKCS11;

/**
 * This implements SecuritySupport used in PluggableFeatureFactory for EE.
 * @author Shing Wai Chan
 */
public class EESecuritySupportImpl extends SecuritySupportImpl {
    static final String INTERNAL_TOKEN = "NSS Certificate DB";
    private static final String NAME_PREFIX = "__SUN_SJSAS_";
    public static final String SPARCV9 = "sparcv9";
    public static final String AMD64 = "amd64";
 
    public EESecuritySupportImpl() {
        super(false);

        if (useNSS()) {
            initNSS();
        } else {
            initJKS();
        }
    }

    // --- override method of SecuritySupportImpl
    /**
     * This method synchronize key file for given realm.
     * @param configContext the ConfigContext
     * @param fileRealmName
     * @exception
     */
    public void synchronizeKeyFile(ConfigContext configContext,
             String fileRealmName) throws Exception {
         // no need to sync if in DAS
         if (AdminService.getAdminService().isDas()) {
             return;
         }

         // creates a synchronization context
         SynchronizationContext synchCtx =
             SynchronizationFactory.createSynchronizationContext(configContext);

         // security service synchronization manager
         SecurityServiceMgr securitySynchMgr = synchCtx.getSecurityServiceMgr();

         // synchronizes a key file
         securitySynchMgr.synchronizeKeyFile(fileRealmName); 
    }

    // ---------------------------------------------------------

    private static synchronized void initNSS() {
        if (initialized) {
            return;
        }

        loadNSSNativeLibrary();

        initialized = true;
        
        String dbDir = System.getProperty(
                SystemPropertyConstants.NSS_DB_PROPERTY);

        boolean isWin = OS.isWindows();
	boolean is64BitVM = false;
	String osArch = System.getProperty("os.arch");
	if (osArch.equals(SPARCV9) || osArch.equals(AMD64)) is64BitVM = true;
		
        String libsoftokenLib =  
                System.getProperty(SystemPropertyConstants.NSS_ROOT_PROPERTY) + File.separator + 
		((is64BitVM) ? (osArch +  File.separator) : ("")) + ((isWin)? "softokn3.dll" : "libsoftokn3.so");

        Map name2PwdMap = IdentityManager.getMap();
        try {
            NssStore nssStore = NssStore.getInstance(null, false);
            List tokenInfoList = nssStore.getTokenInfoList();
            loadSunPKCS11Stores(dbDir, libsoftokenLib, tokenInfoList,
                    name2PwdMap);
            //XXX load NSS certs directly here
            nssStore.initCAStore(NssStore.getNssDbPassword());
            // there should be no other truststore besides CA's
            trustStores.add((KeyStore)nssStore.getTrustStores().get(0));
        } catch(Throwable ex) {
            _logger.log(Level.SEVERE, "nss.init_SunPKCS11_failed", ex);
            throw new IllegalStateException(ex.getMessage());
        }
    }

    //XXX temp, need to change after profile is done
    private boolean useNSS() {
        String dbDir = System.getProperty(
                SystemPropertyConstants.NSS_DB_PROPERTY);
        return (new File(dbDir, "key3.db")).exists();
    }

    private static void loadNSSNativeLibrary() {
        //nspr4, plc4, plds4, softoken are for Tiger
        boolean isWin = OS.isWindows();
        if (!isWin) {
            System.loadLibrary("nspr4");
            // softoken library exists in NSS 3.9 or later.
            System.loadLibrary("plc4");
            System.loadLibrary("plds4");
        } else {
            System.loadLibrary("libnspr4");
            // softoken library exists in NSS 3.9 or later.
            System.loadLibrary("libplc4");
            System.loadLibrary("libplds4");
        }
    }

    /**
     * This methods load SunPKCS11 stores.
     * @param dbDir
     * @param libsoftokenLib
     * @param tokenInfoList
     * @param name2PwdMap
     */
    private static void loadSunPKCS11Stores(String dbDir,
            String libsoftokenLib, List tokenInfoList, Map name2PwdMap)
            throws Exception {

        Provider[] providers = Security.getProviders();
        int ind = -1;
        for (int i = 0; i < providers.length; i++) {
            if (providers[i] instanceof SunPKCS11) {
                ind = i;
            } else {
                break;
            }
        }
     
        Map token2ConfigMap = getOtherConfigMap();
        
        tokenInfoList.add(new NssTokenInfo(INTERNAL_TOKEN, libsoftokenLib, 1)); 
        int size = tokenInfoList.size();
        for (int i = 0; i < size; i++) {
            NssTokenInfo tokenInfo = (NssTokenInfo)tokenInfoList.get(i);
            String tokenName = tokenInfo.getTokenName();
            String configFile = (String)token2ConfigMap.get(tokenName);
            Provider pkcs11Provider = null;
            if (configFile != null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Load PKCS11 stores with " + configFile);
                }
                pkcs11Provider = new SunPKCS11(configFile);
            } else {
                InputStream configStream = null;
                if (INTERNAL_TOKEN.equals(tokenName)) {
                    configStream = getNSSPKCS11ConfigStream(dbDir,
                            libsoftokenLib);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE,
                            "Load NSS PKCS11 stores with dbDir = " + dbDir +
                            ", softoken lib = " + libsoftokenLib);
                    }
                } else {
                    String libname = tokenInfo.getLibname();
                    int slotListIndex = tokenInfo.getSlotListIndex();
                    configStream = getPKCS11ConfigStream(NAME_PREFIX + i,
                            libname, slotListIndex);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE,
                            "Load PKCS11 stores with tokenName = " + tokenName +
                            ", libname = " + libname +
                            ", slotListIndex = " + slotListIndex);
                    }
                }
                pkcs11Provider = new SunPKCS11(tokenName, configStream);
            }
            // put NSS after any per-installed PKCS11 tokens in JDK
            int index = (i == size) ? (ind + i + 2) : (i + 1);
            Security.insertProviderAt(pkcs11Provider, index);
            String password = (String)name2PwdMap.get(tokenName);
            
            if (INTERNAL_TOKEN.equals(tokenName) || password == null) {
                password = NssStore.getNssDbPassword();
            }

            loadStores(tokenName, "PKCS11", pkcs11Provider,
                    null, password, null, password);
        }

        //XXX suppose to load NSS certs by provider
    }

    /**
     * Read the properties attributes under security-service in domain.xml
     * and return the result as an array.
     * @return a Map containing mapping from token to config file.
     * @return ConfigException
     */
    private static Map getOtherConfigMap() throws ConfigException {
        Map token2ConfigMap = new LinkedHashMap();
        ServerContext serverContext = ApplicationServer.getServerContext();
        ElementProperty[] elementProps = null;
        if (serverContext != null) {
            ConfigContext configContext = serverContext.getConfigContext();
            if (configContext != null) {
                SecurityService securityService =
                    ServerBeansFactory.getSecurityServiceBean(configContext);
                if (securityService != null) {
                    elementProps = securityService.getElementProperty();
                }
            }
        } else {
            String domainXMLLocation = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) +
                    IAdminConstants.NODEAGENT_DOMAIN_XML_LOCATION;
            ConfigContext configContext =
                ConfigFactory.createConfigContext(domainXMLLocation);
            if (configContext != null) {
                Domain domain = ServerBeansFactory.getDomainBean(configContext);
                if (domain != null) {
                    NodeAgents nodeAgents = domain.getNodeAgents();
                    if (nodeAgents != null) {
                        NodeAgent nodeAgent = nodeAgents.getNodeAgentByName(
                            System.getProperty(
                            SystemPropertyConstants.SERVER_NAME));
                        if (nodeAgent != null) {
                            elementProps = nodeAgent.getElementProperty();
                        }
                    }
                }
            }
        }

        if (elementProps != null && elementProps.length > 0) {
            for (int i = 0; i < elementProps.length; i++) {
                token2ConfigMap.put(elementProps[i].getName(),
                        elementProps[i].getValue());
            }
        }

        return token2ConfigMap;
    }

    private static InputStream getNSSPKCS11ConfigStream(String dbDir, String library) {
        StringBuffer buf = new StringBuffer("name=__SUN_SJSAS_internal\n");
        buf.append("library=").append(library).append("\n");
        buf.append("nssArgs=\"configdir='").append(dbDir).append("' certPrefix='' keyPrefix='' secmod='secmod.db'\"\n");
        buf.append("slot=2\n");
        buf.append("attributes=compatibility\n");
        buf.append("omitInitialize=true\n");
        return new ByteArrayInputStream(buf.toString().getBytes());
    }

    private static InputStream getPKCS11ConfigStream(String name,
            String library, int slotListIndex) {
        StringBuffer buf = new StringBuffer("name=").append(name).append("\n");
        buf.append("library=").append(library).append("\n");
        buf.append("slotListIndex=").append(slotListIndex).append("\n");
        buf.append("attributes=compatibility\n");
        buf.append("omitInitialize=true\n");
        return new ByteArrayInputStream(buf.toString().getBytes());
    }
}
