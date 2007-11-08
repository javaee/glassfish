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
 * ExceptionType.java
 *
 * Created on July 8, 2002, 12:34 AM
 * 
 * @author  bnevins
 * @version $Revision: 1.3 $
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/instance/ExceptionType.java,v $
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc., 
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A. 
 * All rights reserved. 
 * 
 * This software is the confidential and proprietary information 
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information"). 
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems. 
 *
 */

package com.sun.enterprise.instance;
import java.util.*;

class ExceptionType 
{
	private ExceptionType(String theKey, int theNumArgs) 
	{
		assert theKey != null;
		assert theNumArgs >= 0;
		
		key		= theKey;
		numArgs	= theNumArgs;
		allTypes.add(this);
	}
	
	///////////////////////////////////////////////////////////////////////////

	String getString()
	{
		return key;
	}
	
	///////////////////////////////////////////////////////////////////////////

	int getNumArgs()
	{
		return numArgs;
	}
	
	///////////////////////////////////////////////////////////////////////////

	static ArrayList getAllTypes()
	{
		return allTypes;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	// for testing:
	private static	ArrayList			allTypes = new ArrayList(); 

	static final	ExceptionType	FAIL_DD_LOAD		= new ExceptionType("01", 1);
	static final	ExceptionType	FAIL_DD_SAVE		= new ExceptionType("02", 1);
	static final	ExceptionType	APP_NOT_EXIST		= new ExceptionType("03", 0);
	static final	ExceptionType	BAD_REG				= new ExceptionType("04", 0);
	static final	ExceptionType	MISSING_SERVER_NODE	= new ExceptionType("05", 0);
	static final	ExceptionType	CANT_APPLY			= new ExceptionType("06", 0);
	static final	ExceptionType	UNSUPPORTED			= new ExceptionType("07", 1);
	static final	ExceptionType	NULL_INSTANCE_NAME	= new ExceptionType("08", 0);
	static final	ExceptionType	NULL_INSTANCE		= new ExceptionType("09", 0);
	static final	ExceptionType	INSTANCE_EXISTS		= new ExceptionType("10", 1);
	static final	ExceptionType	PORT_IN_USE			= new ExceptionType("11", 1);
	static final	ExceptionType	PORT_TAKEN			= new ExceptionType("12", 0);
	static final	ExceptionType	CANT_CREATE_ADMIN	= new ExceptionType("13", 0);
	static final	ExceptionType	NO_JAVA_HOME		= new ExceptionType("14", 0);
	static final	ExceptionType	NO_IMQ_HOME			= new ExceptionType("15", 0);
	static final	ExceptionType	NO_SUCH_INSTANCE	= new ExceptionType("16", 1);
	static final	ExceptionType	SERVER_NO_START		= new ExceptionType("17", 0);
	static final	ExceptionType	NO_RECEIVE_TOKENS	= new ExceptionType("18", 0);
	static final	ExceptionType	NO_SUCH_CON_MOD		= new ExceptionType("19", 0);
	static final	ExceptionType	BAD_CON_MOD_INFO	= new ExceptionType("20", 0);
	static final	ExceptionType	WRONG_MOD_INFO		= new ExceptionType("21", 0);
	static final	ExceptionType	NO_SUCH_EJB_MOD		= new ExceptionType("22", 0);
	static final	ExceptionType	BAD_EJB_MOD_INFO	= new ExceptionType("23", 0);
	static final	ExceptionType	IO_ERROR_LOADING_DD	= new ExceptionType("24", 1);
	static final	ExceptionType	IO_ERROR_SAVING_DD	= new ExceptionType("25", 1);
	static final	ExceptionType	ILLEGAL_PORT		= new ExceptionType("26", 0);
	static final	ExceptionType	ILLEGAL_RESTART		= new ExceptionType("27", 0);
	static final	ExceptionType	NULL_ARG			= new ExceptionType("28", 0);
	static final	ExceptionType	NO_INSTANCE_DIR		= new ExceptionType("29", 2);
	static final	ExceptionType	CANNOT_APPLY_CHANGES= new ExceptionType("30", 0);
	static final	ExceptionType	BIZARRO_MESSAGE		= new ExceptionType("31", 0);
	static final	ExceptionType	NO_XML				= new ExceptionType("32", 0);
	static final	ExceptionType	NO_XML_BU			= new ExceptionType("33", 0);
	static final	ExceptionType	FROM_NOT_EXIST		= new ExceptionType("34", 1);
	static final	ExceptionType	FROM_IS_DIR			= new ExceptionType("35", 1);
	static final	ExceptionType	TO_READ_ONLY		= new ExceptionType("36", 1);
	static final	ExceptionType	TO_IS_DIR			= new ExceptionType("37", 1);
	static final	ExceptionType	FAILED_COPY			= new ExceptionType("38", 3);
	static final	ExceptionType	NULL_MODULE_TYPE	= new ExceptionType("39", 0);
	static final	ExceptionType	NO_SUCH_WEB_MOD		= new ExceptionType("40", 0);
	static final	ExceptionType	BAD_WEB_MOD_INFO	= new ExceptionType("41", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);
	//static final	ExceptionType			= new ExceptionType("40", 0);

	
	private final	String				key;
	private final	int					numArgs;

	
}



