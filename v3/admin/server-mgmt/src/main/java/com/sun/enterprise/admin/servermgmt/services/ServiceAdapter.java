/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General public final  License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public final /CDDL+GPL.html
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

import com.sun.enterprise.util.io.ServerDirs;
import java.util.*;
import static com.sun.enterprise.admin.servermgmt.services.Constants.*;

/**
 *
 * @author bnevins
 */
public abstract class ServiceAdapter implements Service {

    ServiceAdapter(ServerDirs serverDirs, AppserverServiceType type) {
        info = new PlatformServicesInfo(serverDirs, type);
    }
    
    @Override
    public PlatformServicesInfo getInfo() {
        return info;
    }

    @Override
    public final boolean isDomain() {
        return info.type == AppserverServiceType.Domain;
    }

    @Override
    public final boolean isInstance() {
        return info.type == AppserverServiceType.Instance;
    }

    @Override
    public final ServerDirs getServerDirs() {
        return info.serverDirs;
    }

    @Override
    public final void createService() {
        info.validate();
        initialize();
        initializeInternal();
        createServiceInternal();
    }
    //////////////////////////////////////////////////////////////////////////
    ////////////////   pkg-private     ///////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////

    void initialize() {
        final String parentPath = info.serverDirs.getServerParentDir().getPath();
        final String serverName = info.serverDirs.getServerName();

        getTokenMap().put(CFG_LOCATION_TN, parentPath);
        getTokenMap().put(ENTITY_NAME_TN, serverName);
        getTokenMap().put(LOCATION_ARGS_START_TN, getLocationArgsStart());
        getTokenMap().put(LOCATION_ARGS_STOP_TN, getLocationArgsStop());
        getTokenMap().put(START_COMMAND_TN, info.type.startCommand());
        getTokenMap().put(STOP_COMMAND_TN, info.type.stopCommand());
        getTokenMap().put(FQSN_TN,  info.fqsn);
        getTokenMap().put(OS_USER_TN, info.osUser);
        getTokenMap().put(SERVICE_NAME_TN, info.smfFullServiceName);
    }

    void trace(String s) {
        if (info.trace)
            System.out.println(TRACE_PREPEND + s);
    }

    final Map<String, String> getTokenMap() {
        return tokenMap;
    }

    private final Map<String, String> tokenMap = new HashMap<String, String>();
    final PlatformServicesInfo info;
}
