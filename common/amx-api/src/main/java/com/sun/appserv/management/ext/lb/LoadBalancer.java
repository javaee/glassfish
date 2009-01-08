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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/ext/lb/LoadBalancer.java,v 1.2 2007/05/05 05:30:49 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:49 $
 */

package com.sun.appserv.management.ext.lb;

import com.sun.appserv.management.base.AMX;
import static com.sun.appserv.management.base.XTypes.LOAD_BALANCER;

import java.util.Date;

/**
   Runtime counterpart for the config MBean {@link com.sun.appserv.management.config.LoadBalancerConfig} 
   representing the load-balancer element.
 */
public interface LoadBalancer extends AMX {

    /** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
    public static final String	J2EE_TYPE = LOAD_BALANCER;

    /**
      Applies changes in the corresponding configuration to this LB 
     */
    public void applyLBChanges();        

    /**
      @return true if there are pending changes for this LB
     */
    public boolean isApplyChangeRequired();       

    /**
      Exports the corresponding LBConfig information and returns the contents as a string.
      @see com.sun.appserv.management.config.LBConfig
     */
    public String getLoadBalancerXML();    
      
    /**
      Returns the timestamp of the most recent export of corresponding LBConfig
     */
    public Date getLastExported();        
    
    /**
      Returns the timestamp of the most recent application of corresponding LBConfig
     */
    public Date getLastApplied();            

    /**
      Returns the timestamp of the last time the stats on this loadbalancer were reset
     */
    public Date getLastResetTime();            

    /**
      Reset the monitoring stats on this loadbalancer.
     */
    public void resetStats();
    
    /**
      Reset the monitoring stats on this loadbalancer.
     */
    public boolean testConnection();
            
    /**
     * Returns the uhealthy/healthy/quiesced status for an insatnce load balanced
     * by this load balancer.
     */
    public String getStatus(String instanceName);     
}
