package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.spi.ConnectorConstants;
import com.sun.logging.LogDomains;

import java.io.File;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectorsUtil {

    protected final static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    /**
     * determine whether the RAR in question is a System RAR
     * @param raName RarName
     * @return boolean
     */
    public static boolean belongsToSystemRA(String raName) {
        boolean result = false;

        for (String systemRarName : ConnectorConstants.systemRarNames) {
            if (systemRarName.equals(raName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * get the installation directory of System RARs
     * @param moduleName RARName
     * @return directory location
     */
    public static String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(ConnectorConstants.INSTALL_ROOT) +
                File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + moduleName;

        return j2eeModuleDirName;
    }

    public static String getLocation(String moduleName) {
        /* TODO V3

            if(moduleName == null) {
                return null;
            }
            String location  = null;
            ConnectorModule connectorModule =
                    dom.getApplications().getConnectorModuleByName(moduleName);
            if(connectorModule != null) {
                location = RelativePathResolver.
                        resolvePath(connectorModule.getLocation());
            }
            return location;
        */
        return null;

    }

    /**
     *  Return the system PM name for the JNDI name
     * @param  jndiName jndi name
     * @return String jndi name for PM resource
     **/
    public  static String getPMJndiName( String jndiName )  {
        return jndiName + ConnectorConstants.PM_JNDI_SUFFIX;
    }

    /**
     * check whether the jndi Name has connector related suffix and return if any.
     * @param name jndi name
     * @return suffix, if found
     */
    public static String getValidSuffix(String name) {
        if (name != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (name.endsWith(validSuffix)) {
                    return validSuffix;
                }
            }
        }
        return null;
    }

    /**
     * If the suffix is one of the valid context return true.
     * Return false, if that is not the case.
     *
     * @param suffix __nontx / __pm
     * @return boolean whether the suffix is valid or not
     */
    public static boolean isValidJndiSuffix(String suffix) {
        if (suffix != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (validSuffix.equals(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given the name of the resource and its jndi env, derive the complete jndi name. (eg; with __PM / __nontx)
     * @param name name of the resource
     * @param env env
     * @return derived name
     */
    public static String deriveJndiName(String name, Hashtable env) {
        String suffix = (String) env.get(ConnectorConstants.JNDI_SUFFIX_PROPERTY);
        if (ConnectorsUtil.isValidJndiSuffix(suffix)) {
            _logger.log(Level.FINE, "JNDI name will be suffixed with :" + suffix);
            return name + suffix;
        }
        return name;
    }


}
