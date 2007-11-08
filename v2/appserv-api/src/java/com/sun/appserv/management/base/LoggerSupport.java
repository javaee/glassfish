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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/base/LoggerSupport.java,v 1.1 2006/12/02 06:02:48 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:02:48 $
 */

package com.sun.appserv.management.base;

import javax.management.ObjectName;


import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;



public final class LoggerSupport
{
	private LoggerSupport()	{}
	
	public static final String	AMX_LOGGER_PREFIX			= "javax.enterprise.system.tools.admin";
	public static final String	AMX_CLIENT_LOGGER_PREFIX	= AMX_LOGGER_PREFIX + ".client";
	public static final String	AMX_SERVER_LOGGER_PREFIX	= AMX_LOGGER_PREFIX + ".server";
	public static final String	AMX_MBEAN_LOGGER_PREFIX	= AMX_SERVER_LOGGER_PREFIX + ".mbeans";
	
	/**
		Name of the root logger for all AMX
	 */
	public static final String	AMX_ROOT_LOGGER			= AMX_LOGGER_PREFIX;
	
	/**
		Name of the root logger for AMX client code.
	 */
	public static final String	AMX_CLIENT_LOGGER			= AMX_CLIENT_LOGGER_PREFIX;
	
	/**
		Name of the root logger for all MBean loggers.
	 */
	public static final String	AMX_MBEAN_ROOT_LOGGER		= AMX_MBEAN_LOGGER_PREFIX;
	
	/**
		Name of the root logger for AMX server code
	 */
	public static final String	AMX_SERVER_LOGGER			= AMX_SERVER_LOGGER_PREFIX;
	
}
