/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.runtime.sqlstore.database;

import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOFatalUserException;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.runtime.sqlstore.database.SpecialDBOperation;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.utility.I18NHelper;
import com.sun.persistence.utility.PropertyHelper;
import com.sun.persistence.utility.database.DBVendorTypeHelper;
import com.sun.persistence.utility.logging.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Different database may have different syntax for query statement. DbVendorType
 * have APIs to give you correct syntax of query based on the database type.
 * @author jie leng
 */
public class DBVendorType  {
    /**
     * Map from property name to property value.
     */
    private HashMap dbMap;

    /**
     * VendorType as returned from {@link DBVendorTypeHelper#getDBType(java.lang.String)}
     */
    private String vendorType;

    /**
     * VendorType as returned from {@link DBVendorTypeHelper#getEnumDBType(java.lang.String)}
     */
    private int enumVendorType;

    /**
     * The logger
     */
    private final static Logger logger;

    /**
     * I18N message handler
     */
    private final static ResourceBundle messages;

    /**
     * Default properties
     */
    private static Properties defaultProperties;
    
    /**
     * Instance of specialDBOperation for this vendor type. Please look at
     * newSpecialDBOperationInstance() to find out how this member is
     * initialized.
     */
    private SpecialDBOperation specialDBOperation;

    private static final SpecialDBOperation DEFAULT_SPECIAL_DB_OPERATION =
        new BaseSpecialDBOperation();

    private final static String EXT = ".properties"; // NOI18N
    private final static String SPACE = " "; // NOI18N
    private final static String NONE = ""; // NOI18N

    private final static String PATH = "com/sun/persistence/runtime/sqlstore/database/"; // NOI18N
    private final static String PROPERTY_OVERRIDE_FILE = ".tpersistence.properties"; // NOI18N

    /**
     * where clause functions
     */
    private final static String MOD = "MOD"; //NOI18N
    private final static String SQRT = "SQRT"; //NOI18N
    private final static String ABS = "ABS"; //NOI18N
    private final static String SUBSTRING = "SUBSTRING"; //NOI18N
    private final static String STRING_CONCAT = "CONCAT"; //NOI18N
    private final static String STRING_LENGTH = "LENGTH"; //NOI18N
    private final static String LOCATE_TWO = "LOCATE_TWO"; //NOI18N
    private final static String LOCATE_THREE = "LOCATE_THREE"; //NOI18N
    private final static String LOCATE_THREE_ARGS = "LOCATE_THREE_ARGS"; //NOI18N
    private final static String TRIM = "TRIM"; //NOI18N    
    private final static String LOWER = "LOWER"; //NOI18N
    private final static String UPPER = "UPPER"; //NOI18N
    private final static String IS_NULL = "IS_NULL"; //NOI18N
    private final static String IS_NOT_NULL = "IS_NOT_NULL"; //NOI18N
    private final static String LIKE_ESCAPE = "LIKE_ESCAPE"; //NOI18N
    private final static String NOT_LIKE_ESCAPE = "NOT_LIKE_ESCAPE"; //NOI18N
    private final static String LIKE = "LIKE"; //NOI18N
    private final static String NOT_LIKE = "NOT_LIKE"; //NOI18N
    private final static String LIKE_THREE_ARGS = "LIKE_THREE_ARGS"; //NOI18N
    private final static String BETWEEN = "BETWEEN"; //NOI18N   
    private final static String NOT_BETWEEN = "NOT_BETWEEN"; //NOI18N   
    private final static String SPECIAL_DB_OPERATION = "SPECIAL_DB_OPERATION"; //NOI18N  

    private static final String[] props = new String[] {
            IS_NULL, IS_NOT_NULL,
            TRIM, LOWER, UPPER,
            STRING_CONCAT, STRING_LENGTH, SUBSTRING, 
            LOCATE_TWO, LOCATE_THREE, LOCATE_THREE_ARGS,
            SQRT, ABS, MOD,         
            LIKE_ESCAPE, NOT_LIKE_ESCAPE, LIKE, NOT_LIKE, LIKE_THREE_ARGS,
            BETWEEN, NOT_BETWEEN,
            SPECIAL_DB_OPERATION
    };

    /**
     * Initialize static fields.
     */
    static {
        logger = LogHelperSQLStore.getLogger();
        messages = I18NHelper.loadBundle(
            "com.sun.persistence.runtime.Bundle", // NOI18N
            DBVendorType.class.getClassLoader());

        defaultProperties = initializeDefaultProperties();
    }
  
