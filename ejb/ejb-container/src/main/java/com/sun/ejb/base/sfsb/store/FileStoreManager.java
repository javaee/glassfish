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

package com.sun.ejb.base.sfsb.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.logging.*;

import com.sun.ejb.spi.sfsb.store.MonitorableSFSBStore;

import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;

import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;

import com.sun.logging.*;

/**
 * An implementation of SFSBStoreManager that uses file system to
 *  persist SFSB state.
 *
 * @author     Mahesh Kannan
 */
 
public class FileStoreManager
    implements SFSBStoreManager, MonitorableSFSBStoreManager
{
    protected static final Logger _logger =
        LogDomains.getLogger(FileStoreManager.class, LogDomains.EJB_LOGGER);

    protected String	clusterId = "";
    protected long	containerId;

    protected File	baseDir;
    protected String	storeName;

    protected int	passivationTimeoutInSeconds;

    private int         loadCount; 
    private int         loadSuccessCount; 
    private int         loadErrorCount; 
    private int         storeCount; 
    private int         storeSuccessCount; 
    private int         storeErrorCount; 
    private int         expiredSessionCount; 

    private boolean     shutdown;

    private Level	TRACE_LEVEL = Level.FINE;

    private int		gracePeriodInSeconds;

    /**
     * No arg constructor
     */
    public FileStoreManager() {
    }

    /****************************************************/
    /**** Implementation of SFSBStoreManager methods ****/
    /****************************************************/

    public void checkpointSave(SFSBBeanState beanState)
	throws SFSBStoreManagerException
    {
        saveState(beanState, false);
	if (_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, storeName + "Checkpoint saved: "
		+ beanState.getId());
	}
    }

    public SFSBBeanState createSFSBBeanState(Object sessionId,
	    long lastAccess, boolean isNew, byte[] state)
    {
	return new SFSBBeanState(clusterId, containerId,
	    sessionId, lastAccess, isNew, state, this);
    }
    
    public SFSBBeanState getState(Object sessionKey) {

        String fileName = sessionKey.toString();
        SFSBBeanState beanState = null;

	if(_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, storeName + "Attempting to load session: "
			    + sessionKey);
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
        
		beanState = new SFSBBeanState("", -1, sessionKey, -1,
		    false, data, this);

                loadSuccessCount++;
		if(_logger.isLoggable(TRACE_LEVEL)) {
		    _logger.log(TRACE_LEVEL, storeName
			    + " Successfully loaded session: " + sessionKey);
		}
            } catch (Exception ex) {
                loadErrorCount++;
                _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_failed",
                    new Object[] {fileName});
                _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_exception", ex);
            } finally {
                try {
		    bis.close();
		} catch (Exception ex) {
		    _logger.log(Level.FINEST, storeName + " Error while "
			    + "closing buffered input stream", ex);
		}
                try {
		    fis.close();
		} catch (Exception ex) {
		    _logger.log(Level.FINEST, storeName + " Error while "
			    + "closing file input stream", ex);
		}
            }
        } else {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(Level.WARNING, storeName + "Could not find passivated "
			+ "file for: " + sessionKey);
	    }
	}
        return beanState;
    }
     
    public void initSessionStore(Map storeEnv) {

        this.storeName = (String) storeEnv.get(
		FileStoreManagerConstants.STORE_MANAGER_NAME);
        try {
	    Long cId = (Long) storeEnv.get(
		    FileStoreManagerConstants.CONTAINER_ID);
	    this.containerId = cId.longValue();
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, "Couldn't get containerID", ex);
	}

        String baseDirName = (String) storeEnv.get(
                FileStoreManagerConstants.PASSIVATION_DIRECTORY_NAME);

        this.baseDir = new File(baseDirName);

        try {
	    Integer sessionTimeout = (Integer) storeEnv.get(
		FileStoreManagerConstants.SESSION_TIMEOUT_IN_SECONDS);
	    if (sessionTimeout != null) {
		this.passivationTimeoutInSeconds = sessionTimeout.intValue();
	    }
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, "Couldn't get session timeout", ex);
	}

        try {
	    Integer graceTimeout = (Integer) storeEnv.get(
		FileStoreManagerConstants.GRACE_SESSION_TIMEOUT_IN_SECONDS);
	    if (graceTimeout != null) {
		this.gracePeriodInSeconds = graceTimeout.intValue();
	    }
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, "Couldn't get session timeout", ex);
	}

        try {
            if ((baseDir.mkdirs() == false) && (!baseDir.isDirectory())) {
		_logger.log(Level.WARNING, "ejb.sfsb_storemgr_mdirs_failed",
			new Object[] {baseDirName});
		//TODO: Log storeName also
	    } else {
		if (_logger.isLoggable(TRACE_LEVEL)) {
		    _logger.log(TRACE_LEVEL, "Successfully Initialized "
			    + "FileStoreManager for: " + storeName);
		}
	    }
        } catch (Exception ex) {
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_failed",
                    new Object[] {baseDirName});
	    _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_exception", ex);
        }

    }

    public void passivateSave(SFSBBeanState beanState)
	throws SFSBStoreManagerException
    {
        saveState(beanState, true);
    }

    public void remove(Object sessionKey) {
        try {
            removeFile(new File(baseDir, sessionKey.toString()));
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                "ejb.sfsb_storemgr_removestate_failed",
                new Object[] {sessionKey.toString()});
            _logger.log(Level.WARNING,
                "ejb.sfsb_storemgr_removestate_exception", ex);
        }
    }
      
    public void removeAll() {
        try {
	    String[] fileNames = baseDir.list();
 	    if (fileNames == null) {
                 return;
 	    }
	    for (int i=0; i<fileNames.length; i++) {
                remove(fileNames[i]);
	    }

	    if (baseDir.delete() == false) {
		if (baseDir.exists()) {
		    Object[] params = {baseDir.getAbsolutePath()};
		    _logger.log(Level.WARNING,
			"ejb.sfsb_storemgr_removedir_failed", params);
		}
	    }
        } catch (Throwable th) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_removeall_exception", th);
        }
    }
      
    public void removeExpired() {
	expiredSessionCount += removeExpiredSessions();
    }

    public int removeExpiredSessions() {
	if (passivationTimeoutInSeconds <= 0) {
	    return 0;
	}
	long threshold = System.currentTimeMillis()
	    - (passivationTimeoutInSeconds * 1000L)
	    - (gracePeriodInSeconds * 1000L);
	int expiredSessions = 0;
        try {
	    String[] fileNames = baseDir.list();
 	    if (fileNames == null) {
            return 0;
 	    }
	    int size = fileNames.length;
	    for (int i=0; (i<size) && (! shutdown); i++) {
		File file = new File(baseDir, fileNames[i]);
		if (file.exists()) {
		    long lastAccessed = file.lastModified();
		    if (lastAccessed < threshold) {
			if (! file.delete()) {
			    if (file.exists()) {
				_logger.log(Level.WARNING, storeName
				    + "Couldn't remove file: " + fileNames[i]);
			    }
			} else {
			    expiredSessions++;
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, storeName + "Exception while getting "
		    + "expired files", ex);
	}

	return expiredSessions;
    }

    public void shutdown() {
        shutdown = true;
    }
      
    public void updateLastAccessTime(Object sessionKey, long time)
        throws SFSBStoreManagerException
    {
	String fileName = sessionKey.toString();
	try {
	    File file = new File(baseDir, fileName);

	    if (file.setLastModified(time) == false) {
		if (file.exists() == false) {
		    _logger.log(Level.WARNING, storeName 
			+ ": Cannot update timsestamp for: " + sessionKey
			    + "; File does not exist");
		} else {
		    throw new SFSBStoreManagerException(
			storeName + ": Cannot update timsestamp for: " + sessionKey);
		}
	    }
	} catch (SFSBStoreManagerException sfsbSMEx) {
	    throw sfsbSMEx;
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, storeName 
		+ ": Exception while updating timestamp", ex);			
	    throw new SFSBStoreManagerException(
		"Cannot update timsestamp for: " + sessionKey
		    + "; Got exception: " + ex);			
	}
    }

    /***************************************************************/
    /**************  Methods on MonitorableSFSBCache  **************/
    /***************************************************************/

    public int getCurrentSize() {
        //return passivatedSessions.getEntryCount();
	try {
	    String[] fileList = baseDir.list();
	    if (fileList != null) {
	        return fileList.length;
	    }
	} catch (Exception ex) {
	}
	return 0;
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

    private void saveState(SFSBBeanState beanState, boolean isPassivated)
	throws SFSBStoreManagerException
    {

        Object sessionKey =  beanState.getId();
        String fileName = sessionKey.toString();

	if(_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, storeName + " Attempting to save "
		    + "session: " + sessionKey);
	}
        File file = null;

        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            storeCount++;
            file = new File(baseDir, fileName);
	    if (file.exists()) {
		if (beanState.isNew()) {
		    _logger.log(Level.WARNING, storeName + " [InternalError] "
			+ "isNew() must be false for: " + sessionKey);
		}
	    } else {
		if (beanState.isNew() == false) {
		    _logger.log(Level.WARNING, storeName + " [InternalError] "
			+ "isNew() must be true for: " + sessionKey);
		}
	    }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte[] data = beanState.getState();
            bos.write(data, 0, data.length);

            storeSuccessCount++;
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, storeName + " Successfully saved "
			+ "session: " + sessionKey);
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
      
    private boolean removeFile(final File file) {
        boolean success = false;
        if(System.getSecurityManager() == null) {
            success = file.delete();
        } else {
            success = (Boolean) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                public java.lang.Object run() {
                    return Boolean.valueOf(file.delete());
                }
            }
            );
        }
        if (!success) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_removestate_failed",
                new Object[] {file.getName()});
        } else {
	    if(_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, storeName + " Removed session: "
			+ file.getName());
            }
	}

	return success;
    }

    public MonitorableSFSBStoreManager getMonitorableSFSBStoreManager() {
	return this;
    }

    public long getCurrentStoreSize() {
	try {
	    return baseDir.list().length;
	} catch (Exception ex) {
	}
	return 0;
    }

    public void appendStats(StringBuffer sbuf) {
    }

    public void monitoringLevelChanged(boolean monitoringOn) {
    }

}
