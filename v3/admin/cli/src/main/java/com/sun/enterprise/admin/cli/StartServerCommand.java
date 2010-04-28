/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import org.glassfish.api.admin.RuntimeType;

/**
 * @author bnevins
 */
public interface StartServerCommand {

    /**
  * @return the type of "this" server
  */
    RuntimeType getType();

    /**
     * Create a launcher for the whatever type of server "we" are.
     */
    void createLauncher() throws GFLauncherException, MiniXmlParserException;
}
