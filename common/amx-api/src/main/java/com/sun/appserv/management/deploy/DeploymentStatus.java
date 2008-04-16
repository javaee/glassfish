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
package com.sun.appserv.management.deploy;

import com.sun.appserv.management.base.OperationStatus;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
	Final status of a deployment.
	
	See {@link com.sun.appserv.management.deploy.DeploymentMgr}.getFinalDeploymentStatus()
 */
public interface DeploymentStatus extends OperationStatus
{
	/**
		Value of the MapCapable.MAP_CAPABLE_CLASS_NAME_KEY when turned into a Map.
	 */
	public final static String	DEPLOYMENT_STATUS_CLASS_NAME	= 
			"com.sun.appserv.management.deploy.DeploymentStatus";
	
	/**
		Status code indicating failure due to improper initialization.
	 */
	public final int	STATUS_CODE_NOT_INITIALIZED = 0;
	
	
	public static final String	STAGE_STATUS_KEY		= STATUS_CODE_KEY;
	
	public static final String	STAGE_STATUS_MESSAGE_KEY = "StageStatusMessage";
	
	public static final String	SUB_STAGES_KEY			= "SubStages";
	
	public static final String	STAGE_THROWABLE_KEY		= THROWABLE_KEY;
	
	public static final String	STAGE_DESCRIPTION_KEY    = "StageDescription";
	
	public static final String	PARENT_KEY               = "Parent";

	public static final String	ADDITIONAL_STATUS_KEY               = "AdditionalStatus";
	
        /**
          * Key within the Map of AdditionalStatus. 
          * The unique module id of the deployed component.
          */     
	public static final String	MODULE_ID_KEY               = "ModuleID";

	/**
	    Get the sub stages for this deployment status, each of
	    which has been converted into a Map<String,Serializable>.
	    This method is included for backward compatibility; please
	    use {@link #getSubStagesList} instead.
	    @return an Iterator for the sub stages
	    @deprecated
	 */
	public Iterator<Map<String,Serializable>> getSubStages();
	
	/**
	    Get the sub stages for this deployment status.
	    The list may not be modified.
	    @return List&lt;DeploymentStatus>
	 */
	public List<DeploymentStatus> getSubStagesList();
	
	/** 
		Legal status codes include:
		<ul>
		<li>OperationStatus#STATUS_CODE_SUCCESS</li>
		<li>OperationStatus#STATUS_CODE_FAILURE</li>
		<li>OperationStatus#STATUS_CODE_WARNING</li>
		<li>#STATUS_CODE_NOT_INITIALIZED</li>
		</ul>
		@return the status for this stage (ignoring sub stages status)
	 */
	public int getStageStatus();
        
	/**
	 * @return a meaningful i18ned reason for failure or warning
	 */
	public String getStageStatusMessage();
	
	/**
	 * @return the exception if an exception was thrown during 
	 *  the execution of the stage
	 */
	public Throwable getStageThrowable();

	/**
	 * @return a meaningful i18ned stage description
	 */
	public String getStageDescription();

	/**
	 * @return the map storing additional properties for this status
	 */
	public Map<String,Serializable> getAdditionalStatus();

    /**
     * @return the parent status for this status if any
     */
    public DeploymentStatus getParent();

    /**
     * set the parent status for this status if any
     * @param parent DeploymentStatus
     */
    public void setParent( DeploymentStatus parent );
}





