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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.Utility;
import com.sun.appserv.management.base.XTypes;

import javax.management.Notification;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 @deprecated Please use JSR 88 instead
 <b>This interface and associated items have been deprecated.</b>
 Please see javax.management.deploy.* at
    {@link http://java.sun.com/javaee/5/docs/api/} and also
    {@link http://jcp.org/en/jsr/detail?id=88}
 <p>
 
 This interface provides facilities to deploy any type of 
 J2EE module. 
 *
 Users of this interface are likely to do the following 
 <p>
  - Start uploading the necessary file for deployment by 
  using the initiateFileUpload and sendBytes APIs. Once 
  the file(s) upload are completed the deploy operation 
  can be initiated. 
 </p>
 <p>
  - Instead of uploading files or when dealing with 
  redeployment with a partial archive delivery, a  
  DeploymentSource object is used to retrieve the 
  delivered files.  This DeploymentSource is encoded
  as a Map.
 </p>
 <p>
  - Deploy operations can be invoked either by using 
  uploaded files ID (obtained from initiateFileUpload) 
  or a DeploymentSource. DeploymentOptions can be passed 
  as a Map of Deployment option name to deployment option 
  value.
 </p>
 </p>
  - Deploy operations are asynchronous therefore they can
  be monitored using the getDeploymentProgress and 
  getStatusCode operations. 
 </p>
 <p>
  - To observe completion of a deployment, the client should
  register itself as a listener on the DeploymentMgr using
  addNotificationListener().  A Notification will be issued in
  which the value of notif.getUserData() will be the deployID
  used for the deployment.  The notif will have the getType() of
  DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE.
 </p>
 <p>
  - In case the client wants to retrieve non portable artifacts
  after successful deployment the file download APIs should be 
  used (initiateFileDownload and receiveBytes).
 </p>
 <p>
  MBean API's concept of Deploy and Undeploy are different from the CLI and GUI concept.
  In order to have a fully functioning application for deploy or a fully removed application for undeploy,
  3 steps must be performed in both cases.  
  Associate and start (also, stop and disassociate) are always performed together in CLI and GUI.
  MBean API users must perform the 3 steps
  explicitly.  I.e. Deploy and Undeploy in the MBean API performs only the transfer and registration 
  of files in the AppServer.  
 </p>
 <p>These are the required steps: </p>
 <strong>Deploy</strong>
 <ul>
 <li>Deploy
 <li>Associate
 <li>Start
 </ul>
 <strong>Undeploy</strong>
 <ul>
 <li>Stop
 <li>Disassociate
 <li>Undeploy
 </ul>
 <p>
 Also, J2EEServer.getDeployedObjectsSet() is based on a running modules. Hence, a deployedObject will appear 
 in the list only when the module is running
 </p>
 @see Util#getAMXNotificationValue
 */
public interface DeploymentMgr extends AMX, Utility, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.DEPLOYMENT_MGR;
	
	/**
		Prefix for all keys used by DeploymentMgr
	 */
	public static final String	KEY_PREFIX	= XTypes.DEPLOYMENT_MGR + ".";
	
	/**
		Key within the Map of a Notification indicating deployment ID.
        @see Util#getAMXNotificationValue
	 */
	public final String	NOTIF_DEPLOYMENT_ID_KEY	= KEY_PREFIX + "DeploymentID";
	
	/**
		Key within the Map of a Notification of type
		DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE, indicating
		the final status of the deployment.
        @see Util#getAMXNotificationValue
	 */
	public final String	NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY	=
	    KEY_PREFIX + "DeploymentCompletedStatus";
	
	/**
		Key within the Map of a Notification of type
		DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE, indicating a
		DeploymentProgress, as a Map.
        @see Util#getAMXNotificationValue
	 */
	public final String	NOTIF_DEPLOYMENT_PROGRESS_KEY	=
	    KEY_PREFIX + "DeploymentProgress";
	
	
	/**
		The type of the Notification emitted when a deployment starts.
		The user data field contains a Map keyed by NOTIF_*_KEY.
	 */
	public final String	DEPLOYMENT_STARTED_NOTIFICATION_TYPE	=
		XTypes.DEPLOYMENT_MGR + ".DeploymentStarted";
		
	/**
		The type of the Notification emitted when a deployment completes.
		The user data field contains a Map keyed by NOTIF_*_KEY.
	 */
	public final String	DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE	=
		XTypes.DEPLOYMENT_MGR + ".DeploymentCompleted";
		
	/**
		The type of the Notification emitted when a deployment is aborted
		via abortDeploy().
		The user data field contains a Map keyed by NOTIF_*_KEY.
	 */
	public final String	DEPLOYMENT_ABORTED_NOTIFICATION_TYPE	=
		XTypes.DEPLOYMENT_MGR + ".DeploymentAborted";
		
	/**
		The type of the Notification emitted for deployment progress.
		The user data field contains a Map keyed by NOTIF_*_KEY.
	 */
	public final String	DEPLOYMENT_PROGRESS_NOTIFICATION_TYPE	=
		XTypes.DEPLOYMENT_MGR + ".DeploymentProgress";
	
    /*
     Constant file name to use to retrieve the client stubs 
     jar file after a successful deployment.
     @see initiateFileDownload(String, String)
     */
    public static final String STUBS_JARFILENAME = "STUBSJAVAFILENAME";

	/**
		Key for startDeploy() options--
		Forcefully (re)deploy the component even if the specified component has
		already been deployed.  The default value is true. 
	 */
	public static final String DEPLOY_OPTION_FORCE_KEY = KEY_PREFIX + "Force";
	
	/**
		Key for undeploy() options--
		If set to true, it deletes all the connection pools and connector
		resources associated with the resource adapter (being undeployed).
		If set to false, the undeploy fails if any pools and resources are still
		associated with the resource adapter.
		<p>
		This option is applicable to connectors(resource adapters) and
		applications(J2EE apps i.e .ear files can contain the resource adapters *.rar).
		The default value is false. 
	 */
	public static final String DEPLOY_OPTION_CASCADE_KEY = KEY_PREFIX + "Cascade";
	
	/**
		Key for startDeploy() options--
		If set to true, verify the syntax and semantics of the deployment descriptor.
		The default value is false. 
	 */
	public static final String DEPLOY_OPTION_VERIFY_KEY = KEY_PREFIX + "Verify";
	
	/**
		Key for startDeploy) options--
		Disables or enables the component after it is deployed.
		The default value is true.
	 */
	public static final String DEPLOY_OPTION_ENABLE_KEY = KEY_PREFIX + "Enable";
	
	/**
		Key for startDeploy) options--
		The context root of the deployable web component. Only applies to web module. 
	 */
	public static final String DEPLOY_OPTION_CONTEXT_ROOT_KEY = KEY_PREFIX + "ContextRoot";
	
	/**
		Key for startDeploy) options--
		Registration name of the deployble component,
		its value should be unique across domain. 
	 */
	public static final String DEPLOY_OPTION_NAME_KEY = KEY_PREFIX + "Name";
	
	/**
		Key for startDeploy) options--
		The description of the component being deployed. 
	 */
	public static final String DEPLOY_OPTION_DESCRIPTION_KEY = KEY_PREFIX + "Description";
	
	/**
		Key for startDeploy) options--
		When true, will generate the static RMI-IIOP stubs and put it in the client.jar.
		Default value for this option is "false".
	 */
	public static final String DEPLOY_OPTION_GENERATE_RMI_STUBS_KEY =  KEY_PREFIX + "GenerateRMIStubs";

	/**
		Key for startDeploy) options--
		This option controls whether availability is enabled for SFSB checkpointing
		(and potentially passivation). When false,
		then all SFSB checkpointing is disabled for either the given j2ee app
		or the given ejb module. When true, the j2ee app or stand-alone
		ejb modules may be enabled. Default value is "false".
	 */
	public static final String DEPLOY_OPTION_AVAILABILITY_ENABLED_KEY =   KEY_PREFIX + "AvailabilityEnabled"; 
  
  
	/**
		Key for startDeploy) options--
		This option controls whether java web start is enabled. 
		Applicable for a J2EEApplication or AppClientModule.
	 */
	public static final String DEPLOY_OPTION_JAVA_WEB_START_ENABLED_KEY =
	    KEY_PREFIX + "JavaWebStartEnabled"; 
	    
	/**
		Key for startDeploy) options--
		This option specifies additional libraries.
		Applicable for a J2EEApplication, WebModule, or EJBModule.
		@see com.sun.appserv.management.config.Libraries
	 */
	public static final String DEPLOY_OPTION_LIBRARIES_KEY =
	    KEY_PREFIX + "Libraries"; 
  
    /**
     initiatiate a new deployment operation, the id 
     returned will be used to transfer the appropriate 
     files on the server. 
     *
     @param totalSize total size of the file to upload
     @return an identifier describing this file upload
     */
    public Object initiateFileUpload( long totalSize )
    		throws IOException;
    		
    		
    /**
        This variant allows a name to be specified.
        
        @param name name to be used for the temp file
        @param totalSize total size of the file to upload
        @return an identifier describing this file upload
     */
    public Object initiateFileUpload( String name, long totalSize )
    		throws IOException;
    
    /**
     For an upload id obtained from initiateFileUpload(), send another
     chunk of bytes for that upload.
     
     @param uploadID the id obtained from initiateFileUpload()
     @param bytes the bytes to upload
     @return true if the total upload has been completed, false otherwise
     */ 
    public boolean uploadBytes(Object uploadID, byte[] bytes)
    	throws IOException;
    
    
	/**
		Create a new deploy ID which may be used via startDeploy() to start
		a new deployment operation.
	   
		@return an new opaque identifier which can be used in startDeploy()
     */
    public Object initDeploy();
    
    /**
     Start the deployment operation using file(s) previously uploaded
     by initializeFileUpload() and uploadBytes().
     <p>
     When the runtime deployment descriptors  
     and other server specific configuration are embedded in 
     the deployable archive, null should be passed for the planUploadID.
     <p>
     Legal keys for use within the options Map include:
     <ul>
		<li>{@link #DEPLOY_OPTION_FORCE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_CASCADE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_VERIFY_KEY}</li>
		<li>{@link #DEPLOY_OPTION_ENABLE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_CONTEXT_ROOT_KEY}</li>
		<li>{@link #DEPLOY_OPTION_NAME_KEY}</li>
		<li>{@link #DEPLOY_OPTION_DESCRIPTION_KEY}</li>
		<li>{@link #DEPLOY_OPTION_GENERATE_RMI_STUBS_KEY}</li>
		<li>{@link #DEPLOY_OPTION_AVAILABILITY_ENABLED_KEY}</li>
		<li>{@link #DEPLOY_OPTION_JAVA_WEB_START_ENABLED_KEY}</li>
		<li>{@link #DEPLOY_OPTION_LIBRARIES_KEY}</li>
     </ul>
     
     @param deployID 		an id obtained from initDeploy()
     @param uploadID 		an id obtained from initiateFileUpload()
     @param planUploadID	an id obtained from initiateFileUpload(), may be null
     @param options contains the list of deployment options
     */
    public void startDeploy( Object deployID, Object uploadID, Object planUploadID, Map<String,String> options);
    
    
    /**
     Start a new deployment operation given a deployment source 
     and a list of options. The DeploymentPlan is null 
     when the runtime deployment descriptors and other 
     server specific configuration is embedded in the 
     deployable archive.
     <p>
     Legal keys for use within the options Map include:
     <ul>
		<li>{@link #DEPLOY_OPTION_FORCE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_CASCADE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_VERIFY_KEY}</li>
		<li>{@link #DEPLOY_OPTION_ENABLE_KEY}</li>
		<li>{@link #DEPLOY_OPTION_CONTEXT_ROOT_KEY}</li>
		<li>{@link #DEPLOY_OPTION_NAME_KEY}</li>
		<li>{@link #DEPLOY_OPTION_DESCRIPTION_KEY}</li>
		<li>{@link #DEPLOY_OPTION_GENERATE_RMI_STUBS_KEY}</li>
		<li>{@link #DEPLOY_OPTION_AVAILABILITY_ENABLED_KEY}</li>
     </ul>
     
     @param deployID an id obtained from initDeploy()
     @param source a DeploymentSource as a Map
     @param plan will contain the deployment plan for this, may be null
     deployment operation if the deployable archive is portable 
     @param options contains the list of deployment options.
     */
    public void startDeploy(
        Object deployID,
        Map<String,? extends Serializable> source,
        Map<String,? extends Serializable> plan,
        Map<String,String> options);
    
    
    /**
    	Return all Notifications, which have already been sent, but which are
    	also queued waiting for this request.
    	<p>
    	The deployment is done if the last Notification is of type
    	{@link #DEPLOYMENT_COMPLETED_NOTIFICATION_TYPE} or
    	{@link #DEPLOYMENT_ABORTED_NOTIFICATION_TYPE}.
    	The deployment is not otherwise affected; you may still call
    	{@link #getFinalDeploymentStatus}.
    	<p>
    	<b>WARNING: This routine is for internal use only, and may not be supported
    	in the future.  External users should use the standard Notification
    	mechanisms by registering as a listener</b>
    	
    	@param deployID 
     	@return any accumulated Notifications
     	@deprecated
     */
    public Notification[] takeNotifications( final Object	deployID);
    
    /**
     abort a given deployment operation, all modification 
     to the server must be rollbacked and all resources 
     cleaned. If the abortion operation cannot be successfully
     completed (because it's too late for instance), it is the 
     responsibility of the client to undeploy the application.
     
     @param deployID the id obtained from initDeploy()
     @return true if the operation was successfully aborted
     
     */ 
    public boolean abortDeploy(Object deployID);
    
    /**
	   	Return the final DeploymentStatus once the deployment has finished.
	   	or null if the deployment has not yet finished.  Once called,
	   	the state associated with this deployment is removed, and the
	   	deployID is no longer valid.  All outstanding notifications pending
	   	for takeNotifications() are also removed and become unavailable.
	   	<p>
    	<b>WARNING: This routine is for internal use only, and may not be supported
    	in the future.  External users should use the standard Notification
    	mechanisms by registering as a listener</b>
     
		@param deployID the id obtained from initDeploy()
		@return a DeploymentStatus, as a Map, or null if not yet finished
     */ 
	public Map<String,Serializable>	getFinalDeploymentStatus(Object deployID);
    
    
    /**
     Undeploys a module or application from the server,
     cleans all associated resources and removed the
     module from the list of installed components.

     @param moduleID the application module ID
     @param optionalParams	optional parameters
     @return a DeploymentStatus for the completed operation, as a Map
     */
    public Map<String,Serializable> undeploy(String moduleID, Map<String,String> optionalParams); 
    
    /**
     Initiates a file download with the given filename. 
     The filename is relative to the application or module 
     URL. If the filename is STUBS_JARFILENAME, the 
     application client stubs jar file will be downloaded. 
     This API can also be used for downloading final WSDL files.
     *
     @param moduleID the deployed component moduleID this file download 
     is related to.
     @param fileName the desired file name corresponding to this module
     @return the operation id
     */
    public Object initiateFileDownload(String moduleID, String fileName)
    	throws IOException;
    	
    /**
    	Get the total length the download will be, in bytes.
    	
     	@param downloadID the file download operation id, from initiateFileDownload()
     */
    public long getDownloadLength( final Object downloadID );
    
    
    /**
    	The maximum allowed transfer size for downloading.
     */
    public static final int	MAX_DOWNLOAD_CHUNK_SIZE	= 5 * 1024 * 1024;
    
    /**
     Download byte chunks from the server using a file 
     operation id obtained via initiateFileDownload API. 
     The bufferSize is the requested number of bytes to 
     be received. If the size of the returned byte[] is less than
     the requestSize, then the transfer has completed, and the
     downloadID is no longer valid.  An attempt to read more than
     the allowed maximum size will throw an exception.  The caller
     can check the total download size in advance via
     getDownloadLength().
     
     @param downloadID the file download operation id, from initiateFileDownload()
     @param requestSize
     @return bytes from the file.
     */
    public byte[] downloadBytes( Object downloadID, int requestSize )
    	throws IOException;




}
