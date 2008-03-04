package com.sun.enterprise.v3.server;

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
*/
@Service
public class SystemTasks implements Init, PostConstruct {

  @Inject
  JavaConfig javaConfig;

  Logger _logger = Logger.getAnonymousLogger();
    
  public void postConstruct() {

      // adding our version of some system properties.
      System.setProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY, System.getProperty("java.home"));

      String hostname = "localhost";
      try {
          // canonical name checks to make sure host is proper
          hostname = NetUtils.getCanonicalHostName();
      } catch (Exception ex) {
          if (_logger != null)
              _logger.log(Level.SEVERE, "cannot determine host name, will use localhost exclusively", ex);
      }
      if (_logger != null)

      System.setProperty(SystemPropertyConstants.HOST_NAME_PROPERTY, hostname);

      Pattern p = Pattern.compile("-D([^=]*)=(.*)");
      for (String jvmOption : javaConfig.getJvmOptions()) {
          Matcher m = p.matcher(jvmOption);
          if (m.matches())
          {
              System.setProperty(m.group(1), TranslatedConfigView.getTranslatedValue(m.group(2)).toString());
              if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Setting " + m.group(1) + " = " + TranslatedConfigView.getTranslatedValue(m.group(2)));
              }
          }
      }
  }
}