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

package com.sun.enterprise.tools.upgrade.common;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.certconversion.ProcessAdaptor;
import com.sun.enterprise.tools.upgrade.cluster.*;

/**
 *
 * author : Gautam Borah
 *
 */

public class CommonInfoModel{

    //Default values to be used in the tmp file created during upgrade
    private String AS_ADMIN_USER = "admin";
    private String AS_ADMIN_ADMINPASSWORD = "adminadmin";
    private String AS_ADMIN_MASTERPASSWORD = "changeit";
    
    //Fields to set inputs
    private String SOURCE_INSTALL_DIR;
    private String TARGET_INSTALL_DIR;
    private String adminUserName = null;
    private String adminPassword = null;
    private File passwordFile = null;
    private String masterPassword = null;
    private String passwordFilePath = null;
    private Map domainCredentials;
    private List passwordFileList;

    //Fields to set values of environment
    private String osName;
    private String[] CERT_ALIASES;

    //Fields to set values of domains root and domain list
    private String sourceDomainRoot="";
    private String targetDomainRoot;
    private boolean sourceInputAsDomainRoot;
    private boolean certificateConversionFlag=false;
    private boolean enlistDomain;
    private List domainList;
    private List domainOptionList;
    private String InstallConfig70;
    private String sourceApplicationRoot;
    
    //Fields to set current values of domain/server instances/cluster
    private String currentDomain;
    private String currentSourceServerInstance = "";
    private String currentCluster;
    private Hashtable domainMapping;
    private Map domainNameMapping;
    
    //Fields to set versions
    private String sourceVersionEdition = null;
    private String targetVersionEdition = null;
    private String sourceVersion = null;
    private String targetVersion = null;
    private String sourceEdition = null;
    private String targetDefaultProfile = null;
    private boolean isInPlace = false;
    
    private Map domainNSSPwdMapping;
    private Map domainTargetNSSPwdMapping;
    private Map domainJKSPwdMapping;
    private Map domainJKSCAPwdMapping;
    private Map domainssPwdFileMapping;
    private Map domainTargetnssPwdFileMapping;

    //Logging fields
    private StringManager stringManager = 
            StringManager.getManager(LogService.UPGRADE_COMMON_LOGGER);
    private static Logger logger=LogService.getLogger(LogService.UPGRADE_LOGGER);

    private static final String CERT_FILE_NAME = "keystore.jks";
    private static final String TRUSTED_KEY_STORE = "cacerts.jks";
    private static final String LICENSE_FILE = "LICENSE.txt";
    private static final String NUMBERSTRING = "0123456789";
    
    public static final String DOMAINS = "domains";
    public static final String CONFIG = "config";
    private static final String SERVER_POLICY_FILE = "server.policy";
    private static final String DEFAULT_WEB_XML_FILE = "default-web.xml";
    private static final String SUN_ACC_XML_FILE = "sun-acc.xml";
    private static final String DOMAIN_XML_FILE = "domain.xml";
    private static final String SOURCE_WSS_SERVER_CONFIG_XML = "wss-server-config.xml";
    private static final String TARGET_WSS_SERVER_CONFIG_XML = "wss-server-config-1.0.xml";
    private static final String HADB_DIR = "hadb";
    private static final String HADBM_JAR_FILE = "hadbm.jar";

    private boolean cliMode = false;
	private boolean nopromptMode = false;
    
    /**
     * CommonInfoModel constructor
     */
    public CommonInfoModel(){
        targetVersion = UpgradeConstants.VERSION_91;
        domainList = new ArrayList();
        domainOptionList = new ArrayList();
        passwordFileList = new ArrayList();
        domainNSSPwdMapping = new HashMap();
        domainTargetNSSPwdMapping = new HashMap();
        domainJKSPwdMapping = new HashMap();
        domainJKSCAPwdMapping = new HashMap();
        domainssPwdFileMapping= new HashMap();
        domainTargetnssPwdFileMapping= new HashMap();
        domainNameMapping= new HashMap();
        domainCredentials = new HashMap();
    }
    
    public void setTargetDomainRoot(String targetDomainsRoot) {
        //If target domains root is already set, avoid overwriting.
        if(targetDomainRoot != null && targetDomainRoot.equals(targetDomainsRoot))
            return;
        this.targetDomainRoot=targetDomainsRoot;
        try {
            String logPath = targetDomainRoot + File.separator + 
                    UpgradeConstants.ASUPGRADE_LOG;
            logger.info(stringManager.getString("upgrade.common.log_redirect") + logPath);
            LogService.initialize(logPath);
        } catch(Exception e) {
            logger.warning(e.getLocalizedMessage());
        }
    }
    
