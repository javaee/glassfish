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


package org.glassfish.ejb.startup;

import com.sun.ejb.containers.EjbContainerUtil;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import java.util.logging.Logger;

/**
 * Ejb container service
 *
 * @author Mahesh Kannan
 */
@Service(name="org.glassfish.ejb.startup.EjbContainerStarter")
public class EjbContainerStarter
    implements Container, PostConstruct, PreDestroy {

    //@Inject Domain domain;

    @Inject
    ServerContext _serverContext;

    @Inject
    EjbContainerUtil ejbContainerUtilImpl;

    @Inject
    Logger logger;

    private String instanceName;

    public void postConstruct() {
        instanceName = _serverContext.getInstanceName();
    }    
    
    public void preDestroy() {
        
    }

    public String getName() {
        return "EjbContainer";
    }

    public Class<? extends org.glassfish.api.deployment.Deployer> getDeployer() {
        return EjbDeployer.class;
    }
}
