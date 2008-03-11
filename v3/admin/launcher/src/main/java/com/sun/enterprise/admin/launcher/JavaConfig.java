/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import java.io.*;
import java.util.*;

/**
 *
 * @author bnevins
 */
class JavaConfig {

    JavaConfig(Map<String, String> map) {
        this.map = map;
    }

    Map<String, String> getMap() {
        return map;
    }

    String getJavaHome() {
        return map.get("java-home");
    }
    List<File> getEnvClasspath() {
        
        if(useEnvClasspath()) {
            String s = System.getenv("CLASSPATH");
            return GFLauncherUtils.stringToFiles(s);
        }
        else {
            return new ArrayList<File>();
        }

    }

    List<File> getPrefixClasspath() {
        String cp = map.get("classpath-prefix");
        
        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }   
        else {
            return new ArrayList<File>();
        }
    }

    List<File> getSuffixClasspath() {
        String cp = map.get("classpath-suffix");
        
        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return new ArrayList<File>();
        }
    }

    List<File> getSystemClasspath() {
        String cp = map.get("system-classpath");
        
        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return new ArrayList<File>();
        }
    }
    private boolean useEnvClasspath() {
        return !Boolean.parseBoolean(map.get("env-classpath-ignored"));
    }
    private Map<String, String> map;
}
