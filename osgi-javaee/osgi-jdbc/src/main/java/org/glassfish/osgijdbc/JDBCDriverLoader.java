/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.osgijdbc;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class JDBCDriverLoader {

    private static Logger logger = Logger.getLogger(JDBCDriverLoader.class.getPackage().getName());

    private static final String DRIVER_INTERFACE_NAME = "java.sql.Driver";
    private static final String META_INF_SERVICES_DRIVER_FILE = "META-INF/services/java.sql.Driver";
    private static final String DBVENDOR_MAPPINGS_ROOT =
            System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + File.separator +
                    "lib" + File.separator + "install" + File.separator + "databases" +
                    File.separator + "dbvendormapping" + File.separator;

    private static final String DS_PROPERTIES = "ds.properties";
    private static final String CPDS_PROPERTIES = "cpds.properties";
    private static final String XADS_PROPERTIES = "xads.properties";
    private static final String DRIVER_PROPERTIES = "driver.properties";
    private static final String VENDOR_PROPERTIES = "dbvendor.properties";


    public static final Map<String, Map<String, String>> dbVendorMappings = new HashMap<String, Map<String, String>>();

    static{
        loadMappings();
    }

    private ClassLoader cl;
    
    public JDBCDriverLoader(ClassLoader cl){
        this.cl = cl;
    }

    private static void loadMappings(){
        dbVendorMappings.put(Constants.DS, ((Map)loadProperties(DS_PROPERTIES)));
        dbVendorMappings.put(Constants.CPDS, ((Map)loadProperties(CPDS_PROPERTIES)));
        dbVendorMappings.put(Constants.XADS, ((Map)loadProperties(XADS_PROPERTIES)));
        dbVendorMappings.put(Constants.DRIVER, ((Map)loadProperties(DRIVER_PROPERTIES)));
        dbVendorMappings.put(Constants.DBVENDOR, ((Map)loadProperties(VENDOR_PROPERTIES)));
    }

    private static Properties loadProperties(String type){
        Properties fileProperties = new Properties();
        String fileName = DBVENDOR_MAPPINGS_ROOT + type;
        File mappingFile = new File(fileName);

        if (mappingFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(mappingFile);
                try {
                    fileProperties.load(fis);
                } finally {
                    fis.close();
                }
            } catch (IOException ioe) {
                logger.fine("IO Exception while loading properties file" +
                        " [ "+mappingFile.getAbsolutePath()+" ] : " +  ioe);
            }
        } else {
            logger.warning("File not found : " + mappingFile.getAbsolutePath());
        }
        return fileProperties;
    }

    /**
     * Get a set of common database vendor names supported in GlassFish.
     * @return database vendor names set.
     */
    private Set<String> getDatabaseVendorNames() {
        Map vendors = dbVendorMappings.get(Constants.DBVENDOR);
        return vendors.keySet();
    }

    private String getDBVendor(String className, String type) {
        String dbVendor = null;
        Map map = dbVendorMappings.get(type);
        Set entrySet = map.entrySet();
        Iterator entryIterator = entrySet.iterator();
        while(entryIterator.hasNext()){
            Map.Entry entry = (Map.Entry)entryIterator.next();
            if(((String)entry.getValue()).equalsIgnoreCase(className)){
                dbVendor = (String)entry.getKey();
                break;
            }
        }
        return dbVendor;
    }

    private String getImplClassNameFromMapping(String dbVendor, String resType) {
        Map fileProperties = dbVendorMappings.get(resType);
        if(fileProperties == null){
            throw new IllegalStateException("Unknown resource type [ " + resType + " ]");
        }
        return (String)fileProperties.get(dbVendor.toUpperCase());
    }


    public Properties loadDriverInformation(File f) {
        String implClass;
        JarFile jarFile = null;

        Properties properties = new Properties();
        try {
            jarFile = new JarFile(f);
            Enumeration e = jarFile.entries();
            while (e.hasMoreElements()) {

                ZipEntry zipEntry = (ZipEntry) e.nextElement();

                if (zipEntry != null) {

                    String entry = zipEntry.getName();
                    if (META_INF_SERVICES_DRIVER_FILE.equals(entry)) {

                        InputStream metaInf = jarFile.getInputStream(zipEntry);
                        implClass = processMetaInfServicesDriverFile(metaInf);
                        if (implClass != null) {
                            if (isLoaded(implClass, Constants.DRIVER, cl)) {
                                String vendor = getVendorFromManifest(f);

                                Set<String> dbVendorNames = getDatabaseVendorNames();
                                if (vendor != null && dbVendorNames.contains(vendor)) {

                                    String dsClassName = getImplClassNameFromMapping(vendor, Constants.DS);
                                    String cpdsClassName = getImplClassNameFromMapping(vendor, Constants.CPDS);
                                    String xadsClassName = getImplClassNameFromMapping(vendor, Constants.XADS);
                                    //String driverClassName = getImplClassNameFromMapping(vendor,DRIVER);
                                    String driverClassName = implClass;

                                    properties.put(Constants.DS, dsClassName);
                                    properties.put(Constants.CPDS, cpdsClassName);
                                    properties.put(Constants.XADS, xadsClassName);
                                    properties.put(Constants.DRIVER, driverClassName);

                                    return properties;

                                } else if (vendor != null) {

                                    Set<String> dsClasses = getImplClassesByIteration(f, Constants.DS, vendor, cl);
                                    if (dsClasses.size() == 1) {
                                        properties.put(Constants.DS, dsClasses.toArray()[0]);
                                    }

                                    Set<String> cpdsClasses = getImplClassesByIteration(f, Constants.CPDS, vendor, cl);
                                    if (cpdsClasses.size() == 1) {
                                        properties.put(Constants.CPDS, cpdsClasses.toArray()[0]);
                                    }

                                    Set<String> xadsClasses = getImplClassesByIteration(f, Constants.XADS, vendor, cl);
                                    if (xadsClasses.size() == 1) {
                                        properties.put(Constants.XADS, xadsClasses.toArray()[0]);
                                    }
                                    properties.put(Constants.DRIVER, implClass);

                                    return properties;
                                }
                            }
                        }
                        logger.finest("Driver loader : implClass = " + implClass);

                    }
                    if (entry.endsWith(".class")) {
                        //Read from MANIFEST.MF file for all jdbc40 drivers and resType
                        //java.sql.Driver.TODO : this should go outside .class check.
                        //TODO : Some classnames might not have these strings
                        //in their classname. Logic should be flexible in such cases.
                        if (entry.toUpperCase().contains("DATASOURCE")) {
                            implClass = getClassName(entry);
                            if (implClass != null) {
                                if (isLoaded(implClass, Constants.XADS, cl)) {
                                    String dbVendor = getDBVendor(implClass, Constants.XADS);
                                    if (dbVendor != null) {
                                        detectImplClasses(properties, dbVendor);
                                        return properties;
                                    } else {
                                        properties.put(Constants.XADS, implClass);
                                    }
                                } else if (isLoaded(implClass, Constants.CPDS, cl)) {
                                    String dbVendor = getDBVendor(implClass, Constants.CPDS);
                                    if (dbVendor != null) {
                                        detectImplClasses(properties, dbVendor);
                                        return properties;
                                    } else {
                                        properties.put(Constants.CPDS, implClass);
                                    }
                                } else if (isLoaded(implClass, Constants.DS, cl)) {
                                    String dbVendor = getDBVendor(implClass, Constants.DS);
                                    if (dbVendor != null) {
                                        detectImplClasses(properties, dbVendor);
                                        return properties;
                                    } else {
                                        properties.put(Constants.DS, implClass);
                                    }
                                }
                            }
                        } else if (entry.toUpperCase().contains("DRIVER")) {
                            implClass = getClassName(entry);
                            if (implClass != null) {
                                if (isLoaded(implClass, Constants.DRIVER, cl)) {
                                    String dbVendor = getDBVendor(implClass, Constants.DRIVER);
                                    if (dbVendor != null) {
                                        detectImplClasses(properties, dbVendor);
                                        return properties;
                                    } else {
                                        properties.put(Constants.DRIVER, implClass);
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
        if (properties.get(Constants.DRIVER) != null) {
            return properties;
        } else {
            throw new RuntimeException("Unable to introspect jar [ "+f.getName()+" ] for Driver Class, " +
                    "no implementation for java.sql.Driver is found");
        }
    }

    private void detectImplClasses(Properties properties, String dbVendor) {
        String xads = getImplClassNameFromMapping(dbVendor, Constants.XADS);
        String cpds = getImplClassNameFromMapping(dbVendor, Constants.CPDS);
        String ds = getImplClassNameFromMapping(dbVendor, Constants.DS);
        String driver = getImplClassNameFromMapping(dbVendor, Constants.DRIVER);

        properties.put(Constants.XADS, xads);
        properties.put(Constants.CPDS, cpds);
        properties.put(Constants.DS, ds);
        properties.put(Constants.DRIVER, driver);
    }


    private Set<String> getImplClassesByIteration(File f, String resType, String dbVendor, ClassLoader cl) {
        SortedSet<String> implClassNames = new TreeSet<String>();
        String implClass = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Enumeration e = jarFile.entries();
            while (e.hasMoreElements()) {

                ZipEntry zipEntry = (ZipEntry) e.nextElement();

                if (zipEntry != null) {

                    String entry = zipEntry.getName();
                    if (DRIVER_INTERFACE_NAME.equals(resType)) {
                        if (META_INF_SERVICES_DRIVER_FILE.equals(entry)) {

                            InputStream inputStream = jarFile.getInputStream(zipEntry);
                            implClass = processMetaInfServicesDriverFile(inputStream);
                            if (implClass != null) {
                                if (isLoaded(implClass, resType, cl)) {
                                    //Add to the implClassNames only if vendor name matches.
                                    if (isVendorSpecific(f, dbVendor, implClass)) {
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
                        //TODO : Some classnames might not have these strings
                        //in their classname. Logic should be flexible in such cases.
                        if (entry.toUpperCase().indexOf("DATASOURCE") != -1 ||
                                entry.toUpperCase().indexOf("DRIVER") != -1) {
                            implClass = getClassName(entry);
                            if (implClass != null) {
                                if (isLoaded(implClass, resType, cl)) {
                                    if (isVendorSpecific(f, dbVendor, implClass)) {
                                        implClassNames.add(implClass);
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

    private boolean isNotAbstract(Class cls) {
        int modifier = cls.getModifiers();
        return !Modifier.isAbstract(modifier);
    }

    /**
     * Reads the META-INF/services/java.sql.Driver file contents and returns
     * the driver implementation class name.
     * In case of JDBC-4.0 drivers or above, the META-INF/services/java.sql.Driver file
     * contains the name of the driver class.
     *
     * @param inputStream
     * @return driver implementation class name
     */
    private String processMetaInfServicesDriverFile(InputStream inputStream) {
        String driverClassName = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                driverClassName = line;
            }
        } catch (IOException ioex) {
            logger.finest("DriverLoader : exception while processing " +
                    "META-INF directory for DriverClassName " + ioex);
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                logger.log(Level.FINE, "Error while closing file handle after reading META-INF file : ", ex);
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                logger.log(Level.FINE, "Error while closing file handle after reading META-INF file : ", ex);
            }
        }
        return driverClassName;
    }

    /**
     * Check if the class has been loaded and if it is a Driver or a
     * DataSource impl.
     *
     * @param classname class
     * @return boolean indicating whether the class is loaded or not
     */
    private boolean isLoaded(String classname, String resType, ClassLoader loader) {
        Class cls = null;
        try {
            cls = loader.loadClass(classname);
        } catch (Throwable t) {
            cls = null;
        }
        return (isResType(cls, resType));
    }

    /**
     * Find if the particular class has any implementations of java.sql.Driver or
     * javax.sql.DataSource or any other resTypes passed.
     *
     * @param cls     class to be introspected
     * @param resType resource-type
     * @return boolean indicating the status
     */
    private boolean isResType(Class cls, String resType) {
        boolean isResType = false;
        if (cls != null) {
            if (Constants.DS.equals(resType)) {
                if (javax.sql.DataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if (Constants.CPDS.equals(resType)) {
                if (javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if (Constants.XADS.equals(resType)) {
                if (javax.sql.XADataSource.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            } else if (Constants.DRIVER.equals(resType)) {
                if (java.sql.Driver.class.isAssignableFrom(cls)) {
                    isResType = isNotAbstract(cls);
                }
            }
        }
        return isResType;
    }


    private String getClassName(String classname) {
        classname = classname.replaceAll("/", ".");
        classname = classname.substring(0, classname.lastIndexOf(".class"));
        return classname;
    }

    private boolean isVendorSpecific(File f, String dbVendor, String className) {
        //File could be a jdbc jar file or a normal jar file
        boolean isVendorSpecific = false;
        String vendor = getVendorFromManifest(f);

        if (vendor == null) {
            //might have to do this part by going through the class names or some other method.
            //dbVendor might be used in this portion
            if (isVendorSpecific(dbVendor, className)) {
                isVendorSpecific = true;
            }
        } else {
            //Got from Manifest file.
            if (vendor.equalsIgnoreCase(dbVendor) ||
                    vendor.toUpperCase().indexOf(dbVendor.toUpperCase()) != -1) {
                isVendorSpecific = true;
            }
        }
        return isVendorSpecific;
    }

    /**
     * Utility method that checks if a classname is vendor specific.
     * This method is used for jar files that do not have a manifest file to
     * look up the classname.
     *
     * @param dbVendor  DB vendor name
     * @param className class name
     * @return true if the className in question is vendor specific.
     */
    private boolean isVendorSpecific(String dbVendor, String className) {
        return className.toUpperCase().indexOf(dbVendor.toUpperCase()) != -1;
    }

    /**
     * Get a vendor name from a Manifest entry in the file.
     *
     * @param f file
     * @return null if no manifest entry found.
     */
    private String getVendorFromManifest(File f) {
        String vendor = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(f);
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                if (mainAttributes != null) {
                    vendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR.toString());
                    if (vendor == null) {
                        vendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString());
                    }
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
        return vendor;
    }
}
