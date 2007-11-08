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
 * SimpleInstanceArranger.java
 *
 * Created on May 4, 2006, 2:37 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Larry White
 */
public class SimpleInstanceArranger {
    
    /** Creates a new instance of SimpleInstanceArranger */
    public SimpleInstanceArranger() {
    }
    
    public void init(List instanceList) {
        _instanceList = instanceList;
        Collections.sort(_instanceList);
    }
    
    public String getReplicaPeerName(String instanceName) {
        int idx = _instanceList.indexOf(instanceName);
        if(idx == -1 || _instanceList.size() == 0) {
            return null;
        }
        if(idx == (_instanceList.size() - 1)) {
            return (String)_instanceList.get(0);
        }
        return (String)_instanceList.get(idx + 1);        
    }
    
    //get name of instance that is replicating to instanceName
    public String getReplicatedFromPeerName(String instanceName) {
        int idx = _instanceList.indexOf(instanceName);
        if(idx == -1 || _instanceList.size() < 2) {
            return null;
        }
        if(idx == 0) {
            return (String)_instanceList.get(_instanceList.size() - 1);
        }        
        return (String)_instanceList.get(idx - 1);        
    }
    
    public boolean isBetterReplicationPartner(String proposedPartnerName, 
            String currentPartnerInstanceName, String currentInstanceName) {
        //this means if you are alone then any other partner is better
        if(currentPartnerInstanceName.equalsIgnoreCase(currentInstanceName)
            && !proposedPartnerName.equalsIgnoreCase(currentPartnerInstanceName) ) {
            return true;
        }
        int currentIdx = _instanceList.indexOf(currentInstanceName);
        int currentPartnerIdx = _instanceList.indexOf(currentPartnerInstanceName);
        int proposedPartnerIdx = _instanceList.indexOf(proposedPartnerName);
    //System.out.println("currentInstanceName=" + currentInstanceName + " currentIdx=" + currentIdx); 
    //System.out.println("currentPartnerInstanceName=" + currentPartnerInstanceName + " currentPartnerIdx=" + currentPartnerIdx); 
    //System.out.println("proposedPartnerName=" + proposedPartnerName + " proposedPartnerIdx=" + proposedPartnerIdx);        
        if(proposedPartnerIdx == -1 || currentPartnerIdx == proposedPartnerIdx) {
            return false;
        } 
    //System.out.println("distanceBetween(currentIdx, proposedPartnerIdx)=" + distanceBetween(currentIdx, proposedPartnerIdx));
    //System.out.println("distanceBetween(currentIdx, currentPartnerIdx)=" + distanceBetween(currentIdx, currentPartnerIdx));
        return ( distanceBetween(currentIdx, proposedPartnerIdx) 
            < distanceBetween(currentIdx, currentPartnerIdx) );
    }
    
    public boolean isBetterOrSameAsReplicationPartner(String proposedPartnerName, 
            String currentPartnerInstanceName, String currentInstanceName) {
        //this means if you are alone then any other partner is better
        if(currentPartnerInstanceName.equalsIgnoreCase(currentInstanceName)
            && !proposedPartnerName.equalsIgnoreCase(currentPartnerInstanceName) ) {
            return true;
        }
        int currentIdx = _instanceList.indexOf(currentInstanceName);
        int currentPartnerIdx = _instanceList.indexOf(currentPartnerInstanceName);
        int proposedPartnerIdx = _instanceList.indexOf(proposedPartnerName);
    //System.out.println("currentInstanceName=" + currentInstanceName + " currentIdx=" + currentIdx); 
    //System.out.println("currentPartnerInstanceName=" + currentPartnerInstanceName + " currentPartnerIdx=" + currentPartnerIdx); 
    //System.out.println("proposedPartnerName=" + proposedPartnerName + " proposedPartnerIdx=" + proposedPartnerIdx);
        if(proposedPartnerIdx == -1) {
            return false;
        } 
        //test - allow reconnect to current partner
        if(currentPartnerIdx == proposedPartnerIdx) {
            //begin work-around code for join notification problems
            //this means being asked to reconnect to same partner less than
            //30 seconds after starting, so its ignored
            ReplicationHealthChecker healthChecker =
                ReplicationHealthChecker.getInstance();
            System.out.println("isBetterOrSameAsReplicationPartner: time since start < 30sec: " +  healthChecker.isTimeSinceInstanceStartLessThan(30 * 1000)); 
            if(healthChecker.isTimeSinceInstanceStartLessThan(30 * 1000)) {
                return false;
            } else {
                return true;
            }
            //above replaces following line for work-around
            
            //return true;
            //end work-around code for join notification problems                                  
            
        } 
    //System.out.println("distanceBetween(currentIdx, proposedPartnerIdx)=" + distanceBetween(currentIdx, proposedPartnerIdx));
    //System.out.println("distanceBetween(currentIdx, currentPartnerIdx)=" + distanceBetween(currentIdx, currentPartnerIdx));
        return ( distanceBetween(currentIdx, proposedPartnerIdx) 
            < distanceBetween(currentIdx, currentPartnerIdx) );
    }    
    
    private int distanceBetween(int a, int b) {
        if(a <= b) {
            return b - a;
        } else {
            return _instanceList.size() - (a - b);
        }
    }
    
    List _instanceList = new ArrayList();
    
}
