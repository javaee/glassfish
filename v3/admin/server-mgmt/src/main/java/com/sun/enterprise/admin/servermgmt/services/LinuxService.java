/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.ServerDirs;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Byron Nevins
 */
public class LinuxService extends NonSMFServiceAdapter {

    static boolean apropos() {

        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp
        // todo temp

        //return OS.isLinux();
        return true;
    }

    LinuxService(ServerDirs dirs, AppserverServiceType type) {
        super(dirs, type);
        if (!apropos()) {
            // programmer error
            throw new IllegalArgumentException(Strings.get("internal.error",
                    "Constructor called but Linux Services are not available."));
        }
    }
    // bnevins, Aug 2010.  The unfortunate FAT interdace of the Service interface makes
    // it confusing -- this method is really the only one that does something --
    // all the other methods do configuration.

    @Override
    public final void createServiceInternal() {
        try {
            trace("**********   Object Dump  **********\n" + this.toString());

            throw new UnsupportedOperationException("Not supported yet.");
            /*
            if (uninstall() == 0 && !isDryRun())
            System.out.println(Strings.get("windows.services.uninstall.good"));
            else
            trace("No preexisting Service with that id and/or name was found");

            install();
             * */
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final String getSuccessMessage() {
        throw new UnsupportedOperationException("Not supported yet.");

        /*
        if (isDryRun())
        return Strings.get("dryrun");

        return Strings.get("LinuxServiceCreated", getName(),
        getServerDirs().getServerName() + " GlassFish Server",
        getServerDirs().getServerDir(), targetXml, targetWin32Exe);
         *
         */
    }

    @Override
    public void writeReadmeFile(String msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public String getLocationArgsStart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocationArgsStop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////////////   ALL PRIVATE BELOW    /////////////////////
    ///////////////////////////////////////////////////////////////////////
    @Override
    public void initializeInternal(){
        setTemplateFile(TEMPLATE_FILE_NAME);
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private static final String TEMPLATE_FILE_NAME = "linux-service.template";
}
