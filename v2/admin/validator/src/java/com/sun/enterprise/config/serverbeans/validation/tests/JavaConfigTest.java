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

package com.sun.enterprise.config.serverbeans.validation.tests;

import java.util.*;
import java.util.logging.Level;
import java.io.File;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.validation.AttrClassName;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;

/**
    Custom Test for Java Config Test which calls the Generic Validation before performing custom tests

    @author Srinivas Krishnan
    @version 2.0
*/

public class JavaConfigTest extends GenericValidator {
    
    public JavaConfigTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        
        if(cce.getChoice().equals(StaticTest.VALIDATE)) {
            StaticTest.setJavaHomeCheck(true);
            JavaConfig javaConfig = (JavaConfig) cce.getObject();
            if(javaConfig.getJavaHome().indexOf("${")<0)
               validateAttribute(ServerTags.JAVA_HOME, javaConfig.getJavaHome(), result);
            validateAttribute(ServerTags.DEBUG_OPTIONS, javaConfig.getDebugOptions(), result);
            validateAttribute(ServerTags.RMIC_OPTIONS, javaConfig.getRmicOptions(), result);
            validateAttribute(ServerTags.JAVAC_OPTIONS, javaConfig.getJavacOptions(), result);
            
            validateAttribute(ServerTags.CLASSPATH_PREFIX, javaConfig.getClasspathPrefix(), result);
            validateAttribute(ServerTags.CLASSPATH_SUFFIX, javaConfig.getClasspathSuffix(), result);
            validateAttribute(ServerTags.NATIVE_LIBRARY_PATH_PREFIX, javaConfig.getNativeLibraryPathPrefix(), result);
            validateAttribute(ServerTags.NATIVE_LIBRARY_PATH_SUFFIX, javaConfig.getNativeLibraryPathSuffix(), result);
            validateAttribute(ServerTags.BYTECODE_PREPROCESSORS, javaConfig.getBytecodePreprocessors(), result);
            JvmOptionsTest.validateJvmOptions(javaConfig.getJvmOptions(), result);
        }
        
        if(cce.getChoice().equals(StaticTest.UPDATE))  {
            validateAttribute(cce.getName(), (String) cce.getObject(), result);
        }
        else if(cce.getChoice().equals(StaticTest.SET))  {
            final String name = cce.getName();
           /*
            * JvmOptions is an element in domain xml. Hence for JvmOptions we
            * need to use camelized name even thouth it is treated as an 
            * attribute.
            */
            if (name.equals("JvmOptions")) {
                JvmOptionsTest.validateJvmOptions((String[])cce.getObject(), result);
            }
        }
        else {
            //Do nothing?
        }
        return result;
    }
    
    public void validateAttribute(String name, String value, Result result) {
        
        if(value == null || value.equals(""))
            return;
        if(name.equals(ServerTags.DEBUG_OPTIONS)) {
            if(!StaticTest.isOptionsValid(value))
                result.failed(smh.getLocalString(getClass().getName() + ".invalidDebugOption",
                    "{0} : Invalid Java Debug options should start with -", new Object[]{value}));
                   
            validateRunJDWP(value, result);        
        }
        if(name.equals(ServerTags.RMIC_OPTIONS)) {
            if(!StaticTest.isOptionsValid(value))
                result.failed(smh.getLocalString(getClass().getName() + ".invalidRmicOption",
                    "{0} : Invalid RMIC options should start with -", new Object[]{value}));
        }
        if(name.equals(ServerTags.JAVAC_OPTIONS)) {
            if(!StaticTest.isOptionsValid(value))
                result.failed(smh.getLocalString(getClass().getName() + ".invalidJavacOptions",
                   "{0} : Invalid javac options should start with -", new Object[]{value}));
        }
        
        if(name.equals(ServerTags.CLASSPATH_PREFIX) || name.equals(ServerTags.CLASSPATH_SUFFIX) || 
                name.equals(ServerTags.NATIVE_LIBRARY_PATH_PREFIX) || 
                         name.equals(ServerTags.NATIVE_LIBRARY_PATH_SUFFIX)) {
            if(!StaticTest.isClassPathValid(value)) 
                result.failed(smh.getLocalString(getClass().getName() + ".invalidClasspath",
                   "{0} Classpath contains invalid path : Check the path", new Object[]{name}));
        }
        if(name.equals(ServerTags.JAVA_HOME)) {
            if(!StaticTest.isJavaHomeValid(value))
                result.failed(smh.getLocalString(getClass().getName() + ".invalidJavaHome",
                   "Warning : (java-home={0}), JDK does not exists in java home", new Object[]{value}));
        }
        if(name.equals(ServerTags.BYTECODE_PREPROCESSORS)) 
        {
            StringTokenizer tokens = new StringTokenizer(value,".");
            while(tokens.hasMoreTokens()) 
            {
               String token = tokens.nextToken().trim();
               if(!AttrClassName.isValidClassName(token)) 
               {
                    result.failed(smh.getLocalString(getClass().getName() + ".invalidClassName",
                   "Attribute (bytecode-preprocessors={0}), {1} Invalid Class Name", new Object[]{value,token}));
               }
            }
        }
    }
    
    public void validateRunJDWP(String value, Result result) {
        
           // code to validate the debug option -Xrunjdwp:transport=dt_socket,address=<value>,server=<value>,suspend=<value>
            String runjdwp = value.substring(value.indexOf("jdwp:")+5);
            int index=0;
            try {
                if(runjdwp != null)
                    index = runjdwp.indexOf("-X");
            } catch(Exception e) {
            }
            String debugStr=null;
            if(index > 0)
                debugStr = runjdwp.substring(0,index);
            else
                debugStr = runjdwp;
            if(debugStr != null) {
                String[] tokens = debugStr.split(",");
                for(int i=0;i<tokens.length;i++) {
                    if(tokens[i].indexOf("=") < 0) {
                            result.failed(smh.getLocalString(getClass().getName() + ".invalidJDWPDebugOption",
                                  "{0} : Invalid -Xrunjdwp option, please check syntax", new Object[]{debugStr}));
                            break;
                    }
                }
            }
    }
}