    public String getTargetDomainRoot() {
        return targetDomainRoot;
    }
    
    public void setSourceDomainRoot(String sourceDomainRoot) {
        this.sourceDomainRoot=sourceDomainRoot;
    }
    
    public String getSourceDomainRoot() {
        return sourceDomainRoot;
    }
    
    public String getSourceDomainPath(){
        return ((DomainInfo)getDomainMapping().get(currentDomain)).getDomainPath();
    }
    
    public String getSourceInstancePath(){
        String currentDomain = this.getCurrentDomain();
        String currentSourceInstance = this.getCurrentSourceInstance();
        DomainInfo dInfo = (DomainInfo) this.getDomainMapping().get(currentDomain);
        return dInfo.getInstancePath(currentSourceInstance);
    }
    
    public String getCurrentSourceInstance() {
        return currentSourceServerInstance;
    }
    
    public void setCurrentSourceInstance(String server) {
        currentSourceServerInstance = server;
    }
    
    public String getTargetConfig() {
        String config = getTargetDomainRoot() + File.separator + 
                (String)domainNameMapping.get(currentDomain) + 
                File.separator + CONFIG;
        return config;
    }
    
    public String getTargetJKSKeyStorePath(){
        String path = getTargetDomainRoot() + File.separator + 
                (String)domainNameMapping.get(currentDomain) + 
                File.separator + CONFIG + File.separator + CERT_FILE_NAME;
        return path;
    }
    
    public String getTargetTrustedJKSKeyStorePath(){
        String path = getTargetDomainRoot() + File.separator + 
                (String)domainNameMapping.get(currentDomain) + 
                File.separator + CONFIG + File.separator + TRUSTED_KEY_STORE;
        return path;
    }
    
    public String getSourceJKSKeyStorePath(){
        String path = getSourceDomainPath() +  File.separator + 
                CONFIG + File.separator + CERT_FILE_NAME;
        return path;
    }
    
    public String getSourceTrustedJKSKeyStorePath(){
        String path = getSourceDomainPath() +  File.separator + 
                CONFIG + File.separator + TRUSTED_KEY_STORE;
        return path;
    }
    
    public String getDestinationDomainPath(){
        return getDestinationDomainPath(this.currentDomain);
    }
    
    public String getDestinationDomainPath(String domainName){
        String path = getTargetDomainRoot() + File.separator + 
                (String)domainNameMapping.get(domainName);
        return path;
    }
    
    public void addDomainName(String domainName){
        domainList.add(domainName);
    }
    
    public List getDomainList(){
        return domainList;
    }
    
    public void addDomainOptionName(String domainName){
        domainOptionList.add(domainName);
    }
    
    public List getDomainOptionList(){
        return domainOptionList;
    }
    
    public String getCurrentDomain(){
        return currentDomain;
    }
    
    public void setCurrentDomain(String domainName){
        currentDomain=domainName;
    }
    
    public boolean getCertificateConversionFlag() {
        return certificateConversionFlag;
    }
    
    public void setCertificateConversionFlag(boolean flag){
        certificateConversionFlag=flag;
    }
    
    public String getSourceInstallDir() {
        return SOURCE_INSTALL_DIR;
    }

    public void setSourceInstallDir(String src) {
        SOURCE_INSTALL_DIR = src;
    }

    public String getTargetInstallDir() {
        return System.getProperty("com.sun.aas.installRoot");
    }
    
    public void setTargetInstallDir(String tgt) {
        TARGET_INSTALL_DIR = tgt;
    }
    
    /**
     * Get the password for the NSS certificate database. 
     * If this is an NSS to NSS or NSS to JKS migration, the String returned 
     * will be the source server password 
     * getTargetCertDbPassword() will return the target db password. 
     * If JKS to NSS, returns the target certificate db password.
     * @return String certpassword - the certificate password for current domain
     * and instance 
     * null if the current instance does not have a password, 
     * and should not be included in the cert migration.
     */
    public String getCertDbPassword() {
        Map instanceNSSPwdMap = (Map)domainNSSPwdMapping.get(currentDomain);
        return (String)instanceNSSPwdMap.get(currentSourceServerInstance);
    }
    
