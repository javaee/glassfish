
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

package com.sun.enterprise.groovy;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ContractProvider for Groovy
 *
 * Almost empty, nothing real to do here
 * 
 * @author Martin Grebac
 */
@Service(name="com.sun.enterprise.groovy.GroovyContainer")
public class GroovyContainer implements Container, PostConstruct, PreDestroy {
    
    public void postConstruct() {}
    
    public URL getInfoSite() {
    try {
            return new URL("http://glassfish-scripting.dev.java.net");
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public Class<? extends Deployer> getDeployer() {
        return GroovyDeployer.class;
    }

    public String getName() {
        return "Groovy";
    }

    public void preDestroy() {}
                
}
