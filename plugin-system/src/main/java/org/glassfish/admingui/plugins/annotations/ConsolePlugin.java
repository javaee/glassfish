/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.annotations;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author jasonlee
 */
@Contract
public interface ConsolePlugin {
    int priority = 300;
}
