/*
 * DirectoryBasedRepository.java
 *
 * Created on October 17, 2006, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.RepositoryChangeListener;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ManifestConstants;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

/**
 * This class is a directory based repository implementation. This mean that all jar
 * file residing a particular directory will be elligible to be added to this 
 * repository instance. Jar File will need to be compliant with the module definition
 * spec which mean have a manifest file with the correct elements OR a separate 
 * manifest file with the same file name with a .mf extension.
 *
 * @author Jerome Dochez
 */
public class DirectoryBasedRepository extends AbstractRepositoryImpl {
    
    private final File repository;
    private final int intervalInMs = Integer.getInteger("hk2.file.directory.changeIntervalTimer", 10);

    /** Creates a new instance of DirectoryBasedRepository */
    public DirectoryBasedRepository(String name, File repository) throws FileNotFoundException {
        super(name,repository.toURI());
        this.repository = repository;
    }


    @Override
    public synchronized boolean addListener(RepositoryChangeListener listener) {

        final boolean returnValue = super.addListener(listener);
        if (returnValue) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                long lastModified = repository.lastModified();
                public void run() {
                    synchronized(this) {
                        if (lastModified<repository.lastModified()) {
                            lastModified = repository.lastModified();
                            // something has changed, look into this...
                            directoryChanged();
                        }
                    }
                }
            }, intervalInMs, intervalInMs);
            timer.purge();
        }
        return returnValue;        
    }

    @Override
    protected void loadModuleDefs(Map<String, ModuleDefinition> moduleDefs, List<URI> libraries) throws IOException {
        if (!repository.exists()) {
            throw new FileNotFoundException(repository.getAbsolutePath());
        }


        try {
            File[] files = repository.listFiles();
            for (File aFile : files) {
                if (aFile.getName().endsWith(".jar")) {
                    ModuleDefinition moduleDef = loadJar(aFile);
                    if (moduleDef!=null) {
                        moduleDefs.put(moduleDef.getName(), moduleDef);
                    } else {
                        libraries.add(aFile.toURI());
                    }
                }
            }

        } catch (IOException e) {
            IOException x = new IOException("Failed to load modules from " + repository);
            x.initCause(e);
            throw x;
        }
    }

    private synchronized void directoryChanged() {

        // not the most efficient implementation, could be revisited later
        HashMap<String, ModuleDefinition> newModuleDefs = new HashMap<String, ModuleDefinition>();
        List<URI> libraries = new ArrayList<URI>();

        try {
            loadModuleDefs(newModuleDefs, libraries);
        } catch(IOException ioe) {
            // we probably need to wait until the jar has finished being copied
            // XXX add some form of retry
        }
        for(ModuleDefinition def : newModuleDefs.values()) {
            if (find(def.getName(), def.getVersion())==null) {
                add(def);
                for (RepositoryChangeListener listener : listeners) {
                    listener.moduleAdded(def);
                }
            }
        }
        for (ModuleDefinition def : findAll()) {
            if (!newModuleDefs.containsKey(def.getName())) {
                remove(def);
                for (RepositoryChangeListener listener : listeners) {
                    listener.moduleRemoved(def);
                }
            }
        }
        List<URI> originalLibraries = super.getJarLocations();
        for (URI location : libraries) {
            if (!originalLibraries.contains(location)) {
                addLibrary(location);
                for (RepositoryChangeListener listener : listeners) {
                    listener.jarAdded(location);
                }
            }
        }
        if (originalLibraries.size()>0) {
            List<URI> copy = new ArrayList<URI>(originalLibraries.size());
            copy.addAll(originalLibraries);
            for (URI originalLocation : copy) {
                if (!libraries.contains(originalLocation)) {
                    removeLibrary(originalLocation);
                    for (RepositoryChangeListener listener : listeners) {
                        listener.jarRemoved(originalLocation);
                    }
                }
            }
        }
    }

}
