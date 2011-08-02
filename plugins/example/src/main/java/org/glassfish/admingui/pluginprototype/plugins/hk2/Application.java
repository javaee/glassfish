/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.pluginprototype.plugins.hk2;

public class Application {

    private String name;
    private boolean enabled;
    private String engines;

    public Application(String name, boolean enabled, String engines) {
        this.name = name;
        this.enabled = enabled;
        this.engines = engines;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEngines(String engines) {
        this.engines = engines;
    }

    public String getEngines() {
        return engines;
    }
}
