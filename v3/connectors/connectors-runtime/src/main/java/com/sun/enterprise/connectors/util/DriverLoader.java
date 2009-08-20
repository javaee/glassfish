package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.jvnet.hk2.annotations.Service;

@Service
/**
 * Driver Loader to load the jdbc drivers and get driver/datasource classnames
 * by introspection. 
 * 
 * @author Shalini M
 */
public class DriverLoader implements ConnectorConstants {

    private static Logger logger =
    LogDomains.getLogger(DriverLoader.class, LogDomains.RSR_LOGGER);

    private static final String DRIVER_INTERFACE_NAME="java.sql.Driver";
    private static final String SERVICES_DRIVER_IMPL_NAME = "META-INF/services/java.sql.Driver";
    private static final String DATABASE_VENDOR_DERBY = "DERBY";
    private static final String DATABASE_VENDOR_JAVADB = "JAVADB";

    /**
     * Gets a set of driver or datasource classnames for the particular vendor.
     * Loads the jdbc driver, introspects the jdbc driver jar and gets the 
     * classnames.
     * @return
     */
    public Set<String> getJdbcDriverClassNames(String dbVendor, String resType) {
        //Map of all jar files with the set of driver implementations. every file
        // that is a jdbc jar will have a set of driver impls.
        Set<String> implClassNames = new TreeSet<String>();
        List<File> jarFileLocations = getJdbcDriverLocations();
        Set<File> allJars = new HashSet<File>();
        
        if(jarFileLocations != null) {
            for(File lib : jarFileLocations) {
                if(lib.isDirectory()) {
                    for(File file : lib.listFiles(new JarFileFilter())) {
                        allJars.add(file);
                    }
                }            
            }
        }
        for (File file : allJars) {
            if (file.isFile()) {
                //Introspect jar and get classnames.
                if(dbVendor!= null && dbVendor.equalsIgnoreCase(DATABASE_VENDOR_JAVADB)) {
                    implClassNames = introspectAndLoadJar(file, resType, DATABASE_VENDOR_DERBY);
                } else {
                    implClassNames = introspectAndLoadJar(file, resType, dbVendor);
                }
                //Found the impl classnames for the particular dbVendor. 
                //Hence no need to search in other jar files.
                if(!implClassNames.isEmpty()) {
                    break;
                }
            }
        }        
        return implClassNames;
    }

