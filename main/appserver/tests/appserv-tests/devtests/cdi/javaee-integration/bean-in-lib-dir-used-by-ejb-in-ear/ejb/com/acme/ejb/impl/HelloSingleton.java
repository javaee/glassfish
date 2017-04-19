package com.acme.ejb.impl;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import com.acme.ejb.api.Hello;
import com.acme.util.TestDatabase;
import com.acme.util.TestDependentBeanInLib;
import com.acme.util.TestManagedBean;
import com.acme.util.TestSessionScopedBeanInLib;
import com.acme.util.UtilInLibDir;

@Singleton
@Startup
@DependsOn("Singleton4")
public class HelloSingleton implements Hello {

    @Resource
    SessionContext sessionCtx;

    @PersistenceUnit(unitName = "pu1")
    @TestDatabase
    private EntityManagerFactory emf;
    
    @Inject
    TestManagedBean tmb;
    
    @Inject
    TestDependentBeanInLib tdbil;
    
    @Inject
    TestSessionScopedBeanInLib tssil;
    

    @PostConstruct
    private void init() {
        System.out.println("HelloSingleton::init()");

        String appName;
        String moduleName;
        appName = (String) sessionCtx.lookup("java:app/AppName");
        moduleName = (String) sessionCtx.lookup("java:module/ModuleName");
        System.out.println("AppName = " + appName);
        System.out.println("ModuleName = " + moduleName);
    }

    public String hello() {
        System.out.println("HelloSingleton::hello()");
        String res = testEMF();
        if (!res.equals("")) return res;
        res = testInjectionOfBeansInLibDir();
        if (!res.equals("")) return res;
        UtilInLibDir uilb = new UtilInLibDir();
        if (!(uilb.add(1, 2) == 3)) {
            return "Can't use utility class in library directory";
        }
        return ALL_OK_STRING;
    }

    private String testInjectionOfBeansInLibDir() {
        if (tmb == null) return "Injection of Managed Bean in lib into an EJB in that ear failed";
        if (tdbil == null) return "Injection of Dependent Bean in lib into an EJB in that ear failed";
        if (tssil == null) return "Injection of SessionScoped Bean in lib into an EJB in that ear failed";
        return "";
    }

    private String testEMF() {
        if (emf == null) return "EMF injection failed, is null in Singleton EJB";
        if (emf.createEntityManager() == null) return "Usage of EMF failed in Singleton EJB";
        return "";
    }

    @PreDestroy
    private void destroy() {
        System.out.println("HelloSingleton::destroy()");
    }

}
