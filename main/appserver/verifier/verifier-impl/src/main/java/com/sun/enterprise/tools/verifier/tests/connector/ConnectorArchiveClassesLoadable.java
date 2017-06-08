/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.tools.verifier.tests.connector;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.util.ArchiveClassesLoadableHelper;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.util.Enumeration;

/**
 * A j2ee archive should be self sufficient and should not depend on any classes to be 
 * available at runtime.
 * The test checks whether all the classes found in the Connector archive are loadable and the
 * classes that are referenced inside their code are also loadable within the jar. 
 *  
 * @author Sanjeeb Sahoo
 */
public class ConnectorArchiveClassesLoadable extends ConnectorTest implements ConnectorCheck {

    public Result check(ConnectorDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String archiveUri = getAbstractArchiveUri(descriptor);
        
        boolean allPassed = true;
        Enumeration entries= null;
        ClosureCompiler closureCompiler=getVerifierContext().getClosureCompiler();;
        try {
                String uri = getAbstractArchiveUri(descriptor);
                FileArchive arch = new FileArchive();
                arch.open(uri);
                entries = arch.entries();
                arch.close();
        } catch(Exception e) {
            e.printStackTrace();
            result.failed(smh.getLocalString(getClass().getName() + ".exception",
                                             "Error: [ {0} ] exception while loading the archive [ {1} ].",
                                              new Object[] {e, descriptor.getName()}));
            return result;
        }
        Object entry;
        while (entries.hasMoreElements()) {
            String name=null;
            entry  = entries.nextElement();
               name = (String)entry;
            if (name.endsWith(".class")) {
                String classEntryName = name.substring(0, name.length()-".class".length()).replace('/','.');
                boolean status=closureCompiler.buildClosure(classEntryName);
                allPassed=status && allPassed;
            }
        }
        if (allPassed) {
            result.setStatus(Result.PASSED);
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                (getClass().getName() + ".passed",
                "All the classes are loadable within [ {0} ] without any linkage error.",
                new Object[] {archiveUri}));
//            result.addGoodDetails(closureCompiler.toString());
        } else {
            result.setStatus(Result.FAILED);
            addErrorDetails(result, compName);
            result.addErrorDetails(ArchiveClassesLoadableHelper.
                    getFailedResult(closureCompiler));
            result.addErrorDetails(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.loadableError",
                            "Please either bundle the above mentioned classes in the application " +
                            "or use optional packaging support for them."));
        }
        return result;
    }
}