    /**
     * @param databaseMetaData Instance of DatabaseMetaData
     * @param identifier identifier of the caller creating a new instance
     * of SQLStoreManager.
     */
    public DBVendorType(DatabaseMetaData databaseMetaData, String identifier)
        throws SQLException {
        
        String vendorName = databaseMetaData.getDatabaseProductName();
        String vendorType = DBVendorTypeHelper.getDBType(vendorName);

        if (logger.isLoggable()) {
            Object[] items = new Object[] {vendorName,vendorType};
            logger.fine("sqlstore.database.dbvendor.vendorname", items); // NOI18N
        }

        this.vendorType     = vendorType;
        enumVendorType      = DBVendorTypeHelper.getEnumDBType(vendorType);
        dbMap               = getDBPropertiesMap(vendorType, vendorName);
 
        specialDBOperation  = newSpecialDBOperationInstance(
                (String)dbMap.get(SPECIAL_DB_OPERATION),
                databaseMetaData,
                identifier);
    }

    /**
     * get properties map for given vendorType and vendorName
     */
    private static HashMap getDBPropertiesMap(String vendorType, String vendorName) {
        //Initialize returned map to default
        HashMap dbHashMap = new HashMap(defaultProperties);
        Properties dbProperties = loadDBProperties(vendorType, vendorName);
        dbHashMap.putAll(dbProperties);

        return dbHashMap;
    }

    /**
     * Initialize default properties.
     */
    private static Properties initializeDefaultProperties() {
        //Load default properties if not already loaded
        if (defaultProperties == null) {
            // Load default (sql92) Properties
            defaultProperties = new Properties();
            try {
                loadFromResource(DBVendorTypeHelper.DEFAULT_DB, defaultProperties);
            } catch (IOException e) {
                throw new JDOFatalInternalException(I18NHelper.getMessage(messages,
                                "sqlstore.database.dbvendor.cantloadDefaultProperties"), // NOI18N
                                 e);
            }
        }

        return defaultProperties;
     }

    /**
     * Load properties for database specified by vendorType and vendorName
     */
    private static Properties loadDBProperties(String vendorType, String vendorName) {
        // Load actual Properties. Even if it is unknown db,
        Properties dbProperties = new Properties();
        if (!vendorType.equals(DBVendorTypeHelper.DEFAULT_DB)) {
            try {
                loadFromResource(vendorType, dbProperties);
            } catch (IOException e) {
                // else ignore
                if (logger.isLoggable()) {
                    logger.fine("sqlstore.database.dbvendor.init.default", vendorType); // NOI18N
                }
            }
        }
        overrideProperties(dbProperties, vendorName);
        return dbProperties;
    }

    /**
     * Overrides default properties with the ones provided
     * by the user
     */
    private static void overrideProperties(Properties dbProperties, String vendorName) {
        boolean debug = logger.isLoggable();

        Properties overridingProperties = new Properties();
        try {
            PropertyHelper.loadFromFile(overridingProperties, PROPERTY_OVERRIDE_FILE);
        } catch (Exception e) {
            if (debug) {
                logger.fine("sqlstore.database.dbvendor.overrideproperties"); // NOI18N
            }
            return; 	// nothing to override
        }

        //Prepare a clean vendor name by replacing all ' '  and '/' with underscores.
        String cleanVendorName = vendorName.toLowerCase().replace(' ', '_');
        cleanVendorName = cleanVendorName.replace('/', '_');

        String propertyPrefix = "database." + cleanVendorName + "."; // prefix // NOI18N

        for (int i = 0; i < props.length; i++) {
            String o = overridingProperties.getProperty(propertyPrefix + props[i]);
            if (o != null) {
                if (debug) {
                    Object[] items = new Object[] {props[i],o};
                    logger.fine("sqlstore.database.dbvendor.overrideproperties.with", items); // NOI18N
                }
                dbProperties.setProperty(props[i], o);
            }
        }
    }

    /**
     * loads database properties list from the specified resource
     * into specified Properties object.
     * @param resourceName  Name of resource.
     * @param properties    Properties object to load
     */
    private static void loadFromResource(String resourceName, Properties properties)
            throws IOException {
        String fullResourceName = PATH + resourceName + EXT;
        PropertyHelper.loadFromResource(properties, fullResourceName, DBVendorType.class.getClassLoader());
    }

