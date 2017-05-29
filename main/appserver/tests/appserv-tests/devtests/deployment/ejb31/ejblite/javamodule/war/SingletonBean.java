/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.EJBException;
import javax.ejb.EJB;
import javax.ejb.LocalBean;

import javax.ejb.SessionContext;

@Singleton
@LocalBean
public class SingletonBean implements java.util.Observer {
  

    @EJB private  SingletonBean me;

    @EJB private  StatefulBean sf;

    @Resource private SessionContext sessionCtx;

    private SingletonBean sb2;
    private SingletonBean sb3;
    private SingletonBean sb4;
    private SingletonBean sb5;
    private StatelessBean slsb;
    private StatelessBean slsb2;
    private StatelessBean slsb3;
    private StatelessBean slsb4;
    private StatelessBean slsb5;
	
    public void update(java.util.Observable a,java.lang.Object b)  {}

    @PostConstruct
    private void init() {

	System.out.println("In SingletonBean:init() me = " + me);
	
	if( sessionCtx.getContextData() == null ) {
	    throw new EJBException("null context data");
	}

	try {
	    InitialContext ic = new InitialContext();

	    // Lookup simple form of portable JNDI name 
	    StatelessBean stateless = (StatelessBean) 
		ic.lookup("java:module/StatelessBean");

	    stateless.hello();

	    // Lookup fully-qualified form of portable JNDI name
	    StatelessBean stateless2 = (StatelessBean) 
		ic.lookup("java:module/StatelessBean!com.acme.StatelessBean");

	    stateless2.hello();

	    sb2 = (SingletonBean) ic.lookup("java:module/SingletonBean");
	    sb3 = (SingletonBean) ic.lookup("java:module/SingletonBean!com.acme.SingletonBean");

	    sb4 = (SingletonBean) ic.lookup("java:module/ES1");
	    sb5 = (SingletonBean) ic.lookup("java:module/env/ES2");

	    slsb = (StatelessBean) ic.lookup("java:module/StatelessBean");
	    slsb2 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean");
	    slsb3 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean!com.acme.StatelessBean");

	    slsb4 = (StatelessBean) ic.lookup("java:app/EL1");
	    slsb5 = (StatelessBean) ic.lookup("java:app/env/EL2");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:module/ModuleName"));

	} catch(NamingException ne) {
	    throw new EJBException(ne);
	}
    }

    public void hello() {
	System.out.println("In SingletonBean:hello()");
	if( sessionCtx.getContextData() == null ) {
	    throw new EJBException("null context data");
	}

    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SingletonBean:destroy()");
	if( sessionCtx.getContextData() == null ) {
	    throw new EJBException("null context data");
	}

    }


}
