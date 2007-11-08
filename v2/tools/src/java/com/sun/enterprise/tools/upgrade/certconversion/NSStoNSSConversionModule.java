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

public class NSStoNSSConversionModule implements BaseModule{
    
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    private StringManager sm;
    private static final String PKCS12_OUTPUTFILE_OPTION = "-o";
    private static final String PKCS12_INPUTFILE_OPTION = "-i";
    private static final String NSS_DB_LOCATION_OPTION = "-d";
    private static final String ALIAS_OPTION = "-n";
    private static final String NSS_PWD_OPTION = "-K";
    private static final String NSS_DB_PREFIX = "-P";
    private static final String KEYSTORE_PWD_OPTION = "-W";
    private static final String LIST_KEY_ID = "-K";
    private static final String CREATE_NSS_DB = "-N";
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
    private List pkcs12PathList;
    private List keyList;
    private CommonInfoModel commonInfo;
    
    public NSStoNSSConversionModule(){
        sm = StringManager.getManager(LogService.UPGRADE_CERTCONVERSION_LOGGER);
    }
    
    public boolean upgrade(CommonInfoModel commonInfo){
        try {
            String currentDomain = commonInfo.getCurrentDomain();
            String currentInstance = currentDomain + ":" + commonInfo.getCurrentSourceInstance();
            if(!(commonInfo.getDomainOptionList().contains(currentDomain)) || commonInfo.getCertDbPassword() == null)
                return true;
            
            this.pkcs12PathList=new ArrayList();
            this.keyList = new ArrayList();
            this.commonInfo = commonInfo;
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.start_certificate_migration",currentInstance));
            doBackup(commonInfo);
            listAllKeysFromSourceInstall();
            generatePKCS12Certificates();
            //runPkcs12ToJks();
            migratePkcs12ToNss("");
            deletePKCS12Files();
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.finished_certificate_migration",currentInstance));
        }catch(CertificateException ce) {
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.certconversion.could_not_migrate_certificates",ce));
            UpdateProgressManager.getProgressManager().setContinueUpgrade(false);
            return false;
        }
        return true;
    }
    
    private void doBackup(CommonInfoModel commonInfo) throws CertificateException{
        // Need to take the backup for target 8.xse certificates
        //doCACertificateBackup();
        //doKeyPairBackup();
    }
    
    private void doCACertificateBackup() {
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
    }    */
    }
    
    private void doKeyPairBackup() {
    /*FileInputStream in = null;
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
    }      */
    }
    
    
    public void recovery(CommonInfoModel commonInfo) {
    /*File keypairKeyStoreOriginalFile = new File(jksPath);
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
    new File(trustJksPath+".back1").delete();    */
    }
    
    private void listAllKeysFromSourceInstall() throws CertificateException{
        String osName = commonInfo.getOSName();
        String sourceDomainDir = commonInfo.getSourceInstancePath();
        String configDir =   sourceDomainDir + File.separator + CONFIG;
        String source70Lib = commonInfo.getSourceInstallDir() +File.separator + LIB;
        String source70Bin = commonInfo.getSourceInstallDir() +File.separator + BIN;
        String certUtilPath = "";
        String certutilLocation = commonInfo.getTargetInstallDir() +File.separator + LIB  +File.separator + UPGRADE;
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
    
    private void migratePkcs12ToNss(String certPrefix) throws CertificateException{
        String osName = commonInfo.getOSName();
        int size = keyList.size();
        //for (int i =0;i<size;i++) {
        //String pkcsFile = removeWhiteSpace("" + keyList.get(i));
        String configDir =   commonInfo.getTargetConfig();//sourceDomainDir + File.separator + CONFIG;
        String source70Lib = commonInfo.getTargetInstallDir() +File.separator + LIB;
        String source70Bin = commonInfo.getTargetInstallDir() +File.separator + BIN;
        String pk12UtilPath = "";
        if(osName.indexOf("Windows") == -1)
            pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_UNIX;
        else
            pk12UtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + PK12_UTIL_WIN;
        //initializeNSSDB(certPrefix);
        String pk12utilLocation =  commonInfo.getTargetInstallDir() + File.separator + LIB;
        Iterator itr = pkcs12PathList.iterator();
        while(itr.hasNext()) {
            String alias = (String)itr.next();
            _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.processing_keypair",alias));
            String[] commandArray = {pk12UtilPath,
                    source70Lib,
                    source70Bin,
                    pk12utilLocation,
                    PKCS12_INPUTFILE_OPTION, alias,
                    NSS_DB_LOCATION_OPTION, configDir,
                    NSS_PWD_OPTION, commonInfo.getTargetCertDbPassword(),
                    KEYSTORE_PWD_OPTION, commonInfo.getCertDbPassword()
            };
            
            StringWriter  result = new StringWriter();
            int exitVal = ProcessAdaptor.executeProcess(commandArray, result);
            result.flush();
            if(exitVal == 0) {
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.alias_transferred",alias));
            } else {
                _logger.log(Level.INFO,sm.getString("enterprise.tools.upgrade.certconversion.certificateError", alias,commonInfo.getCurrentDomain(),result.toString()));
                throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.certificateError", alias,commonInfo.getCurrentDomain(),result.toString()));
            }
        }
        
        //}      //end of for
    }
    
    private void  initializeNSSDB(String certPrefix) throws CertificateException {
        String osName = commonInfo.getOSName();
        String sourceDomainDir = commonInfo.getSourceInstancePath();
        String configDir =   commonInfo.getTargetConfig();
        String source70Lib = commonInfo.getSourceInstallDir() +File.separator + LIB;
        String source70Bin = commonInfo.getSourceInstallDir() +File.separator + BIN;
        String certutilLocation = commonInfo.getTargetInstallDir() +File.separator + LIB;
        String certUtilPath = "";
        if(osName.indexOf("Windows") == -1)
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_UNIX;
        else
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_WIN;
        File key3db = new File(configDir+File.separator+certPrefix+"key3.db");
        //If DB is already there, don't do anything
        if(key3db.exists())
            return;
        StringWriter  result = new StringWriter();
        String nssPwd = commonInfo.getTargetCertDbPassword();
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
                CREATE_NSS_DB  + " " +NSS_DB_LOCATION_OPTION +
                " " + configDir + " " +NSS_DB_PREFIX +
                " "+ certPrefix + " " +CERT_NSS_PWD_OPTION +
                " " + nssPwdFile +
                " " +source70Lib +
                " " +source70Bin +
                " " +certutilLocation;
        int exitVal = ProcessAdaptor.executeProcess(commandString, result);
        result.flush();
        String resultString =  result.toString();
        if(exitVal == 0) {
            //parseAndGetKeys(resultString);
        }else {
            _logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.certconversion.error_executing_certutil",resultString));
            throw new CertificateException(sm.getString("enterprise.tools.upgrade.certconversion.error_executing_certutil",resultString));
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