    public void setCertDbPassword(String pwd) {
        setCertDbPassword(currentDomain, currentSourceServerInstance, pwd);
    }
    
    public void setCertDbPassword(String domain, String pwd) {
        setCertDbPassword(domain, currentSourceServerInstance, pwd);
    }
    
    public void setCertDbPassword(String domain, String instance, String pwd) {
        Map instanceNssPwdMap = (Map)domainNSSPwdMapping.get(domain);
        if(instanceNssPwdMap == null) {
            instanceNssPwdMap = new HashMap();
        }
        instanceNssPwdMap.put(instance, pwd);
        domainNSSPwdMapping.put(domain,instanceNssPwdMap);
    }
    
    public String getTargetCertDbPassword() {
        return (String)domainTargetNSSPwdMapping.get(currentDomain);
    }
    
    public void setTargetCertDbPassword(String pwd) {
        domainTargetNSSPwdMapping.put(currentDomain,pwd);
    }
    
    public void setTargetCertDbPassword(String domain, String pwd) {
        domainTargetNSSPwdMapping.put(domain,pwd);
    }
    
    public String[] getCertAliases() {
        return CERT_ALIASES;
    }
    
    public String getJksKeystorePassword() {
        return (String)domainJKSPwdMapping.get((String)domainNameMapping.get(currentDomain));
    }
    
    public void setJksKeystorePassword(String pwd) {
        domainJKSPwdMapping.put((String)domainNameMapping.get(currentDomain),pwd);
    }
    
    public void setJksKeystorePassword(String domain, String pwd) {
        domainJKSPwdMapping.put((String)domainNameMapping.get(currentDomain),pwd);
    }
    
    public String getJksCAKeystorePassword() {
        return (String)domainJKSCAPwdMapping.get((String)domainNameMapping.get(currentDomain));
    }
    
    public void setJksCAKeystorePassword(String pwd) {
        domainJKSCAPwdMapping.put((String)domainNameMapping.get(currentDomain),pwd);
    }
    
    public void setJksCAKeystorePassword(String domain, String pwd) {
        domainJKSCAPwdMapping.put((String)domainNameMapping.get(currentDomain),pwd);
    }
    
    public Hashtable getDomainMapping() {
        return domainMapping;
    }
    
    public void setDomainMapping(Hashtable domainMapping) {
        this.domainMapping = domainMapping;
    }
    
    public String getOSName() {
        return osName;
    }
    
    public void setOSName(String osName){
        this.osName = osName;
    }
    
    public String getNSSPwdFile() {
        return (String)domainssPwdFileMapping.get(currentDomain);
    }
    
    public void setNSSPwdFile(String pwdFile){
        domainTargetnssPwdFileMapping.put(currentDomain,pwdFile);
    }
    
    public String getTargetNSSPwdFile() {
        return (String)domainTargetnssPwdFileMapping.get(currentDomain);
    }
    
    public void setTargetNSSPwdFile(String pwdFile){
        domainTargetnssPwdFileMapping.put(currentDomain,pwdFile);
    }
    
    public void setInstallConfig70(String config) {
        InstallConfig70 = config;
    }
    
    public String getInstallConfig70() {
        return InstallConfig70;
    }
    
    public Map getTargetDomainNameMapping(){
        return domainNameMapping;
    }
    
    public boolean isInPlace() {
        return isInPlace;
    }
    
    public void setIsInPlace(boolean isInPlace) {
        this.isInPlace = isInPlace;
    }
    
    /**
     * @return a logger to use in the Module implementation classes
     */
    public static Logger getDefaultLogger() {
        if (logger==null) {
            logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
        }
        return logger;
    }
    
    public boolean isUpgradeJKStoJKS() {
        //Case : 8.xPE/9.0PE -> developer profile	    
        String sourceVersionAndEdition = getSourceVersionAndEdition();			
        if (UpgradeConstants.VERSION_AS80_PE.equals(sourceVersionAndEdition) ||
                UpgradeConstants.VERSION_AS81_PE.equals(sourceVersionAndEdition)) {
            return true;
        }
        return false;
    }
    
