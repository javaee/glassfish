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
import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.logging.DefaultSessionLog;

/**
 *
 * PUBLIC:
 *
 * This platform is used when TopLink is not within any server (Oc4j, WebLogic, ...)
 * This is also the default platform for all newly created DatabaseSessions.
 *
 * This platform has:
 *
 * - No external transaction controller class
 * - No runtime services (JMX/MBean)
 * - No launching of container Threads
 *
 */
public final class NoServerPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: Initialize so that runtime services and JTA are disabled.
     */
    public NoServerPlatform(DatabaseSessionImpl newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
        ;
        this.disableJTA();
    }

    /**
     * PUBLIC: getServerNameAndVersion(): Answer null because this does not apply to NoServerPlatform.
     *
     * @return String serverNameAndVersion
     */
    public String getServerNameAndVersion() {
        return null;
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer null because this does not apply.
     *
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
        return null;
    }

    /**
     * INTERNAL: launchContainerThread(Thread thread): Do nothing because container Threads are not launchable
     * in this platform
     *
     * @param Thread thread : the instance of Thread
     * @return void
     */
    public void launchContainerThread(Thread thread) {
    }

    /**
     * INTERNAL: getServerLog(): Return the ServerLog for this platform
     *
     * Return the default ServerLog in the base
     *
     * @return oracle.toplink.essentials.logging.SessionLog
     */
    public oracle.toplink.essentials.logging.SessionLog getServerLog() {
        return new DefaultSessionLog();
    }    

    /**
     * INTERNAL:
     * When there is no server, the original connection will be returned
     */
    public java.sql.Connection unwrapOracleConnection(Platform platform, java.sql.Connection connection){
        return connection;
    }
}
