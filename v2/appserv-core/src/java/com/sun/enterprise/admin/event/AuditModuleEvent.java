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
 * Audit Module Event - emitted by DAS after comletion of
 * create/delete/update operation on security-service.audit-module element 
 * Contains name of module and action type.
 */
public class AuditModuleEvent extends AdminEvent {

    /**
     * Constant denoting action code 
     */
    public static final int ACTION_CREATE = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_UPDATE = 3;

    /**
     * Event type
     */
    static final String eventType = AuditModuleEvent.class.getName();

    /**
     * Attributes
     */
    private int     actionType;
    private String  moduleName;

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager( AuditModuleEvent.class );

    /**
     * Create a new AuditModuleEvent.
     * @param type event type, a string representation for the event
     * @param instance name of the instance to which the event applies
     * @param module name of the Audit Module on which event happened.
     * @param action type of action - one of AuditModuleEvent.ACTION_CREATE,
     *        AuditModuleEvent.ACTION_DELETE, AuditModuleEvent.ACTION_UPDATE
     * @throws IllegalArgumentException if specified action is not valid
     */
    public AuditModuleEvent(String instance, String module, int action) {
        this(eventType, instance, module, action);
    }

    /**
     * Create a new AuditModuleEvent.
     * @param type event type, a string representation for the event
     * @param instance name of the instance to which the event applies
     * @param module name of the Audit Module on which event happened.
     * @param action type of action - one of AuditModuleEvent.ACTION_CREATE,
     *        AuditModuleEvent.ACTION_DELETE, AuditModuleEvent.ACTION_UPDATE
     * @throws IllegalArgumentException if specified action is not valid
     */
    public AuditModuleEvent(String type, String instance, String module, int action) {
        super(type, instance);
        moduleName = module;
        setAction(action);
    }

    /**
     * Get name of the Audit Module on which event happened
     */
    public String getModuleName() {
        return moduleName;
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
        if (action==ACTION_CREATE ||
            action==ACTION_DELETE ||
            action==ACTION_UPDATE )
            valid = true;
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_action", ""+action );
            throw new IllegalArgumentException( msg );
        }
        this.actionType = action;
    }

}
