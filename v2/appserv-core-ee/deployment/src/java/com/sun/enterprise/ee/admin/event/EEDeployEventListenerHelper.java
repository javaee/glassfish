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

package com.sun.enterprise.ee.admin.event;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.DeployEventListenerHelper;
import com.sun.enterprise.admin.event.PEDeployEventListenerHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.ee.synchronization.api.ApplicationsMgr;
import com.sun.enterprise.ee.synchronization.api.SynchronizationContext;
import com.sun.enterprise.ee.synchronization.api.SynchronizationFactory;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * Managed the synchronization functionalities needed 
 * @author deployment dev team
 */
public class EEDeployEventListenerHelper extends PEDeployEventListenerHelper {

    /**
     * Synchronize the application
     */
    public void synchronize(BaseDeployEvent event) throws AdminEventListenerException {
       
        try {
            if (AdminService.getAdminService().isDas()) { //on DAS
                //NO OP, i.e. the bits are not synched.
                return;
            }

            // creates a synchronization context
            SynchronizationContext synchCtx = 
                SynchronizationFactory.createSynchronizationContext(
                    event.getConfigContext());
           
            // applications synchronization manager
            ApplicationsMgr appSynchMgr = synchCtx.getApplicationsMgr();
              
            // synchronizes an application
            appSynchMgr.synchronize(event.getJ2EEComponentName()); 
        } catch (SynchronizationException ex) {
            throw new AdminEventListenerException(ex);
        }
    }
}
