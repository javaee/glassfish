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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.module.maven.sc;

/** A class with all the constant literals used by script-creation facility.
 * @author Kedar Mhaswade (km@dev.java.net)
 */
public final class ScriptConstants {
    
    public static final String COMMENT0           = "#";
    /* Sections */
    public static final String SYSTEM_SECTION     = "system";
    public static final String COPYRIGHT_SECTION  = "copyright";
    public static final String HEADER_SECTION     = "header";
    public static final String SOURCE_SECTION     = "source";
    public static final String ENVVARS_SECTION    = "env.vars";
    public static final String SHELLVARS_SECTION  = "shell.vars";
    public static final String JVM_SECTION        = "jvm";
    public static final String CLASSPATH_SECTION  = "classpath";
    public static final String SYS_PROPS_SECTION  = "java.system.properties";
    
    /* Sections */

    /* Property Names */
    public static final String MAIN_CLASS_PROP = "main.class";
    public static final String MAIN_JAR_PROP   = "jar";
    public static final String JVM_OPTS_PROP   = "jvm.options";    
    /* Property Names  */

    /* Values */
    public static final String WINDOWS_SCRIPT_HOME_VALUE = "%cd%";
    public static final String UNIX_SCRIPT_HOME_VALUE    = "`dirname $0`";
    public static final String DEFAULT_SHELL_PATH_VALUE  = "#!/bin/sh";
    /* Values */
    /* Others */
    public static final String JAVA_EXE_ENVVAR          = "JAVA";    
    public static final String SCRIPT_HOME_TOKEN        = "script.home";
    public static final String LOCAL_VAR_REFER_PATTERN  = "#\\{[a-zA-Z\\d\\._]+\\}";
    public static final String EXT_PROP_REFER_PATTERN   = "\\$\\{[a-zA-Z\\d\\._]+\\}";
    public static final String SECTION_START            = "[";
    public static final String SECTION_END              = "]";
    public static final String UNIX                     = "unix";
    public static final String WINDOWS                  = "windows";
    public static final String OPERATING_SYSTEM         = "os";    
    public static final String SRC                      = "SourceFile";
    public static final String DEST                     = "DestinationFile";
    public static final String WIN_SCRIPT_EXTENSION     = ".bat";    
    
    /* Others */    
}
