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
 * DeploymentSource.java
 *
 * Created on April 8, 2004, 9:13 AM
 */

package com.sun.appserv.management.deploy;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarInputStream;

import com.sun.appserv.management.base.MapCapable;

/**
	Abstraction for archives delivery for a deployment operation

	This interface may be instantiated by using routines in
	{@link com.sun.appserv.management.deploy.DeploymentSupport}
 */
public interface DeploymentSource extends MapCapable
{
	/**
		Value of the MAP_CAPABLE_TYPE_KEY when turned into a Map.
	 */
	public final static String	DEPLOYMENT_SOURCE_CLASS_NAME	= 
			"com.sun.appserv.management.deploy.DeploymentSource";
			
    /** 
     * (Optional)
     * @return the archive as a file handle if possible 
     * otherwise return null.
     */
    public File getArchive();
       
    /** 
     * @return a JarInputStream on the archive. 
     */
    public JarInputStream getArchiveAsStream()
    	throws IOException;
    
    /** 
     * @return true if this archive is complete or false
     * if it is a partial delivery (redeploy).
     */
    public boolean isCompleteArchive();
    
    /**
     * In case of a partial delivery.
     * @return entries added iterator
     */
    public String[] getEntriesAdded();
    
    /**
     * In case of a partial archive delivery.
     * @return entries removed iterator
     */
    public String[] getEntriesRemoved();
    
    /**
     * In case of a partial delivery.
     * @return entries deleted iterator
     */
    public String[] getEntriesDeleted();
}