    /**
     * Returns new instance of class specified by specialDBOpClassName.
     * The class is required to implement interface SpecialDBOperation.
     * If specialDBOpClassName is null or cannot be loaded, then an instance
     * of BaseSpecialDBOperation is returned.
     * @param specialDBOpClassName Name of a class that implements
     * SpecialDBOperation
     * @param databaseMetaData DatabaseMetaData for the connection for which
     * this SpecialDBOperation is created
     * @param identifier Identifier of pmf used to obtain databaseMetaData.
     * This can be null in non managed environment.
     * @return An instance of SpecialDBOperation specified by specialDBOpClassName.
     */
    private SpecialDBOperation newSpecialDBOperationInstance(
        final String specialDBOpClassName, DatabaseMetaData databaseMetaData,
        String identifier) {
        SpecialDBOperation retInstance = null;
        if (specialDBOpClassName != null) {
            final ClassLoader loader = DBVendorType.class.getClassLoader();
            Class clz = (Class)java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            if (loader != null) {
                                return Class.forName(specialDBOpClassName,
                                    true, loader);
                            } else {
                                return Class.forName(specialDBOpClassName);
                            }
                        } catch(Exception ex) {
                            if (logger.isLoggable()) {
                                logger.log(Logger.INFO,
                                    "core.configuration.cantloadclass", // NOI18N
                                    specialDBOpClassName);
                            }
                            return null;
                        }
                    }
                }
            );

            if (clz != null) {
                try {
                    retInstance = (SpecialDBOperation)clz.newInstance();
                    retInstance.initialize(databaseMetaData, identifier);
                } catch(Exception ex) {
                    throw new JDOFatalUserException(
                        I18NHelper.getMessage(messages,
                        "sqlstore.database.dbvendor.cantinstantiateclass", // NOI18N
                        specialDBOpClassName), ex);
                }
            }
        }
        return (retInstance != null)? retInstance : DEFAULT_SPECIAL_DB_OPERATION;
    }
    
    /**
     * Returns the string that represents "mod" clause
     * for this database
     */
    public String getMod(String lhs, String rhs) {
        String s = (String)dbMap.get(MOD);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               MOD));
        }
        
        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getModFunctionName", s); // NOI18N
        }

        return String.format(s, lhs, rhs);
    }
    
    /**
     * Returns the string that represents "abs" clause
     * for this database
     */
    public String getAbs(String expr) {
        String s = (String)dbMap.get(ABS);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               ABS));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getabs", s); // NOI18N
        }

        return String.format(s, expr);
    }

    /**
     * Returns the string that represents "Sqrt" clause
     * for this database
     */
    public String getSqrt(String expr) {
        String s = (String)dbMap.get(SQRT);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               SQRT));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getsqrt", s); // NOI18N
        }

        return String.format(s, expr);
    }
    
    /**
     * Returns the string that represents concatenation operation
     * for this database
     */
    public String getConcat(String lhs, String rhs) {
        String s = (String)dbMap.get(STRING_CONCAT);
         if (s == null) {
               throw new JDOUserException(I18NHelper.getMessage(messages,
                   "core.constraint.illegalop", // NOI18N
                   STRING_CONCAT));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getstringconcat", s); // NOI18N
        }

        return String.format(s, lhs, rhs);
    }
    
    /**
     * Returns the string that represents length operation
     * for this database
     */
    public String getLength(String expr) {
        String s = (String)dbMap.get(STRING_LENGTH);
        if (s == null) {
              throw new JDOUserException(I18NHelper.getMessage(messages,
                  "core.constraint.illegalop", // NOI18N
                  STRING_LENGTH));
       }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getstringlength", s); // NOI18N
        }

        return String.format(s, expr);
    }
    
    /**
     * Returns the string that represents "Substring" clause
     * for this database
     */
    public String getSubstring(String str, String start, String length) {
        String s = (String)dbMap.get(SUBSTRING);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               SUBSTRING));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getsubstring", s); // NOI18N
        }

        return String.format(s, str, start, length);
    }
    
    /**
     * Returns the string that represents "locate" clause
     * for this database
     */    
    public String getLocate(String pattern, String str, String start) {
        String s = null;       
         
        if (!isLocateThreeArgs() || (start == null))
            s = (String)dbMap.get(LOCATE_TWO);
        else 
            s = (String)dbMap.get(LOCATE_THREE);
        
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               "Locate")); // NOI18N
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getlocate", s); // NOI18N
        }

        return String.format(s, pattern, str, start);
    }
    
    /**
     * Returns true if position has three argument for this database
     */
    public boolean isLocateThreeArgs() {
        String s = (String)dbMap.get(LOCATE_THREE_ARGS);
        Boolean b = Boolean.valueOf(s);

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getposition3args", b); // NOI18N
        }

        return b.booleanValue();
    }

    
    /**
     * Returns the string that represents string lower operation
     * for this database
     */
    public String getLower(String expr) {
        String s = (String)dbMap.get(LOWER);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               LOWER));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getstringlower", s); // NOI18N
        }

        return String.format(s, expr);
    }
      
    /**
     * Returns the string that represents string upper operation
     * for this database
     */
    public String getUpper(String expr) {
        String s = (String)dbMap.get(UPPER);
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               UPPER));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getstringupper", s); // NOI18N
        }

        return String.format(s, expr);
    }
    
     /**
     * Returns the string that represents "trim" clause
     * for this database
     */   
    public String getTrim(String trimSpec, String trimChar, String str) {
         String s = (String)dbMap.get(TRIM);
         if (s == null) {
            throw new JDOUserException(I18NHelper.getMessage(messages,
                "core.constraint.illegalop", // NOI18N
                TRIM));
         }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getTrim", s); // NOI18N
        }

        return String.format(s, trimSpec, trimChar, str);
    }
    
    /**
     * Returns the string that represents nul comparision clause
     * for this database
     */    
    public String getNull(String expr, boolean sense) {
        String s = null;
        String loggerKey = null;
        if (sense) {
            // get IS_NULL expression
            s = (String)dbMap.get(IS_NULL);
            loggerKey = "sqlstore.database.dbvendor.getisnull"; // NOI18N
        }
        else {
            // get IS_NOT_NULL expression
            s = (String)dbMap.get(IS_NOT_NULL);
            loggerKey = "sqlstore.database.dbvendor.getisnotnull"; // NOI18N
        }

        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               "Null Comparision"));  // NOI18N
        }

        if (logger.isLoggable()) {
            logger.fine(loggerKey, s);
        }

        return String.format(s, expr);
    }
    
    /**
     * Returns the string that represents "like" clause
     * for this database
     */  
    public String getLike(String expr, String pattern, String escape, 
            boolean sense) {
        String s = null;
        
        if (!isLikeThreeArgs() || (escape == null)) {
            s = sense ? (String)dbMap.get(LIKE):(String)dbMap.get(NOT_LIKE);
        } else {
            s = sense ? (String)dbMap.get(LIKE_ESCAPE):(String)dbMap.get(NOT_LIKE_ESCAPE);
        }
        
        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               "substring"));
        }

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getlike", s); // NOI18N
        }

        return String.format(s, expr, pattern, escape); 
    }
    
    /**
     * Returns true if position has three argument for this database
     */
    public boolean isLikeThreeArgs() {
        String s = (String)dbMap.get(LIKE_THREE_ARGS);
        Boolean b = Boolean.valueOf(s);

        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getlikethreeargs", b); // NOI18N
        }

        return b.booleanValue();
    }
    
     public String getBetween(String expr, String lower, String upper, 
             boolean sense) {
        String s = null;
        String loggerKey = null;
        if (sense) {
            // get BETWEEN expression
            s = (String)dbMap.get(BETWEEN);
            loggerKey = "sqlstore.database.dbvendor.getbwtween"; // NOI18N
        }
        else {
            // get NOT BETWEEN expression
            s = (String)dbMap.get(NOT_BETWEEN);
            loggerKey = "sqlstore.database.dbvendor.getnotbetween"; // NOI18N
        }

        if (s == null) {
           throw new JDOUserException(I18NHelper.getMessage(messages,
               "core.constraint.illegalop", // NOI18N
               BETWEEN));  // NOI18N
        }

        if (logger.isLoggable()) {
            logger.fine(loggerKey, s);
        }

        return String.format(s, expr, lower, upper);
     }
     
    /**
     * Returns database vendor type
     */
    public String getName() {
        return vendorType;
    }

    /**
     * Returns a SpecialDBOperation object
     */
    public SpecialDBOperation getSpecialDBOperation() {
        if (logger.isLoggable()) {
            logger.fine("sqlstore.database.dbvendor.getSpecialDBOperation", specialDBOperation); // NOI18N
        }

        return specialDBOperation;
    }

}
