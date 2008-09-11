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

package com.sun.enterprise.registration.glassfish;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import java.util.logging.Logger;
import com.sun.enterprise.registration.RegistrationException;
import com.sun.enterprise.registration.impl.SysnetRegistrationService;

/**
 * The Network Service is responsible for starting grizzly and register the
 * top level proxy. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints proxy to
 * particular context root.
 *
 * @author Jerome Dochez
 */
@Service(name="TransferService")
@Async
public class TransferService implements Startup, PostConstruct {
    
    @Inject
    Logger logger;


    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.START;
    }
    
    public void postConstruct() {
        SysnetRegistrationService srs = 
                new SysnetRegistrationService(
                RegistrationUtil.getServiceTagRegistry());
        if (srs.isRegistrationEnabled()) {
            logger.severe("Reg is enabled.");
            try {
                logger.severe("trying transfer..");
                srs.transferEligibleServiceTagsToSysNet();
            } catch (RegistrationException re) {
                logger.warning(re.getMessage());
            }
        }
    }
    

}
