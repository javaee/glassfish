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

/*
 * J2EEModuleCallBackImpl.java
 *
 * Created on April 29, 2002, 6:26 PM
 */

package com.sun.enterprise.management.util;

import com.sun.enterprise.management.model.J2EEDeployedObjectMdl;
import com.sun.enterprise.management.util.J2EEModuleCallBack;

import javax.management.MBeanException;

/**
 *
 * @author  prakash
 * @version 
 */
public class J2EEModuleCallBackImpl implements J2EEModuleCallBack {

    private String name;
    private String parentName;
    private String deploymentDescriptor;
    private String serverName;
    private J2EEManagementObjectManager mgmtObjectManager;
    private StartStopCallback sscb;

    public J2EEModuleCallBackImpl(String nm, String pName, String dd,
				J2EEManagementObjectManager mgmtObjectManager) {
        this.name = nm;
        this.parentName = pName;
        this.deploymentDescriptor = dd;
	this.mgmtObjectManager = mgmtObjectManager;
    }

    public J2EEModuleCallBackImpl(String nm, String pName, String serverName, String dd,
                                J2EEManagementObjectManager mgmtObjectManager) {
        this.name = nm;
        this.parentName = pName;
        this.deploymentDescriptor = dd;
        this.serverName = serverName;
        this.mgmtObjectManager = mgmtObjectManager;
    }

    public J2EEModuleCallBackImpl(String nm, String pName, String serverName, String dd,
                                StartStopCallback sscb) {
        this.name = nm;
        this.parentName = pName;
        this.deploymentDescriptor = dd;
        this.serverName = serverName;
        this.sscb = sscb;
    }

    public String getDeploymentDescriptor() {
        return this.deploymentDescriptor;
    }

    /* Invokes the start method of StartStop callback */
    public void start(Object module) {
        try {
            sscb.startModule((J2EEDeployedObjectMdl)module);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Invokes the stop method of StartStop callback */
    public void stop(Object module) {
        try {
            sscb.stopModule((J2EEDeployedObjectMdl)module);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    public String getName() {
        return this.name;
    }
    
    public String getParentName() {
        return this.parentName;
    }

    public String getServerName() {
        return this.serverName;
    }
    
}
