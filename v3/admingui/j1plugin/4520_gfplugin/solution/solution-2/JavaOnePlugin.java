
package org.example.glassfish.admingui;
import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;
import java.net.URL;

/**
 *      <p> Returns the URL of the Integration Point configuration file 
 *          console-config.xml </p>
 *
 *      @return URL of configration file. 
 */
@Service
public class JavaOnePlugin implements ConsoleProvider {
   
    public URL getConfiguration() { return null; }
}