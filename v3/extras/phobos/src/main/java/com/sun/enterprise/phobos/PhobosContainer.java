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

package com.sun.enterprise.phobos;

import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.container.ContainerProvider;
import org.jvnet.hk2.annotations.Service;

/**
 * Phobos ContractProvider
 *
 * @author dochez
 */
@Service(name="com.sun.enterprise.phobos.PhobosContainer")
public class PhobosContainer implements ContainerProvider {

    // so far in phobos, the container is empty as we start an engine for each application.
    public Class<? extends Deployer> getDeployer() {
        return PhobosDeployer.class;
    }

    public String getName() {
        return "Phobos";
    }
}
