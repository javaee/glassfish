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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import com.sun.enterprise.module.ImportPolicy;
import com.sun.enterprise.module.Module;
import org.jvnet.hk2.annotations.Service;

/**
 * Class responsible for adding ant.jar to the import dependencies of 
 * webtier.jar if the version of the Java runtime is less than 1.6, because
 * the JSP container relies on ant for javac compilations in this case.
 *
 * @author jluehe
 */
@Service
public class WebImportPolicy implements ImportPolicy {

    private static final String JAVA_VERSION = "java.specification.version";

    public void prepare(Module myModule) {

        if (System.getProperty(JAVA_VERSION).compareTo("1.6")>=0) {
            // On Java 6, JSPs are compiled in memory (JSR 199 style), no ant
            // required
            return;
        }

        Module antModule = myModule.getRegistry().makeModuleFor("org.glassfish.external:ant", null);
        if(antModule==null)
            throw new LinkageError("Unable to locate ant module");
        myModule.addImport(antModule);
    }
}
