/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.example.glassfish.admingui;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;

import java.net.URL;


/**
 *  <p> This is a noop file just to help test out the {@link ConsoleProvider}
 *      and {@link ConsolePluginService} files.</p>
 *
 *  @author  anilam
 */
@Service
public class JavaOnePlugin implements ConsoleProvider {

    public URL getConfiguration() { return null; }
}
