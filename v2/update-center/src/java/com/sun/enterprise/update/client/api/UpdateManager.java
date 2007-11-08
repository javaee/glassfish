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

package com.sun.enterprise.update.client.api;

import com.sun.enterprise.update.client.api.download.DownloadManager;
import com.sun.enterprise.update.client.api.install.Installer;
import com.sun.enterprise.update.beans.Module;
import java.util.List;

/**
 * The public API for update center functionality. This API is used by 
 * both GUI(Swing application) and CLI(asupdate.bat).
 *
 * @author Satish Viswanatham
 */
public class UpdateManager {
    
    /**
     *  This method is used to get the singleton UpdatManager object 
     *
     *  @return UpdateManager singleton object
     */
    public synchronized static UpdateManager getInstance() {
        if (inst == null) {
            inst = new UpdateManager();
        }
        return inst;
    }

    /**
     *  This method is used to get download manager for a catelog
     *
     *  @param  catelogID   ID of the catelog 
     *
     *  @return DownloadManager for the catelog
     */
    public DownloadManager getDownloadManager(String catelogID) {return null;}

    /**
     *  This method is used to get information about installed applications
     *
     *  @return InstalledModules for the current update center
     */
    public InstalledModules getInstalledModules() {return null;}

    /**
     *  This method is used to get information about available updates
     *
     *  @param  catelogID   ID of the catelog 
     *
     *  @return Updates for the specified catelog (if any available)
     */
    public Updates getUpdates(String catelogID) {return null;}

    /**
     *  This method is used to get information about available new applications
     *
     *  @param  catelogID   ID of the catelog 
     *
     *  @return NewModules for the specified catelog (if any available)
     */
    public NewModules getNewModules(String catelogID) {return null;}

    /**
     *  This method is used to get installer - installs new application or
     *  updates
     *
     *  @return NewApplications for the specified catelog (if any available)
     */
    public Installer getInstaller() {return null;}

    // -- PRIVATE VARS

    private UpdateManager() {}

    private static UpdateManager inst = null;
}
