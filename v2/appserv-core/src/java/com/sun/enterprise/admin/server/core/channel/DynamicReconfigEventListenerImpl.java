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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2004 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.server.core.channel;

import com.sun.enterprise.admin.event.DynamicReconfigEventListener;
import com.sun.enterprise.admin.event.DynamicReconfigEvent;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This is implemenation of DynamicReconfigEvent listener
 *
 * @author Satish Viswanatham
 */
public class DynamicReconfigEventListenerImpl 
                    implements DynamicReconfigEventListener {

  public void processEvent(DynamicReconfigEvent event) 
                            throws AdminEventListenerException{

    String inst = event.getInstanceName();
    if (inst == null) {
       String msg = localStrings.getString(
            "admin.server.core.channel.impl.no_inst_name");
       throw new AdminEventListenerException(msg); 
    }

    RMIClient client = AdminChannel.getRMIClient(inst);

    if (client == null) {
       String msg = localStrings.getString(
            "admin.server.core.channel.impl.no_rmi_client", inst);
       throw new AdminEventListenerException(msg); 
    }
    boolean resNeeded = client.isRestartNeeded();

    if (event.getActionType() == DynamicReconfigEvent.ACTION_ENABLED) {
        if (resNeeded == true) {
            // can not set dynamic reconfig, throw exception
           String msg = localStrings.getString(
            "admin.server.core.channel.impl.restart_required", inst);
            throw new AdminEventListenerException(msg);
        } 
    } else if (event.getActionType() == DynamicReconfigEvent.ACTION_DISABLED) {
        client.setRestartNeeded(true);
    }
  }

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( DynamicReconfigEventListenerImpl.class );


}
