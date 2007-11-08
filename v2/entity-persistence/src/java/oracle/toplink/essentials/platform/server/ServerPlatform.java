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

/**
 * PUBLIC:
 *
 * This is the interface describing the behaviour for ServerPlatformBase, and any other
 * class that wants to provide behaviour for a server.
 *
 * This interface provides the behaviour for
 *
 * - Which external transaction controller to use
 * - Whether or not to enable JTA (external transaction control)
 * - How to register/unregister for runtime services (JMX/MBean)
 * - Whether or not to enable runtime services
 * - How to launch container Threads
 *
 * Any subclasses of ServerPlatformBase created by the user must implement this interface.
 *
 * public API:
 *
 *  String getServerNameAndVersion()
 *
 * @see ServerPlatformBase
 */
public interface ServerPlatform {

    /**
     * INTERNAL: getDatabaseSession(): Answer the instance of DatabaseSession the receiver is helping.
     *
     * @return DatabaseSession
     */
    public abstract DatabaseSessionImpl getDatabaseSession();

    /**
     * PUBLIC: getServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     *
     * @return String serverNameAndVersion
     */
    public abstract String getServerNameAndVersion();

    /**
     * INTERNAL: getModuleName(): Answer the name of the module (jar name) that my session
       * is associated with.
       * Answer "unknown" if there is no module name available.
     *
     * @return String moduleName
     */
    public abstract String getModuleName();

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * For this server platform. This is read-only.
     *
     * If the subclasses of the ServerPlatformBase do not provide the Class desired, then
     * a new subclass should be created to return the desired class.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public abstract Class getExternalTransactionControllerClass();

    /**
     * INTERNAL: setExternalTransactionControllerClass(Class newClass): Set the class of external
     * transaction controller to use in the DatabaseSession.
     * This is defined by the user via the sessions.xml.
     *
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public void setExternalTransactionControllerClass(Class newClass);
    
    /**
     * INTERNAL: initializeExternalTransactionController(): Populate the DatabaseSession's
     * external transaction controller with an instance of my transaction controller class.
     *
     * To change the external transaction controller class, we recommend creating a subclass of
       * ServerPlatformBase, and overriding getExternalTransactionControllerClass()
       *
       * @see ServerPlatformBase
     *
     * @return void
     *
     */
    public abstract void initializeExternalTransactionController();

    /**
     * INTERNAL: isJTAEnabled(): Answer true if the DatabaseSession's external transaction controller class will
     * be populated with my transaction controller class at runtime. If the transaction controller class is
     * overridden in the DatabaseSession, my transaction controller class will be ignored.
     *
     * @return boolean isJTAEnabled
     * @see #getExternalTransactionControllerClass()
     * @see #disableJTA()
     */
    public abstract boolean isJTAEnabled();

    /**
     * INTERNAL: disableJTA(): Configure the receiver such that my external transaction controller class will
     * be ignored, and will NOT be used to populate DatabaseSession's external transaction controller class
     * at runtime.
     *
     * @return void
     * @see #getExternalTransactionControllerClass()
     * @see #isJTAEnabled()
     */
    public abstract void disableJTA();

    /**
     * INTERNAL: isRuntimeServicesEnabled(): Answer true if the JMX/MBean providing runtime services for
     * the receiver's DatabaseSession will be deployed at runtime.
     *
     * @return boolean isRuntimeServicesEnabled
     * @see #disableRuntimeServices()
     */
    public abstract boolean isRuntimeServicesEnabled();

    /**
     * INTERNAL: disableRuntimeServices(): Configure the receiver such that no JMX/MBean will be registered
     * to provide runtime services for my DatabaseSession at runtime.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     */
    public abstract void disableRuntimeServices();

    /**
     * INTERNAL: registerMBean(): Create and deploy the JMX MBean to provide runtime services for my
     * databaseSession.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #unregisterMBean()
     */
    public abstract void registerMBean();

    /**
     * INTERNAL: unregisterMBean(): Unregister the JMX MBean that was providing runtime services for my
     * databaseSession.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #registerMBean()
     */
    public abstract void unregisterMBean();

    /**
     * INTERNAL:  This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * Oracle Specific support. (ie TIMESTAMPTZ)
     * This is added as a workaround for bug 4460996
     */
    public java.sql.Connection unwrapOracleConnection(Platform platform, java.sql.Connection connection);
    
    /**
     * INTERNAL: launchContainerRunnable(Runnable runnable): Use the container library to
     * start the provided Runnable.
     *
     * Default behaviour is to use Thread(runnable).start()
     *
     * @param Runnable runnable: the instance of runnable to be "started"
     * @return void
     */
    public void launchContainerRunnable(Runnable runnable);

    /**
     * INTERNAL: getServerLog(): Return the ServerLog for this platform
     *
     * Return the default ServerLog in the base
     *
     * @return oracle.toplink.essentials.logging.SessionLog
     */
    public oracle.toplink.essentials.logging.SessionLog getServerLog();
}
