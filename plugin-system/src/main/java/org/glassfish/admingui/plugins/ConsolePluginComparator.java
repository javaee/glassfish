/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins;

import java.util.Comparator;

/**
 *
 * @author jasonlee
 */
public class ConsolePluginComparator implements Comparator<ConsolePluginMetadata> {
    @Override
    public int compare(ConsolePluginMetadata cp1, ConsolePluginMetadata cp2) {
        return cp2.getPriority() - cp1.getPriority();
    }
}