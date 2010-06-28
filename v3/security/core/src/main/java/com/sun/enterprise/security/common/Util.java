/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.security.common;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.security.auth.callback.CallbackHandler;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.embedded.Server;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author venu
 * TODO: need to change this class, it needs to be similar to SecurityServicesUtil
 */
@Service
@Scoped(Singleton.class)
public class Util {
    //private static Habitat habitat = Globals.getDefaultHabitat();
    @Inject
    private static Habitat habitat;
    @Inject 
    private ProcessEnvironment penv;

    @Inject
    private static SecurityService securityService;
   
    //stuff required for AppClient
    private CallbackHandler callbackHandler;
    private Object appClientMsgSecConfigs;
    
    //Note: Will return Non-Null only after Util has been 
    //Injected in some Service.
    public static Habitat getDefaultHabitat() {
        return habitat;
    }
    
    public static Util getInstance() {
        // return my singleton service
        return habitat.getComponent(Util.class);
    }
    
    public boolean isACC() {
        return penv.getProcessType().equals(ProcessType.ACC);
    }
    public boolean isServer() {
        return penv.getProcessType().isServer();
    }
    public boolean isNotServerORACC() {
        return penv.getProcessType().equals(ProcessType.Other);
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public Object getAppClientMsgSecConfigs() {
        return appClientMsgSecConfigs;
    }

    public void setAppClientMsgSecConfigs(Object appClientMsgSecConfigs) {
        this.appClientMsgSecConfigs = appClientMsgSecConfigs;
    }
    
    public static boolean isEmbeddedServer() {
        List<String> servers = Server.getServerNames();
        if (!servers.isEmpty()) {
            return true;
        }
        return false;
    }
    
    public static File writeConfigFileToTempDir(String fileName) throws IOException {
        File filePath = new File(fileName);

        if (filePath.exists()) {
            //the string provided is a filepath, so return
            return filePath;
        }
        File localFile = null;
        //Parent directories until the fileName exist, so create the file that has been provided
        if (filePath.getParentFile() != null && filePath.getParentFile().exists()) {
            localFile = filePath;
            localFile.createNewFile();

        } else {
            /*
             * File parent directory does not exist - so create parent directory as user.home/.glassfish-{embedded}/config
             * */
            String userHome = System.getProperty("user.home");

            String embeddedServerName = getCurrentEmbeddedServerName();
            File tempDir = new File(userHome + File.separator + ".glassfishv3-"+embeddedServerName+File.separator + "config");
            boolean mkDirSuccess = true;
            if (!tempDir.exists()) {
                mkDirSuccess = tempDir.mkdirs();
            }

            localFile = new File(tempDir.getAbsolutePath()+File.separator + fileName);


            if (mkDirSuccess && !localFile.exists()) {
                localFile.createNewFile();
            }
        }
        FileOutputStream oStream = new FileOutputStream(localFile);
        InputStream iStream = Util.class.getResourceAsStream("/config/" + fileName);

        while (iStream != null && iStream.available() > 0) {
            oStream.write(iStream.read());
        }

        oStream.close();
        if  (iStream != null) {
            iStream.close();
        }
        return localFile;

    }

    public static String getCurrentEmbeddedServerName() {
        List<String> embeddedServerNames = Server.getServerNames();
        String embeddedServerName = (embeddedServerNames.get(0) == null) ? "embedded" : embeddedServerNames.get(0);
        return embeddedServerName;

    }

    public static void copyConfigFiles(String fromInstanceDir, String toInstanceDir)throws IOException{
        //For security reasons, permit only an embedded server instance to carry out the copy operations
        if(!isEmbeddedServer()) {
            return;
        }

        if((fromInstanceDir == null) || (toInstanceDir == null)) {
            throw new IllegalArgumentException("Null inputs");
        }
        
        File fileFromInstanceDir = new File(fromInstanceDir);
        File fileToInstanceDir =  new File(toInstanceDir);

        List<String> fileNames = new ArrayList<String>();


        //Add FileRealm keyfiles to the list

        //This is under the assumption that the domain.xml for the fromInstance has the property
        //com.sun.aas.instanceRoot expanded (ie) it is a non-embedded gf (as in the EjbContainer case)

        List<AuthRealm> authRealms = securityService.getAuthRealm();
        for(AuthRealm authRealm:authRealms) {
            String className = authRealm.getClassname();
            if ("com.sun.enterprise.security.auth.realm.file.FileRealm".equals(className)) {
                List<Property> props = authRealm.getProperty();
                for (Property prop:props) {
                    if("file".equals(prop.getName())) {
                        fileNames.add(prop.getValue());
                    }
                }
            }
        }

         //Add keystore and truststore files

        // For the embedded server case, will the system properties be set in case of multiple embedded instances?
        //Not sure - so obtain the other files from the usual locations instead of from the System properties

        // String keyStoreFileName = System.getProperty(keyStoreProp);
        //String trustStoreFileName = System.getProperty(trustStoreProp);
        // String loginConf = System.getProperty(SYS_PROP_LOGIN_CONF);
        // String secPolicy = System.getProperty(SYS_PROP_JAVA_SEC_POLICY);

        String keyStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + "keystore.jks";
        String trustStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + "cacerts.jks";

        fileNames.add(keyStoreFileName);
        fileNames.add(trustStoreFileName);

        //Add login.conf and security policy

        String loginConf = fromInstanceDir + File.separator + "config" + File.separator + "login.conf";
        String secPolicy = fromInstanceDir + File.separator + "config" + File.separator + "security.policy";       
        
        fileNames.add(loginConf);
        fileNames.add(secPolicy);

        File toConfigDir = new File(fileToInstanceDir, "config");
        if (!toConfigDir.exists()) {
            toConfigDir.mkdir();
        }

        //Copy files into new directory

        for(String fileName:fileNames) {
            int beginIndex = fileName.lastIndexOf(File.separator);
            FileUtils.copyFile(new File(fileName), new File(toConfigDir,fileName.substring(beginIndex) + 1));
        }


    }
    
}
