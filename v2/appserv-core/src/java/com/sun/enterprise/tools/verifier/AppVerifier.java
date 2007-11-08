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

package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.io.File;

public  class AppVerifier {

    static Logger _logger=LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    Method verify = null;
    Object verifier =null;
    
    public AppVerifier() throws Exception {
        init();
    }

    private void init() throws Exception {
        String name = "com.sun.enterprise.tools.verifier.Verifier";
        try {
            Class verifierClass = Class.forName(name);
            verify = verifierClass.getDeclaredMethod("verify",
                    new Class[] {Application.class,
                                 AbstractArchive.class,
                                 List.class,
                                 File.class});
            Constructor constructor = verifierClass.getDeclaredConstructor();
            verifier = constructor.newInstance();
        } catch (ClassNotFoundException e) {
            _logger.log(Level.SEVERE,"verifier.class.notfound",
                    new Object[] {name});
            throw e;
        }
        catch (NoSuchMethodException e) {
            _logger.log(Level.SEVERE,"verifier.method.notfound",e);
            throw e;
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"verifier.intialization.error", e);
            throw e;
        }
    }

    public void verify(Application application, 
                       AbstractArchive abstractArchive,
                       List classPath,
                       File jspOutDir) throws Exception{
        Object result = verify.invoke(verifier,
                new Object[] {application, abstractArchive, classPath, jspOutDir});
        if(((Integer)result).intValue() > 0)
            throw new Exception("Some verifier tests Failed.");
    }
}
