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

package com.sun.ejb.base.sfsb;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.util.logging.*;
import com.sun.logging.*;

import com.sun.ejb.spi.sfsb.SFSBBeanState;
import com.sun.ejb.spi.sfsb.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.SFSBStoreManagerException;
import com.sun.ejb.spi.sfsb.SFSBStoreManagerConstants;
import com.sun.ejb.spi.sfsb.SFSBUUIDUtil;

import com.sun.ejb.spi.monitorable.sfsb.MonitorableSFSBStore;

import com.sun.appserv.util.cache.CacheListener;
import com.sun.ejb.containers.util.cache.PassivatedSessionCache;

/**
 * @author     Mahesh Kannan
 */
 
public abstract class AbstractFileStoreManager
    implements SFSBStoreManager, MonitorableSFSBStore,
        CacheListener
{
    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    protected File        baseDir;
    protected String      storeManagerName;

    protected int         passivationTimeoutInSeconds;

    private int         loadCount; 
    private int         loadSuccessCount; 
    private int         loadErrorCount; 
    private int         storeCount; 
    private int         storeSuccessCount; 
    private int         storeErrorCount; 
    private int         expiredSessionCount; 

    private boolean     shutdown;
    private PassivatedSessionCache  passivatedSessions;

    private Level	TRACE_LEVEL = Level.FINE;
    private ClassLoader	classLoader;

    /**
     * No arg constructor
     */
    public AbstractFileStoreManager() {
    }

    /****************************************************/
    /**** Implementation of SFSBStoreManager methods ****/
    /****************************************************/

    public void initSessionStore(Map storeEnv) {

        String baseDirName = (String) storeEnv.get(
                SFSBStoreManagerConstants.PASSIVATION_DIRECTORY_NAME);
        if (baseDirName == null) {
            baseDirName = ".";
        }
        baseDir = new File(baseDirName);

        this.storeManagerName = (String) storeEnv.get(
		SFSBStoreManagerConstants.STORE_MANAGER_NAME);

        this.classLoader = (ClassLoader) storeEnv.get(
		SFSBStoreManagerConstants.CLASS_LOADER);

        try {
            Integer sessionTimeout = (Integer) storeEnv.get(
                    SFSBStoreManagerConstants.SESSION_TIMEOUT_IN_SECONDS);
            passivationTimeoutInSeconds = sessionTimeout.intValue();
        } catch (Exception ex) {
        }

        try {
            if ((baseDir.mkdirs() == false) && (!baseDir.isDirectory())) {
		_logger.log(Level.WARNING, "ejb.sfsb_storemgr_mdirs_failed",
			new Object[] {baseDirName});
	    }

            passivatedSessions = new PassivatedSessionCache(
		    passivationTimeoutInSeconds * 1000);
            passivatedSessions.init(8192, null);
            passivatedSessions.addCacheListener(this);
            onInitialization();
        } catch (Exception ex) {
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_failed",
                    new Object[] {baseDirName});
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_exception", ex);
        }

    }
    
    protected void onInitialization() {
    }

    public SFSBBeanState getState(Object sessionKey) {

        String fileName = sessionKey.toString();
        SFSBBeanState beanState = null;

	if(_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, "[SFSBStore] Attempting to load session: "
			    + sessionKey);
	}

        if (passivatedSessions.remove(fileName) == null) {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBStore] Could not find "
			+ "state for session: " + sessionKey);
	    }
            return null;
        }

        File file = new File(baseDir, fileName);
        if (file.exists()) {
            int dataSize = (int) file.length();
            byte[] data = new byte[dataSize];
            BufferedInputStream bis = null;
            FileInputStream fis = null;
            try {
                loadCount++;
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                int offset = 0;
                for (int toRead = dataSize; toRead > 0; ) {
                    int count = bis.read(data, offset, toRead);
                    offset += count;
                    toRead -= count;
                }	
        
                beanState = new SFSBBeanState(sessionKey, -1, false, data);
                loadSuccessCount++;
		if(_logger.isLoggable(TRACE_LEVEL)) {
		    _logger.log(TRACE_LEVEL, "[SFSBStore] Successfully Loaded "
			    + "session: " + sessionKey);
		}
            } catch (Exception ex) {
                loadErrorCount++;
                _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_failed",
                    new Object[] {fileName});
                _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_exception", ex);
                remove(sessionKey);
            } finally {
                try {
		    bis.close();
		} catch (Exception ex) {
		    _logger.log(Level.FINE, "Error while closing buffered input stream", ex);
		}
                try {
		    fis.close();
		} catch (Exception ex) {
		    _logger.log(Level.FINE, "Error while closing file input stream", ex);
		}
            }
        } else {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBStore] Could not find passivated "
			+ "file for: " + sessionKey);
	    }
	}
        return beanState;
    }
     
    public void checkpointSave(SFSBBeanState[] beanStates,
            boolean transactionFlag)
    {
        //Not yet implemented
    }
 
    public void passivateSave(SFSBBeanState beanState) {
        saveState(beanState, true);
    }

    public void remove(Object sessionKey) {
        try {
            passivatedSessions.remove(sessionKey);
            removeFile(new File(baseDir, sessionKey.toString()));
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                "ejb.sfsb_storemgr_removestate_failed",
                new Object[] {sessionKey.toString()});
            _logger.log(Level.WARNING,
                "ejb.sfsb_storemgr_removestate_exception", ex);
        }
    }
      
    public void removeExpired() {
        if( shutdown ) {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "[SFSBStore] Server is being shutdown hence " 
                    + "method cannot be executed" );
		}
            return;
        }
        passivatedSessions.trimExpiredEntries(Integer.MAX_VALUE);
    }
      
    public void removeAll() {
        try {
	    String[] fileNames = baseDir.list();
	    for (int i=0; i<fileNames.length; i++) {
                remove(fileNames[i]);
	    }

	    if (baseDir.delete() == false) {
		Object[] params = {baseDir.getAbsolutePath()};
		_logger.log(Level.WARNING,
			"ejb.sfsb_storemgr_removedir_failed", params);
	    }
        } catch (Throwable th) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_removeall_exception", th);
        }
    }
      
    public void shutdown() {
        shutdown = true;
    }
      
    public MonitorableSFSBStore getMonitorableSFSBStore() {
        return this;
    }

    /***************************************************************/
    /**************  Methods on MonitorableSFSBCache  **************/
    /***************************************************************/

    public int getCurrentSize() {
        return passivatedSessions.getEntryCount();
    }

    public int getLoadCount() {
        return loadCount;
    }
    
    public int getLoadSuccessCount() {
        return loadSuccessCount;
    }
    
    public int getLoadErrorCount() {
        return loadErrorCount;
    }

    public int getPassivationCount() {
        return storeCount;
    }
    
    public int getPassivationSuccessCount() {
        return storeSuccessCount;
    }
    
    public int getPassivationErrorCount() {
        return storeErrorCount;
    }

    public int getCheckpointCount() {
        return storeCount;
    }
    
    public int getCheckpointSuccessCount() {
        return storeSuccessCount;
    }
    
    public int getCheckpointErrorCount() {
        return storeErrorCount;
    }

    public int getExpiredSessionCount() {
        return expiredSessionCount;
    }


    /***************************************************************/
    /*********************  Internal methods  **********************/
    /***************************************************************/

    private void saveState(SFSBBeanState beanState, boolean isPassivated) {

        Object sessionKey =  beanState.getId();
        String fileName = sessionKey.toString();

	if(_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, "[SFSBStore] Attempting to save session: "
		    + sessionKey);
	}
        File file = null;

        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            storeCount++;
            file = new File(baseDir, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte[] data = beanState.getState();
            bos.write(data, 0, data.length);

            storeSuccessCount++;
            if (isPassivated) {
                passivatedSessions.put(fileName, new Long(beanState.getLastAccess()));
		if(_logger.isLoggable(TRACE_LEVEL)) {
		    _logger.log(TRACE_LEVEL, "[SFSBStore] Successfully saved session: "
			    + sessionKey);
		}
            }
        } catch (Exception ex) {
            storeErrorCount++;
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_savestate_failed",
                new Object[] {fileName});
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_savestate_exception", ex);
            try { removeFile(file); } catch (Exception ex1) {}
            String errMsg = "Could not save session: " + beanState.getId();
            throw new SFSBStoreManagerException(errMsg, ex);
        } finally {
            try {
		if (bos != null) bos.close();
	    } catch (Exception ex) {
		_logger.log(Level.FINE, "Error while closing buffered output stream", ex);
	    }
            try {
		if (fos != null) fos.close();
	    } catch (Exception ex) {
		_logger.log(Level.FINE, "Error while closing file output stream", ex);
	    }
        }
    }
      
    private void removeFile(File file) {
        final File localFile = file;
        boolean success = false;
        if(System.getSecurityManager() == null) {
            success = localFile.delete();
        } else {
            success = (Boolean) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                public java.lang.Object run() {
                    return Boolean.valueOf(localFile.delete());
                }
            }
            );
        }
        if (!success) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_removestate_failed",
                new Object[] {file.getName()});
        } else {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBStore] Removed session: "
			+ file.getName());
            }
	}
    }

    protected void addPassivatedSession(String fileName, long lastAccessTime) {
        passivatedSessions.add(fileName, new Long(lastAccessTime));
    }

    /****************************************************/
    /**** Implementation of CacheListener method ****/
    /****************************************************/
    public void trimEvent(Object sessionKey, Object lastAccessedAt) {
        //This can only happen through trimExpiredSessions
        //So it is already running in an async thread
	if(_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, "[SFSBStore] Removing expired session: "
			    + sessionKey);
	}
        remove(sessionKey);
        expiredSessionCount++;
    }

    protected ClassLoader getClassLoader() {
	return this.classLoader;
    }

}
