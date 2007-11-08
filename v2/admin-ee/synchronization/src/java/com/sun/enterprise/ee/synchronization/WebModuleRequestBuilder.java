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
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * Builds a synchronization request for a web module.
 *
 * @author Nazrul Islam
 */
public class WebModuleRequestBuilder extends RequestBuilderBase {
    
    public WebModuleRequestBuilder(ConfigContext ctx, String serverName) {
        super(ctx,serverName);
    }

    String getApplicationName(ConfigBean cb) {
        return ((WebModule)cb).getName();
    }

    public ApplicationSynchRequest build(WebModule webModule) {
        ApplicationSynchRequest asr = super.build(webModule);

        SynchronizationRequest sr = buildJspDir(webModule);
        asr.setJSPRequest(sr);

        SynchronizationRequest ejbReq = buildEjbDir(webModule);
        asr.setEJBRequest(ejbReq);

        return asr;
    }

    List getAllDirectories(ConfigBean cb) {
        ArrayList list = new ArrayList();

        list.add( getAppDir(cb) );
        list.add( getJspDir(cb, PEFileLayout.J2EE_MODULES_DIR) );
        list.add( getPolicyDir(cb) );
        list.add( getXmlDir(cb, PEFileLayout.J2EE_MODULES_DIR) );
        list.add( getEjbDir(cb, PEFileLayout.J2EE_MODULES_DIR) );
        list.add( getAppLibsDir(cb) );
        list.add( getJwsDir(cb) );

        return list;
    }

    String getAppDir(ConfigBean cb) {
        String src = OPEN_PROP
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY
                   + CLOSE_PROP
                   + File.separator + PEFileLayout.APPS_ROOT_DIR
                   + File.separator + PEFileLayout.J2EE_MODULES_DIR
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildAppDir(ConfigBean cb) {
        //String src = ((WebModule)cb).getLocation();
        return buildRequest( getAppDir(cb) );
    }

    SynchronizationRequest buildJspDir(ConfigBean cb) {
        return buildJspDir(cb, PEFileLayout.J2EE_MODULES_DIR);
    }

    SynchronizationRequest buildEjbDir(ConfigBean cb) {
        return buildEjbDir(cb, PEFileLayout.J2EE_MODULES_DIR);
    }

    SynchronizationRequest buildXmlDir(ConfigBean cb) {
        return buildXmlDir(cb, PEFileLayout.J2EE_MODULES_DIR);
    }
}
