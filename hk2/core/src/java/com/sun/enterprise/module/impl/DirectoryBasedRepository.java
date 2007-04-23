/*
 * DirectoryBasedRepository.java
 *
 * Created on October 17, 2006, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

    /** Creates a new instance of DirectoryBasedRepository */
    public DirectoryBasedRepository(String name, File repository) throws FileNotFoundException {
        super(name,repository.toURI());
        this.repository = repository;
    }

    @Override
    protected Map<String, ModuleDefinition> loadModuleDefs() throws IOException {
        if (!repository.exists()) {
            throw new FileNotFoundException(repository.getAbsolutePath());
        }

        Map<String, ModuleDefinition> moduleDefs = new HashMap<String, ModuleDefinition>();

        try {
            File[] files = repository.listFiles();
            for (File aFile : files) {
                if (aFile.getName().endsWith(".jar")) {
                    ModuleDefinition moduleDef = loadJar(aFile);
                    if (moduleDef!=null) {
                        moduleDefs.put(moduleDef.getName(), moduleDef);
                    }
                }
            }

            return moduleDefs;
        } catch (IOException e) {
            IOException x = new IOException("Failed to load modules from " + repository);
            x.initCause(e);
            throw x;
        }
    }

    /**
     * This module adds a new Module Definition to the repository.
     *
     * @param definition is the module definition
     * @return true if the addition was successful
     */
    public void add(ModuleDefinition definition) throws IOException {

        // we need to copy all locations as found in the definition to our repository.
        for (URI location : definition.getLocations()) {
            InputStream is=null;
            FileOutputStream os=null;
            try {
                is = location.toURL().openStream();
                ReadableByteChannel channel = Channels.newChannel(is);
                // this naming scheme may need to be reworked...
                File outFile = new File(repository, definition.getName() + ".jar");
                os = new FileOutputStream(outFile);
                FileChannel outChannel = os.getChannel();
                long bytes;
                long transferedBytes=0;
                do {
                    bytes = outChannel.transferFrom(channel,transferedBytes , 4096);
                    transferedBytes+=bytes;
                } while (bytes==4096);
            } finally {
                try {
                    if (is!=null)
                        is.close();
                } catch(IOException e) {
                    // ignore
                }
                try {
                    if (os!=null)
                        os.close();
                } catch(IOException e) {
                    // ignore
                }                
            }
        }

        // we also need to save the definition as an external manifest file since
        // the defintion may have been expressed with different means than our
        // manifest entries.
        Manifest manifest = definition.getManifest();
        if (manifest!=null) {
            if (manifest.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME)!=null) {
                return;
            }
        }
        // write the external manifest file
        // TODO : have some plugability here where we separate the Manifest IO from
        // the definition and the respository
        File manifestFile = new File(repository, definition.getName() + ".mf");
        manifest = new Manifest();
        Attributes attr = manifest.getMainAttributes();
        // name
        attr.put(ManifestConstants.BUNDLE_NAME, definition.getName());
        // list of imported bundle
        StringBuffer buffer = new StringBuffer();
        for (URI location : definition.getLocations()) {
            if (buffer.length()>0) {
                buffer.append(", ");
            }
            String fileName = location.getPath().substring(location.getPath().lastIndexOf('/'));
            buffer.append(fileName);
        }
        attr.put(ManifestConstants.BUNDLE_IMPORT_NAME, buffer.toString());
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(manifestFile);
            manifest.write(new BufferedOutputStream(os));
        } finally {
            if (os!=null) {
                os.close();
            }
        }
    }

}
