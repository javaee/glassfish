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
 * @(#) DescriptorConstants.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.deployment;

/**
 * Contains all deployment descriptor constants.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public interface DescriptorConstants {

    /**
     * Bean Pool - maximum size, a pool of slsb can grow to.
     */ 
    int MAX_POOL_SIZE_DEFAULT = -1;
    
    /**
     * Bean Pool - maximum time a caller will have to wait when pool 
     * has reached maximum configured size and an instance is not 
     * available to process the incoming request.
     */ 
    int MAX_WAIT_TIME_DEFAULT = -1;
    
    /**
     * Bean Pool - size of slsb pool grows in increments specified by 
     * resize-quantity, within the configured upper limit max-pool-size. 
     */
    int POOL_RESIZE_QTY_DEFAULT = -1;
    
    /**
     * Bean Pool - minimum number of slsb instances maintained in a pool.
     */ 
    int STEADY_POOL_SIZE_DEFAULT = -1;   
    
    /**
     * Bean Pool - idle bean instance in a pool becomes a candidate for 
     * passivation (sfsb/eb) or deletion (slsb), when this timeout expires.
     */ 
    int POOL_IDLE_TIMEOUT_DEFAULT = -1;
    
    /**
     * Bean Cache - sfsb and eb are created and cached, on demand.
     */     
    int MAX_CACHE_SIZE_DEFAULT = -1;
    
    /**
     * Bean Cache - resize quantity
     */     
    int RESIZE_QUANTITY_DEFAULT = -1;
    
    /**
     * Bean Cache - Passivated bean (sfsb/eb) instance is removed if it 
     * is not accesed within  this time, after passivation
     */
    int REMOVAL_TIMEOUT_DEFAULT = -1;   
    
    /**
     * Bean Cache - idle bean instance in a pool becomes a candidate for 
     * passivation (sfsb/eb) or deletion (slsb), when this timeout expires.
     */
    int CACHE_IDLE_TIMEOUT_DEFAULT = -1;
    
    /**
     * ejb - refresh period in seconds
     */
    int REFRESH_PERIOD_IN_SECONDS_DEFAULT = -1;
}
