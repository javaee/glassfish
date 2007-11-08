/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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

public class JKStoNSSConversionModule implements BaseModule{
    
    
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private StringManager sm;
    private static final String PKCS12_INPUTFILE_OPTION = "-i";
    private static final String NSS_DB_LOCATION_OPTION = "-d";
    private static final String NSS_PWD_OPTION = "-K";
    private static final String KEYSTORE_PWD_OPTION = "-W";
    private static final String PK12_UTIL_UNIX = "pk12util.sh";
    private static final String PK12_UTIL_WIN = "pk12util.bat";
    private static final String CONFIG = "config";
    private static final String BIN = "bin";
    private static final String LIB = "lib";
    
    String pathOfNSSDbFiles;
    private String jksPath;
    private String trustJksPath;
    private List pkcs12PathList;
    private String nssKeyStorePassword;
    private List keyList;
    private InputStream isJksPath;
    private OutputStream osJksPath;
    private OutputStream trustJKSPathStream;
    private String jksKeyStorePassword;
    private String jksCAkeyStorePassword;
    private boolean certificateError = false;
    private char[] pwd;
    private KeyStore trustedJksKeyStore;
    private CommonInfoModel commonInfo;
    private String pkcs12FilePath;
    private OutputStream pkcs12KeystoreStream;
    private KeyStore jksKeyStore;
    private KeyStore pkcs12KeyStore;
    
    
    public JKStoNSSConversionModule(){
        sm = StringManager.getManager(LogService.UPGRADE_CERTCONVERSION_LOGGER);
    }
    
    public void recovery(CommonInfoModel commonInfo) {
        
    }
    public boolean upgrade(CommonInfoModel commonInfo){
        String currentDomain = commonInfo.getCurrentDomain();
        if(!(commonInfo.getDomainOptionList().contains(currentDomain)))
            return true;
        pkcs12FilePath = commonInfo.getDestinationDomainPath() + File.separator +"pkcsFile" +".pkcs12";
        this.jksPath=commonInfo.getSourceJKSKeyStorePath();
        this.trustJksPath=commonInfo.getSourceTrustedJKSKeyStorePath();
        this.jksKeyStorePassword=commonInfo.getJksKeystorePassword();
        this.jksCAkeyStorePassword=commonInfo.getJksCAKeystorePassword();
        this.pkcs12PathList=new ArrayList();
        this.keyList = new ArrayList();
        this.nssKeyStorePassword=commonInfo.getCertDbPassword();
        this.commonInfo = commonInfo;
        try {
            runPkcs12ToJks();
        }catch(CertificateException ce) {
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",ce));
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            return false;
        }
        return true;
    }
    
    
    
    private void runPkcs12ToJks() throws CertificateException{
        openInputKeystoreJKS();
        openOutputKeystore();
        convertjksTopkcs12();
        storepkcs12KeyStore();
        migratepkcs12TonssDB();
        //deletePKCS12Files();
        
    }

    private void migratepkcs12TonssDB() throws CertificateException {
    // START CR 6409992  
    // Check if the keyList is of zero length, if yes do not 
    // invoke ps12util
    if( keyList.isEmpty() ) return;
    // END CR 6409992
    String targetDomainDir = commonInfo.getDestinationDomainPath();
    String configDir =   targetDomainDir + File.separator + CONFIG;
    String targetLib = commonInfo.getTargetInstallDir() +File.separator + LIB;
    String targetBin = commonInfo.getTargetInstallDir() +File.separator + BIN;
	String pk12UtilPath = "";
    String osName = commonInfo.getOSName();
    if(osName.indexOf("Windows") == -1)
        pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_UNIX;
    else
        pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_WIN;
    String pk12utilLocation =  commonInfo.getTargetInstallDir() + File.separator + LIB;
    String[] commandArray = {pk12UtilPath,
                             targetLib,
                             targetBin,
                             pk12utilLocation,
                             PKCS12_INPUTFILE_OPTION, pkcs12FilePath,
                             NSS_DB_LOCATION_OPTION, configDir,
                             NSS_PWD_OPTION, commonInfo.getCertDbPassword(),
                             KEYSTORE_PWD_OPTION, commonInfo.getJksKeystorePassword()
                             }; 
    StringWriter  result = new StringWriter();
    int exitVal = ProcessAdaptor.executeProcess(commandArray, result);
    result.flush();
    if(exitVal == 0) {
        Iterator itr = keyList.iterator();
        while(itr.hasNext())
        _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.alias_transferred",(String)itr.next()));
    } else {
        _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.certificateError", pkcs12FilePath,commonInfo.getCurrentDomain(),result.toString()));
        throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificateError", pkcs12FilePath,commonInfo.getCurrentDomain(),result.toString()));
    }
    }
    
