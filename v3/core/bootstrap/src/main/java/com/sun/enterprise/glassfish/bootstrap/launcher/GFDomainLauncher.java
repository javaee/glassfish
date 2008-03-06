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
package com.sun.enterprise.glassfish.bootstrap.launcher;

import com.sun.enterprise.module.bootstrap.BootException;
import java.util.*;
import com.sun.enterprise.glassfish.bootstrap.Main;
import com.sun.enterprise.glassfish.bootstrap.launcher.util.ASenvPropertyReader;
import org.glassfish.universal.Test;

/**
 * GFDomainLauncher
 * This class is a package-private subclass of GFLauncher designed for
 * domain launching
 * @author bnevins
 */
class GFDomainLauncher extends GFLauncher {

    void internalLaunch() throws GFLauncherException {
        try {
            if(info.isEmbedded())
                launchEmbedded();
            else
                launchExternal();
        }
        catch (BootException ex) {
            throw new GFLauncherException("unknownError", ex);
        }
    }

    private void launchEmbedded() throws GFLauncherException, BootException {
        Main main = new Main();
        main.start(info.getArgsAsStringArray());
        GFLauncherLogger.info("FinishedEmbedded", info.getDomainName());

    }

    private void launchExternal() throws GFLauncherException, BootException {
        GFLauncherLogger.info("Launching Embedded -- External Launching not yet supported.");
        launchEmbedded();
    }
}

