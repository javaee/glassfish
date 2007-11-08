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

import java.util.Locale;
import java.util.logging.Level;
import java.io.File;
import java.util.Vector;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.config.serverbeans.validation.AttrClassName;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;

/**
    Custom Test for Profiler Test which calls the Generic Validation before performing custom tests

    @author Srinivas Krishnan
    @version 2.0
*/

public class ProfilerTest extends GenericValidator {
    
    public ProfilerTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        
        if(cce.getChoice().equals(StaticTest.VALIDATE)) {
            Profiler profiler = (Profiler) cce.getObject();
            validateAttribute(ServerTags.CLASSPATH, profiler.getClasspath(), result);
            JvmOptionsTest.validateJvmOptions(profiler.getJvmOptions(), result);
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
        return result;
    }
    
    public void validateAttribute(String name, String value, Result result) {
        
        if(value == null || value.equals(""))
            return;
        if(name.equals(ServerTags.CLASSPATH)) {
            if(!StaticTest.isClassPathValid(value)) {
                result.failed(smh.getLocalString(getClass().getName() + ".invalidProfilerClasspath",
                   "Attribute:(classpath={0}), Invalid path in classpath", new Object[]{value}));
            }
        }
    }
}
