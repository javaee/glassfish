/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.mr8;

import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;

import javax.ejb.SessionContext;
import javax.ejb.Stateful;

@DeclareRoles({"javaUsers"})

@Stateful
public class HelloStatefulEJB implements HelloStateful {
	@Resource
	private SessionContext ctx;

	@PostConstruct
	public void postConstruction() {
		System.out.println("In HelloStatefulEJB::postConstruction()");
	}

	public String hello(String name) {
		System.out.println("In HelloStatefulEJB::hello('"+name+"')");
		String principalName = "NONE";
        String principalType = "UNKNOWN";
		Principal p = ctx.getCallerPrincipal();
		if (p != null) {
	        principalName = p.getName();
	        principalType = p.getClass().getName();
		}
		String result = principalName + " is " + principalType;
		System.out.println("Caller Principal: " + result);
		return result;
	}

	public boolean inRole(String roleName) {
		System.out.println("In HelloStatefulEJB::inRole('"+roleName+"')");
		//try {
			boolean result = ctx.isCallerInRole(roleName); 
			System.out.println("In HelloStatefulEJB::inRole('"+roleName+"') - " + result);
			return result;
		//}
		//catch (Exception exc) {
		//	System.out.println("In HelloStatefulEJB - Exception: " + exc.toString());
		//	exc.printStackTrace();
		//	return false;
		//}
	}

	@RolesAllowed({"javaUsers"})
	public void methodAuthUser() {
    	System.out.println("In HelloStatefulEJB::methodAuthUser()");
    }

	@RolesAllowed({"**"})
	public void methodAnyAuthUser() {
    	System.out.println("In HelloStatefulEJB::methodAnyAuthUser()");
    }
}
