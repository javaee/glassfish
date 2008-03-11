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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import java.util.*;

/**
 * This is the main Launcher class designed for external and internal usage.
 * Each of the 3 kinds of server -- domain, node-agent and instance -- need
 * to sublass this class.  
 * @author bnevins
 */
public abstract class GFLauncher {

    /**
     * 
     * @return The info object that contains startup info
     */
    public final GFLauncherInfo getInfo()
    {
        return info;
    }

    /**
     * An info object is created automatically use this method to set your own
     * instance.
     * @param info the info instance
     */
    public final void setInfo(GFLauncherInfo info)
    {
        this.info = info;
    }

    abstract void internalLaunch() throws GFLauncherException;

    /**
     * Launches the server.  Any fatal error results in a GFLauncherException
     * No unchecked Throwables of any kind will be thrown.
     * 
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException 
     */
    public void launch() throws GFLauncherException
    {
        try {
            System.out.println("CLASSLOADER: " + getClass().getClassLoader());
            setup();
            internalLaunch();
        }
        catch (GFLauncherException gfe) {
            throw gfe;
        }
        catch (Throwable t) {
            // hk2 might throw a java.lang.Error
            throw new GFLauncherException("unknownError", t);
        }
    }

    private void setup() throws GFLauncherException {
        ASenvPropertyReader pr = new ASenvPropertyReader();
        asenvProps = pr.getProps();
        GFLauncherLogger.info("asenv properties:\n" + pr.toString());
        info.setup();
    }
    private void getJavaExe() {
        /* possible locations:
         * JAVA_HOME
         * java.home
         * asenv
         * Path
         */
        //SystemPropertyConstants.JAVA_ROOT_PROPERTY
        
    }
    GFLauncherInfo info = new GFLauncherInfo();
    Map<String,String> asenvProps;

}

