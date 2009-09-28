/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

public class ProbeParam {
    String name = null;
    String type = null;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
    public ProbeParam(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return " Name=" + name + " Type=" + type;
    }
}
