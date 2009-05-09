/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import java.io.*;
import java.util.*;

/**
 * This class wraps the profiler element in java-config
 * Note that the V2 dtd says that there can be generic property elements in the
 * profiler element.  I don't know why anyone would use them -- but if they do I 
 * turn it into a "-D" System Property
 * @author bnevins
 */
public class Profiler {
    Map<String, String> config;
    List<String> jvmOptions;
    
    Profiler(Map<String, String> config, List<String> jvmOptions, Map<String, String> sysProps) {
        this.config = config;
        enabled = Boolean.parseBoolean(this.config.get("enabled"));
        this. jvmOptions = jvmOptions;
        jvmOptions.addAll(getPropertiesAsJvmOptions(sysProps));
    }
    
    List<String> getJvmOptions() {
        if(!enabled) {
            return Collections.emptyList();
        }
        return jvmOptions;
    }
    
    Map<String,String> getConfig() {
        if(!enabled) {
            return Collections.emptyMap();
        }
        return config;
    }
            
    List<File> getClasspath() {
        if(!enabled) {
            return Collections.emptyList();
        }
        
        String cp = config.get("classpath");
        
        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return Collections.emptyList();
        }
    }

    List<File> getNativePath() {
        if(!enabled) {
            return Collections.emptyList();
        }
            
        String cp = config.get("native-library-path");
        
        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return Collections.emptyList();
        }
    }

    boolean isEnabled() {
        return enabled;
    }

   private List<String> getPropertiesAsJvmOptions(Map<String,String> props) {
        List<String> list = new ArrayList<String>();
        Set<String> keys = props.keySet();
        
        for(String name : keys) {
            String value = props.get(name);
            
            if(value != null) {
                list.add("-D" + name + "=" + value);
            }
            else {
                list.add("-D" + name);
            }
        }
        return list;
    }
    private boolean enabled;
}
