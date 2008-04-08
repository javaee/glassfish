package org.example.glassfish.admingui;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;
import java.net.URL;


/**
 *  <p> Returns the URL to <code>console-config.xml</code> where
 *      <code>IntegrationPoint</code>s are defined.</p>
 *
 *  @return URL of configration file.
 */
@Service
public class MyPlugin implements ConsoleProvider {
    public URL getConfiguration() { return null; }
}
