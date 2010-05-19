/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ha.store.adapter.file;

import org.glassfish.ha.store.spi.BackingStore;
import org.glassfish.ha.store.spi.BackingStoreException;

import java.io.*;

import java.util.Properties;
import java.util.logging.*;

/**
 * An implementation of BackingStore that uses file system to
 * persist any Serializable data
 *
 * @author Mahesh Kannan
 */
public class SimpleFileBackingStore<K, V>
        extends BackingStore<K, V> {

    protected static final Logger _logger =
            Logger.getLogger(SimpleFileBackingStore.class.getName());

    protected File baseDir;

    protected String storeName;

    private boolean shutdown;

    private static Level TRACE_LEVEL = Level.INFO;

    private Class<K> keyClazz;

    private Class<V> vClazz;

    private ClassLoader loader;

    /**
     * No arg constructor
     */
    public SimpleFileBackingStore() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected void initialize(String storeName, Class<K> keyClazz, Class<V> vClazz, Properties storeEnv) {
        loader = Thread.currentThread().getContextClassLoader();

        this.storeName = storeName;

        String baseDirName = (String) storeEnv.get("base.dir.path");

        this.baseDir = new File(baseDirName);

        try {
            if ((baseDir.mkdirs() == false) && (!baseDir.isDirectory())) {
                throw new BackingStoreException("Create base directory (" + baseDirName + ") failed");
            } else {
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "Successfully Initialized "
                            + "SimpleFileBackingStore for: " + storeName);
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_failed",
                    new Object[]{baseDirName});
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_init_exception", ex);
        }

    }

    @Override
    public V load(K key, String version) throws BackingStoreException {

        String fileName = key.toString();
        V value = null;

        byte[] data = readFromfile(fileName);
        try {
            ByteArrayInputStream bis2 = new ByteArrayInputStream(data);
            ObjectInputStream ois = super.createObjectInputStream(bis2);
            value = (V) ois.readObject();
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, storeName
                        + " Successfully read session: " + key);
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_failed",
                    new Object[]{fileName});
            _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_loadstate_exception", ex);
        }

        return value;
    }

    public void remove(Object sessionKey) {
        try {
            removeFile(new File(baseDir, sessionKey.toString()));
        } catch (Exception ex) {
            _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_removestate_failed",
                    new Object[]{sessionKey.toString()});
            _logger.log(Level.WARNING,
                    "ejb.sfsb_storemgr_removestate_exception", ex);
        }
    }

    @Override
    public void destroy() {
        try {
            String[] fileNames = baseDir.list();
            if (fileNames == null) {
                return;
            }
            for (int i = 0; i < fileNames.length; i++) {
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
        } finally {
            FileBackingStoreFactory.removemapping(storeName);
        }
    }

    @Override
    public int removeExpired(long idleForMillis) {
        long threshold = System.currentTimeMillis() - idleForMillis;
        int expiredSessions = 0;
        try {
            String[] fileNames = baseDir.list();
            if (fileNames == null) {
                return 0;
            }
            int size = fileNames.length;
            for (int i = 0; (i < size) && (!shutdown); i++) {
                File file = new File(baseDir, fileNames[i]);
                if (file.exists()) {
                    long lastAccessed = file.lastModified();
                    if (lastAccessed < threshold) {
                        if (!file.delete()) {
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


    @Override
    public int size() throws BackingStoreException {
        String[] numFiles = baseDir.list();
        return numFiles == null ? 0 : numFiles.length;
    }

    @Override
    public void save(K sessionKey, V value, boolean isNew)
            throws BackingStoreException {

        String fileName = sessionKey.toString();

        if (_logger.isLoggable(TRACE_LEVEL)) {
            _logger.log(TRACE_LEVEL, storeName + " Attempting to save "
                    + "session: " + sessionKey);
        }
        writetoFile(sessionKey, fileName, getSerializedState(sessionKey, value));
    }

    @Override
    public void updateTimestamp(Object sessionKey, long time)
            throws BackingStoreException {
        touchFile(sessionKey, sessionKey.toString(), time);
    }

    protected void touchFile(Object sessionKey, String fileName, long time)
            throws BackingStoreException {
        try {
            File file = new File(baseDir, fileName);

            if (file.setLastModified(time) == false) {
                if (file.exists() == false) {
                    _logger.log(Level.WARNING, storeName
                            + ": Cannot update timsestamp for: " + sessionKey
                            + "; File does not exist");
                } else {
                    throw new BackingStoreException(
                            storeName + ": Cannot update timsestamp for: " + sessionKey);
                }
            }
        } catch (BackingStoreException sfsbSMEx) {
            throw sfsbSMEx;
        } catch (Exception ex) {
            _logger.log(Level.WARNING, storeName
                    + ": Exception while updating timestamp", ex);
            throw new BackingStoreException(
                    "Cannot update timsestamp for: " + sessionKey
                            + "; Got exception: " + ex);
        }
    }

    protected boolean removeFile(final File file) {
        boolean success = false;
        if (System.getSecurityManager() == null) {
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
                    new Object[]{file.getName()});
        } else {
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, storeName + " Removed session: "
                        + file.getName());
            }
        }

        return success;
    }

    protected byte[] getSerializedState(K key, V value)
            throws BackingStoreException {

        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            data = bos.toByteArray();
        } catch (IOException ioEx) {
            throw new BackingStoreException("Error during getSerializedState", ioEx);
        } finally {
            try {
                oos.close();
            } catch (IOException ioEx) {/* Noop */}
            try {
                bos.close();
            } catch (IOException ioEx) {/* Noop */}
        }

        return data;
    }

    private byte[] readFromfile(String fileName) {
        byte[] data = null;
        if (_logger.isLoggable(TRACE_LEVEL)) {
            _logger.log(TRACE_LEVEL, storeName + "Attempting to load session: "
                    + fileName);
        }

        File file = new File(baseDir, fileName);
        if (file.exists()) {
            int dataSize = (int) file.length();
            data = new byte[dataSize];
            BufferedInputStream bis = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                int offset = 0;
                for (int toRead = dataSize; toRead > 0;) {
                    int count = bis.read(data, offset, toRead);
                    offset += count;
                    toRead -= count;
                }
            } catch (Exception ex) {
                _logger.log(Level.WARNING,
                        "FileStore.readFromfile failed", ex);
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
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(Level.WARNING, storeName + "Could not find "
                        + "file for: " + fileName);
            }
        }

        return data;
    }

    protected void writetoFile(K sessionKey, String fileName, byte[] data)
            throws BackingStoreException {
        File file = new File(baseDir, fileName);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data, 0, data.length);
            bos.flush();
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, storeName + " Successfully saved "
                        + "session: " + sessionKey);
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_savestate_failed",
                    new Object[]{fileName});
            _logger.log(Level.WARNING, "ejb.sfsb_storemgr_savestate_exception", ex);
            try {
                removeFile(file);
            } catch (Exception ex1) {
            }
            String errMsg = "Could not save session: " + sessionKey;
            throw new BackingStoreException(errMsg, ex);
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
}
