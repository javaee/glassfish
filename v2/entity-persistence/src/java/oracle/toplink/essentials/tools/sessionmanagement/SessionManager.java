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
package oracle.toplink.essentials.tools.sessionmanagement;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import oracle.toplink.essentials.logging.*;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Global session location.</p>
 *
 * <p><b>Description</b>: This allows for a global session local which can
 * be accessed globally from other classes.  This is needed for EJB data stores
 * as they must have a globally accessible place to access the session.
 * This can be by EJB session beans, BMP beans and CMP beans as well as Servlets and
 * other three-tier services.</p>
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Store a global session.
 * <li> Allow the storage of alternative sessions as well.
 * </ul></p>
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public class SessionManager {    
    protected static SessionManager manager;
    protected AbstractSession defaultSession;
    protected Hashtable sessions = new Hashtable();
    
    /**
     * PUBLIC:
     * The default constructor to create a new session manager.
     */
    public SessionManager() {
        sessions = new Hashtable(5);
    }

    /**
       * INTERNAL:
       * add an named session to the hashtable.
       * session must have a name prior to setting into session Manager
       */
    public void addSession(Session session) {
        getSessions().put(session.getName(), session);
    }

    /**
     * ADVANCED:
     * add an named session to the hashtable.
     */
    public void addSession(String sessionName, Session session) {
        session.setName(sessionName);
        getSessions().put(sessionName, session);
    }

    /**
     * PUBLIC:
     * Return the default session.
     */
    public Session getDefaultSession() {
        if (defaultSession == null) {
            defaultSession = getSession("default");
        }
        return defaultSession;
    }

    /**
     * INTERNAL:
     * Destroy the session defined by sessionName on this manager.
     */
    public void destroySession(String sessionName) {
        DatabaseSession session = (DatabaseSession)getSessions().get(sessionName);

        if (session != null) {
            destroy(session);
        } else {
            logAndThrowException(SessionLog.WARNING, ValidationException.noSessionRegisteredForName(sessionName));
        }
    }

    private void destroy(DatabaseSession session) {
        if (session.isConnected()) {
            session.logout();
        }

        sessions.remove(session.getName());
        session = null;
    }

    /**
     * INTERNAL:
     * Destroy all sessions held onto by this manager.
     */
    public void destroyAllSessions() {
        Enumeration toRemoveSessions = getSessions().elements();

        while (toRemoveSessions.hasMoreElements()) {
            destroy((DatabaseSession)toRemoveSessions.nextElement());
        }
    }

    private ClassLoader getMyClassLoader(){
        ClassLoader classLoader = null;
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try{
                return (ClassLoader)AccessController.doPrivileged(new PrivilegedGetClassLoaderForClass(this.getClass()));
            } catch (PrivilegedActionException exc){
                return null;
            }
        } else {
            return PrivilegedAccessHelper.getClassLoaderForClass(this.getClass());
        }
    }
    
    /**
     * PUBLIC:
     * Return the singleton session manager.
     * This allow global access to a set of named sessions.
     */
    public synchronized static SessionManager getManager() {
        if (manager == null) {
            manager = new SessionManager();
        }

        return manager;
    }

    /**
     * PUBLIC:
     * Return the session by name.
     * Use the classLoader that loaded the SessionManager.
     */
    public AbstractSession getSession(String sessionName) {
        return getSession(sessionName, getMyClassLoader(), true, false, false);
    }

    /**
     * PUBLIC:
     * Return the session by name.
     * Use the classLoader that loaded the SessionManager.
     * Log the session in only if the user specifies to.
     */
    public AbstractSession getSession(String sessionName, boolean shouldLoginSession) {
        return getSession(sessionName, getMyClassLoader(), shouldLoginSession, false, false);
    }

    /**
     * PUBLIC:
     * Return the session by name.
     * Use the classLoader that loaded the SessionManager.
     * Log the session in only if the user specifies to.
     * Refresh the session only if the user specifies to.
     */
    public AbstractSession getSession(String sessionName, boolean shouldLoginSession, boolean shouldRefreshSession) {
        return getSession(sessionName, getMyClassLoader(), shouldLoginSession, shouldRefreshSession, false);
    }

    /**
     * PUBLIC:
     * Return the session by name.
     * Provide the class loader for loading the project, the configuration file
     * and the deployed classes.
     * E.g. SessionManager.getManager().getSession("mySession", MySessionBean.getClassLoader());
     * This method will cause the class loader to be compared with the classloader
     * used to load the original session of this name, with this classloader.  If
     * they are not the same then the session will be refreshed.
     */
    public AbstractSession getSession(String sessionName, ClassLoader objectClassLoader) {
        return getSession(sessionName, objectClassLoader, true, false, false);
    }

    /**
     * PUBLIC:
     * Return the session by name, loading the configuration from the file
     * specified in the xmlLoader. Provide the class loader for loading the
     * project, the configuration file and the deployed classes. Pass in true for
     * shouldLoginSession if the session returned should be logged in before
     * returned otherwise false. Pass in true for shouldRefreshSession if the
     * XMLSessionConfigLoader should reparse the configuration file for new
     * sessions. False, will cause the XMLSessionConfigLoader not to parse the
     * file again.
     * This method will cause the class loader to be compared with the classloader
     * used to load the original session of this name, with this classloader.  If
     * they are not the same then the session will be refreshed.
     */
    public synchronized AbstractSession getSession(String sessionName, ClassLoader objectClassLoader, boolean shouldLoginSession, boolean shouldRefreshSession, boolean shouldCheckClassLoader) {
        AbstractSession session = (AbstractSession)getSessions().get(sessionName);
        if (shouldCheckClassLoader && (session != null) && !session.getDatasourcePlatform().getConversionManager().getLoader().equals(objectClassLoader)) {
            //bug 3766808  if a different classloader is being used then a reload of the session should
            //be completed otherwise failures may occur
            shouldRefreshSession = true;
        }
        if ((session == null) || shouldRefreshSession) {
            if (session != null) {
                if (session.isDatabaseSession() && session.isConnected()) {
                    ((DatabaseSession)session).logout();
                }

                getSessions().remove(sessionName);
            }
        }

        if (session == null) {
            logAndThrowException(SessionLog.WARNING, ValidationException.noSessionFound(sessionName, ""));
        } else if (shouldLoginSession && !session.isConnected()) {
            ((DatabaseSession)session).login();
        }

        return session;
    }

    /**
     * INTERNAL:
     * Log exceptions to the default log then throw them.
     */
    private void logAndThrowException(int level, RuntimeException exception) throws RuntimeException {
        AbstractSessionLog.getLog().logThrowable(level, exception);
        throw exception;
    }

    /**
     * INTERNAL:
     * Set a hashtable of all sessions
     */
    public void setSessions(Hashtable sessions) {
        this.sessions = sessions;
    }

    /**
     * INTERNAL:
     * Return a hashtable on all sessions.
     */
    public Hashtable getSessions() {
        return sessions;
    }

    /**
     * PUBLIC:
     * Set the default session.
     * Other sessions are supported through the getSession by name API.
     */
    public void setDefaultSession(Session defaultSession) {
        this.defaultSession = (AbstractSession)defaultSession;
        addSession("default", defaultSession);
    }

    /**
     * INTERNAL:
     * Set the singleton session manager.
     * This allows global access to a set of named sessions.
     */
    public static void setManager(SessionManager theManager) {
        manager = theManager;
    }
}
