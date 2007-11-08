/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.sessions;

import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.internal.sessions.IdentityMapAccessor;
import oracle.toplink.essentials.threetier.*;

/**
 * INTERNAL:
 * IdentityMapAccessor subclass for client sessions.
 * Note: A client session will always use it's parent session's IdentityMapManager
 */
public class ClientSessionIdentityMapAccessor extends IdentityMapAccessor {

    /**
     * INTERNAL:
     * Create a ClientSessionIdentityMapAccessor
     * Since the parent session's identity map manager is used, an IdentityMapManager
     * does not need to be supplied to the constructor
     */
    public ClientSessionIdentityMapAccessor(ClientSession session) {
        super(session, null);
    }

    /**
     * INTERNAL:
     * Was PUBLIC: customer will be redirected to {@link oracle.toplink.essentials.sessions.Session}.
     * Reset the entire object cache.
     * This method blows away both this session's and its parents caches, including the server cache or any other cache.
     * This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     */
    public void initializeAllIdentityMaps() {
        ((ClientSession)session).getParent().getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    /**
     * INTERNAL:
     * Return the IdentityMapManager for the client session.
     * This overrides the IdentityMapAccessor version of getIdentityMapManager to
     * return the parent session's IdentityMapManager
     */
    public IdentityMapManager getIdentityMapManager() {
        return ((ClientSession)session).getParent().getIdentityMapAccessorInstance().getIdentityMapManager();
    }

    /**
     * INTERNAL:
     * The client session does not have a local indentity map, so this has no effect and should not be used.
     */
    public void initializeIdentityMap(Class theClass) {
        ;// Do nothing	
    }

    /**
     * INTERNAL:
     * The client session does not have a local indentity map, so this has no effect and should not be used.
     */
    public void initializeIdentityMaps() {
        ;// Do nothing	
    }

    /**
     * INTERNAL:
     * The identity map manager cannot be set on a client session since it
     * looks at it's parent session's identity map manager.
     */
    public void setIdentityMapManager(IdentityMapManager identityMapManager) {
    }
}
