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
 * DeploymentEvent.java
 *
 * Created on April 8, 2003.
 */

package com.sun.enterprise.deployment.backend;

import java.util.EventObject;

/**
 * A <code>DeploymentEvent</code> event gets delivered whenever a 
 * <code>DeploymentEventListener</code> registers itself with the 
 * <code>DeploymentEventManager</code>.
 *
 * @author Marina Vatkina
 */
public class DeploymentEvent
    extends EventObject
{

    public static final int UNKNOWN       = 0;

    public static final int PRE_DEPLOY    = 1;

    public static final int POST_DEPLOY   = 2;

    public static final int PRE_UNDEPLOY  = 3;

    public static final int POST_UNDEPLOY = 4;


    /** Event type */
    private int eventType = 0;

    /** 
     * Constructs <code>DeploymentEvent</code> from an event type
     * and a <code>DeploymentEventInfo</code> instance.
     * @param eventType as one of the valid Event types.
     * @param info the DeploymentEventInfo instance to be used for this event.
     */
    public DeploymentEvent(int eventType, DeploymentEventInfo info) {
        super(info);
        this.eventType = eventType;
    }

    /** 
     * Returns event type of this event.
     * @return event type of this event.
     */
    public int getEventType() {
        return eventType;
    }

    /** 
     * Returns event info of this event.
     * @return event info of this event.
     */
    public DeploymentEventInfo getEventInfo() {
        return (DeploymentEventInfo)getSource();
    }

}