    private void storepkcs12KeyStore()throws CertificateException{
        try{
            pkcs12KeystoreStream = new FileOutputStream(pkcs12FilePath);
            pkcs12KeyStore.store(pkcs12KeystoreStream, jksKeyStorePassword.toCharArray());
            Enumeration aliases = pkcs12KeyStore.aliases();
            while(aliases.hasMoreElements()){
                String alias = (String)aliases.nextElement();
                _logger.log(Level.INFO, "Added keystore alias: " + alias);
            }
        }catch(Exception ex){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"),ex);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"));
        }finally{
            if(pkcs12KeystoreStream!=null)
                try{pkcs12KeystoreStream.close();}catch(Exception e){}
        }
    }
    
    
    public void openInputKeystoreJKS() throws CertificateException{
        InputStream inputStreamJks = null;
        try{
            inputStreamJks = new FileInputStream(jksPath);
            jksKeyStore = KeyStore.getInstance("JKS");
            jksKeyStore.load(inputStreamJks, jksKeyStorePassword.toCharArray());
        }catch(Exception e){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }finally{
            if(inputStreamJks!=null)
                try{inputStreamJks.close();}catch(Exception e){}
        }
    }
    
    public void openOutputKeystore()throws CertificateException{
        try{
            pkcs12KeyStore = KeyStore.getInstance("PKCS12");
            pkcs12KeyStore.load(null, jksKeyStorePassword.toCharArray());
        }catch(Exception e){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }finally{
        }
    }
    
    public void writeToOutputKeystore(Key key, java.security.cert.Certificate[] cert, String alias) throws CertificateException{
        try{
            pkcs12KeyStore.setKeyEntry(alias, key, jksKeyStorePassword.toCharArray(), cert);
        } catch(Exception e){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }
    }
    public void writeToOutputTrustedKeystore(java.security.cert.Certificate cert, String alias) throws CertificateException{
        try{
            pkcs12KeyStore.setCertificateEntry(alias, cert);
        } catch(Exception e){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }
    }
    public void convertjksTopkcs12() throws CertificateException{
        try{
            java.util.Enumeration en = jksKeyStore.aliases();
            int i=0;
            for(; en.hasMoreElements(); ){
                String alias = (String) en.nextElement();
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.processing_keypair",alias));
         	Key key = jksKeyStore.getKey(alias, jksKeyStorePassword.toCharArray());
                // START CR 6409992 
                /* Fix for 6409992. We are not transferring the default key cert pair
                 * with the alias  "s1as" 
                 */
                if( !"s1as".equals(alias) )  {
                    keyList.add(alias);
                    java.security.cert.Certificate[] cert = jksKeyStore.getCertificateChain(alias);
                    writeToOutputKeystore(key, cert, alias);
                    // java.security.cert.Certificate cert1 = jksKeyStore.getCertificate(alias);
                    //writeToOutputTrustedKeystore(cert1,alias);
                 }
                // END CR 6409992 
                i++;
            }
        }catch(CertificateException e){
            throw e;
        }catch(Exception e){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.processing_keypair",e));
            throw new CertificateException(e.getMessage());
        }finally{
        }
    }
    
    private void deletePKCS12Files() {
        new File(pkcs12FilePath).delete();
    }
    
    
    
    public static void main(String[] args){
        new JKStoNSSConversionModule();
        //convModule.upgrade(args);
    }
    
    public String getName() {
        return sm.getString("enterprise.tools.upgrade.certconversion.moduleName");
    }
    
}

