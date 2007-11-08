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
 * IIOPCluster.java
 *
 * Created on June 13, 2004, 12:02 AM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 *
 * @author  prakash
 */
import java.util.*;
public class IIOPCluster {
    
    // This clusterName is not really an attribute of IIOCluster element.  This is the name obtained from current cluster from common info model
    private String clusterName;
    private List iiopServerInstanceList;
    private IIOPServerInstance currentServerInstance;
    
    /** Creates a new instance of IIOPCluster */
    public IIOPCluster(String clName) {
        this.clusterName = clName;
    }
    public void addIIOPServerInstance(String serverName){
        IIOPServerInstance serverInstance = new IIOPServerInstance(serverName);
        this.currentServerInstance = serverInstance;
        if(this.iiopServerInstanceList == null)
            this.iiopServerInstanceList = new ArrayList();
        this.iiopServerInstanceList.add(serverInstance);
    }   
    public void addIIOPEndPoint(String id, String host, String port){
        this.currentServerInstance.addIIOPEndPoint(id,host,port);
    }    
    public List getIIOPServerInstanceList(){
        return this.iiopServerInstanceList;
    }
    public String getClusterName(){
        return this.clusterName;
    }
    public void setClusterName(String clName){
        this.clusterName = clName;
    }
}
