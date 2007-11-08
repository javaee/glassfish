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
 * CertificateTransfer.java
 *
 * Created on June 13, 2005, 11:40 AM
 */

package com.sun.enterprise.tools.upgrade.certconversion;

import com.sun.enterprise.tools.upgrade.common.BaseModule;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.io.FileUtils;

/**
 * This class is for future use when the server supports the use of both 
 * NSS and JKS keystore types.
 * The CertificateTransfer class copies the source certificate databases to the target server
 * and configure the target to use the appropriate keystore type JKS/NSS
 *
 * @author Hans Hrasna
 */
public class CertificateTransfer implements BaseModule {
    
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.certconversion");
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private Vector recoveryList = new Vector();
    private UpgradeUtils utils;
    private String JAVA_HOME;
    private CommonInfoModel cim;
    private String targetJksPath;
    private String sourceJksPath;
    private String targetCaJksPath;
    private String sourceCaJksPath;
    private String targetNssPath;
    private String sourceNssPath;
    private String targetCaNssPath;
    private String sourceCaNssPath;
    
    private static String JKS_CERTS = "keystore.jks";
    private static String CA_JKS_CERTS = "cacerts.jks";
    private static String NSS_CERTS = "key3.db";
    private static String CA_NSS_CERTS = "cert8.db";
    private static String CA_70_CERTS = "cert7.db";
    
    /** Creates a new instance of CertificateTransfer */
    public CertificateTransfer() {
        JAVA_HOME = System.getProperty("com.sun.aas.java.home");
    }
    
    public String getName() {
        return stringManager.getString("enterprise.tools.upgrade.certconversion.moduleName");
    }
    
    public boolean upgrade(CommonInfoModel cmi) {
        cim = cmi;
        logger.log(Level.INFO, stringManager.getString("enterprise.tools.upgrade.certconversion.start_certificate_migration",cmi.getCurrentDomain()));
        targetJksPath = cim.getTargetJKSKeyStorePath();
        sourceJksPath = cim.getSourceJKSKeyStorePath();
        targetCaJksPath = cim.getTargetTrustedJKSKeyStorePath();
        sourceCaJksPath = cim.getSourceTrustedJKSKeyStorePath();
        String sourceConfigPath = cim.getSourceDomainPath()+ File.separator + "config";
        String targetConfigPath = cim.getTargetConfig();
        targetNssPath = targetConfigPath + File.separator + NSS_CERTS;
        sourceNssPath = sourceConfigPath + File.separator + NSS_CERTS;
        targetCaNssPath = targetConfigPath + File.separator + CA_NSS_CERTS;
        if (cim.getSourceVersionAndEdition().equals(UpgradeConstants.VERSION_AS7X_PE)) {
            sourceCaNssPath = sourceConfigPath + File.separator + CA_70_CERTS;
        } else {
            sourceCaNssPath = sourceConfigPath + File.separator + CA_NSS_CERTS;
        }
        utils = UpgradeUtils.getUpgradeUtils(cim);
        //String tv = cim.getTargetVersionAndEdition();
        String sv = cim.getSourceEdition();//cim.getSourceVersionAndEdition();
        
        //8.0PE and 8.1PE have JKS certs
        if(sv.equals(UpgradeConstants.EDITION_PE)) {
       // if(sv.equals(UpgradeConstants.VERSION_AS80_PE) || sv.equals(UpgradeConstants.VERSION_AS81_PE)) {
            //PE source upgrade will always create a domain with developer profile.
            return jksToJks();
            /*if(tv.equals(UpgradeConstants.VERSION_AS90_PE)) {
                return jksToJks();
            }
            else if ( tv.equals(UpgradeConstants.VERSION_AS91_EE) ){
                return jksToNss();
            }*/
        }
        //7.0PE, 7.1SE, 7.1EE, 8.0EE and 8.1EE have NSS certs
        //7.x not supported.
        //if(sv.equals(UpgradeConstants.VERSION_AS81_EE)) {
        //EE source will always create a domain with enterprise profile
        if(sv.equals(UpgradeConstants.EDITION_EE)) {
            //Not a valid upgrade since EE source to a developer profile not supported.
            //Target will be an enterprise profile always.
	    return nssToNss();
            /*if(tv.equals(UpgradeConstants.VERSION_AS90_PE)) {
                return nssToJks();
            } else if ( tv.equals(UpgradeConstants.VERSION_AS91_EE) ){
                return nssToNss();
            }*/
        }
        return false;
    }
    
