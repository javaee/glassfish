package com.sun.appserv.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
*
* This class provides static methods to make accessable the version 
* as well as the individual parts that make up the version
*
*/
public class Version {

    /**
     * Name of the property resources that contain version information. The
     * property resources are searched in their order the array. As soon as
     * a property resource is found, the search stops. Note that if EE
     * property resource is present in the classpath it will take precedence.
     */
    private static final String[] resourceClassNames = 
            {"com/sun/enterprise/ee/server/Version.properties",
            "com/sun/enterprise/server/Version.properties"};

    /**
     * Name of the properties used to override build time values.
     */
    private static final String PRODUCT_NAME = "product.name";
    private static final String ABBREV_PRODUCT_NAME = "abbrev.product.name";
    private static final String FULL_VERSION = "full.version";
    private static final String MAJOR_VERSION = "major.version";
    private static final String MINOR_VERSION = "minor.version";
    private static final String BUILD_ID = "build.id";

    /**
    * version strings populated during build
    */
    private static String product_name          = "GlassFish";
    private static String abbrev_product_name   = "GlassFish";
    private static String full_version          = "10.0-SNAPSHOT";
    private static String major_version         = "10";
    private static String minor_version         = "0"; 
    private static String build_id              = "b001";

    static {
        for (int i = 0; i < resourceClassNames.length; i++) {
            Properties props = new Properties();
            boolean found = false;
            InputStream is = Version.class.getClassLoader().
                               getResourceAsStream(resourceClassNames[i]);
            
            if (is != null) {
                try {
                    props.load(is);
                    found = true;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    // Ignore this IOException. 
                } finally {
                    try {
                        is.close();
                    } catch (IOException ee) {
                        // Ignore exception in closing stream
                    }
                }
            }
            if (found) {
                product_name = props.getProperty(PRODUCT_NAME, product_name);
                abbrev_product_name = props.getProperty(ABBREV_PRODUCT_NAME,
                        abbrev_product_name);
                full_version = props.getProperty(FULL_VERSION, full_version);
                major_version = props.getProperty(MAJOR_VERSION, major_version);
                minor_version = props.getProperty(MINOR_VERSION, minor_version);
                // Build id should always come from build. 
                break;
            }
        }
    }

    /**
    * Returns version
    */ 
    public static String getVersion() {
        return product_name + " " + full_version;
    }

    /**
    * Returns full version including build id
    */
    public static String getFullVersion() {
        return (getVersion() + " (build " + build_id + ")");
    }

    /**
    * Returns abbreviated version.
    */
    public static String getAbbreviatedVersion() {
        return abbrev_product_name + major_version +
               "." + minor_version;
    }

    /**
    * Returns Major version
    */ 
    public static String getMajorVersion() {
    	return major_version;
    }

    /**
    * Returns Minor version
    */ 
    public static String getMinorVersion() {
    	return minor_version;
    }

    /**
    * Returns Build version
    */ 
    public static String getBuildVersion() {
    	return build_id;
    }

    /**
    * Returns Proper Product Name
    */
    public static String getProductName() {
    	return product_name;
    }

    /**
    * Returns Abbreviated Product Name
    */
    public static String getAbbrevProductName() {
    	return abbrev_product_name;
    }

}
