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
package oracle.toplink.essentials.platform.server.sunas;

import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.transaction.sunas.SunAS9TransactionController;
import oracle.toplink.essentials.platform.server.ServerPlatformBase;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.logging.JavaLog;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing SunAS9-specific server behaviour.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use an SunAS9-specific controller class
 *
 */
public class SunAS9ServerPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public SunAS9ServerPlatform(DatabaseSessionImpl newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * for Oc4j. This is read-only.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see ServerPlatformBase.isJTAEnabled()
     * @see ServerPlatformBase.disableJTA()
     * @see ServerPlatformBase.initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
    	if (externalTransactionControllerClass == null){
    		externalTransactionControllerClass = SunAS9TransactionController.class;
    	}
        return externalTransactionControllerClass;
    }

    public SessionLog getServerLog() {
        return  new JavaLog();
    }
}
