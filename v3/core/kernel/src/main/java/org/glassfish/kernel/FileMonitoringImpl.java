package org.glassfish.kernel;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.admin.FileMonitoring;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jerome Dochez
 */
@Service
public class FileMonitoringImpl implements FileMonitoring, PostConstruct {

    @Inject
    ExecutorService executor;

    @Inject
    ScheduledExecutorService scheduledExecutor;
            
    final Map<File, List<FileChangeListener>> listeners = new HashMap<File, List<FileChangeListener>>();
    final Map<File, Long> monitored = new HashMap<File, Long>();

    public void postConstruct() {
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (monitored.isEmpty()) {
                    return;
                }
                // check our list of monitored files for any changes
                Set<File> monitoredFiles = new HashSet<File>();
                monitoredFiles.addAll(listeners.keySet());
                for (File file : monitoredFiles) {
                    if (!file.exists()) {
                        removed(file);
                        listeners.remove(file);
                        monitored.remove(file);
                    } else 
                    if (file.lastModified()!=monitored.get(file)) {
                        // file has changed
                        monitored.put(file, file.lastModified());
                        changed(file);
                    }
                }

            }
        }, 0, 500, TimeUnit.MILLISECONDS);

    }

    public synchronized void monitors(File file, FileChangeListener listener) {

        if (monitored.containsKey(file)) {
            listeners.get(file).add(listener);
        } else {
            List<FileChangeListener> list = new ArrayList<FileChangeListener>();
            list.add(listener);
            listeners.put(file, list);
            monitored.put(file, file.lastModified());
        }
    }


    private void removed(final File file) {
        for (final FileChangeListener listener : listeners.get(file)) {
            executor.submit(new Runnable() {
                public void run() {
                    listener.deleted(file);
                }
            });
        }

    }

    private void changed(final File file) {
        for (final FileChangeListener listener : listeners.get(file)) {
            executor.submit(new Runnable() {
                public void run() {
                    listener.changed(file);
                }
            });
        }
    }
}