    public boolean isUpgradeJKStoNSS() {
        //Case : 8.xPE/9.0PE -> enterprise profile - Domain always created with developer profile.	    
        return false;
        /*if (UpgradeConstants.EDITION_EE.equals(getTargetEdition()) ||
                UpgradeConstants.EDITION_SE.equals(getTargetEdition())) {
            String sourceVersionAndEdition = getSourceVersionAndEdition();			
            if (UpgradeConstants.VERSION_AS80_PE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS81_PE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS90_PE.equals(sourceVersionAndEdition)) {
                return true;
            }
        }
        return false;*/
    }
    
    public boolean isUpgradeNSStoNSS() {
        //Case : 8.1EE->9.1 : domain created as enterprise in target. 7.x not supported for now.
        String sourceVersionAndEdition = getSourceVersionAndEdition();
        if(UpgradeConstants.VERSION_AS81_EE.equals(sourceVersionAndEdition)) {
            return true;
        }
        /*if (UpgradeConstants.EDITION_EE.equals(getTargetEdition()) ||
                UpgradeConstants.EDITION_SE.equals(getTargetEdition())) {
            String sourceVersionAndEdition = getSourceVersionAndEdition();			
            if (UpgradeConstants.VERSION_AS7X_PE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS7X_SE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS7X_EE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS81_EE.equals(sourceVersionAndEdition)) {
                return true;
            }
        }*/
        return false;
    }
    
    public boolean isUpgradeNSStoJKS() {
        //Case : 8.1EE -> 9.1 domain with developer profile : Upgrade not required.
        return false;
        /*if (UpgradeConstants.EDITION_PE.equals(getTargetEdition())) {            
            String sourceVersionAndEdition = getSourceVersionAndEdition();			
            if (UpgradeConstants.VERSION_AS7X_PE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS7X_SE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS7X_EE.equals(sourceVersionAndEdition) ||
                    UpgradeConstants.VERSION_AS81_EE.equals(sourceVersionAndEdition)){
                return true;
            }
        }
        return false;*/
    }
    
    public boolean checkUpgradefrom8xpeto9x() {
        boolean checkUpgradefrom8xpeto9x = false;
        String sourceVersionAndEdition = getSourceVersionAndEdition();		
        String targetVersion = getTargetVersion();			
        checkUpgradefrom8xpeto9x = (UpgradeConstants.VERSION_AS80_PE.equals(sourceVersionAndEdition) ||
                                    UpgradeConstants.VERSION_AS81_PE.equals(sourceVersionAndEdition) ||
                                    UpgradeConstants.VERSION_AS82_PE.equals(sourceVersionAndEdition)) && 
                                    UpgradeConstants.VERSION_91.equals(targetVersion);
        return checkUpgradefrom8xpeto9x;
    }

    public boolean checkUpgradefrom8xeeto9x() {
        boolean checkUpgradefrom8xeeto9x = false;
        String sourceVersionAndEdition = getSourceVersionAndEdition();
        String targetVersion = getTargetVersion();
        checkUpgradefrom8xeeto9x = (UpgradeConstants.VERSION_AS81_EE.equals(sourceVersionAndEdition) ||
                                    UpgradeConstants.VERSION_AS82_EE.equals(sourceVersionAndEdition)) &&
                                    UpgradeConstants.VERSION_91.equals(targetVersion);
        return checkUpgradefrom8xeeto9x;
    }
 
    public boolean checkUpgradefrom9xpeto9x() {
        boolean checkUpgradefrom9xpeto9x = false;
        String sourceVersionAndEdition = getSourceVersionAndEdition();		
        String targetVersion = getTargetVersion();			
        checkUpgradefrom9xpeto9x = UpgradeConstants.VERSION_AS90_PE.equals(sourceVersionAndEdition) &&
                                   UpgradeConstants.VERSION_91.equals(targetVersion);
        return checkUpgradefrom9xpeto9x;
    }

    public boolean checkUpgrade8xto9x() {
        boolean checkUpgrade8xto9x = false;
        checkUpgrade8xto9x = checkUpgradefrom8xpeto9x() ||
                             checkUpgradefrom8xeeto9x();
        return checkUpgrade8xto9x;
    }

    public boolean checkUpgrade9xto9x() {
        boolean checkUpgrade9xto9x = false;
        checkUpgrade9xto9x = checkUpgradefrom9xpeto9x();
        return checkUpgrade9xto9x;
    }

    //NOT NEEDED SINCE DOMAINS ROOT INPUT FOR SOURCE IS NOT SUPPORTED
    /*public void enlistDomainsFromSource(java.util.List domains){
        if(domains == null)
            this.enlistDomainsFromSource();
        if(enlistDomain)
            return;
        this.setDomainMapping(UpgradeUtils.getUpgradeUtils(this).enlistDomainsFromUserDefinedDirectories(domains));
        this.enlistDomain=true;
    }*/
    
