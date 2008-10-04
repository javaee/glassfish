package org.glassfish.api.admin;

import org.jvnet.hk2.annotations.Contract;

import java.io.File;

/**
 * Service to monitor changes to files.
 * 
 * @author Jerome Dochez
 */
@Contract
public interface FileMonitoring {

    /**
     * Registers a FileChangeListener for a particular file
     * @param file the file of interest
     * @param listener the listener to notify
     */
    public void monitors(File file, FileChangeListener listener); 

    public interface FileChangeListener {
        public void changed(File changedFile);
    }
}
