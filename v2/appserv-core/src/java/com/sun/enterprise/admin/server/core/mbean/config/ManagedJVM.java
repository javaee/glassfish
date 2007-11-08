/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.server.core.mbean.config;

/**
    Represents the manageable Java Virtual Machine. Various things can be
    queried for in this JVM. An MBean correponding to this JVM will be
    registered in the MBeanServer. The JVM will be always associated with
    a Server Instance or the Admin Server. JVMPI will be used to get
    certain management information from a running JVM.
    <p>
    For iAS SE it is thought that there will be one JVM per Server Instance.
    But it should be possible to have multiple JVMs.
    <p>
    ObjectName of this MBean is:
        ias:type=jvm, ServerInstance=<serverInstance>
*/

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;

//Admin imports
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.ObjectNames;

public class ManagedJVM extends  ConfigMBeanBase implements ConfigAttributeName.JavaConfig
{
 private static final String[][] MAPLIST  =  {
   { kJavaHome,                ATTRIBUTE + ServerTags.JAVA_HOME  },
   { kDebugEnabled,            ATTRIBUTE + ServerTags.DEBUG_ENABLED  },
   { kDebugOptions,            ATTRIBUTE + ServerTags.DEBUG_OPTIONS  },
   { kRmicOptions,             ATTRIBUTE + ServerTags.RMIC_OPTIONS},
   { kJavacOptions,            ATTRIBUTE + ServerTags.JAVAC_OPTIONS},
   { kClasspathPrefix,         ATTRIBUTE + ServerTags.CLASSPATH_PREFIX},
    { kServerClasspath,         ATTRIBUTE + ServerTags.SERVER_CLASSPATH},
   { kClasspathSuffix,         ATTRIBUTE + ServerTags.CLASSPATH_SUFFIX},
   { kNativeLibraryPathPrefix, ATTRIBUTE + ServerTags.NATIVE_LIBRARY_PATH_PREFIX},
   { kNativeLibraryPathSuffix, ATTRIBUTE + ServerTags.NATIVE_LIBRARY_PATH_SUFFIX},
   { kEnvClasspathIgnored,     ATTRIBUTE + ServerTags.ENV_CLASSPATH_IGNORED},
 };

 private  static final String[]   ATTRIBUTES  = {
    kJavaHome                 + ",   String,  RW",
    kDebugEnabled             + ",   boolean, RW",
    kDebugOptions             + ",   String,  RW",
    kRmicOptions              + ",   String,  RW",
    kJavacOptions             + ",   String,  RW",
    kClasspathPrefix          + ",   String,  RW",   
    kServerClasspath          + ",   String,  RW",   
    kClasspathSuffix          + ",   String,  RW",
    kNativeLibraryPathPrefix  + ",   String,  RW",
    kNativeLibraryPathSuffix  + ",   String,  RW",
    kEnvClasspathIgnored      + ",   boolean, RW",
 };

 private static final String[]   OPERATIONS  =
 {
    "createProfiler(String name, String classpath, String nativeLibraryPath, Boolean enabled), ACTION",
    "deleteProfiler(), ACTION",
    "isProfilerExist(), INFO",
    "getProfiler(), INFO", 
    "getJvmOptions(), INFO",
    "setJvmOptions(String[] options), ACTION",
 };

 private final String         JVM_NODE_PATH = "/server/java-config";

    /**
        Default constructor sets MBean description tables
    */
    public ManagedJVM() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    /**
        Constructs Config MBean for JAVA-CONFIG.
        @param instanceName The server instance name.
    */
    public ManagedJVM(String instanceName) throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kJvmType, new String[]{instanceName});
    }

    /**
    This operation creates Profiler according to attributes and adds(links) it to current HTTP Service;
    If attribute is 'null' then default value will be set.
     */
    public void createProfiler(String name, String classpath, String nativeLibraryPath, Boolean enabled) throws ConfigException
    {
        Profiler element = new Profiler();
        if(name!=null)
            element.setName(name);
        if(classpath!=null)
            element.setClasspath(classpath);
        if(nativeLibraryPath!=null)
            element.setNativeLibraryPath(nativeLibraryPath);
        if(enabled!=null)
            element.setEnabled(enabled.booleanValue());
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        javaConfig.setProfiler(element);
        
        getConfigContext().flush();
    }
    
    /**
    This operation deletes Profiler according to id if it connected to current HTTP Service.
    @throws ConfigException in case of failure.
     */
    public void deleteProfiler() throws ConfigException
    {
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        javaConfig.setProfiler(null);
        getConfigContext().flush();
    }

    /**
    This operation returns true   if  Profiler connected to current HTTP Service.
    @throws ConfigException in case of failure.
     */
    public boolean isProfilerExist() throws ConfigException
    {
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        return  (javaConfig.getProfiler()!=null);
    }

    /**
     * Returns the name of the profiler this JVM is configured with.
     * Returns null if no profiler is registered.
     */
    public String getProfiler() throws ConfigException
    {
        String profilerName = null;
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        Profiler profiler = javaConfig.getProfiler();
        if (profiler != null)
        {
            profilerName = profiler.getName();
        }
        return profilerName;
    }

    /**
    This operation returns list of JvmOptions  connected to this class.
     */
    public String[] getJvmOptions() throws ConfigException
    {
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        return javaConfig.getJvmOptions();
    }

    /**
    This operation returns list of JvmOptions  connected to this class.
     */
    public void setJvmOptions(String[] options) throws ConfigException
    {
        JavaConfig  javaConfig  = (JavaConfig)getBaseConfigBean();
        javaConfig.setJvmOptions(options);
        getConfigContext().flush();
    }
}