    private Set<String> getImplClassesByIteration(File f, String resType, String dbVendor) {
        SortedSet<String> implClassNames = new TreeSet<String>();
        String implClass = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Enumeration e = jarFile.entries();
            while(e.hasMoreElements()) {

                ZipEntry zipEntry = (ZipEntry) e.nextElement();

                if (zipEntry != null) {

                    String entry = zipEntry.getName();
                    if (DRIVER_INTERFACE_NAME.equals(resType)) {
                        if (SERVICES_DRIVER_IMPL_NAME.equals(entry)) {

                            InputStream metaInf = jarFile.getInputStream(zipEntry);
                            implClass = processMetaInf(metaInf);
                            if (implClass != null) {
                                if (isLoaded(implClass, resType)) {
                                    //Add to the implClassNames only if vendor name matches.
                                    if(isVendorSpecific(f, dbVendor)) {
                                        implClassNames.add(implClass);
                                    }
                                }
                            }
                            logger.finest("Driver loader : implClass = " + implClass);
                            
                        }
                    }
                    if (entry.endsWith(".class")) {
                        //Read from metainf file for all jdbc40 drivers and resType
                        //java.sql.Driver.TODO : this should go outside .class check.
                        //If no implClass has been identified so far
                        if (implClassNames.isEmpty()) {
                            //might have Driver/DataSource strings in it.
                           if(entry.indexOf("DataSource") != -1 || entry.indexOf("Driver") != -1) {
                                implClass = getClassName(entry);
                                if(implClass != null) {
                                    if (isLoaded(implClass, resType)) {
                                        if (isVendorSpecific(f, dbVendor)) {
                                            implClassNames.add(implClass);
                                        }
                                    }
                                }                                
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error while getting Jdbc driver classnames ", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    logger.log(Level.FINE, "Exception while closing JarFile '"
                            + jarFile.getName() + "' :", ex);
                }
            }
        }
        //Could be one or many depending on the connection definition class name 
        return implClassNames;
    }

    /**
     * Returns a list of all driver class names that were loaded from the jar file.
     * @param f
     * @param dbVendor
     * @return Set of driver/datasource class implementations based on resType
     */
    private Set<String> introspectAndLoadJar(File f, String resType, String dbVendor) {

        logger.finest("DriverLoader : introspectAndLoadJar ");
       
        return getImplClassesByIteration(f, resType, dbVendor);
                
    }

    /**
     * Reads the META-INF/services/java.sql.Driver file contents and returns
     * the driver implementation class name.
     * In case of jdbc40 drivers, the META-INF/services/java.sql.Driver file
     * contains the name of the driver class.
     * @param metaInf
     * @return driver implementation class name
     */
    private String processMetaInf(InputStream metaInf) {
        String driverClassName = null;
        InputStreamReader reader = null;
        BufferedReader buffReader = null;
        try {
            reader = new InputStreamReader(metaInf);
            buffReader = new BufferedReader(reader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                driverClassName = line;
            }
        } catch(IOException ioex) {
             logger.finest("DriverLoader : exception while processing " +
                     "META-INF directory for DriverClassName " + ioex);
        } finally {
            try {
                if(buffReader != null)
                    buffReader.close();
            } catch (IOException ex) {
                logger.log(Level.FINE, "Error while closing File handles after reading META-INF files : ", ex);
            }
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException ex) {
                logger.log(Level.FINE, "Error while closing File handles after reading META-INF files : ", ex);
            }
        }
        return driverClassName;
    }
    
    /**
     * Check if the classname has been loaded and if it is a Driver or a 
     * DataSource impl.
     * @param classname
     * @return
     */
    private boolean isLoaded(String classname, String resType) {
        Class cls = null;
        try {
            //This will fail in case the driver is not in classpath.
            cls = ConnectorRuntime.getRuntime().getConnectorClassLoader().loadClass(classname);
        //Check shud be made here to look into the lib directory now to see
        // if there are any newly installed drivers.
        //If so, create a URLClassLoader and load the class with common
        //classloader as the parent.
        } catch (Exception ex) {
            cls = null;
        } catch (Throwable t) {
            cls = null;
        } 
        return (isResType(cls, resType));
    }
    
    /**
     * Find if the particular class has any implementations of java.sql.Driver or
     * javax.sql.DataSource or any other resTypes passed.
     * @param cls
     * @return
     */
    private boolean isResType(Class cls, String resType) {
        boolean isResType = false;
        if (cls != null) {
            Class[] interfaces = cls.getInterfaces();
            if (interfaces.length != 0) {
                for (int n = 0; n < interfaces.length; n++) {
                    String i = interfaces[n].getName();
                    if (resType.equals(i)) {
                        isResType = true;
                        break;
                    }
                }
            }
        }
        return isResType;
    }

    
    /**
     * This method should be executed before driverLoaders are queries for 
     * classloader to load the classname.
     * @param f
     * @param classname
     * @return
     */
    //TODO remove later
    /*private boolean loadClass(File f, String classname, String resType) {
        List<URL> urls = new ArrayList<URL>();
        Class urlCls = null;
        boolean isLoaded = false;
        try {
            urls.add(f.toURI().toURL());
        } catch (MalformedURLException ex) {
        }
        ClassLoader loader = ConnectorRuntime.getRuntime().getConnectorClassLoader();
        if (!urls.isEmpty()) {
            ClassLoader urlClassLoader =
                    new URLClassLoader(urls.toArray(new URL[urls.size()]), loader);
            try {
                urlCls = urlClassLoader.loadClass(classname);
            } catch (ClassNotFoundException ex) {
            }
            isLoaded = isResType(urlCls, resType);
            if(isLoaded) {
                //Loaded the class and verified it to be a jdbc driver implementing
                //java.sql.Driver or implementing javax.sql.DataSource
                //Register the url classloader for later use.
                //For ojdbc14 n ojdbc5 (same class names different url class loaders
                //will be added.
                classLoaders.put(classname, urlClassLoader);
            }
        } else {
        }
        return isLoaded;
    }*/

