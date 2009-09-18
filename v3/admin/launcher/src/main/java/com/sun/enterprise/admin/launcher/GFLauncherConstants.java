/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

/**
 *
 * @author bnevins
 */
class GFLauncherConstants {
    static final String JAVA_NATIVE_SYSPROP_NAME    = "java.library.path";
    static final String NEWLINE                     = System.getProperty("line.separator");
    static final String LIBDIR                      = "lib";
    static final String PS                          = java.io.File.pathSeparator;
    static final String SPARC                       = "sparc";
    static final String SPARCV9                     = "sparcv9";
    static final String X86                         = "x86";
    static final String AMD64                       = "amd64";
    static final String NATIVE_LIB_PREFIX           = "native-library-path-prefix";
    static final String NATIVE_LIB_SUFFIX           = "native-library-path-suffix";
    static final String BTRACE_PATH                 = "lib/monitor/btrace-agent.jar";
    static final String DEFAULT_LOGFILE             = "logs/server.log";
}
