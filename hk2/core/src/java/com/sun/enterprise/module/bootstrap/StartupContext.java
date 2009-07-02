/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
 * StartupContext.java
 *
 * Created on October 26, 2006, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.bootstrap;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * This class contains important information about the startup process
 * @author Jerome Dochez
 */

@Service
@Scoped(Singleton.class)
public class StartupContext {
/*
 * January 27, 2009 -- bnevins -- important note.
 * Surprisingly, root is a directory *underneath* the install root!
 * startup code in v3 proper that uses this object will assume that the install-root
 * is getRootDirectory().getParentFile() !!!!!
 * I.e. in the normal v3 case root will be: /glassfish/modules
 * This behavior is weird and caused issue #54 in Embedded
 */
    final File root;
    final Properties args;
    final long timeZero;
    public final static String TIME_ZERO_NAME = "__time_zero";  //NO I18N
    public static final String ARGS_PROP       = "hk2.startup.context.args";
    public static final String ORIGINAL_CP     = "-startup-classpath";
    public static final String ORIGINAL_CN     = "-startup-classname";
    public static final String ORIGINAL_ARGS   = "-startup-args";
    public static final String ARG_SEP         = ",,,";
    public static final String ROOT_PROP = "hk2.startup.context.root";
    public final static String STARTUP_MODULE_NAME = "hk2.startup.context.mainModule";

    public StartupContext() {
        this.root = new File(System.getProperty("user.dir"));
        this.timeZero = System.currentTimeMillis();
        args = new Properties();
    }

    /** Creates a new instance of StartupContext */
    public StartupContext(File root, String[] args) {
        this.root = absolutize(root);
        this.args = ArgumentManager.argsToMap(args);
        this.timeZero = System.currentTimeMillis();
    }

    public StartupContext(File root, Properties args) {
        this.root = root;
        this.args = args;
        if (args.containsKey(TIME_ZERO_NAME)) {
            this.timeZero = Long.decode(args.getProperty(TIME_ZERO_NAME)).longValue();
        } else {
            this.timeZero = System.currentTimeMillis();            
        }
    }

    /**
     * Gets the "root" directory where modules are installed
     *
     * <p>
     * This path is always absolutized.
     *
     */
    public File getRootDirectory() {
        return root;
    }
        
    public Properties getArguments() {
        return args;
    }

    public String[] getOriginalArguments() {
        // See how ASMain packages the arguments
        String s = args.getProperty(ORIGINAL_ARGS);
        if (s == null) return new String[0];
        StringTokenizer args = new StringTokenizer(s, ARG_SEP, false);
        List<String> result = new ArrayList<String>();
        while (args.hasMoreTokens()) {
            result.add(args.nextToken());
        }
        return result.toArray(new String[0]);
    }

    public String getStartupModuleName() {
        return String.class.cast(args.get(STARTUP_MODULE_NAME));
    }
    /**
     * Returns the time at which this StartupContext instance was created.
     * This is roughly the time at which the hk2 program started.
     *
     * @return the instanciation time
     */
    public long getCreationTime() {
        return timeZero;
    }
    
    private File absolutize(File f)
    {
        try 
        { 
            return f.getCanonicalFile(); 
        }
        catch(Exception e)
        {
            return f.getAbsoluteFile();
        }
    }
}
