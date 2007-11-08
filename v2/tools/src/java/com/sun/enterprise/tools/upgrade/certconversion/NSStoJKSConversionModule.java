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
 * author : Gautam Borah
 *
 */

public class NSStoJKSConversionModule implements BaseModule{
    
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private StringManager sm;
    private static final String PKCS12_OUTPUTFILE_OPTION = "-o";
    private static final String NSS_DB_LOCATION_OPTION = "-d";
    private static final String ALIAS_OPTION = "-n";
    private static final String NSS_PWD_OPTION = "-K";
    private static final String KEYSTORE_PWD_OPTION = "-W";
    private static final String LIST_CERTIFICATE_OPTION = "-L";
    private static final String LIST_KEY_ID = "-K";
    private static final String CERT_NSS_PWD_OPTION = "-f";
    private static final String CERT_UTIL_UNIX = "certutil.sh";
    private static final String PK12_UTIL_UNIX = "pk12util.sh";
    private static final String CERT_UTIL_WIN = "certutil.bat";
    private static final String PK12_UTIL_WIN = "pk12util.bat";
    private static final String CONFIG = "config";
    private static final String BIN = "bin";
    private static final String LIB = "lib";
    private static final String UPGRADE = "upgrade";
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
    private KeyStore jksKeyStore;
    private KeyStore trustedJksKeyStore;
    private CommonInfoModel commonInfo;
    
    public NSStoJKSConversionModule(){
        sm = StringManager.getManager(LogService.UPGRADE_CERTCONVERSION_LOGGER);
    }
    