    /* Configure the appserver JDK java.security and the appserver to use JKS
     *   1) Set the default provider in the Java Security properties file ($JAVA_HOME/lib/security/java.security)
     * to sun.security.provider.Sun: security.provider.1=sun.security.provider.Sun
     *
     * 2) Set keystore.type=jks
     *
     * 3) Configure the appserver to use JKS certs
      <jvm-options>
        -Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/config/keystore.jks
      </jvm-options>
      <jvm-options>
        -Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/config/cacerts.jks
      </jvm-options>
     */
    private void configureJks() {
        
        //set keystore.type=jks
        String securityFile = JAVA_HOME + File.separator + "lib" + File.separator + "security" + File.separator + "java.security";
        File security = getSecurityFile();
        if(!security.exists()){
            logger.warning(stringManager.getString("enterprise.tools.upgrade.certconversion.errorConfiguringJKS"));
            return;
        }
        
        //TODO 
        
    }
    
    /* Configure the appserver JDK java.policy and the appserver to use PKCS#11
     * Install the Sun PKCS#11 provider statically
     *
     * 1) Find/Create pkcs11.cfg
     *
     * 2) Set the default provider in the Java Security properties file ($JAVA_HOME/lib/security/java.security)
     * to sun.security.pkcs11.SunPKCS11: security.provider.1=sun.security.pkcs11.SunPKCS11 /opt/bar/cfg/pkcs11.cfg
     *
     * 3) Set keystore.type=pkcs11
     * security.provider.1=sun.security.pkcs11.SunPKCS11 /opt/bar/cfg/pkcs11.cfg
     *
     * Configure the appserver to use NSS certs
      <jvm-options>
        -Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/config/key3.db
      </jvm-options>
      <jvm-options>
        -Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/config/certs8.db
      </jvm-options>
     */
    
    private void configureNss() {
        File security = getSecurityFile();
        if(!security.exists()){
            logger.warning(stringManager.getString("enterprise.tools.upgrade.certconversion.errorConfiguringNSS"));
            return;
        }
        //TODO 
        
    }
    
    /* Transfer and configure JKS certs to a target originally configured with NSS certs
     **/
    private boolean jksToNss() {
        configureJks();
        return copyJksCerts();
    }
    
     /* Transfer and configure NSS certs to a target originally configured with JKS certs
      **/
    private boolean nssToJks() {
        configureNss();
        return copyNssCerts();
        
    }
    
    private boolean jksToJks() {
        backupJksCerts();
        return copyJksCerts();
    }
    
    private boolean nssToNss() {
        backupNssCerts();
        return copyNssCerts();
    }
    
    private boolean copyJksCerts() {
        try {
            UpgradeUtils.copyFile(sourceJksPath, targetJksPath);
            UpgradeUtils.copyFile(sourceCaJksPath, targetCaJksPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, stringManager.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",e));
            return false;
        }
        return true;
    }
    
    private boolean backupJksCerts() {
        try {
            backup(targetJksPath);
            backup(targetCaJksPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, stringManager.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",e));
            return false;
        }
        return true;
    }
    
    private boolean copyNssCerts() {
        
        try {
            UpgradeUtils.copyFile(sourceNssPath, targetNssPath);
            UpgradeUtils.copyFile(sourceCaNssPath, targetCaNssPath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, stringManager.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",e));
            return false;
        }
        return true;
    }
    
    private boolean backupNssCerts() {
        try {
            backup(targetNssPath);
            backup(targetCaNssPath);
        } catch (Exception e) {
            //if there is an exception, don't do anything
            //logger.log(Level.SEVERE, stringManager.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",e));
            //return false;
        }
        return true;
    }
    
    private void backup(String filePath) throws IOException {
        String backupFilePath = filePath + ".bak";
        utils.copyFile(filePath, backupFilePath);
        recoveryList.add(filePath);
    }
    
    public void recovery(CommonInfoModel commonInfo) {
        Enumeration e = recoveryList.elements();
        while(e.hasMoreElements()){
            String recoverPath = (String)e.nextElement();
            String backupPath = recoverPath + ".bak";
            try {
                utils.copyFile(backupPath, recoverPath);
                new File(backupPath).delete();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, stringManager.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",ioe.getMessage()),new Object[]{recoverPath,ioe});
            }
        }
    }
    
    /* @returns $JAVA_HOME/lib/security/java.security */
    private File getSecurityFile(){
        String securityFile = JAVA_HOME + File.separator + "lib" + File.separator + "security" + File.separator + "java.security";
        return new File(securityFile);
    }
}
