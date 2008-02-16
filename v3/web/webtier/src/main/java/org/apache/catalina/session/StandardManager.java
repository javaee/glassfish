

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.security.SecurityUtil;

/**
 * Standard implementation of the <b>Manager</b> interface that provides
 * simple session persistence across restarts of this component (such as
 * when the entire server is shut down and restarted, or when a particular
 * web application is reloaded.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision: 1.13 $ $Date: 2007/01/04 01:31:58 $
 */

public class StandardManager
    extends ManagerBase
    implements Lifecycle, PropertyChangeListener {

    // ---------------------------------------------------- Security Classes
    private class PrivilegedDoLoad
        implements PrivilegedExceptionAction {

        PrivilegedDoLoad() {           
        }

        public Object run() throws Exception{
           doLoad();
           return null;
        }                       
    }
        
    private class PrivilegedDoUnload
        implements PrivilegedExceptionAction {

        private boolean expire;

        PrivilegedDoUnload(boolean expire) {
            this.expire = expire;
        }

        public Object run() throws Exception{
            doUnload(expire);
            return null;
        }            
           
    }        

    
    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "StandardManager/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    private int maxActiveSessions = -1;


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static final String name = "StandardManager";


    /**
     * Path name of the disk file in which active sessions are saved
     * when we stop, and from which these sessions are loaded when we start.
     * A <code>null</code> value indicates that no persistence is desired.
     * If this pathname is relative, it will be resolved against the
     * temporary working directory provided by our context, available via
     * the <code>javax.servlet.context.tempdir</code> context attribute.
     */
    private String pathname = "SESSIONS.ser";


    /**
     * Has this component been started yet?
     */
    private boolean started = false;

    // START SJSAS 6359401
    /*
     * The absolute path name of the file where sessions are persisted.
     */
    private String absPathName;
    // END SJSAS 6359401

    int rejectedSessions=0;
    long processingTime=0;


    // ------------------------------------------------------------- Properties


    /**
     * Set the Container with which this Manager has been associated.  If
     * it is a Context (the usual case), listen for changes to the session
     * timeout property.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        // Default processing provided by our superclass
        super.setContainer(container);

        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
            setMaxInactiveIntervalSeconds
                ( ((Context) this.container).getSessionTimeout()*60 );
            ((Context) this.container).addPropertyChangeListener(this);
        }

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }

    /** Number of session creations that failed due to maxActiveSessions
     *
     * @return
     */
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Set the maximum number of actives Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        support.firePropertyChange("maxActiveSessions",
                                   Integer.valueOf(oldMaxActiveSessions),
                                   Integer.valueOf(this.maxActiveSessions));

    }


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }


    /**
     * Return the session persistence pathname, if any.
     */
    public String getPathname() {

        return (this.pathname);

    }


    /**
     * Set the session persistence pathname to the specified value.  If no
     * persistence support is desired, set the pathname to <code>null</code>.
     *
     * @param pathname New session persistence pathname
     */
    public void setPathname(String pathname) {

        String oldPathname = this.pathname;
        this.pathname = pathname;
        support.firePropertyChange("pathname", oldPathname, this.pathname);

    }


    // --------------------------------------------------------- Public Methods

    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {

        if ((maxActiveSessions >= 0) &&
          (sessions.size() >= maxActiveSessions)) {
            rejectedSessions++;
            throw new IllegalStateException
                (sm.getString("standardManager.createSession.ise"));
        }

        return (super.createSession());

    }


    /*
     * Releases any resources held by this session manager.
     */
    public void release() {
        super.release();
        clearStore();
    }


    // START SJSAS 6359401
    /*
     * Deletes the persistent session storage file.
     */
    public void clearStore() {
        File file = file();
        if (file != null && file.exists()) {
            file.delete();
        }
    }
    // END SJSAS 6359401


    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    public void load() throws ClassNotFoundException, IOException {
        if (SecurityUtil.isPackageProtectionEnabled()){   
            try{
                AccessController.doPrivileged( new PrivilegedDoLoad() );
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                if (exception instanceof ClassNotFoundException){
                    throw (ClassNotFoundException)exception;
                } else if (exception instanceof IOException){
                    throw (IOException)exception;
                }
                if (log.isDebugEnabled())
                    log.debug("Unreported exception in load() "
                        + exception);                
            }
        } else {
            doLoad();
        }       
    }


    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    private void doLoad() throws ClassNotFoundException, IOException {    
        if (log.isDebugEnabled())
            log.debug("Start: Loading persisted sessions");

        // Initialize our internal data structures
        sessions.clear();

        // Open an input stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        if (log.isDebugEnabled())
            log.debug(sm.getString("standardManager.loading", pathname));
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null) {
                IOUtilsCaller caller = getWebUtilsCaller();
                if (caller != null) {
                    try {
                        ois = caller.createObjectInputStream(
                                        bis, true, classLoader);
                    } catch (Exception ex) {}
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating custom object input stream for class loader ");
                    }
                    ois = new CustomObjectInputStream(bis, classLoader);
                }
            }
            if (ois == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating standard object input stream");
                }
                ois = new ObjectInputStream(bis);
            }
        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled())
                log.debug("No persisted data file found");
            return;
        } catch (IOException e) {
            log.error(sm.getString("standardManager.loading.ioe", e), e);
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }

        // Load the previously unloaded active sessions
        synchronized (sessions) {
            try {
                Integer count = (Integer) ois.readObject();
                int n = count.intValue();
                if (log.isDebugEnabled())
                    log.debug("Loading " + n + " persisted sessions");
                for (int i = 0; i < n; i++) {
                    StandardSession session =
                        StandardSession.deserialize(ois, this);
                    session.setManager(this);
                    sessions.put(session.getIdInternal(), session);
                    session.activate();
                }
            } catch (ClassNotFoundException e) {
              log.error(sm.getString("standardManager.loading.cnfe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } catch (IOException e) {
              log.error(sm.getString("standardManager.loading.ioe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } finally {
                // Close the input stream
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException f) {
                    // ignored
                }

                // Delete the persistent storage file
                if (file != null && file.exists() )
                    file.delete();
            }
        }

        if (log.isDebugEnabled())
            log.debug("Finish: Loading persisted sessions");
    }


    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @exception IOException if an input/output error occurs
     */
    public void unload() throws IOException {
        unload(true);
    }

    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @doExpire true if the unloaded sessions are to be expired, false
     * otherwise
     *
     * @exception IOException if an input/output error occurs
     */        
    protected void unload(boolean doExpire) throws IOException {
        if (SecurityUtil.isPackageProtectionEnabled()){       
            try{
                AccessController.doPrivileged( new PrivilegedDoUnload(doExpire) );
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                if (exception instanceof IOException){
                    throw (IOException)exception;
                }
                if (log.isDebugEnabled())
                    log.debug("Unreported exception in unLoad() "
                        + exception);                
            }        
        } else {
            doUnload(doExpire);
        }       
    }
        
    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @doExpire true if the unloaded sessions are to be expired, false
     * otherwise
     *
     * @exception IOException if an input/output error occurs
     */
    private void doUnload(boolean doExpire) throws IOException {   

        if (log.isDebugEnabled())
            log.debug("Unloading persisted sessions");

        // Open an output stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        
        //HERCULES: add
        boolean haveValidDirectory = isDirectoryValidFor(file.getAbsolutePath());
        if(!haveValidDirectory)
            return;
        //end HERCULES: add         
        
        if (log.isDebugEnabled())
            log.debug(sm.getString("standardManager.unloading", pathname));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            IOUtilsCaller caller = getWebUtilsCaller();
            if (caller != null) {
                try {
                    oos = caller.createObjectOutputStream(
                                new BufferedOutputStream(fos), true);
                } catch (Exception ex) {}
            }
            // Use normal ObjectOutputStream if there is a failure during
            // stream creation
            if (oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(fos)); 
            }
        } catch (IOException e) {
            log.error(sm.getString("standardManager.unloading.ioe", e), e);
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
            throw e;
        }

        // Write the number of active sessions, followed by the details
        ArrayList list = new ArrayList();
        synchronized (sessions) {
            if (log.isDebugEnabled())
                log.debug("Unloading " + sessions.size() + " sessions");
            try {
                // START SJSAS 6375689
                Session actSessions[] = findSessions();
                if (actSessions != null) {
                    for (int i = 0; i < actSessions.length; i++) {
                        StandardSession session = (StandardSession)
                            actSessions[i];
                        ((StandardSession) session).passivate();
                    }
                }
                // END SJSAS 6375689
                oos.writeObject(Integer.valueOf(sessions.size()));
                Iterator elements = sessions.values().iterator();
                while (elements.hasNext()) {
                    StandardSession session =
                        (StandardSession) elements.next();
                    list.add(session);
                    /* SJSAS 6375689
                    ((StandardSession) session).passivate();
                    */
                    oos.writeObject(session);
                }
            } catch (IOException e) {
                log.error(sm.getString("standardManager.unloading.ioe", e), e);
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException f) {
                        ;
                    }
                    oos = null;
                }
                throw e;
            }
        }

        // Flush and close the output stream
        try {
            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e) {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
            throw e;
        }

        if (doExpire) {
            // Expire all the sessions we just wrote
            if (log.isDebugEnabled())
                log.debug("Expiring " + list.size() + " persisted sessions");
            Iterator expires = list.iterator();
            while (expires.hasNext()) {
                StandardSession session = (StandardSession) expires.next();
                try {
                    session.expire(false);
                } catch (Throwable t) {
                    ;
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Unloading complete");

    }
    
    
    /**
     * Check if the directory for this full qualified file
     * exists and is valid
     * Hercules: added method
     */    
    private boolean isDirectoryValidFor(String fullPathFileName) {
        int lastSlashIdx = fullPathFileName.lastIndexOf(File.separator);
        if(lastSlashIdx == -1) {
            return false;
        }
        String result = fullPathFileName.substring(0, lastSlashIdx);
        //System.out.println("PATH name = " + result);
        File file = new File(result);
        boolean isDirValid = file.isDirectory();
        //System.out.println("IS DIR VALID: " + result);
        return isDirValid;
    }    


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        if( ! initialized )
            init();
        
        // Validate and update our current component state
        if (started) {
            log.info(sm.getString("standardManager.alreadyStarted"));
            return;
        }
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Force initialization of the random number generator
        if (log.isTraceEnabled())
            log.trace("Force random number initialization starting");
        String dummy = generateSessionId();
        if (log.isTraceEnabled())
            log.trace("Force random number initialization completed");

        // Load unloaded sessions, if any
        try {
            load();
        } catch (Throwable t) {
            log.error(sm.getString("standardManager.managerLoad"), t);
        }

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        if (log.isDebugEnabled())
            log.debug("Stopping");
        
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Write out sessions
        try {
            unload(false);
        } catch (IOException e) {
            log.error(sm.getString("standardManager.managerUnload"), e);
        }

        // Expire all active sessions and notify their listeners
        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];
            if (!session.isValid())
                continue;
            try {
                session.expire();
            } catch (Throwable t) {
                ;
            }
        }

        // Require a new random number generator if we are restarted
        this.random = null;

        if( initialized ) {
            destroy();
        }
    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process property change events from our associated Context.
     *
     * @param event The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        // Validate the source of this event
        if (!(event.getSource() instanceof Context))
            return;
        Context context = (Context) event.getSource();

        // Process a relevant property change
        if (event.getPropertyName().equals("sessionTimeout")) {
            try {
                setMaxInactiveIntervalSeconds
                    ( ((Integer) event.getNewValue()).intValue()*60 );
            } catch (NumberFormatException e) {
                log.error(sm.getString("standardManager.sessionTimeout",
                                 event.getNewValue().toString()));
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return a File object representing the pathname to our
     * persistence file, if any.
     */
    private File file() {

        // START SJSAS 6359401
        if (absPathName != null) {
            return new File(absPathName);
        }
        // END SJSAS 6359401

        if ((pathname == null) || (pathname.length() == 0))
            return (null);
        File file = new File(pathname);
        if (!file.isAbsolute()) {
            if (container instanceof Context) {
                ServletContext servletContext =
                    ((Context) container).getServletContext();
                File tempdir = (File)
                    servletContext.getAttribute(Globals.WORK_DIR_ATTR);
                if (tempdir != null)
                    file = new File(tempdir, pathname);
            }
        }

        // START SJSAS 6359401
        if (file != null) {
            absPathName = file.getAbsolutePath();
        }
        // END SJSAS 6359401

        return (file);

    }


    /**
     * Invalidate all sessions that have expired.
     */
    public void processExpires() {

        long timeNow = System.currentTimeMillis();

        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            sessions[i].isValid();
        }

        long timeEnd = System.currentTimeMillis();
        processingTime += ( timeEnd - timeNow );
    }

}
