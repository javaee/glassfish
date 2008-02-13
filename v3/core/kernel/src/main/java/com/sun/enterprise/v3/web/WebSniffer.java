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

package com.sun.enterprise.v3.web;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;


/**
 * Implementation of the Sniffer for the web container.
 * 
 * @author Jerome Dochez
 */
@Service(name="web")
@Scoped(Singleton.class)
public class WebSniffer  extends GenericSniffer implements Sniffer {

    public WebSniffer() {
        this("web", "WEB-INF/web.xml", null);
    }
    
    public WebSniffer(String containerName, String appStigma, String urlPattern) {
        super(containerName, appStigma, urlPattern);
    }    

    //final String[] containers = { "com.sun.enterprise.web.WebContainer" ,"com.sun.enterprise.security.SecurityContainer"};
    final String[] containers = { "com.sun.enterprise.web.WebContainer" };
    public String[] getContainersNames() {
        return containers;
    }    
}
