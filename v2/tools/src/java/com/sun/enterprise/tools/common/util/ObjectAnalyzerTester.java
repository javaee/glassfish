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

/*
 * ObjectAnalyzerTester.java
 *
 * Created on November 29, 2000, 6:54 PM
 */

package com.sun.enterprise.tools.common.util;

/**
 *
 * @author  bnevins
 * @version 
 */
public class ObjectAnalyzerTester extends Object 
{
	static public void main(String[] args)
	{
		System.out.println("String: \n" + ObjectAnalyzer.getMethods("java.lang.String"));//NOI18N
		System.out.println("StringXXX: \n" + ObjectAnalyzer.getMethods("java.lang.StringXXX"));//NOI18N
//		System.out.println("IASEJBModuleItem: \n" + ObjectAnalyzer.getMethods("com.iplanet.ias.tools.forte.ejb.EJBModuleItem")); //NOI18N
//		System.out.println("com.sun.forte4j.j2ee.appsrv.ejbmodule.EJBModuleItem: \n" + ObjectAnalyzer.getMethods("com.sun.forte4j.j2ee.appsrv.ejbmodule.EJBModuleItem")); //NOI18N
//		System.out.println("com.sun.forte4j.j2ee.appsrv.ejbmodule.EJBModuleItem.Module: \n" + ObjectAnalyzer.getMethods("com.sun.forte4j.j2ee.appsrv.ejbmodule.EJBModuleItem.Module")); //NOI18N
	}
}
