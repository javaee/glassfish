/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.module.bootstrap;

import java.util.Properties;

/**
 * This class contains important information about the startup process.
 * This is one of the initial objects to be populated in the {@link org.jvnet.hk2.component.Habitat},
 * so {@link Populator}s can depend on this object.
 *
 * Do not add domain specific knowledge here. Since this takes a properties object in the constructor,
 * such knowledge can be maintained outside this object.
 *
 * @author Jerome Dochez, Sanjeeb Sahoo
 */

public class StartupContext {
    final Properties args;
    final long timeZero;
    public final static String TIME_ZERO_NAME = "__time_zero";  //NO I18N
    public final static String STARTUP_MODULE_NAME = "hk2.startup.context.mainModule";
    public final static String STARTUP_MODULESTARTUP_NAME = "hk2.startup.context.moduleStartup";

    public StartupContext() {
        this(new Properties());
    }

    public StartupContext(Properties args) {
        this.args = (Properties)args.clone();
        if (this.args.containsKey(TIME_ZERO_NAME)) {
            this.timeZero = Long.decode(this.args.getProperty(TIME_ZERO_NAME));
        } else {
            this.timeZero = System.currentTimeMillis();
        }
    }

    /**
     * Return the properties that constitues this context. Except the well known properties like
     * {@link #TIME_ZERO_NAME}, {@link #STARTUP_MODULE_NAME}, {@link #STARTUP_MODULESTARTUP_NAME},
     * this class does not know about any other properties. It is up to the user set them and get them.
     *
     */
    public Properties getArguments() {
        return args;
    }

    public String getStartupModuleName() {
        return String.class.cast(args.get(STARTUP_MODULE_NAME));
    }

    public String getPlatformMainServiceName() {
        String v = String.class.cast(args.get(STARTUP_MODULESTARTUP_NAME));
//        // todo : dochez, horrible hack to work around ArgumentManager clumsyness
//        if (v==null) {
//            return String.class.cast(args.get("-"+ STARTUP_MODULESTARTUP_NAME));
//        }
        return v;
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
    
}