    private String getClassName(String classname) {
        classname = classname.replaceAll("/", ".");
        classname = classname.substring(0, classname.lastIndexOf(".class"));
        return classname;
    }

    private boolean isVendorSpecific(File f, String dbVendor) {
        //File could be a jdbc jar file or a normal jar file
        boolean isVendorSpecific = false;
        String vendor = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            vendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR.toString());
            if (vendor == null) {
                vendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString());
            }
            if(vendor == null) {
                //might have to do this part by going through the class names or some other method.
                //dbVendor might be used in this portion
                if(isVendorSpecificByIteration(dbVendor, f)){
                    isVendorSpecific = true;
                }
            } else {
                if(vendor.equalsIgnoreCase(dbVendor) || vendor.toUpperCase().indexOf(dbVendor.toUpperCase()) != -1) {
                    isVendorSpecific = true;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception while reading manifest file : ", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    logger.log(Level.FINE, "Exception while closing JarFile '"
                            + jarFile.getName() + "' :", ex);
                }
            }
        }
        return isVendorSpecific;
    }

    private List<File> getJdbcDriverLocations() {
	List<File> jarFileLocations = new ArrayList<File>();
        jarFileLocations.add(getLocation(SystemPropertyConstants.DERBY_ROOT_PROPERTY));
        jarFileLocations.add(getLocation(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        jarFileLocations.add(getLocation(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY));
        return jarFileLocations;
    }

    private File getLocation(String property) {
        return new File(System.getProperty(property) + File.separator + "lib");    
    }
    
    private static class JarFileFilter implements FilenameFilter {

        private final String JAR_EXT = ".jar";

        public boolean accept(File dir, String name) {
            return name.endsWith(JAR_EXT);
        }
    }

    /**
     * Utility method that checks if a jar file is vendor specific by iteration.
     * This method is used for jar files that do not have a manifest file to 
     * look up the classname.
     * @param dbVendor
     * @param f
     * @return true if f is vendor specific.
     */
    private boolean isVendorSpecificByIteration(String dbVendor, File f) {
        JarFile jarFile = null;
        boolean isVendorSpecific = false;
        try {
            jarFile = new JarFile(f);
            Enumeration e = jarFile.entries();
            for (; e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (entry != null) {
                    String classname = entry.getName();
                    if (classname.endsWith(".class")) {        
                        //classname usually contains the dbVendor string
                        if(classname.toUpperCase().indexOf(dbVendor.toUpperCase()) != -1) {
                            isVendorSpecific = true;
                            break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception while introspecting jdbc jar file " +
                    "for driver/datasource classname introspection : ", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    logger.log(Level.FINE, "Exception while closing JarFile '"
                            + jarFile.getName() + "' :", ex);
                }
            }
        }

        return isVendorSpecific;
    }

    /**
     * Find if the specific file is a db vendor specific jar file. 
     * Methods to find : 1. read manifest entries for implementation vendor
     * 2. if manifest not found, then iterate thru the jar file for classes and
     * find if the classnames have the dbvendor name in them. This is just an 
     * approximation to find if the jar file would match the dbvendor chosen.
     * @param f
     * @param dbVendor
     * @return
     */
    private boolean isVendorSpecificByManifest(File f, String dbVendor) {
        boolean isVendorSpecific = false;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            String implVendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION.toString());
            if(implVendor != null) {
                if(implVendor.toUpperCase().indexOf(dbVendor.toUpperCase()) != -1) {
                    isVendorSpecific = true;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception while reading manifest file : ", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    logger.log(Level.FINE, "Exception while closing JarFile '"
                            + jarFile.getName() + "' :", ex);
                }
            }
        }

        return isVendorSpecific;
    }
}