    public boolean upgrade(CommonInfoModel commonInfo){
        try {
            String currentDomain = commonInfo.getCurrentDomain();
            if(!(commonInfo.getDomainOptionList().contains(currentDomain)) || commonInfo.getCertDbPassword() == null)
                return true;
            this.jksPath=commonInfo.getTargetJKSKeyStorePath();
            this.trustJksPath = commonInfo.getTargetTrustedJKSKeyStorePath();
            this.jksKeyStorePassword=commonInfo.getJksKeystorePassword();
            this.jksCAkeyStorePassword=commonInfo.getJksCAKeystorePassword();
            this.pkcs12PathList=new ArrayList();
            this.keyList = new ArrayList();
            this.nssKeyStorePassword=commonInfo.getCertDbPassword();
            this.commonInfo = commonInfo;
            
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.start_certificate_migration",currentDomain));
            doBackup(commonInfo);
            listAllKeysFromSourceInstall();
            generatePKCS12Certificates();
            runPkcs12ToJks();
            deletePKCS12Files();
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.finished_certificate_migration",currentDomain));
        }catch(CertificateException ce) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",ce));
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            return false;
        }
        return true;
    }
    
    private void doBackup(CommonInfoModel commonInfo) {
        doCACertificateBackup();
        doKeyPairBackup();
    }
    
    private void doCACertificateBackup() {
        FileInputStream in = null;
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
        }
    }
    
    private void doKeyPairBackup() {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            KeyStore keystoreSource = KeyStore.getInstance(KeyStore.getDefaultType());
            KeyStore keystoreTarget = KeyStore.getInstance(KeyStore.getDefaultType());
            in = new FileInputStream(new File(jksPath));
            keystoreSource.load(in,jksKeyStorePassword.toCharArray());
            keystoreTarget.load(null, jksKeyStorePassword.toCharArray());
            java.util.Enumeration en = keystoreSource.aliases();
            for(; en.hasMoreElements(); ){
                String alias = (String) en.nextElement();
                Key key = keystoreSource.getKey(alias, jksKeyStorePassword.toCharArray());
                java.security.cert.Certificate[] cert = keystoreSource.getCertificateChain(alias);
                keystoreTarget.setKeyEntry(alias, key, jksKeyStorePassword.toCharArray(), cert);
            }
            out = new FileOutputStream(jksPath+".back");
            keystoreTarget.store(out, jksKeyStorePassword.toCharArray());
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
    
    private void listAllKeysFromSourceInstall() throws CertificateException{
        String osName = commonInfo.getOSName();
        String sourceDomainDir = commonInfo.getSourceInstancePath();
        String configDir =   sourceDomainDir + File.separator + CONFIG;
        String source70Lib = commonInfo.getSourceInstallDir() +File.separator + LIB;
        String source70Bin = commonInfo.getSourceInstallDir() +File.separator + BIN;
        String certutilLocation = commonInfo.getTargetInstallDir() +File.separator + LIB  +File.separator + UPGRADE;
        String certUtilPath = "";
        if(osName.indexOf("Windows") == -1)
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_UNIX;
        else
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_WIN;
        
        StringWriter  result = new StringWriter();
        String nssPwd = commonInfo.getCertDbPassword();
        //String nssPwdFile = commonInfo.getNSSPwdFile();
        String domainPath = commonInfo.getDestinationDomainPath();
        String nssPwdFile = domainPath +File.separator +"pwdfile";
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new FileOutputStream(nssPwdFile ));
            pw.println(nssPwd);
            pw.flush();
            pw.close();
        }catch(FileNotFoundException fe) {
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.unknownError "),fe);
        }finally {
            try {
                if(pw !=null)
                    pw.close();
            }catch(Exception e){}
        }
        String commandString =   certUtilPath + " " +
                LIST_KEY_ID  + " " + NSS_DB_LOCATION_OPTION +
                " " + configDir + " " + CERT_NSS_PWD_OPTION +
                " " + nssPwdFile +
                " " +source70Lib +
                " " +source70Bin +
                " " +certutilLocation;
        int exitVal = ProcessAdaptor.executeProcess(commandString, result);
        result.flush();
        String resultString =  result.toString();
        if(exitVal == 0) {
            parseAndGetKeys(resultString);
        }else {
            _logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.certconversion.error_executing_certutil",resultString));
        throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.error_executing_certutil",resultString));
        }
        
    }
    
    private void parseAndGetKeys(String input) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(input));
            //Reading the Line <0> KEY
            String readString =reader.readLine();
            while(readString != null) {
                //Key starts from 4th Index
                String marker = readString.substring(0,1);
                String anotherMarker = readString.substring(2,3);
                if(!(marker.equals("<") && anotherMarker.equals(">"))) {
               _logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.certconversion.error_executing_certutil",input));
                    return;
                }
                String alias = readString.substring(4);
                keyList.add(alias);
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.alias_info",commonInfo.getCurrentDomain(), alias));            readString =reader.readLine();
            }
        }catch (Exception e) {
            _logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.certconversion.unknownError"),e);
        }
    }
    
    private void  generatePKCS12Certificates() throws CertificateException{
        String osName = commonInfo.getOSName();
        int size = keyList.size();
        for (int i =0;i<size;i++) {
            String pkcsFile = removeWhiteSpace("" + keyList.get(i));
            String pkcsFilePath = commonInfo.getDestinationDomainPath() + File.separator +pkcsFile +".pkcs12";
            String sourceDomainDir = commonInfo.getSourceInstancePath();
            String configDir =   sourceDomainDir + File.separator + CONFIG;
            String source70Lib = commonInfo.getSourceInstallDir() +File.separator + LIB;
            String source70Bin = commonInfo.getSourceInstallDir() +File.separator + BIN;
            String pk12UtilPath = "";
            if(osName.indexOf("Windows") == -1)
                pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_UNIX;
            else
                pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_WIN;
            String pk12utilLocation =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + UPGRADE;
            /*String commandString = pk12UtilPath + " " +
            PKCS12_OUTPUTFILE_OPTION  + " " + pkcsFilePath + " " +
            NSS_DB_LOCATION_OPTION + " " + configDir + " " +
            ALIAS_OPTION + " " + keyList.get(i) + " " +
            NSS_PWD_OPTION + " " + commonInfo.getCertDbPassword() +" " +
            KEYSTORE_PWD_OPTION + " " + commonInfo.getCertDbPassword(); */
            String[] commandArray = {pk12UtilPath,
                    source70Lib,
                    source70Bin,
                    pk12utilLocation,
                    PKCS12_OUTPUTFILE_OPTION, pkcsFilePath,
                    NSS_DB_LOCATION_OPTION, configDir,
                    ALIAS_OPTION, ""+ keyList.get(i) + "",
                    NSS_PWD_OPTION, commonInfo.getCertDbPassword(),
                    KEYSTORE_PWD_OPTION, commonInfo.getCertDbPassword()
            };
            StringWriter  result = new StringWriter();
            //int exitVal = ProcessAdaptor.executeProcess(commandString, result);
            int exitVal = ProcessAdaptor.executeProcess(commandArray, result);
            result.flush();
            //If process execution is successful add pkcs12file to  pkcs12PathList
            if(exitVal == 0)
                pkcs12PathList.add(pkcsFilePath);
            else {
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.certificateError", keyList.get(i),commonInfo.getCurrentDomain(),result.toString()));
                throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificateError", keyList.get(i),commonInfo.getCurrentDomain(),result.toString()));
            }
            
        }
    }
    
    private String removeWhiteSpace(String str) {
        
        String concat="";
        StringTokenizer st = new StringTokenizer(str);
        while(st.hasMoreTokens()) {
            concat=concat+st.nextToken();
        }
        return concat;
    }
    private void runPkcs12ToJks() throws CertificateException{
        // to convert from pkcs12 to jks
        openOutputKeystoreJKS();
        openOutputTrustedKeystoreJKS();
        openAllInputKeystorePKCS12();
        if(!certificateError)
            storeJksKeyStore();
        if(!certificateError)
            storeJksTrustedKeyStore();
    }
    
    private void storeJksKeyStore() throws CertificateException{
        try{
            osJksPath = new FileOutputStream(jksPath);
            jksKeyStore.store(osJksPath, jksKeyStorePassword.toCharArray());
        }catch(Exception ex){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"),ex);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"));
        }finally{
            if(osJksPath!=null)
                try{osJksPath.close();}catch(Exception e){}
        }
    }
    
    private void storeJksTrustedKeyStore() throws CertificateException{
        try{
            trustJKSPathStream = new FileOutputStream(trustJksPath);
            trustedJksKeyStore.store(trustJKSPathStream, jksCAkeyStorePassword.toCharArray());
        }catch(Exception ex){
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"),ex);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificate_JKS_Error"));
        }finally{
            if(osJksPath!=null)
                try{osJksPath.close();}catch(Exception e){}
        }
    }
    
    public void openOutputKeystoreJKS() throws CertificateException{
        InputStream inputStreamJks = null;
        try{
            inputStreamJks = new FileInputStream(jksPath);
            jksKeyStore = KeyStore.getInstance("JKS");
            jksKeyStore.load(inputStreamJks, jksKeyStorePassword.toCharArray());
        }catch(Exception e){
            certificateError = true;
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }finally{
            if(inputStreamJks!=null)
                try{inputStreamJks.close();}catch(Exception e){}
        }
    }
    
    public void openOutputTrustedKeystoreJKS() throws CertificateException{
        InputStream inputStreamJks = null;
        try{
            inputStreamJks = new FileInputStream(trustJksPath);
            trustedJksKeyStore = KeyStore.getInstance("JKS");
            trustedJksKeyStore.load(inputStreamJks, jksCAkeyStorePassword.toCharArray());
        }catch(Exception e){
            certificateError = true;
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }finally{
            if(inputStreamJks!=null)
                try{inputStreamJks.close();}catch(Exception e){}
        }
    }
    
    private void openAllInputKeystorePKCS12() throws CertificateException{
        int size = pkcs12PathList.size();
        for(int i=0; i<size; i++){
            openInputKeystorePKCS12((String)pkcs12PathList.get(i), nssKeyStorePassword);
        }
    }
    
    public void openInputKeystorePKCS12(String pkcs12Path, String pkcs12Pwd) throws CertificateException{
        FileInputStream fis = null;
        KeyStore pkcs12KeyStore = null;
        try{
            fis = new FileInputStream(pkcs12Path);
            pkcs12KeyStore = KeyStore.getInstance("PKCS12");
            pkcs12KeyStore.load(fis, pkcs12Pwd.toCharArray());
            java.util.Enumeration en = pkcs12KeyStore.aliases();
            for(; en.hasMoreElements(); ){
                String alias = (String) en.nextElement();
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.processing_keypair",alias));
                Key key = pkcs12KeyStore.getKey(alias, pkcs12Pwd.toCharArray());
                java.security.cert.Certificate[] cert = pkcs12KeyStore.getCertificateChain(alias);
                List caList = new ArrayList();
                int i =0;
                if(cert.length > 1) {
                    i=1;
                    for(;i<cert.length;i++) {
                        if(cert[i] instanceof X509Certificate) {
                            if(!certificateError)
                                writeToOutputTrustedKeystore(cert[i],alias + "ca" + i);
                                    /*X509Certificate x509cert = (X509Certificate)cert[i];
                                    Principal principal = x509cert.getIssuerDN();
                    caList.add(principal.getName()+"\n");*/
                        }
                    }
                }
                if(cert.length == 1) {
                    X509Certificate x509cert = (X509Certificate)cert[0];
                    Principal issuerPrincipal = x509cert.getIssuerDN();
                    Principal subjectPrincipal = x509cert.getSubjectDN();
                    String subject = subjectPrincipal.getName();
                    String issuer = issuerPrincipal.getName();
                    if(subject.equals(issuer) && (!certificateError))
                        writeToOutputTrustedKeystore(cert[i],alias);
                    else
                        _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.ca_list",issuer));
                }
                //_logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.ca_list",caList));
                if(!certificateError)
                    writeToOutputKeystore(key, cert, alias);
            }
        }catch(Exception e){
            certificateError = true;
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.certificate_PKCS12_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificate_PKCS12_Error"));
        }finally{
            if(fis!=null)
                try{fis.close();}catch(Exception e){}
        }
    }
    
    public void writeToOutputKeystore(Key key, java.security.cert.Certificate[] cert, String alias) throws CertificateException{
        try{
            jksKeyStore.setKeyEntry(alias, key, jksKeyStorePassword.toCharArray(), cert);
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.alias_transferred",alias));
        } catch(Exception e){
            certificateError = true;
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }
    }
    
    public void writeToOutputTrustedKeystore(java.security.cert.Certificate cert, String alias) throws CertificateException{
        try{
            trustedJksKeyStore.setCertificateEntry(alias, cert);
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.ca_transferred",alias));
        } catch(Exception e){
            certificateError = true;
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"),e);
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.JKS_Password_Error"));
        }
    }
    
    private void deletePKCS12Files() {
        String pkcsFilesPath = commonInfo.getDestinationDomainPath();
        String[] fileList = new File(pkcsFilesPath).list();
        for(int i=0; i<fileList.length; i++){
            File pkcsFile = new File(pkcsFilesPath+File.separator+fileList[i]);
            if(pkcsFile.isFile() && fileList[i].endsWith(".pkcs12") ){
                pkcsFile.delete();
            }
        }
        String domainPath = commonInfo.getDestinationDomainPath();
        String nssPwdFile = domainPath +File.separator +"pwdfile";
        File pwdfile = new File(nssPwdFile);
        pwdfile.delete();
    }
    
    public static void main(String[] args){
        CommonInfoModel commonInfo = new CommonInfoModel();
        commonInfo.setSourceInstallDir(args[0]);
        commonInfo.setTargetInstallDir(args[1]);
        commonInfo.setCertDbPassword(args[2]);
        commonInfo.setJksKeystorePassword(args[3]);
        NSStoJKSConversionModule convModule = new NSStoJKSConversionModule();
        convModule.upgrade(commonInfo);
    }
    
    public String getName() {
        return sm.getString("enterprise.tools.upgrade.certconversion.moduleName");
    }
    
}

