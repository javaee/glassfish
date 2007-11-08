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

import com.sun.enterprise.tools.upgrade.certconversion.ProcessAdaptor;

import java.io.*;
import java.security.KeyStore;
import java.util.logging.Level;

/**
 *
 * author : Servesh Singh
 *
 */

public class PasswordVerifier {
    private static final String NSS_DB_LOCATION_OPTION = "-d";
    private static final String LIST_KEY_ID = "-K";
    private static final String CERT_NSS_PWD_OPTION = "-f";
    private static final String CERT_UTIL_UNIX = "certutil.sh";
    private static final String CERT_UTIL_WIN = "certutil.bat";
    private static final String CONFIG = "config";
    private static final String BIN = "bin";
    private static final String LIB = "lib";
    private static final String UPGRADE = "upgrade";
    
    public static boolean verifySourceNSSPassword(CommonInfoModel commonInfo,String configDir) {
        if(commonInfo.getSourceDomainRootFlag()&& (new File(commonInfo.getSourceDomainRoot()).equals(new File(commonInfo.getTargetDomainRoot()))))
            return true;
        if(!(new File(configDir).exists()))
            return true;
        String osName = commonInfo.getOSName();
        String libDir = "";
        String binDir = "";
        String certutilLocation = "";
        if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)){
            libDir = commonInfo.getSourceInstallDir() +File.separator + LIB;
            binDir = commonInfo.getSourceInstallDir() +File.separator + BIN;
            //certutil/pk12util should be picked from target installation
            certutilLocation = commonInfo.getTargetInstallDir() +File.separator + LIB + File.separator + UPGRADE;
        } else {
            libDir = commonInfo.getTargetInstallDir() +File.separator + LIB;
            binDir = commonInfo.getTargetInstallDir() +File.separator + BIN;
            certutilLocation = commonInfo.getTargetInstallDir() +File.separator + LIB;
        }
        String certUtilPath = "";
        if(osName.indexOf("Windows") == -1)
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_UNIX;
        else
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_WIN;
        
        StringWriter  result = new StringWriter();
        String nssPwd = commonInfo.getCertDbPassword();
        String domainRoot = commonInfo.getTargetDomainRoot();
        //String domainPath = commonInfo.getDestinationDomainPath();
        String nssPwdFile = domainRoot +File.separator +"pwdfile";
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new FileOutputStream(nssPwdFile ));
            pw.println(nssPwd);
            pw.flush();
            pw.close();
        }catch(FileNotFoundException fe) {
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
                " " +libDir +
                " " +binDir +
                " " +certutilLocation;
        int exitVal = ProcessAdaptor.executeProcess(commandString, result);
        result.flush();
        String resultString =  result.toString();
        File pwdfile = new File(nssPwdFile);
        pwdfile.delete();
        if(exitVal == 0) {
            return parseAndVerify(resultString);
        } else if(exitVal == 255) { //no keys found
            return true;
        } else {
            return false;
        }
        
    }
    
    public static boolean verifyTargetNSSPassword(CommonInfoModel commonInfo,String configDir) {
        if(commonInfo.getSourceDomainRootFlag()&& (new File(commonInfo.getSourceDomainRoot()).equals(new File(commonInfo.getTargetDomainRoot()))))
            return true;
        if(!(new File(configDir).exists()))
            return true;
        String osName = commonInfo.getOSName();
        String libDir = "";
        String binDir = "";
        libDir = commonInfo.getTargetInstallDir() +File.separator + LIB;
        binDir = commonInfo.getTargetInstallDir() +File.separator + BIN;
        String certutilLocation = libDir;
        String certUtilPath = "";
        if(osName.indexOf("Windows") == -1)
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_UNIX;
        else
            certUtilPath =  commonInfo.getTargetInstallDir() + File.separator + LIB + File.separator + CERT_UTIL_WIN;
        
        StringWriter  result = new StringWriter();
        String nssPwd = commonInfo.getTargetCertDbPassword();
        String domainRoot = commonInfo.getTargetDomainRoot();
        //String domainPath = commonInfo.getDestinationDomainPath();
        String nssPwdFile = domainRoot +File.separator +"pwdfile";
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new FileOutputStream(nssPwdFile ));
            pw.println(nssPwd);
            pw.flush();
            pw.close();
        }catch(FileNotFoundException fe) {
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
                " " +libDir +
                " " +binDir +
                " " +certutilLocation;
        int exitVal = ProcessAdaptor.executeProcess(commandString, result);
        result.flush();
        String resultString =  result.toString();
        File pwdfile = new File(nssPwdFile);
        pwdfile.delete();
        if(exitVal == 0) {
            return parseAndVerify(resultString);
        }else {
            return false;
        }
        
    }
    
    public static boolean verifySourceNSSPassword(CommonInfoModel commonInfo) {
        if(commonInfo.getSourceDomainRootFlag()&& (new File(commonInfo.getSourceDomainRoot()).equals(new File(commonInfo.getTargetDomainRoot()))))
            return true;
        /*File sourceDomain = new File(commonInfo.getSourceDomainPath());
        String [] dirs = sourceDomain.list();
        if(dirs.length == 1) {
            //_logger.log(Level.WARNING, sm.getString("enterprise.tools.upgrade.no_server_instance", sourceDomain));
            //continue;
            return false;
        }
        String instanceName ="";
        if(dirs[0].equals("admin-server"))
            instanceName = dirs[1];
        else
            instanceName = dirs[0];
        commonInfo.setCurrentSourceInstance(instanceName);
         */
        String instanceName = commonInfo.getCurrentSourceInstance();
        String certificateDomainDir = "";
        //if((commonInfo.getSourceVersionAndEdition().equals(UpgradeConstants.VERSION_AS80_PE) ||
        //        commonInfo.getSourceVersionAndEdition().equals(UpgradeConstants.VERSION_AS81_PE) ||
        //        commonInfo.getSourceVersionAndEdition().equals(UpgradeConstants.VERSION_AS90_PE))&&
        //        (commonInfo.getTargetVersionAndEdition().equals(UpgradeConstants.VERSION_AS90_SE) ||
        //        commonInfo.getTargetVersionAndEdition().equals(UpgradeConstants.VERSION_AS90_EE) ||
                //Added for CR 6468082
        //        commonInfo.getTargetVersionAndEdition().equals(UpgradeConstants.VERSION_AS91_EE)))
        if(UpgradeConstants.EDITION_EE.equals(commonInfo.getSourceEdition()))	
            certificateDomainDir = commonInfo.getDestinationDomainPath();
        else
            certificateDomainDir = commonInfo.getSourceInstancePath();
        String configDir =   certificateDomainDir + File.separator + CONFIG;
        return verifySourceNSSPassword(commonInfo,configDir);
    }
    
    private static boolean parseAndVerify(String input) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(input));
            //Reading the Line <0> KEY
            String readString =reader.readLine();
            while(readString != null) {
                //Key starts from 4th Index
                String marker = readString.substring(0,1);
                String anotherMarker = readString.substring(2,3);
                if(!(marker.equals("<") && anotherMarker.equals(">"))) {
                    return false;
                }
                readString =reader.readLine();
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public static boolean verifyKeystorePassword(String jksPath, String jksKeyStorePassword ){
        //if(commonInfo.getSourceDomainRootFlag()&& (new File(commonInfo.getSourceDomainRoot()).getPath().equals(new File(commonInfo.getTargetDomainRoot()).getPath())))
        //return true;
        File jksfile = new File(jksPath);
        if(!jksfile.exists())    {
            if(jksKeyStorePassword.equals("changeit"))
                return true;
            else
                return false;
        }
        InputStream inputStreamJks = null;
        KeyStore jksKeyStore;
        try{
            inputStreamJks = new FileInputStream(jksPath);
            jksKeyStore = KeyStore.getInstance("JKS");
            jksKeyStore.load(inputStreamJks, jksKeyStorePassword.toCharArray());
        }catch(Exception e){
            return false;
        }finally{
            if(inputStreamJks!=null)
                try{inputStreamJks.close();}catch(Exception e){}
        }
        return true;
    }
}
