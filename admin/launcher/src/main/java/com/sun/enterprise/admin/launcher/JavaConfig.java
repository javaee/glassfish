/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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