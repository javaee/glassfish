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
package com.sun.enterprise.security.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        String userHome = System.getProperty("user.home");
  
        String embeddedServerName = getCurrentEmbeddedServerName();
        File tempDir = new File(userHome + File.separator + ".glassfishv3-"+embeddedServerName+File.separator + "config");
        boolean mkDirSuccess = true;
        if (!tempDir.exists()) {
            mkDirSuccess = tempDir.mkdirs();
        }
        
        File localFile = new File(tempDir.getAbsoluteFile()+File.separator + fileName);
        
        if(localFile.exists()) {
            //file already written to tmp dir, so return
            return localFile;
        }
     
        if (mkDirSuccess && !localFile.exists()) {
            localFile.createNewFile();
        }
        FileOutputStream oStream = new FileOutputStream(localFile);
        InputStream iStream = Util.class.getClassLoader().getResourceAsStream("config"+File.separator + fileName);

        while (iStream.available() > 0) {
            oStream.write(iStream.read());
        }

        oStream.close();
        iStream.close();
        return localFile;

    }

    public static String getCurrentEmbeddedServerName() {
        List<String> embeddedServerNames = Server.getServerNames();
        String embeddedServerName = (embeddedServerNames.get(0) == null) ? "embedded" : embeddedServerNames.get(0);
        return embeddedServerName;

    }
    
}
