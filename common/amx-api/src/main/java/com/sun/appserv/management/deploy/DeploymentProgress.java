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
 * DeploymentProgress.java
 *
 * Created on April 8, 2004, 9:35 AM
 */

package com.sun.appserv.management.deploy;

import java.util.Map;
import java.util.Locale;

import javax.management.openmbean.CompositeData;

import com.sun.appserv.management.base.MapCapable;



/**
	Interface to provide deployment feedback while deployment 
	is executing in the server backend.

	This interface may be instantiated by using routines in
	{@link com.sun.appserv.management.deploy.DeploymentSupport}
 */
public interface DeploymentProgress extends MapCapable
{
	/**
		Value of the MAP_CAPABLE_TYPE_KEY when turned into a Map.
	 */
	public final static String	DEPLOYMENT_PROGRESS_CLASS_NAME	=
			"com.sun.appserv.management.deploy.DeploymentProgress";


	/**
		Key for the progress percent as returned by getProgressPercent().
	 */
	public static final String	PROGRESS_PERCENT_KEY	= "ProgressPercent";
	
	/**
		Key for the Description as returned by getDescription().
	 */
	public static final String	DESCRIPTION_KEY	= "Description";
	
	/**
		Key prefix for the Description as returned by getDescription().  The key for
		a given locale is LOCALIZED_DESCRIPTION_KEY_BASE + "_" + Locale.toString()
	 */
	public static final String	LOCALIZED_DESCRIPTION_KEY_BASE	= "LocalizedDescription";

    
    /**
     * @return the deployment progress number between 0 and 
     * 100 (deployment finished). This number is purely 
     * indicative and cannot be used to calculate actual 
     * remaining time
     * @return number from 0 to 100 indicating status
     */
    public byte getProgressPercent();
    
    /**
     * @return the last meaningful description of the current
     * deployment operation
     */
    public String getDescription();
    
    /**
     * @return the last meaningful localized description of 
     * the current deployment operation.
     */
    public String getLocalizedDescription(Locale locale);
}