    public void enlistDomainsFromSource(){
        //Return if this method has been invoked already
        if(enlistDomain)
            return;
        
        String source = getSourceInstallDir();
        //Return if source directory input is null
        if( source == null) {
            return;
        }
        
        //Remove any trailing File separators
        if(source.endsWith(File.separator)) {
            source = source.substring(0,
                    source.length() - File.separator.length());
        }
        
        //Set source domain name, domain root and mapping from the input
        String domainName = source.substring(source.lastIndexOf(File.separator) + 
                File.separator.length(), source.length());
        String domainRoot = source.substring(0,
                source.lastIndexOf(domainName) - File.separator.length());
        setSourceDomainRoot(domainRoot);
        setSourceDomainRootFlag(true);
        setDomainMapping(UpgradeUtils.getUpgradeUtils(this).enlistDomains( 
                domainName, domainRoot));
        enlistDomain=true;
    }
    
    private boolean sourceIsDomain() {
        return UpgradeUtils.getUpgradeUtils(this).checkSourceInputAsDomain();
    }
    
    //REMOVE SINCE ITS DUPLICATE EFFORT
    /*public boolean checkSourceInputAsDomainRoot(String source) {
        return UpgradeUtils.getUpgradeUtils(this).checkSourceInputAsDomainRoot(source,this);
    }*/
    
    public boolean isValid70Domain(String domainPath) {
        return UpgradeUtils.getUpgradeUtils(this).isValid70Domain(domainPath);
    }
    
    public boolean getSourceDomainRootFlag() {
        return sourceInputAsDomainRoot;
    }
    
    public void setSourceDomainRootFlag(boolean fl) {
        this.sourceInputAsDomainRoot = fl;
    }
    
    public void printInfo(){
        getDefaultLogger().finest("SOURCE_INSTALL_DIR=="+SOURCE_INSTALL_DIR);
        getDefaultLogger().finest("SOURCE_INSTALL_DIR=="+TARGET_INSTALL_DIR);
        int size = domainList.size();
        for(int i=0;i<size;i++) {
            String domainName = (String)domainList.get(i);
            getDefaultLogger().finest("**********" + domainName + "****************");
            getDefaultLogger().finest("NSSPWD=="+ domainNSSPwdMapping.get(domainName));
            getDefaultLogger().finest("JKSPWD=="+ domainJKSPwdMapping.get(domainName));
            getDefaultLogger().finest("********************************************");
        }
        
    }
    
    public String getSourceServerPolicyFileName() {
        return getSourceDomainPath() + File.separator + CONFIG + 
                File.separator + SERVER_POLICY_FILE ;
    }
    
    public String getTargetServerPolicyFileName(){
        return getDestinationDomainPath() + File.separator + CONFIG +
                File.separator + SERVER_POLICY_FILE;
    }
    
    public String getSourceDefaultWebXMLFileName(){
        return getSourceDomainPath() + File.separator + CONFIG +
                File.separator + DEFAULT_WEB_XML_FILE;
    }
    
    public String getTargetDefaultWebXMLFileName(){
        return getDestinationDomainPath() + File.separator + CONFIG +
                File.separator + DEFAULT_WEB_XML_FILE;
    }
    
    public String getSourceSunACCFileName(){
        return getSourceDomainPath() + File.separator + CONFIG + 
                File.separator + SUN_ACC_XML_FILE ;
    }
    
    public String getTargetSunACCFileName(){
        return getDestinationDomainPath() + File.separator + CONFIG + 
                File.separator + SUN_ACC_XML_FILE ;
    }
    
    public String getSourceConfigXMLFile(){
        return getSourceDomainPath() + File.separator + CONFIG + 
                File.separator + DOMAIN_XML_FILE ;
    }
    
    public String getTargetConfigXMLFile(){
        return getDestinationDomainPath() + File.separator + CONFIG +
                File.separator + DOMAIN_XML_FILE ;
    }
    
    public String getSourceVersionAndEdition(){
        if(this.sourceVersionEdition == null){
            String sourceInstallDir = this.getSourceInstallDir();			
            if(sourceInstallDir != null) {	
                this.sourceVersionEdition = new VersionExtracter(
                        sourceInstallDir,this).getVersion();
            }				
        }
        return this.sourceVersionEdition;
    }
    
