package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import java.util.Collections;
import java.util.List;
import org.glassfish.internal.api.Init;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.config.support.TranslatedConfigView;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Init service to take care of vm related tasks.
 *
 * @author Jerome Dochez
 * @author Byron Nevins
 */
@Service
public class SystemTasks implements Init, PostConstruct {

    @Inject
    JavaConfig javaConfig;
   
    @Inject
    Server server;
    
    @Inject
    Domain domain;
    
    Logger _logger = Logger.getAnonymousLogger();

    public void postConstruct() {
        setSystemPropertiesFromEnv();
        setSystemPropertiesFromDomainXml();
        resolveJavaConfig();
    }

    /*
     * Here is where we make the change Post-TP2 to *not* use JVM System Properties
     */
    private void setSystemProperty(String name, String value) {
        System.setProperty(name, value);
    }

    private void setSystemPropertiesFromEnv() {
        // adding our version of some system properties.
        setSystemProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY, System.getProperty("java.home"));

        String hostname = "localhost";
        try {
            // canonical name checks to make sure host is proper
            hostname = NetUtils.getCanonicalHostName();
        }
        catch (Exception ex) {
            if (_logger != null)
                _logger.log(Level.SEVERE, "cannot determine host name, will use localhost exclusively", ex);
        }
        if (_logger != null)
            setSystemProperty(SystemPropertyConstants.HOST_NAME_PROPERTY, hostname);
    }

    private void setSystemPropertiesFromDomainXml() {
        // precedence order from high to low
        // 1. server
        // 2. server-config
        // so we need to add System Properties in *reverse order* to get the 
        // right precedence.

        List<SystemProperty> serverSPList = server.getSystemProperty();
        List<SystemProperty> configSPList = getConfigSystemProperties();

        setSystemProperties(configSPList);
        setSystemProperties(serverSPList);
    }

    private List<SystemProperty> getConfigSystemProperties() {
        try {
            String configName = server.getConfigRef();
            Configs configs = domain.getConfigs();
            List<Config> configsList = configs.getConfig();
            Config config = null;

            for (Config c : configsList) {
                if (c.getName().equals(configName)) {
                    config = c;
                    break;
                }
            }
            
            return config.getSystemProperty();
        }
        catch(Exception e) {  //possible NPE if domain.xml has issues!
            return Collections.emptyList();
        }
    }

    private void resolveJavaConfig() {
        Pattern p = Pattern.compile("-D([^=]*)=(.*)");
        for (String jvmOption : javaConfig.getJvmOptions()) {
            Matcher m = p.matcher(jvmOption);
            if (m.matches()) {
                setSystemProperty(m.group(1), TranslatedConfigView.getTranslatedValue(m.group(2)).toString());
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Setting " + m.group(1) + " = " + TranslatedConfigView.getTranslatedValue(m.group(2)));
                }
            }
        }
    }

    private void setSystemProperties(List<SystemProperty> spList) {
        for (SystemProperty sp : spList) {
            String name = sp.getName();
            String value = sp.getValue();
            if(ok(name)) {
                setSystemProperty(name,value);
            }
        }
    }
    
    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}

