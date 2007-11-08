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

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.essentials.sessions.ExternalTransactionController;
import oracle.toplink.essentials.internal.localization.ToStringLocalization;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.essentials.internal.databaseaccess.Platform;

/**
 * Implementation of oracle.toplink.essentials.platform.server.ServerPlatform
 *
 * PUBLIC:
 *
 * This is the abstract superclass of all platforms for all servers. Each DatabaseSession
 * contains an instance of the receiver, to help the DatabaseSession determine:
 *
 * - Which external transaction controller to use
 * - Whether or not to enable JTA (external transaction control)
 * - How to register/unregister for runtime services (JMX/MBean)
 * - Whether or not to enable runtime services
 * - How to launch container Threads
 *
 * Subclasses already exist to provide configurations for Oc4J, WebLogic, and WebSphere.
 *
 * If the user wants a different external transaction controller class or
 * to provide some different behaviour than the provided ServerPlatform(s), we recommend
 * subclassing oracle.toplink.essentials.platform.server.ServerPlatformBase (or a subclass),
 * and overriding:
 *
 * ServerPlatformBase.getExternalTransactionControllerClass()
 * ServerPlatformBase.registerMBean()
 * ServerPlatformBase.unregisterMBean()
 *
 * for the desired behaviour.
 *
 * @see oracle.toplink.essentials.platform.server.ServerPlatformBase
 *
 * public API:
 *
 * String getServerNameAndVersion()
 */
public abstract class ServerPlatformBase implements ServerPlatform {

    /**
     * externalTransactionControllerClass: This is a user-specifiable class defining the class
     * of external transaction controller to be set into the DatabaseSession
     */
    protected Class externalTransactionControllerClass;
	
    /**
     * INTERNAL:
     * isRuntimeServicesEnabled: Determines if the JMX Runtime Services will be deployed at runtime
     */
    private boolean isRuntimeServicesEnabled;

    /**
     * INTERNAL:
     * isJTAEnabled: Determines if the external transaction controller will be populated into the DatabaseSession
     * at runtime
     */
    private boolean isJTAEnabled;

    /**
     * INTERNAL:
     * isCMP: true if the container created the server platform, because we're configured
     * for CMP.
     */
    private boolean isCMP;
    
    /**
     * INTERNAL:
     * databaseSession: The instance of DatabaseSession that I am helping.
     */
    private DatabaseSessionImpl databaseSession;

    /**
     * INTERNAL:
     * Default Constructor: Initialize so that runtime services and JTA are enabled. Set the DatabaseSession that I
     * will be helping.
     */
    public ServerPlatformBase(DatabaseSessionImpl newDatabaseSession) {
        this.isRuntimeServicesEnabled = true;
        this.isJTAEnabled = true;
        this.databaseSession = newDatabaseSession;
        this.setIsCMP(false);
    }

    /**
     * INTERNAL: getDatabaseSession(): Answer the instance of DatabaseSession the receiver is helping.
     *
     * @return DatabaseSession databaseSession
     */
    public DatabaseSessionImpl getDatabaseSession() {
        return this.databaseSession;
    }

    /**
     * PUBLIC: getServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     *
     * Default is "unknown"
     *
     * @return String serverNameAndVersion
     */
    public String getServerNameAndVersion() {
        return ToStringLocalization.buildMessage("unknown");
    }

