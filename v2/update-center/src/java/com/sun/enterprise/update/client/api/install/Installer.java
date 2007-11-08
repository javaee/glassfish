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

package com.sun.enterprise.update.client.api.install;

import com.sun.enterprise.update.client.api.download.DownloadManager;
import com.sun.enterprise.update.UpdateFailureException;
import com.sun.enterprise.update.beans.Module;
import java.util.List;

/**
 * This API is used by both GUI(Swing application) and CLI(asupdate.bat) to
 * install downloaded modules and updates.
 *
 * @author Satish Viswanatham
 */
public class Installer {
    
    /**
     *  This method is used to get download manager for a catelog
     *
     *  @param  catelogID   ID of the catelog 
     *  @param  Module      Module/Update to be installed
     *
     *  @throws UpdateFailureException in case of failure 
     */
    public void install(String catelogID, Module mod)
            throws UpdateFailureException {}

    /**
     *  This method is used to get information about installed applications
     *
     *  @param  catelogID   ID of the catelog 
     *  @param  Module      Module/Update to be un-installed
     *
     *  @throws UpdateFailureException in case of failure 
     */
    public void uninstall(String catelogID, Module mod) 
            throws UpdateFailureException {}

}
