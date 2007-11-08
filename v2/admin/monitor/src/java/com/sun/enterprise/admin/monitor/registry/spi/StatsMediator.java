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

/* StatsIntrospector.java
 * $Id: StatsMediator.java,v 1.3 2005/12/25 03:43:36 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:36 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;

/**
 * Provides the bridge between an MBean and a Stats interface class.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public interface StatsMediator {
	
	/**
	 */
	public javax.management.MBeanAttributeInfo[] getAttributeInfos();
	
	/**
	 */
	public Object getAttribute(String name);
	
	public Object invoke(String method, Object[] params, String[] sign);
}
