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

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.grizzly.Controller;

/**
 * Utility class that creates Grizzly's SelectorThread instance based on 
 * the HttpService of domain.xml
 * 
 * @author Jeanfrancois Arcand
 */
public class GrizzlyHttpEmbed {

    // TODO: Must get the information from domain.xml Config objects.
    public static GrizzlyServiceListener createListener(HttpService service,
            int port, Controller controller){
        System.setProperty("product.name", "GlassFish/v3");      
        GrizzlyServiceListener grizzlyServiceListener 
                = new GrizzlyServiceListener();
	//TODO: Configure via domain.xml
        //grizzlyServiceListener.setController(controller);
        grizzlyServiceListener.setPort(port);   
        grizzlyServiceListener.setMaxProcessorWorkerThreads(5);
        GrizzlyServiceListener.setWebAppRootPath(
                System.getProperty("com.sun.aas.instanceRoot") + "/docroot");
        return grizzlyServiceListener;
    }
     
}