    /**
     * INTERNAL: getModuleName(): Answer the name of the module (jar name) that my session
       * is associated with.
       * Answer "unknown" if there is no module name available.
       *
       * Default behaviour is to return "unknown".
     *
     * @return String moduleName
     */
    public String getModuleName() {
        return "unknown";
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * For this server platform. This is read-only.
     *
       * If the user wants a different external transaction controller class than the provided ServerPlatform(s),
       * we recommend subclassing oracle.toplink.essentials.platform.server.ServerPlatformBase (or a subclass),
       * and overriding:
       *
       * ServerPlatformBase.getExternalTransactionControllerClass()
       *
       * for the desired behaviour.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.essentials.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
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
    public void setExternalTransactionControllerClass(Class newClass) {
        this.externalTransactionControllerClass = newClass;
    }
    
    /**
     * INTERNAL: initializeExternalTransactionController(): Populate the DatabaseSession's
     * external transaction controller with an instance of my transaction controller class.
     *
     * To change the external transaction controller class, we recommend creating a subclass of
       * ServerPlatformBase, and overriding getExternalTransactionControllerClass().
       *
       * @see ServerPlatformBase
     *
     * @return void
     *
     */
    public void initializeExternalTransactionController() {
        this.ensureNotLoggedIn();

        //BUG 3975114: Even if JTA is disabled, override if we're in CMP
        //JTA must never be disable during CMP (WLS/Oc4j)
        if (!isJTAEnabled() && !isCMP()) {
            return;
        }
        //BUG 3975114: display a warning if JTA is disabled and we're in CMP
        if (!isJTAEnabled() && isCMP()) {
            AbstractSessionLog.getLog().warning("jta_cannot_be_disabled_in_cmp");
        }

        //check if the transaction controller class is overridden by a preLogin or equivalent,
        //or if the transaction controller was already defined, in which case they should have written 
        //a subclass. Show a warning
        try {
            if (getDatabaseSession().getExternalTransactionController() != null) {
                this.externalTransactionControllerNotNullWarning();
                return;
            }
            ExternalTransactionController controller = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    controller = (ExternalTransactionController)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(this.getExternalTransactionControllerClass()));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof InstantiationException) {
                        throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
                    } else {
                        throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
                    }
                }
            } else {
                controller = (ExternalTransactionController)PrivilegedAccessHelper.newInstanceFromClass(this.getExternalTransactionControllerClass());
            }
            getDatabaseSession().setExternalTransactionController(controller);
        } catch (InstantiationException instantiationException) {
            throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
        } catch (IllegalAccessException illegalAccessException) {
            throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
        }
    }

    /**
     * INTERNAL: externalTransactionControllerNotNullWarning():
     * When the external transaction controller is being initialized, we warn the developer
     * if they have already defined the external transaction controller in some way other
     * than subclassing ServerPlatformBase.
     *
     * @see #getExternalTransactionControllerClass()
     */
    protected void externalTransactionControllerNotNullWarning() {
        getDatabaseSession().warning("External_transaction_controller_not_defined_by_server_platform", SessionLog.EJB);
    }

    /**
     * INTERNAL: isJTAEnabled(): Answer true if the DatabaseSession's external transaction controller class will
     * be populated with my transaction controller class at runtime. If the transaction controller class is
     * overridden in the DatabaseSession, my transaction controller class will be ignored.
     *
     * Answer true if TopLink will be configured to register for callbacks for beforeCompletion and afterCompletion.
     *
     * @return boolean isJTAEnabled
     * @see #getExternalTransactionControllerClass()
     * @see #disableJTA()
     */
    public boolean isJTAEnabled() {
        return this.isJTAEnabled;
    }

    /**
     * INTERNAL: disableJTA(): Configure the receiver such that my external transaction controller class will
     * be ignored, and will NOT be used to populate DatabaseSession's external transaction controller class
     * at runtime.
       *
       * TopLink will NOT be configured to register for callbacks for beforeCompletion and afterCompletion.
     *
     * @return void
     * @see #getExternalTransactionControllerClass()
     * @see #isJTAEnabled()
     */
    public void disableJTA() {
        this.ensureNotLoggedIn();
        this.isJTAEnabled = false;
    }

    /**
     * INTERNAL: isRuntimeServicesEnabled(): Answer true if the JMX/MBean providing runtime services for
     * the receiver's DatabaseSession will be deployed at runtime.
     *
     * @return boolean isRuntimeServicesEnabled
     * @see #disableRuntimeServices()
     */
    public boolean isRuntimeServicesEnabled() {
        return this.isRuntimeServicesEnabled;
    }

    /**
     * INTERNAL: disableRuntimeServices(): Configure the receiver such that no JMX/MBean will be registered
     * to provide runtime services for my DatabaseSession at runtime.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     */
    public void disableRuntimeServices() {
        this.ensureNotLoggedIn();
        this.isRuntimeServicesEnabled = false;
    }

    /**
     * INTERNAL: registerMBean(): Create and deploy the JMX MBean to provide runtime services for my
     * databaseSession.
       *
       * Default is to do nothing.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #unregisterMBean()
     */
    public void registerMBean() {
        if (!this.isRuntimeServicesEnabled()) {
            return;
        }
        this.serverSpecificRegisterMBean();
    }

    /**
     * INTERNAL: serverSpecificRegisterMBean(): Server specific implementation of the
     * creation and deployment of the JMX MBean to provide runtime services for my
     * databaseSession.
     *
     * Default is to do nothing. This should be subclassed if required.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #registerMBean()
     */
    public void serverSpecificRegisterMBean() {
    }

    /**
     * INTERNAL: unregisterMBean(): Unregister the JMX MBean that was providing runtime services for my
     * databaseSession.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #registerMBean()
     */
    public void unregisterMBean() {
        if (!this.isRuntimeServicesEnabled()) {
            return;
        }
        this.serverSpecificUnregisterMBean();
    }

    /**
     * INTERNAL:  This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * Oracle Specific support. (ie TIMESTAMPTZ)
     * This is added as a workaround for bug 4460996
     */
    public java.sql.Connection unwrapOracleConnection(Platform platform, java.sql.Connection connection){
        try {
            return connection.getMetaData().getConnection();
        } catch (java.sql.SQLException e){
            ((DatabaseSessionImpl)getDatabaseSession()).log(SessionLog.WARNING, SessionLog.CONNECTION, "cannot_unwrap_connection", e);
            return connection;            
        }
    }  
    

    /**
     * INTERNAL: serverSpecificUnregisterMBean(): Server specific implementation of the
     * unregistration of the JMX MBean from its server.
     *
     * Default is to do nothing. This should be subclassed if required.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     */
    public void serverSpecificUnregisterMBean() {
    }

    /**
     * INTERNAL: launchContainerRunnable(Runnable runnable): Use the container library to
     * start the provided Runnable.
     *
     * Default behaviour is to use Thread(runnable).start()
     *
     * @param Runnable runnable: the instance of runnable to be "started"
     * @return void
     */
    public void launchContainerRunnable(Runnable runnable) {
        new Thread(runnable).start();
    }

    /**
     * INTERNAL: Make sure that the DatabaseSession has not logged in yet.
       * Throw a ValidationException if we have.
     *
     */
    protected void ensureNotLoggedIn() {
        //RCM: Allow for a null database session
        if (getDatabaseSession() == null) {
            return;
        }
        if (getDatabaseSession().isConnected()) {
            throw ValidationException.serverPlatformIsReadOnlyAfterLogin(this.getClass().getName());
        }
    }

    /**
     * INTERNAL: getServerLog(): Return the ServerLog for this platform
     *
     * Return the default ServerLog in the base
     *
     * @return oracle.toplink.essentials.logging.SessionLog
     */
    public oracle.toplink.essentials.logging.SessionLog getServerLog() {
        return new ServerLog();
    }
    
    /**
     * INTERNAL: isCMP(): Answer true if we're in the context of CMP (i.e. the container created me)
     *
     * @return boolean 
     */
    public boolean isCMP() {
        return isCMP;
    }

    /**
     * INTERNAL: setIsCMP(boolean): Define whether or not we're in the context of CMP (i.e. the container created me)
     *
     * @return void 
     */
    public void setIsCMP(boolean isThisCMP) {
        isCMP = isThisCMP;
    }

}
