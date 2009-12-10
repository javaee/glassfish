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
package com.sun.enterprise.web.connector.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Host;

/**
 * Listener used to receive events from Catalina when a <code>Context</code>
 * is removed or when a <code>Host</code> is removed.
 *
 * @author Jean-Francois Arcand
 */
public class CatalinaListener  implements ContainerListener{
    
    public void containerEvent(ContainerEvent event) {    
        if (Container.REMOVE_CHILD_EVENT.equals(event.getType()) ) {
            Context context;
            String contextPath;
            Host host;

            Object container = event.getData();            
            if ( container instanceof Context) {
                context = (Context)container;
                
                if (context != null && !context.hasConstraints() &&
                        context.findFilterDefs().length == 0 ){        
                    contextPath = context.getPath();
                    host = (Host)context.getParent();
                    int[] ports = host.getPorts();
                    for (int i=0; i < ports.length; i++){
                        removeContextPath(ports[i],contextPath); 
                    }
                }
            } 
        }  
    }  
    
    
    /**
     * Remove from the <code>FileCache</code> all entries related to 
     * the <code>Context</code> path.
     * @param port the <code>FileCacheFactory</code> port
     * @param contextPath the <code>Context</code> path
     */
    private void removeContextPath(int port, String contextPath) {
        // FIXME: I can't spot where Grizzly is registering mbeans, and this code
        // tries to invoke it and fails during the undeployment.
        // Commented out for now for the sake of JavaOne demo.

//        ArrayList<GrizzlyConfig> list =
//                GrizzlyConfig.getGrizzlyConfigInstances();
//        for(GrizzlyConfig config: list){
//            if (config.getPort() == port){
//                config.invokeGrizzly("removeCacheEntry",
//                        new Object[]{contextPath},
//                        new String[]{"java.lang.String"});
//            }
//        }
    }  
}

