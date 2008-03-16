/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admingui;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;
import java.net.URL;

/**
 *
 * @author anilam
 */
/**
 *  <p> Returns the URL of the Integration Point configuration file
 *	console-config.xml </p>
 *
 *  @return URL of configration file.
 */
@Service
public class WebConsolePlugin implements ConsoleProvider {
    
    public URL getConfiguration(){ 
            return null;
    }
}

