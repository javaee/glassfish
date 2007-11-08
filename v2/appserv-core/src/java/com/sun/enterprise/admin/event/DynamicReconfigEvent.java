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

package com.sun.enterprise.admin.event;

import com.sun.enterprise.admin.event.AdminEvent;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * DynamicReconfigEvent - emitted by DAS upon changes
 * "dynamic-reconfiguration-enabled" attribute in "config" element
 */
public class DynamicReconfigEvent extends AdminEvent implements CommandEvent {

    /**
     * Constant denoting action code 
     */
    public static final int ACTION_DISABLED     = 0;
    public static final int ACTION_ENABLED      = 1;

    /**
     * Event type
     */
    public static final String eventType = DynamicReconfigEvent.class.getName();

    /**
     * Attributes
     */
    private int      actionType;

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager( DynamicReconfigEvent.class );

    /**
     * Create a new DynamicReconfigEvent.
     * @param instance name of the instance to which the event applies
     * @param action type of action - one of DynamicReconfigEvent.ACTION_ENABLED,
     *        DynamicReconfigEvent.ACTION_DISABLED
     * @throws IllegalArgumentException if specified action is not valid
     */
    public DynamicReconfigEvent(String instance, int action) {
        super(eventType, instance);
        setAction(action);
    }


    /**
     * Get action type for this event.
     */
    public int getActionType() {
        return actionType;
    }

    /**
     * Set action to specified value. If action is not one of allowed,
     * then IllegalArgumentException is thrown.
     * @throws IllegalArgumentException if action is invalid
     */
    private void setAction(int action) {
        boolean valid = false;
        if (action==ACTION_ENABLED      ||
            action==ACTION_DISABLED)
            valid = true;
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_action", ""+action );
            throw new IllegalArgumentException( msg );
        }
        this.actionType = action;
    }

}