    public void clearSourceAndTargetVersions(){
        this.sourceVersionEdition = null;
    }
    
    public String getSourceVersion(){
        if(sourceVersion == null) {
            sourceVersion = UpgradeUtils.getUpgradeUtils(this).
                    versionString(this.getSourceVersionAndEdition());
        }
        return sourceVersion;
    }
    
    public String getTargetVersion(){
        return targetVersion;	
    }
    
    public String getSourceEdition(){
        if(sourceEdition == null) {
            sourceEdition = UpgradeUtils.getUpgradeUtils(this).
                    editionString(this.getSourceVersionAndEdition());
        }
        return sourceEdition;
    }
    
    public String getTargetDefaultProfile() {
        if(targetDefaultProfile == null) {	    
            targetDefaultProfile = new VersionExtracter(
                    this.getTargetInstallDir(), this).getTargetDefaultProfile();
        }
        return targetDefaultProfile;	
    }
    
    public boolean isUpgradeSupported(){
        String sourceVersionAndEdition = this.getSourceVersionAndEdition();
        String sourceVersion = this.getSourceVersion();
        String sourceEdition = this.getSourceEdition();
        String targetVersion = this.getTargetVersion();
        String targetDefaultProfile = this.getTargetDefaultProfile();

        //Same version : No upgrade required.	 
        if(sourceVersion.equals(targetVersion)) {
            logger.log(Level.INFO, stringManager.getString(
                    "upgrade.common.same_version_upgrade_not_required"));
            return false;
        }
   
        //Check if the Source version and edition supports this version upgrade.	
        if(!((java.util.HashSet)UpgradeConstants.supportMap.get(
                sourceVersionAndEdition)).contains(targetVersion)) {
            logger.info(stringManager.getString(
                    "upgrade.common.upgrade_not_supported"));		
            return false;
        }	    

        //Edition Profile checks for upgrade support
        if(UpgradeConstants.EDITION_EE.equals(sourceEdition)) {
            if(UpgradeConstants.DEVELOPER_PROFILE.equals(targetDefaultProfile)) {
                //Case where 9.1PE stand-alone installer is used to install target
                if(!isInPlace()) {
                    if(isNssHadbFound()) {
                        //8.xEE to 9.1 enterprise profile upgrade SBS mode
                        return true;
                    } else {
                        //8.xEE to 9.1 developer profile upgrade SBS mode
                        logger.info(
                            stringManager.getString(
                                "upgrade.common.upgrade_not_supported_EEDeveloperSBS"));
                        return false;
                    }
                } else {
					// An EE to developer (i.e. pe) is not valid. cr6567370
					logger.info(stringManager.getString(
                            "upgrade.common.upgrade_not_supported_EEToDeveloper"));					
                    return false;
                }
            } else {
                //Case where 9.1EE stand-alone installer is used to install target
                //Default profile will always be enterprise
                return true;
            }
        } else if(UpgradeConstants.EDITION_PE.equals(sourceEdition)) {
            logger.log(Level.INFO, stringManager.getString(
                    "upgrade.common.developer_profile_created"));
            return true;
        } else {
            //Any other Source Edition Not supported.
            logger.info(stringManager.getString("upgrade.common.upgrade_not_supported")); 
            return false;
        }	    
    }
    
    public String getCurrentCluster(){
        return this.currentCluster;
    }
    
    public void setCurrentCluster(String clu){
        this.currentCluster = clu;
    }
    
    public String findLatestDomainDir(String domainRoot) {
        UpgradeUtils upgrUtils = UpgradeUtils.getUpgradeUtils(this);
        return upgrUtils.findLatestDomainDir(domainRoot,currentDomain);
    }
    
    public String getAdminUserName(){
        return this.adminUserName;
    }
    
    public void setAdminUserName(String adminUserName){
        this.adminUserName=adminUserName ;
    }
    
    public String getAdminPassword(){
        return this.adminPassword;
    }
    
    public void setAdminPassword(String adminPassword){
        this.adminPassword = adminPassword;
    }
   
