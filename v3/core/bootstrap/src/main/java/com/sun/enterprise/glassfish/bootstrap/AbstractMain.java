package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.PlatformMain;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

/**
 * Top level abstract main class
 *
 * @author Jerome Dochez
 */
public abstract class AbstractMain extends PlatformMain {

    final File bootstrapFile;

    protected ASMainHelper helper;

    final protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1    

    abstract Logger getLogger();

    abstract long getSettingsLastModification();

    protected abstract String getPreferedCacheDir();

    abstract boolean createCache(File cacheDir) throws IOException;

    AbstractMain() {
        this.bootstrapFile = findBootstrapFile();
        System.setProperty(StartupContext.ROOT_PROP, bootstrapFile.getParent());
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        System.setProperty("com.sun.aas.installRoot",glassfishDir.getAbsolutePath());
    }

    public void start(String[] args) throws Exception {
        helper = new ASMainHelper(logger);
        helper.parseAsEnv(glassfishDir);
        run(logger, args);
    }

    protected void run(Logger logger, String... args) throws Exception {
        this.logger = logger;
        domainDir = helper.getDomainRoot(new StartupContext(bootstrapFile, args));
        helper.verifyAndSetDomainRoot(domainDir);
        
        File cacheProfileDir = new File(domainDir, getPreferedCacheDir());
        setUpCache(bootstrapFile.getParentFile(), cacheProfileDir);
    }

    protected void setSystemProperties() throws Exception {
       /* Set a system property called com.sun.aas.installRootURI.
         * This property is used in felix/conf/config.properties and possibly
         * in other OSGi framework's config file to auto-start some modules.
         * We can't use com.sun.aas.installRoot,
         * because that com.sun.aas.installRoot is a directory path, where as
         * we need a URI.
         */
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        URI installRootURI = new File(installRoot).toURI();
        System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        URI instanceRootURI = new File(instanceRoot).toURI();
        System.setProperty("com.sun.aas.instanceRootURI", instanceRootURI.toString());        
    }

    boolean isCacheOutdated(long lastModified, File cacheDir) {
        Properties persistedInfo = loadCacheInformation(cacheDir);
        long recordedLastModified = parse(persistedInfo, "LastModified");
        // check that we have not moved our domain's directory, felix is sensitive to absolute path
        String location = persistedInfo.getProperty("Location");
        try {
            if (!cacheDir.toURI().toURL().toString().equals(location)) {
                recordedLastModified=0;
            }
        } catch (MalformedURLException e) {
            getLogger().log(Level.SEVERE, "Could not load cache metadata, cache will be reset",e);
            recordedLastModified=0;
        }

        // if the recordedLastModified is different than our most recent entry,
        // we flush the felix cache, otherwise we reuse it.
        return (recordedLastModified!=lastModified);
    }
    
    protected void setUpCache(File sourceDir, File cacheDir) throws IOException  {

        long lastModified = getLastModified(sourceDir, 0);
        long settingsLastModified = getSettingsLastModification();
        if (settingsLastModified>lastModified) {
            lastModified = settingsLastModified;
        }

        if (isCacheOutdated(lastModified, cacheDir)) {
            flushAndCreate(cacheDir, lastModified);
        }
        System.setProperty("com.sun.hk2.cacheDir", cacheDir.getAbsolutePath());
    }

    protected void flushAndCreate(File cacheDir, long lastModified) throws IOException {
        
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            // remove this old cache so felix creates a new one.
            getLogger().info("Removing cache dir " + cacheDir+ " left from a previous run");
            if (!deleteRecursive(cacheDir)) {
                getLogger().warning("Not able to delete " + cacheDir);
            }
        }

        if (!createCache(cacheDir)) {
            throw new IOException("Could not create cache");
        }

        // now record our new LastModified
        try {
            saveCacheInformation(cacheDir,cacheDir.toURI().toURL().toString(), lastModified);
        } catch (MalformedURLException e) {
            getLogger().log(Level.SEVERE, "Could not save cache metadata, cache will be reset at next startup",e);
        }
    }

    public long getLastModified(File directory, long current) {

        for (File file : directory.listFiles()) {
            long lastModified;
            if (file.isDirectory()) {
                lastModified = getLastModified(file, current);
            } else {
                lastModified = file.lastModified();
            }
            if (lastModified>current) {
                current=lastModified;
            }
        }
        return current;
    }

    public Properties loadCacheInformation(File cacheDir) {

        long recordedLastModified = 0;
        Properties persistedInfo = new Properties();
        File lastModifiedFile = new File(cacheDir.getParentFile(), cacheDir.getName()+".lastmodified");
        if (lastModifiedFile.exists()) {

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(lastModifiedFile));
                persistedInfo.load(is);
                try {
                    recordedLastModified = Long.parseLong(persistedInfo.getProperty("LastModified"));
                    // check that we have not moved our domain's directory, felix is sensitive to absolute path
                    String location = persistedInfo.getProperty("Location");
                    if (!cacheDir.toURI().toURL().toString().equals(location)) {
                        recordedLastModified=0;
                    }
                } catch (NumberFormatException e) {
                    recordedLastModified = 0;
                }
            } catch(IOException e) {
                getLogger().info("Cannot read recorded lastModified, OSGi cache will be flushed");
            } finally {
                if (is!=null) {
                    try {
                        is.close();
                    } catch(IOException e){};
                }
            }
        }
        return persistedInfo;
    }

    public long parse(Properties info, String name) {

        try {
            return Long.parseLong(info.getProperty(name));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void saveCacheInformation(File cacheDir, String location, long lastModified) {
           // now record our new LastModified
        ObjectOutputStream os = null;
        File lastModifiedFile = new File(cacheDir.getParentFile(), cacheDir.getName() + ".lastmodified");
        try {
            lastModifiedFile.delete();
            if (!lastModifiedFile.createNewFile()) {
                getLogger().warning("Cannot create new lastModified file");
                return;
            }
            os = new ObjectOutputStream(new FileOutputStream(lastModifiedFile));
            Properties persistedInfo = new Properties();
            persistedInfo.put("LastModified", (new Long(lastModified).toString()));
            persistedInfo.put("Location", cacheDir.toURI().toURL().toString());
            persistedInfo.store(os, null);

        } catch (IOException e) {
            getLogger().info("Cannot create record of lastModified file");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public File findBootstrapFile() {
        try {
            return Which.jarFile(getClass());
        } catch (IOException e) {
            throw new RuntimeException("Cannot get bootstrap path from "
                    + getClass() + " class location, aborting");
        }
    }

    boolean deleteRecursive(File dir) {
        for (File f : dir.listFiles()) {
            if(f.isFile()) {
                f.delete();
            } else {
                deleteRecursive(f);
            }
        }
        return dir.delete();
    }

    /**
     * This method is used to copy a given file to another file
     * using the buffer sixe specified
     *
     * @param fin  the source file
     * @param fout the destination file
     */
    public static void copyFile(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream fos = new FileOutputStream(fout);
        copy(inStream, fos, fin.length());
    }


    public static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    public static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);

    }
}