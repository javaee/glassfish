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

package com.sun.enterprise.php;

import com.sun.enterprise.module.*;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;

/**
 * This import policy will set the repository for Php Related jar files
 *
 * @author Jerome Dochez
 */
//@Service
public class PhpImportPolicy implements ImportPolicy {

    public void prepare(Module module) {
        Repository phpRepo = new PhpRepository();
        try {
            phpRepo.initialize();
        } catch (IOException e) {
            // ignore for now.
        }
        module.getRegistry().addRepository(phpRepo);
        ModuleDependency dep = new ModuleDependency("quercus.jar", null);
        module.addImport(dep);
    }
}
