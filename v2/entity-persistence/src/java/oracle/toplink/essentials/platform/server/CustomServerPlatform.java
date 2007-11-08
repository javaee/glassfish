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
package oracle.toplink.essentials.platform.server;

import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.platform.server.ServerPlatformBase;

/**
 * INTERNAL:
 *
 * This is the concrete subclass responsible for handling backward compatibility for 9.0.4.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use a user-specified controller class
 *
 * This platform adds:
 *
 * setExternalTransactionControllerClass(Class newClass): to allow the user to define
 * the external transaction controller when the 904 sessions.xml defines an
 * external-transaction-controller-class.
 *
 */
public final class CustomServerPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: JTA is disabled until a transaction controller class is set.
       * Runtime services are disabled.
     */
    public CustomServerPlatform(DatabaseSessionImpl newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * in the DatabaseSession
     * This is defined by the user via the 904 sessions.xml.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
        return externalTransactionControllerClass;
    }

    /**
     * INTERNAL: externalTransactionControllerNotNullWarning():
       * When the external transaction controller is being initialized, we warn the developer
       * if they have already defined the external transaction controller in some way other
       * than subclassing ServerPlatformBase.
       *
       * This warning is omitted in 9.0.4.
     *
       * @see ServerPlatformBase
     *
     * @return void
     *
     */
    protected void externalTransactionControllerNotNullWarning() {
        //do nothing, because it would be really annoying to show a warning here
    }
}
