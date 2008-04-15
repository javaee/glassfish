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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/base/AllDottedNames.java,v 1.2 2007/05/05 05:30:30 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:30 $
 */


package com.sun.appserv.management.base;

/**
	Marker interface for exposing all <i>asadmin</i> CLI dotted names 
	as JMX <i>Attributes</i>.
	<p>
	The set of dotted names is variable and dynamic.  For that reason, no 
	specific dotted names are defined in this interface. A client wishing
	to determine which dotted names are available can determine them by
	obtaining MBeanInfo or by calling {@link Util#getExtra} and then calling
	{@link Extra#getAttributeNames}.  
	<p>
	Attributes can be accessed through the usual mechanisms such as
	{@link StdAttributesAccess} and {@link DottedNames}.

	@see DottedNames
	@see com.sun.appserv.management.config.ConfigDottedNames
	@see com.sun.appserv.management.monitor.MonitoringDottedNames
 */
public interface AllDottedNames extends AMX
{
}



