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
package com.sun.enterprise.ee.synchronization;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * Builds a synchronization request for a J2EE application.
 *
 * @authoer Nazrul Islam
 */
public class ApplicationRequestBuilder extends RequestBuilderBase {
    
    public ApplicationRequestBuilder(ConfigContext ctx, String serverName) {
        super(ctx,serverName);
    }

    String getApplicationName(ConfigBean cb) {
        return ((J2eeApplication)cb).getName();
    }

    public ApplicationSynchRequest build(J2eeApplication app) {
        ApplicationSynchRequest asr = super.build(app);

        SynchronizationRequest sr = buildJspDir(app);
        asr.setJSPRequest(sr);

        sr = buildEjbDir(app);
        asr.setEJBRequest(sr);
        
        return asr;
    }

    List getAllDirectories(ConfigBean cb) {
        ArrayList list = new ArrayList();

        // application directory
        list.add( getAppDir(cb) );

        // jsp directory
        list.add( getJspDir(cb, PEFileLayout.J2EE_APPS_DIR) );

        // ejb directory
        list.add( getEjbDir(cb, PEFileLayout.J2EE_APPS_DIR) );

        // xml directory
        list.add( getXmlDir(cb, PEFileLayout.J2EE_APPS_DIR) );

        // policy directory
        list.add( getPolicyDir(cb) );

        // applibs directory
        list.add( getAppLibsDir(cb) );

        // java web start directory
        list.add( getJwsDir(cb) );

        return list;
    }

    private String getAppDir(ConfigBean cb) {
        String src = OPEN_PROP
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY
                   + CLOSE_PROP
                   + File.separator + PEFileLayout.APPS_ROOT_DIR
                   + File.separator + PEFileLayout.J2EE_APPS_DIR
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildAppDir(ConfigBean cb) {
        //String src = ((J2eeApplication)cb).getLocation();
        return buildRequest( getAppDir(cb) );
    }

    SynchronizationRequest buildJspDir(ConfigBean cb) {
        return buildJspDir(cb, PEFileLayout.J2EE_APPS_DIR);
    }

    SynchronizationRequest buildEjbDir(ConfigBean cb) {
        return buildEjbDir(cb, PEFileLayout.J2EE_APPS_DIR);
    }

    SynchronizationRequest buildXmlDir(ConfigBean cb) {
        return buildXmlDir(cb, PEFileLayout.J2EE_APPS_DIR);
    }
}
