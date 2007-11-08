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

package com.sun.enterprise.tools.upgrade.certconversion;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.security.*;
import java.security.cert.*;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.common.*;

/**
 *
 * author : Servesh Singh
 *
 */

public class JKStoJKSConversionModule implements BaseModule{
    
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private StringManager sm;
    private String jksPath;
    private String sourceJksPath;
    private String trustJksPath;
    private String sourceTrustJksPath;
    private String jksKeyStorePassword;
    private String jksCAkeyStorePassword;
    private boolean certificateError = false;
    private CommonInfoModel commonInfo;
    
    public JKStoJKSConversionModule(){
        sm = StringManager.getManager(LogService.UPGRADE_CERTCONVERSION_LOGGER);
    }
    
    public boolean upgrade(CommonInfoModel commonInfo){
        try {
            String currentDomain = commonInfo.getCurrentDomain();
            if(!(commonInfo.getDomainOptionList().contains(currentDomain)))
                return true;
            this.jksPath=commonInfo.getTargetJKSKeyStorePath();
            this.sourceJksPath=commonInfo.getSourceJKSKeyStorePath();
            this.trustJksPath = commonInfo.getTargetTrustedJKSKeyStorePath();
            this.sourceTrustJksPath = commonInfo.getSourceTrustedJKSKeyStorePath();
            this.jksKeyStorePassword=commonInfo.getJksKeystorePassword();
            this.jksCAkeyStorePassword=commonInfo.getJksCAKeystorePassword();
            this.commonInfo = commonInfo;
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.start_certificate_migration",currentDomain));
            doBackup(commonInfo);
            copyKeyPairs(sourceJksPath,jksPath);
            copyCACertificates(sourceTrustJksPath,trustJksPath);
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.finished_certificate_migration",currentDomain));
        }catch(CertificateException ce) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",ce));
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            return false;
        }
        return true;
    }
    
    private void doBackup(CommonInfoModel commonInfo) throws CertificateException {
        doCACertificateBackup();
        doKeyPairBackup();
    }
    
    private void doCACertificateBackup() throws CertificateException{
        copyCACertificates(trustJksPath, trustJksPath+".back");
    /*FileInputStream in = null;
    FileOutputStream out = null;
    try {
        KeyStore keystoreSource = KeyStore.getInstance(KeyStore.getDefaultType());
        KeyStore keystoreTarget = KeyStore.getInstance(KeyStore.getDefaultType());
        in = new FileInputStream(new File(trustJksPath));
        keystoreSource.load(in,jksCAkeyStorePassword.toCharArray());
        keystoreTarget.load(null, jksCAkeyStorePassword.toCharArray());
        java.util.Enumeration en = keystoreSource.aliases();
        for(; en.hasMoreElements(); ){
            String alias = (String) en.nextElement();
            java.security.cert.Certificate cert = keystoreSource.getCertificate(alias);
            keystoreTarget.setCertificateEntry(alias,cert);
        }
        out = new FileOutputStream(trustJksPath+".back");
        keystoreTarget.store(out, jksCAkeyStorePassword.toCharArray());
    } catch (java.security.cert.CertificateException e) {
        _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
    } catch (NoSuchAlgorithmException e) {
         _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
    } catch (FileNotFoundException e) {
         _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
              // Keystore does not exist
    } catch (KeyStoreException e) {
         _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
    } catch (IOException e) {
         _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
    }catch(Exception e) {
         _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
    }finally {
        try {
            if(in!=null)
                in.close();
            if(out!=null)
                out.close();
        }catch(Exception ex){}
    }  */
    }
    
    private void copyCACertificates(String sourceTrustJksPath, String targetTrustJksPath) throws CertificateException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            KeyStore keystoreSource = KeyStore.getInstance(KeyStore.getDefaultType());
            KeyStore keystoreTarget = KeyStore.getInstance(KeyStore.getDefaultType());
            in = new FileInputStream(new File(sourceTrustJksPath));
            keystoreSource.load(in,jksCAkeyStorePassword.toCharArray());
            keystoreTarget.load(null, jksCAkeyStorePassword.toCharArray());
            java.util.Enumeration en = keystoreSource.aliases();
            for(; en.hasMoreElements(); ){
                String alias = (String) en.nextElement();
                java.security.cert.Certificate cert = keystoreSource.getCertificate(alias);
                keystoreTarget.setCertificateEntry(alias,cert);
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.ca_transferred",alias));
                
            }
            out = new FileOutputStream(targetTrustJksPath);
            keystoreTarget.store(out, jksCAkeyStorePassword.toCharArray());
        } catch (java.security.cert.CertificateException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (NoSuchAlgorithmException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (FileNotFoundException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
            // Keystore does not exist
        } catch (KeyStoreException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (IOException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        }catch(Exception e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        }finally {
            try {
                if(in!=null)
                    in.close();
                if(out!=null)
                    out.close();
            }catch(Exception ex){}
        }
    }
    
    private void doKeyPairBackup() throws CertificateException{
        copyKeyPairs(jksPath, jksPath+".back") ;
    }
    
    private void copyKeyPairs(String sourceJksPath, String targetJksPath) throws CertificateException{
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            KeyStore keystoreSource = KeyStore.getInstance(KeyStore.getDefaultType());
            KeyStore keystoreTarget = KeyStore.getInstance(KeyStore.getDefaultType());
            in = new FileInputStream(new File(sourceJksPath));
            keystoreSource.load(in,jksKeyStorePassword.toCharArray());
            keystoreTarget.load(null, jksKeyStorePassword.toCharArray());
            java.util.Enumeration en = keystoreSource.aliases();
            for(; en.hasMoreElements(); ){
                String alias = (String) en.nextElement();
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.processing_keypair",alias));
                Key key = keystoreSource.getKey(alias, jksKeyStorePassword.toCharArray());
                java.security.cert.Certificate[] cert = keystoreSource.getCertificateChain(alias);
                keystoreTarget.setKeyEntry(alias, key, jksKeyStorePassword.toCharArray(), cert);
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.alias_transferred",alias));
            }
            out = new FileOutputStream(targetJksPath);
            keystoreTarget.store(out, jksKeyStorePassword.toCharArray());
        } catch (java.security.cert.CertificateException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (NoSuchAlgorithmException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (FileNotFoundException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
            // Keystore does not exist
        } catch (KeyStoreException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        } catch (IOException e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        }catch(Exception e) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.unknownError"));
        }finally {
            try {
                if(in!=null)
                    in.close();
                if(out!=null)
                    out.close();
            }catch(Exception ex){}
        }
    }
    
    public void recovery(CommonInfoModel commonInfo) {
        File keypairKeyStoreOriginalFile = new File(jksPath);
        File keypairKeyStoreBackupFile = new File(jksPath+".back");
        File trustedKeyStoreOriginalFile = new File(trustJksPath);
        File trustedKeyStoreBackupFile = new File(trustJksPath+".back");
        new File(jksPath+".back1").delete();
        new File(trustJksPath+".back1").delete();
        boolean success = keypairKeyStoreOriginalFile.renameTo(new File(jksPath+".back1"));
        if(!success) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_recover_certificates"));
            return;
        }
        success = trustedKeyStoreOriginalFile.renameTo(new File(trustJksPath+".back1"));
        if(!success) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_recover_certificates"));
            return;
        }
        keypairKeyStoreOriginalFile.delete();
        trustedKeyStoreOriginalFile.delete();
        success = keypairKeyStoreBackupFile.renameTo(keypairKeyStoreOriginalFile);
        if(!success) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_recover_certificates"));
            File keypairKeyStoreanotherBackupFile =  new File(jksPath+".back1");
            File trustedKeyStoreanotherBackupFile = new File(trustJksPath+".back1");
            keypairKeyStoreanotherBackupFile.renameTo(keypairKeyStoreOriginalFile);
            trustedKeyStoreanotherBackupFile.renameTo(trustedKeyStoreOriginalFile);
            return;
        }
        success = trustedKeyStoreBackupFile.renameTo(trustedKeyStoreOriginalFile);
        if(!success) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_recover_certificates"));
            File keypairKeyStoreanotherBackupFile =  new File(jksPath+".back1");
            File trustedKeyStoreanotherBackupFile = new File(trustJksPath+".back1");
            keypairKeyStoreanotherBackupFile.renameTo(keypairKeyStoreOriginalFile);
            trustedKeyStoreanotherBackupFile.renameTo(trustedKeyStoreOriginalFile);
            return;
        }
        new File(jksPath+".back1").delete();
        new File(trustJksPath+".back1").delete();
    }
    
    
    public static void main(String[] args){
        CommonInfoModel commonInfo = new CommonInfoModel();
        commonInfo.setSourceInstallDir(args[0]);
        commonInfo.setTargetInstallDir(args[1]);
        commonInfo.setCertDbPassword(args[2]);
        commonInfo.setJksKeystorePassword(args[3]);
        JKStoJKSConversionModule convModule = new JKStoJKSConversionModule();
        convModule.upgrade(commonInfo);
    }
    
    public String getName() {
        return sm.getString("enterprise.tools.upgrade.certconversion.moduleName");
    }
    
}

