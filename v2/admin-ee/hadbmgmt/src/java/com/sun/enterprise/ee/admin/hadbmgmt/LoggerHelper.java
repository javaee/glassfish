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
 * LoggerHelper.java
 *
 * Created on April 21, 2004, 6:20 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.util.logging.*;

/**
 *
 * @author  bnevins
 */
class LoggerHelper
{
	private LoggerHelper()
	{
	}
	
	///////////////////////////////////////////////////////////////////////////

	final static Logger get()
	{
		// the final should cause this to be inlined...
		return logger;
	}
	
	///////////////////////////////////////////////////////////////////////////

	final static void set(Logger loger)
	{
		logger = loger;
	}

	///////////////////////////////////////////////////////////////////////////
	////////         Convenience methods        ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	final static void finest(String s)						{ if(logger != null) logger.finest(StringHelper.get(s)); }
	final static void finest(String s, Object o)			{ if(logger != null) logger.log(Level.FINEST, StringHelper.get(s, o)); }
	final static void finest(String s, Object o, Object o2) { if(logger != null) logger.log(Level.FINEST, StringHelper.get(s, o, o2)); }

	final static void finer(String s)						{ if(logger != null) logger.finer(StringHelper.get(s)); }
	final static void finer(String s, Object o)				{ if(logger != null) logger.log(Level.FINER, StringHelper.get(s, o)); }
	final static void finer(String s, Object o, Object o2)	{ if(logger != null) logger.log(Level.FINER, StringHelper.get(s, o, o2)); }

	final static void fine(String s)						{ if(logger != null) logger.fine(StringHelper.get(s)); }
	final static void fine(String s, Object o)				{ if(logger != null) logger.log(Level.FINE, StringHelper.get(s, o)); }
	final static void fine(String s, Object o, Object o2)	{ if(logger != null) logger.log(Level.FINE, StringHelper.get(s, o, o2)); }

	final static void info(String s)						{ if(logger != null) logger.info(StringHelper.get(s)); }
	final static void info(String s, Object o)				{ if(logger != null) logger.log(Level.INFO, StringHelper.get(s, o)); }
	final static void info(String s, Object o, Object o2)	{ if(logger != null) logger.log(Level.INFO, StringHelper.get(s, o, o2)); }

	final static void warning(String s)						{ if(logger != null) logger.warning(StringHelper.get(s)); }
	final static void warning(String s, Object o)			{ if(logger != null) logger.log(Level.WARNING, StringHelper.get(s, o)); }
	final static void warning(String s, Object o, Object o2){ if(logger != null) logger.log(Level.WARNING, StringHelper.get(s, o, o2)); }

	final static void severe(String s)						{ if(logger != null) logger.severe(StringHelper.get(s)); }
	final static void severe(String s, Object o)			{ if(logger != null) logger.log(Level.SEVERE, StringHelper.get(s, o)); }
	final static void severe(String s, Object o, Object o2) { if(logger != null) logger.log(Level.SEVERE, StringHelper.get(s, o, o2)); }
	
	///////////////////////////////////////////////////////////////////////////

	private static Logger	logger = null;
}
