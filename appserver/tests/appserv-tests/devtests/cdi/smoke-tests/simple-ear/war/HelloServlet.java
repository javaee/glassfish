/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import javax.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.annotation.Resource;
import javax.naming.*;

@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)
@EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
public class HelloServlet extends HttpServlet {

    @Resource(mappedName = "java:module/foobarmanagedbean")
    private FooBarManagedBean fbmb;
    @Resource
    private FooBarManagedBean fbmb2;

    @EJB(name = "java:module/env/m2")
    private Hello m1;
    @EJB(name = "java:app/a1")
    private HelloRemote a1;
    @EJB(name = "java:app/env/a2")
    private HelloRemote a2;
    @Resource(name = "java:app/env/myString")
    protected String myString;
    private Hello singleton1;
    private Hello singleton2;
    private Hello singleton3;
    private Hello singleton4;
    private Hello singleton5;
    private HelloRemote stateless1;
    private HelloRemote stateless2;

    @Resource
    private Foo2ManagedBean foo;
    @Resource(name = "foo2ref", mappedName = "java:module/somemanagedbean")
    private Foo2ManagedBean foo2;
    @Resource(name = "foo3ref", mappedName = "java:app/cdi-full-ear-ejb/somemanagedbean")
    private Foo foo3;
    private Foo2ManagedBean foo4;
    private Foo2ManagedBean foo5;
    private Foo foo6;
    private Foo2ManagedBean foo7;
    private Foo foo8;

    private String msg = "";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
        System.out.println("myString = '" + myString + "'");
        if ((myString == null) || !(myString.equals("myString"))) {
            msg += "@Resource lookup of myString failed";
            throw new RuntimeException("Invalid value " + myString + " for myString");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");
        checkForNull(foo, "@Resource lookup of ManagedBean failed");
        checkForNull(foo2, "@Resource lookup of module-level ManagedBean failed");
        checkForNull(foo3, "@Resource lookup of app-level ManagedBean through a super-interface failed");

        PrintWriter out = resp.getWriter();
        try {
            InitialContext ic = new InitialContext();
            String appName = (String) ic.lookup("java:app/AppName");
            String moduleName = (String) ic.lookup("java:module/ModuleName");
            checkForNull(appName, "AppName lookup returned null");
            checkForNull(moduleName, "ModuleName lookup returned null");

            // lookup via intermediate context
            Context appCtx = (Context) ic.lookup("java:app");
            Context appCtxEnv = (Context) appCtx.lookup("env");
            stateless2 = (HelloRemote) appCtxEnv.lookup("AS2");
            checkForNull(stateless2, "lookup of stateless EJB via java:app in intermediate context failed");
            NamingEnumeration<Binding> bindings = appCtxEnv.listBindings("");
            System.out.println("java:app/env/ bindings ");
            while (bindings.hasMore()) {
                System.out.println("binding : " + bindings.next().getName());
            }


            foo4 = (Foo2ManagedBean) ic.lookup("java:module/somemanagedbean");
            checkForNull(foo4, "programmatic lookup of module-level managedbean failed");
            foo5 = (Foo2ManagedBean) ic.lookup("java:app/" + moduleName
                    + "/somemanagedbean");
            checkForNull(foo5, "programmatic lookup of module-level managedbean through application context failed");
            foo6 = (Foo) ic.lookup("java:app/cdi-full-ear-ejb/somemanagedbean");
            checkForNull(foo6, "programmatic lookup of application-level managedbean failed");
            foo7 = (Foo2ManagedBean) ic.lookup("java:comp/env/foo2ref");
            checkForNull(foo7, "programmatic lookup of module-level managedbean through a component reference failed");
            foo8 = (Foo) ic.lookup("java:comp/env/foo3ref");
            checkForNull(foo8, "programmatic lookup of module-level managedbean through a component reference failed");

            singleton1 = (Hello) ic.lookup("java:module/m1");
            checkForNull(singleton1, "programmatic lookup of module-level singleton EJB failed");

            // standard java:app name for ejb
            singleton2 = (Hello) ic.lookup("java:app/cdi-full-ear-ejb/HelloSingleton");
            checkForNull(singleton2, "programmatic lookup of module-level singleton EJB through app reference failed");

            singleton3 = (Hello) ic.lookup("java:global/" + appName + "/cdi-full-ear-ejb/HelloSingleton");
            checkForNull(singleton3, "programmatic lookup of module-level singleton EJB through global reference failed");

            // lookup some java:app defined by ejb-jar
            singleton4 = (Hello) ic.lookup("java:app/env/AS1");
            checkForNull(singleton4, "programmatic lookup of module-level singleton EJB through EJB name failed");
            // global dependency
            singleton5 = (Hello) ic.lookup("java:global/GS1");
            checkForNull(singleton5, "programmatic lookup of singleton EJB through global name failed");

            stateless1 = (HelloRemote) ic.lookup("java:app/env/AS2");
            checkForNull(stateless1, "programmatic lookup of app-level stateless EJB failed");

            System.out.println("My AppName = "
                    + ic.lookup("java:app/AppName"));

            System.out.println("My ModuleName = "
                    + ic.lookup("java:module/ModuleName"));



            try {
                org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:module/MORB1");
                msg += " Not getting naming exception when we try to see ejb-jar module-level dependency";
                throw new RuntimeException("Should have gotten naming exception");
            } catch (NamingException ne) {
                System.out.println("Successfully was *not* able to see ejb-jar module-level dependency");
            }

        } catch (Exception e) {
            msg += "Exception occurred during test Exception: " + e.getMessage();
            e.printStackTrace();
        }

        foo.foo();
        foo2.foo();
        foo3.foo();

        foo4.foo();
        foo5.foo();
        foo6.foo();
        foo7.foo();
        foo8.foo();

        m1.hello();
        a1.hello();
        a2.hello();
        singleton1.hello();
        singleton2.hello();
        singleton3.hello();
        singleton4.hello();
        singleton5.hello();

        stateless1.hello();
        stateless2.hello();

        out.println(msg);

    }

    protected void checkForNull(Object o, String errorMessage){
        if (o == null) msg += " " + errorMessage;
    }
}
