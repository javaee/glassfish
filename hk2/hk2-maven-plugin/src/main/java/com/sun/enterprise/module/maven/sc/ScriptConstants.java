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