    public String getPasswordFile() {
        if (passwordFilePath == null) {
            try {
                passwordFile = java.io.File.createTempFile("ugpw", null);
                FileWriter writer = new FileWriter(passwordFile);
                writer.write("AS_ADMIN_PASSWORD=" + getAdminPassword() +"\n");
                writer.write("AS_ADMIN_ADMINPASSWORD=" + getAdminPassword() +"\n");
                writer.write("AS_ADMIN_MASTERPASSWORD=" + getMasterPassword() + "\n");
                writer.close();
                passwordFilePath = passwordFile.getAbsolutePath();
            } catch (IOException ioe) {
                logger.severe(stringManager.getString("upgrade.common.general_exception") + " " + ioe.getMessage());        
            }
        }
        return passwordFilePath;
    }
    
    public void setPasswordsFromFile(String path) {
        try {
            File userPasswordFile = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(userPasswordFile));
            while( reader.ready() ) {
                String line = reader.readLine();
                if ( line.startsWith("AS_ADMIN_PASSWORD=") ) {
                    setAdminPassword(line.substring(line.indexOf("=") + 1));
                } else if ( line.startsWith("AS_ADMIN_ADMINPASSWORD=") ) {
                    setAdminPassword(line.substring(line.indexOf("=") + 1));
                } else if ( line.startsWith("AS_ADMIN_MASTERPASSWORD=") ) {
                    setMasterPassword(line.substring(line.indexOf("=") + 1));
                }
            }
            reader.close();
        } catch (Exception e) {
            logger.severe(stringManager.getString("upgrade.common.general_exception") + " " + e.getMessage());
        }
    }
    
    public void setMasterPassword(String pw) {
        masterPassword = pw;
    }
    
    public String getMasterPassword() {
        return masterPassword;
    }
    
    public void deletePasswordFile() {
        if (passwordFile != null) {
            passwordFile.delete();
        }
    }
    
    public String getSourceInitConfFileName() {
        //Init Conf File does not exist in versions > 8.x	    
        return null;
    }
    
    public void recover() {
        UpgradeUtils.getUpgradeUtils(this).recover();
    }
    
    public boolean processClinstnceConfFiles(Vector files){
        return ClustersInfoManager.getClusterInfoManager().processClinstanceConfFiles(files);
    }

    public String getSourceWssServerConfigXML(){
        return getSourceDomainPath() + File.separator + CONFIG
                + File.separator + SOURCE_WSS_SERVER_CONFIG_XML;
    }

    public String getTargetWssServerConfigXML(){
        return getDestinationDomainPath() + File.separator + CONFIG
            + File.separator + TARGET_WSS_SERVER_CONFIG_XML;
    }

    public void setSourceApplicationRoot(String applRoot) {
        this.sourceApplicationRoot = applRoot;
    }

    public String getSourceApplicationRoot() {
        return this.sourceApplicationRoot;
    }

    public void setDomainCredentials(String dName, String dValues) {
        domainCredentials.put(dName, dValues);
    }

    public String getDomValuesFromPasswordFile(String domainName) {
        return (String)domainCredentials.get(domainName);
    }

    public void addPasswordFile(String passwordFile) {
        passwordFileList.add(passwordFile);
    }

    public List getPasswordFileList() {
        return passwordFileList;
    }
    
    /**
      * Method to build the ClusterInfo object required for processing clusters.
      */	  
    public void processDomainXmlForClusters() {
        ClustersInfoManager.getClusterInfoManager().gatherClusterInfo(this);
    }

    public boolean isNssHadbFound() {     
        String strHadbDir = getTargetInstallDir() + File.separator + HADB_DIR;	    
        File hadbDir = new File(strHadbDir);
	if(!hadbDir.exists()) {
	    return false;
        } else {
            String[] hadbList = hadbDir.list();
            if((hadbList == null)||(hadbList.length <= 0)){
                // not a valid directory
                return false;
            }else{
                String strHadbmJarFile = strHadbDir + File.separator + hadbList[0] +
                    File.separator + "lib" + File.separator + HADBM_JAR_FILE;
                File hadbmJarFile = new File(strHadbmJarFile);
                if(!hadbmJarFile.exists()) {
                    return false;
                }
            }
        }
        return true;
    }	

    public void setCliMode(boolean b) {
        cliMode = b;
    }
    
    //returns true if started in CLI
    public boolean getCliMode() {
        return cliMode;
    }
    
	//- Must know when in noprompt mode
	public boolean isNoprompt(){
		return nopromptMode;
	}
	public void setNoprompt(boolean flag){
		nopromptMode = flag;
	}
	public String getDefaultMasterPassword(){
		return AS_ADMIN_MASTERPASSWORD;
	}
}
