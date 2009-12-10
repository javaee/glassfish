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
 * ZipItem.java
 *
 * Created on February 2, 2002, 12:53 PM
 * 
 * @author  bnevins
 * @version $Revision: 1.3 $
 * <BR> <I>$Source: /cvs/glassfish/admin/backup/src/java/com/sun/enterprise/config/backup/util/ZipItem.java,v $
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

package com.sun.enterprise.backup.util;

import java.io.File;

/** 
 * This class encapsulates the two pieces of information required to make a
 * ZipEntry -- the "real" path, and the path you want to appear in the zip file
 */
public class ZipItem 
{
	/** 
     * Construct a ZipItem
     *
	 * @param file The actual file
	 * @param name The zip entry name - i.e. the relative path in the zip file
	 * @throws ZipFileException
	 */	
	public ZipItem(File file, String name) throws ZipFileException
	{
		//if(!file.exists())
		//	throw new ZipFileException("File doesn't exist: " + file);
		if(name == null || name.length() <= 0)
			throw new ZipFileException("null or empty name for ZipItem");
		
		this.file = file;
		this.name = name;
	}

	/** 
     * Returns a String represenation of the real filename and the zip entry
     * name.
     *
	 * @return String with the path and the zip entry name
	 */	
	public String toString()
	{
		return "File: " + file.getPath() + ", name: " + name;
	}

    /**
     * Returns the zip entry name 
     * 
     * @return   the zip entry name
     */
    public String getName() 
    {
        return this.name;
    }

    /**
     * Returns the actual file
     *
     * @return  the actual file
     */
    public File getFile() 
    {
        return this.file;
    }
	
	///////////////////////////////////////////////////////////////////////////

	File	file;
	String	name;
}
