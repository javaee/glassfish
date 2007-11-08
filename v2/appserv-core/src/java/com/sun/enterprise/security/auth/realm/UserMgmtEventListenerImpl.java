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

package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.UserMgmtEvent;
import com.sun.enterprise.admin.event.UserMgmtEventListener;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.server.pluggable.SecuritySupport;


/**
 * Implements interface UserMgmtEventListener.
 * So that users can be dynamically created/updated/deleted.
 * @author Shing Wai Chan
 */
public class UserMgmtEventListenerImpl implements UserMgmtEventListener {

    /**
     * user added.
     * It is called whenever a UserMgmtEvent with action of
     * UserMgmtEvent.ACTION_USERADD is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void userAdded(UserMgmtEvent event)
             throws AdminEventListenerException {
        reloadRealm(event);
    }

    /**
     * user deleted.
     * It is called whenever a UserMgmtEvent with action of
     * UserMgmtEvent.ACTION_USERREMOVE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void userRemoved(UserMgmtEvent event)
             throws AdminEventListenerException {
        reloadRealm(event);
    }

    /**
     * user updated (attributes change).
     * It is called whenever a UserMgmtEvent with action of
     * UserMgmtEvent.ACTION_USERUPDATE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void userUpdated(UserMgmtEvent event)
             throws AdminEventListenerException {
        reloadRealm(event);
    }

    /**
     * In this moment, user management is supported only in FileRealm.
     * The FileRealm will be refreshed in this case.
     * Other realms will throw Exception.
     * Administrative API may be factored out during JSR 196 implementation.
     * @param event the UserMgmtEvent
     * @exception AdminEventListenerException
     */
    private void reloadRealm(UserMgmtEvent event)
            throws AdminEventListenerException {
        try {
            String realmName = event.getAuthRealmName();
            Realm realm = Realm.getInstance(realmName);

            // should always true in this moment
            if (realm instanceof FileRealm) {
                SecuritySupport secSupp = SecurityUtil.getSecuritySupport();
                secSupp.synchronizeKeyFile(event.getConfigContext(), realmName);
            }

            realm.refresh();
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }
}
