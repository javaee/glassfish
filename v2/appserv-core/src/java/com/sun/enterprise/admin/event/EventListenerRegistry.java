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

package com.sun.enterprise.admin.event;

import java.util.List;

/**
 * Simplification of the AdminEventListenerRegistry
 *
 * @author Jerome Dochez
 */
public interface EventListenerRegistry {
    

    /**
     * Add a listener for AdminEvent 
     * @see com.sun.enterprise.admin.event.AdminEvent
     *
     * @param eventType     type of the event
     * @param listener      listener implemention for notifications
     *
     * @throws IllegalArgumentException if event type and listener type 
     *                                  are not compatible
     */
    public void addEventListener(
            String eventType, AdminEventListener listener);
    
    /**
     * Remove specified event listener from event registry.
     * @param listener the event listener to remove
     */
    public void removeEventListener(AdminEventListener listener);
    
    /**
     * Returns a list of registered listener for a event type
     * @param the event type 
     * @return the list of registered listeners
     */
    public List<AdminEventListener> getListeners(String eventType);

    /**
     * Handle error in event listeners.
     * @param t throwable thrown by event listeners
     * @param result result for the event that was being processed
     */
    void handleListenerError(AdminEvent event, 
        Throwable t, AdminEventResult result);    
}
