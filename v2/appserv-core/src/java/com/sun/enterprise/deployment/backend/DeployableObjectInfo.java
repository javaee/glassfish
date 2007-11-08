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
 * EnterpriseInfo.java
 *
 * Created on November 15, 2001, 9:17 PM
 */

package com.sun.enterprise.deployment.backend;

import java.io.*;
import java.util.*;
import com.sun.enterprise.util.io.FileUtils;

/**
 *
 * @author  bnevins
 * @version 
 */

abstract public class DeployableObjectInfo {

	protected DeployableObjectInfo(File rootPath, String name) 
	{
		this(rootPath, name, null);
	}

	///////////////////////////////////////////////////////////////////////////

	protected DeployableObjectInfo(File rootPath, String name, File archive) 
	{
		this.rootPath	= rootPath;
		this.name		= name;
		this.archive	= archive;
	}

	///////////////////////////////////////////////////////////////////////////

	/** Prepares a human-readable dump of all the information
	 * contained by this Object.  Recursively calls toString()
	 * on contained objects.
	 * @return Returns a String
	 */	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("*********** Archive Info Dump ***********\n");
		
		sb.append("Root Path:        " + getRootPath() + '\n');
		sb.append("Name:             " + getName() + '\n');
		
		if(archive != null)
			sb.append("Original Archive: " + getArchive() + '\n');
		
		return sb.toString();
	}

	///////////////////////////////////////////////////////////////////////////

	/** Returns the official name (ID) of the object
	 * @return The String ID of this object
	 */	
	public String getName()
	{
		return name;
	}


	///////////////////////////////////////////////////////////////////////////

	/** Returns the top-level directory that this object resides in
	 * @return A File object pointing at the top-level directory containing this object
	 */	
	public File getRootPath()
	{
		return rootPath;
	}

	///////////////////////////////////////////////////////////////////////////

	File getArchive()
	{
		return archive;
	}

	///////////////////////////////////////////////////////////////////////////

	private File				rootPath;
	private String				name;
	private File				archive		= null;
}
