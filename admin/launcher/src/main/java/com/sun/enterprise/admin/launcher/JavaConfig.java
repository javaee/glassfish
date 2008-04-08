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
    
    List<String> getDebugOptions() {
        // we MUST break this up into the total number of -X commands (currently 2),
        // Since our final command line is a List<String>, we can't have 2
        // options in one String -- the JVM will ignore the second option...
        // sample "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9999"
        List<String> empty = Collections.emptyList();
        String s = map.get("debug-options");
        
        if(!GFLauncherUtils.ok(s)) {
            return empty;
        }
        String[] ss = s.split(" ");
        
        if(ss == null || ss.length <= 0) {
            return empty;
        }
        return Arrays.asList(ss);
    }

    boolean isDebugEnabled() {
        return Boolean.parseBoolean(map.get("debug-enabled"));
    }
    
    private boolean useEnvClasspath() {
        return !Boolean.parseBoolean(map.get("env-classpath-ignored"));
    }
    
    private Map<String, String> map;
}
/*
 * Sample java-config from a V2 domain.xml
 *  <java-config 
        classpath-suffix="" 
        debug-enabled="false" 
        debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009" 
        env-classpath-ignored="true" 
        java-home="${com.sun.aas.javaRoot}"  
        javac-options="-g" 
        rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g" 
        system-classpath="">
 * */