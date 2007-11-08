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

package com.sun.enterprise.admin.server.core.mbean.config;

//iAS imports
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.instance.InstanceDefinition;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.ExecException;
import com.sun.enterprise.util.io.FileUtils;

// JMS Provider/MQ related imports
import com.sun.enterprise.jms.IASJmsUtil;
import com.sun.messaging.jmq.jmsspi.JMSAdmin;

//Admin imports
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.StringValidator;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.common.OperationProgress;
import com.sun.enterprise.admin.common.ServerInstanceStatus;
import com.sun.enterprise.admin.common.RequestID;
import com.sun.enterprise.admin.common.exception.InstanceAlreadyExistsException;
import com.sun.enterprise.admin.common.exception.NoSuchInstanceException;
import com.sun.enterprise.admin.common.exception.ControlException;
import com.sun.enterprise.admin.common.ByteChunk;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanEasyConfig;
import com.sun.enterprise.admin.server.core.AdminService;

//Channel related
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

//JMX imports
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;

//Other imports
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.domains.registry.DomainEntry;
import com.sun.enterprise.admin.common.domains.registry.DomainRegistry;
//import com.sun.enterprise.admin.verifier.tests.StaticTest;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.RelativePathResolver;

/**
    A managed Controller of the entire Admin Server. All the life cycle
    operations on all server side entities are exposed by this controller. Thus
    it is the <strong> management interface <strong> for the object that
    controls the various entities like Server Instances.
    <p>
    It is guaranteed that there will always be single instance of this type
    of MBean in the MBeanServer.
    <p>
    ObjectName of this MBean is: ias:type=controller
*/

public class ServerController extends AdminBase {
    private Hashtable				mStreamTable	= null;
    private static final Logger  sLogger = 
            Logger.getLogger(AdminConstants.kLoggerName);
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ServerController.class );
    private AdminContext mAdminContext;

    public ServerController() {
        mStreamTable = new Hashtable();
    }

    public ServerController(AdminContext adminContext) {
        this();
        mAdminContext = adminContext;
        throw new RuntimeException( "ServerController: is this code used?" );
    }

    /**
        Creates a Server Instance using the Admin Server and given instance name.
        By default the Server Instance will be created on the same machine and
        any available port and will be auto-restarted when the entire server
        is restarted after shutdown. The corresponding MBean will be
        registered in the MBeanServer.

        @param instanceName - name of the Server Instance
        @param hostPort - an instance of HostAndPort to indicate host:port
        @param runAsUser - The user id with which the instance and its config
        will be created. If it is null, the instance will be created with
        default user id ie the admin userid.
        @param autoStart - boolean indicating whether this Instance will be
            started when the entire Server is restarted.

        @throws ControlException if the Server Instance can't be created
    */
	
    public void createServerInstance(String         instanceName, 
                                     HostAndPort    hostPort,
                                     String         runAsUser, 
                                     boolean        autoStart) 
        throws InstanceAlreadyExistsException, ControlException
    {
        if(isBadInstanceName(instanceName))
        {
            String msg = localStrings.getString( "admin.server.core.mbean.bad_instance_name", instanceName );
            throw new ControlException( msg );
        }
        
        ObjectName siObjectName = 
            ObjectNames.getServerInstanceObjectName(instanceName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(siObjectName))
        {
            try
            {
                InstanceDefinition instance = new InstanceDefinition(instanceName,
                        hostPort.getPort());
                if (runAsUser != null)
                {
                    instance.setUser(runAsUser);
                }
                ServerManager.instance().createServerInstance(instance);
                sLogger.log(Level.INFO, "mbean.created_instance", instanceName);
            }
            catch (Exception e)
            {
                sLogger.log(Level.WARNING, "mbean.create_instance_failed", e);
                throw new ControlException(e.getMessage());
            }
        }
        else
        {
            String msg = localStrings.getString( "admin.server.core.mbean.config.instance_already_exists", instanceName );
	    throw new InstanceAlreadyExistsException( msg );
        }
    }

    public void createServerInstance(HostAndPort    hAndp, 
                                     String         instanceName, 
                                     String         mailHost,  
                                     String         user, 
                                     String         docRoot, 
                                     int            jmsPort, 
                                     String         jmsUser, 
                                     String         jmsPasswd,
                                     boolean        autoStart)
        throws InstanceAlreadyExistsException, ControlException
    {
        if(isBadInstanceName(instanceName))
        {
            String msg = localStrings.getString( "admin.server.core.mbean.bad_instance_name" );
            throw new ControlException( msg );
        }
        
        ArgChecker.checkValid(hAndp, "hostport"); //noi18n
        ArgChecker.checkValid(instanceName, "instanceName", 
                              StringValidator.getInstance()); //noi18n
        ObjectName siObjectName = 
            ObjectNames.getServerInstanceObjectName(instanceName);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        if (!mbs.isRegistered(siObjectName))
        {
            try
            {
                /* call the script to create instance */
                InstanceDefinition instance = 
                    new InstanceDefinition(hAndp.getHost(), hAndp.getPort(), 
                        instanceName, mailHost, user, docRoot, jmsPort, 
                        jmsUser, jmsPasswd);
                ServerManager.instance().createServerInstance(instance);
                sLogger.log(Level.INFO, "mbean.created_instance", instanceName);
            }
            catch (Exception e)
            {
                sLogger.log(Level.WARNING, "mbean.create_instance_failed", e);
                throw new ControlException(e.getMessage());
            }
        }
        else
        {
            throw new InstanceAlreadyExistsException(instanceName);
        }
    }
    /**
        Deletes an existing Server Instance. The instance should not be
        running. The corresponding MBean will be deregistered from the
        MBeanServer.

        @param instanceName - the name of the Server Instance
        @throws ControlException if the Server Instance is running or does not
        exist or some other problem occurs
    */
	
    public void deleteServerInstance(String instanceName) throws
        NoSuchInstanceException, ControlException
    {
        if (instanceName == null)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.null_name" );
            throw new IllegalArgumentException( msg );
        }

        ServerManager sm = ServerManager.instance();
        
        /* get running status */
        if(!sm.instanceExists(instanceName))
        {
            String msg = localStrings.getString(
                    "admin.server.core.mbean.config.instance_does_not_exist",
                    instanceName);
            throw new ControlException(msg);
        }
        boolean alive = false;
        RMIClient serverInstancePinger = AdminChannel.getRMIClient(instanceName);
        alive = serverInstancePinger.isAlive();
        if(alive)
        {
			String msg = localStrings.getString(
                    "admin.server.core.mbean.config.delete_alive_instance", 
                    instanceName);
            throw new ControlException(msg);
        }

        
        try
        {
            // Unregister all instance related MBeans
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName[] objectNames  = ObjectNameHelper.
                    getInstanceRelatedMBeans(mbs, instanceName);
            for(int i=0; i<objectNames.length; i++)
            {
                try
                {
                    mbs.unregisterMBean(objectNames[i]);
                }
                catch(Exception e)
                {
                    sLogger.log(Level.CONFIG, "delete_unregistration_failed", 
                            objectNames[i]);
                }
            }
            // Message to Config to refresh contexts
            InstanceEnvironment instanceEnvironment = new 
                    InstanceEnvironment(instanceName);
            String fileUrl  = instanceEnvironment.getConfigFilePath();
            ConfigFactory.removeConfigContext(fileUrl);

            String instanceRoot = instanceEnvironment.getInstancesRoot();
            deleteJMSProviderInstance(instanceRoot, instanceName);
            
            // Now stop it delete the files from file system */
            sm.deleteServerInstance(instanceName);
            sLogger.log(Level.INFO, "mbean.del_instance_ok", instanceName);
        }
        catch(Exception e)
        {
            sLogger.log(Level.WARNING, "mbean.del_instance_failed", e);
            throw new ControlException(e.getMessage());
        }
    }

    /**
	Delete JMS provider data specific to this instance.
    */
    private void deleteJMSProviderInstance(String instancesRoot, 
                                           String instanceName)
    {
        InstanceEnvironment instanceEnvironment;
        ConfigContext configContext;
        JmsService jmsService;
        String fileUrl;

        try
        {
            if (mAdminContext != null) {
                configContext = mAdminContext.getAdminConfigContext();
            } else {
                instanceEnvironment = new InstanceEnvironment(instanceName);
                fileUrl = instanceEnvironment.getConfigFilePath();
                configContext = ConfigFactory.createConfigContext(fileUrl);
            }

            jmsService = (JmsService)ConfigBeansFactory.getConfigBeanByXPath(
                configContext, ServerXPathHelper.XPATH_JMS_SERVICE);
        
            if ((jmsService != null) && (jmsService.isEnabled()))  {
                JMSAdmin	jmsAdmin = null;
                JavaConfig	javaConfig;
                    String		java_home = null,
                    domainName = null,
                        mqInstanceName = null,
                        mqBin = null,
                    argArray[] = new String [ 4 ];

                javaConfig = (JavaConfig)ConfigBeansFactory.getConfigBeanByXPath(
                configContext, ServerXPathHelper.XPATH_JAVACONFIG);
                java_home = javaConfig.getJavaHome();

                mqBin = System.getProperty("com.sun.aas.imqBin");
                domainName = ServerManager.instance().getDomainName();

                jmsAdmin = IASJmsUtil.getJMSAdminFactory().getJMSAdmin();

            /*
             * Use utility method to construct MQ broker instance name
             */
            mqInstanceName = IASJmsUtil.getBrokerInstanceName(domainName,
                        instanceName, jmsService);

            argArray[0] = "-javahome";
            argArray[1] = java_home;
            argArray[2] = "-varhome";
	    String mqInstanceDir = instancesRoot + File.separator +
	      		           IASJmsUtil.MQ_DIR_NAME;
            argArray[3] = mqInstanceDir;

            /*
             * Call MQ SPI method to delete the instance.
             */
                jmsAdmin.deleteProviderInstance(mqBin, argArray, mqInstanceName);
            }
        } 
        catch (Exception e)
	    {
            sLogger.log(Level.FINE, "mbean.del_jms_instance_failed", e);
	    }
    }


    /**
        Shuts down the entire Admin Server. 
        It starts a thread in which the server is stopped.
        This method <code> has to </code> give call to the
        stop script asynchronously.
        This method returns immediately after spawning a 
        Shutdown thread. There may be some timing issues
        with this method.
    */
    
	public void shutdown()
    {
        sLogger.log(Level.INFO, "mbean.shutdown_started");
//		String adminId          = ServerManager.ADMINSERVER_ID;
		String adminId          = AdminService.getAdminService().getInstanceName();
        new Thread(new ShutdownThread(adminId)).start();
    }

    /**
        Shuts down all the Server Processes, and exits, so that to restart
        the system, some external script has to be run by administrator.
        All the MBeans will be deregistered, all the JVMs will be gracefully
        stopped.
    */
    
	public void shutdownAndExit()
    {
    }

    /**
        Restarts the entire Server implying a shutdown and then start.
        Start implies it as in the method start.

        @see #start
    */

	public void restart() throws ControlException
    {
    }

    /**
        Gets the progress for an (earlier) operation with a specific RequestID.
    */
    /*
	public OperationProgress getProgress(RequestID requestID) throws
        ControlException
    {
        return null;
    }

    */

    public String getVersion() throws ControlException
    {
	return Version.getVersion();
    }

    public String getFullVersion() throws ControlException
    {
          return Version.getFullVersion();
    }

    /**
        Uploads the given ByteChunk to the Server. This will be saved
        in an area defined by the server and then the complete path
        where the file is saved is returned. This path can later be
        used for deployment to a specific Server Instance. Argument may not be
		null.
     
        @param byteChunk the ByteChunk instance that contains bytes of a file.
        @throws IllegalArgumentException, ControlException
		@return String representing the complete path of the file.
    */
	
    public String uploadToServer(ByteChunk byteChunk) throws ControlException
    {
		if (byteChunk == null)
		{
			throw new IllegalArgumentException();
		}
        String fileName  = byteChunk.getChunkedFileName();
        String localPath = AdminService.getAdminService().getTempDirPath();
        String targetDirName = byteChunk.getTargetDir();
        if (targetDirName != null)
        {
            File targetDir = new File(localPath, targetDirName);
            if (!targetDir.exists())
            {
                targetDir.mkdir();
            }
            localPath = FileUtils.safeGetCanonicalPath(targetDir);
        }
        File uploadFile = new File(localPath, fileName);
        localPath = FileUtils.safeGetCanonicalPath(uploadFile);
		if (byteChunk.isFirst())
		{
            /* The file in temporary area should not exist on first chunk */
            if (uploadFile.exists())
            {
				/*
                String msg = localStrings.getString
                    ("admin.server.core.mbean.config.temp_upload_file_exists",
                     localPath);
				throw new ControlException(msg);
				*/
				/* Commenting out the above as a workaround for the Windows problem.
				   One more try will be done, and it will only be logged as a message for 
			       now. 07/17/2002 - Kedar (this is a workaround) */
				sLogger.log(Level.INFO, "mbean.temp_upload_file_exists", localPath);
                boolean couldDelete = uploadFile.delete();
                if (couldDelete)
                {
                    sLogger.log(Level.FINE, "mbean.delete_temp_file_ok", localPath);
                }
                else
                {
                    sLogger.log(Level.INFO, "mbean.delete_temp_file_failed", localPath);
                }
            }
			OutputStream outStream = createOutputStream(localPath);
			mStreamTable.put(localPath, outStream);
            sLogger.log(Level.INFO, "mbean.begin_upload", localPath);
		}
        saveFile(localPath, byteChunk);
        return ( localPath );
    }
    
    private void saveFile(String filePath, ByteChunk aChunk) 
		throws ControlException
    {
		OutputStream sOut = null;
        try
        {
            sOut			= (OutputStream) mStreamTable.get(filePath);
			byte[] bytes	= aChunk.getBytes();
            sOut.write(bytes);
        }
        catch(Exception e)
        {
            throw new ControlException(e.getMessage());
        }
        finally
        {
            try
            {
				if (aChunk.isLast())
				{
					sOut.close();
					mStreamTable.remove(filePath);
                    sLogger.log(Level.INFO, "mbean.upload_done", filePath);
				}
            }
            catch(Exception fe)
            {
				throw new ControlException(fe.getMessage());
            }
        }
    }
	
	private OutputStream createOutputStream(String filePath) throws ControlException
	{
		OutputStream fOut = null;
		
		try
		{
			fOut = new FileOutputStream(filePath);
		}
		catch(Exception e)
		{
			try
			{
				if (fOut != null)
				{
					fOut.close();
				}
			}
			catch (Exception ce)
			{
                sLogger.log(Level.WARNING, "mbean.upload_failed", filePath);
			}
			
			throw new ControlException(e.getMessage());
		}
		return ( fOut );
	}

	public String[] listServerInstances()
	{
            ServerManager sm = ServerManager.instance();
            boolean countAdmin = false;
            String[] instanceNames = 
                    sm.getInstanceNames (countAdmin); //do not count admin itself.
            return ( instanceNames );
	}
        
  //***************************************************  
  //static MBean attributes and opeartions descriptions
    /** Implementation of <code>getMBeanInfo()</code>
     * Uses helper class <code>MBeanEasyConfig</code> to construct whole MBeanXXXInfo tree.
     * @return <code>MBeanInfo</code> objects containing full MBean description.
     */
  static String[] mAttrs = new String[0];
  static String[] mOpers = {
      "createServerInstance(String instanceName, com.sun.enterprise.admin.util.HostAndPort hostPort, String runAsUser, boolean autoStart), ACTION",
      "deleteServerInstance(String instanceName), ACTION",
      "shutdown(), ACTION",
      "shutdownAndExit(), ACTION",
      "restart(), ACTION",
//      "getProgress(com.sun.enterprise.admin.common.RequestID requestID), ACTION",
      "getVersion(), ACTION_INFO",
      "getFullVersion(), ACTION_INFO",
      "uploadToServer(com.sun.enterprise.admin.common.ByteChunk byteChunk), ACTION_INFO",
      "listServerInstances(), INFO",
      "prepareDownload(String filePath), ACTION",
      "downloadFile(int chunkIndex), ACTION",
      "stopDomain(boolean stopAdmin), ACTION",
      "getRunningInstanceNames(), ACTION",
      "listDomains(), INFO",
  };
	
	/** Implementation of <code>getMBeanInfo()</code>
	    Uses helper class <code>MBeanEasyConfig</code> to construct whole MBeanXXXInfo tree.
	    @return <code>MBeanInfo</code> objects containing full MBean description.
	*/
	public MBeanInfo getMBeanInfo()
	{
	    try 
	    {
	        return (new MBeanEasyConfig(getClass(), mAttrs, mOpers, null)).getMBeanInfo();
	    } 
	    catch(Exception e)
	    {
            sLogger.log(Level.WARNING, "mbean.mbeaninfo_failed", e);
            return null;
	    }
	}

    private final class Lock
    {
        private boolean inUse;

        private Lock()
        {
            inUse   = false;
        }

        private synchronized void acquire() 
            throws InterruptedException, IllegalAccessException
        {
            while (inUse)
            {
                wait();
            }
            inUse   = true;
        }

        private synchronized void release() throws IllegalAccessException
        {
            inUse   = false;
            notify();
        }

        private synchronized void attempt(long milliseconds) 
            throws InterruptedException, IllegalAccessException
        {
            if (inUse)
            {
                wait(milliseconds);
                if (inUse) //The lock is still in use. Give up.
                {
					String msg = localStrings.getString( "admin.server.core.mbean.config.another_thread_holding_lock" );
                    throw new IllegalAccessException( msg );
                }
            }
            inUse   = true;
        }
    }

    private static final class DownloadInfo
    {
        private File    downloadFile;
        private int     numChunks;
        private long    numBytesRead;
        private boolean isPrepared;

        private DownloadInfo()
        {
            reset();
        }

        private synchronized void reset()
        {
            downloadFile    = null;
            numChunks       = 0;
            numBytesRead    = 0;
            isPrepared      = false;
        }
    }

    private Lock            lock;
    private DownloadInfo    downloadInfo;


    /**
     */
    public Object prepareDownload(String filePath) throws ControlException
    {
        sLogger.log(Level.CONFIG, "mbean.prep_download", filePath);
        filePath = RelativePathResolver.resolvePath(filePath);
        File downloadFile = new File(filePath);
        if (!downloadFile.exists())
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.file_doesnot_exist", filePath );
            throw new ControlException( msg );
        }
        try
        {
            if (lock == null)
            {
                lock = new Lock();
            }
            lock.attempt(1000);
        }
        catch (Exception ie)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.could_not_acquire_lock", ie.toString() );
            throw new ControlException( msg );
        }
        if (downloadInfo == null)
        {
            downloadInfo = new DownloadInfo();
        }
        downloadInfo.downloadFile = downloadFile;
        long size   = downloadInfo.downloadFile.length();
        downloadInfo.numChunks   = (int)(size / ByteChunk.kChunkMaxSize);
        if (downloadInfo.numChunks * (long)ByteChunk.kChunkMaxSize < size)
        {
            downloadInfo.numChunks += 1;
        }
        /*
          Debug.println("File=" + downloadInfo.downloadFile.getAbsolutePath() + 
                      ", " +  "size=" + size + ", " + 
                      "Num chunks=" + downloadInfo.numChunks);
        */
        downloadInfo.isPrepared = true;
        return null;
    }

    /**
     */
    public ByteChunk downloadFile(int chunkIndex) throws ControlException
    {
        sLogger.log(Level.FINE, "mbean.begin_download");
        if (downloadInfo == null)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.call_preparedownload_first" );
            throw new ControlException( msg );
        }
        else if (!downloadInfo.isPrepared)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.call_preparedownload_first" );
            throw new ControlException( msg );
        }
        if ((chunkIndex >= downloadInfo.numChunks) || (chunkIndex < 0))
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.invalid_chunk_index" );
            throw new ControlException( msg );
		}
        RandomAccessFile    raf         = null;
        ByteChunk           byteChunk   = null;
        try
        {
            raf = new RandomAccessFile(downloadInfo.downloadFile, "r");
            byte[] bytes = new byte[ByteChunk.kChunkMaxSize];
            raf.seek(downloadInfo.numBytesRead);
            int actualBytesRead = raf.read(bytes, 0, ByteChunk.kChunkMaxSize);
            /*
            Debug.println("Read " + actualBytesRead + " from " + 
                          downloadInfo.numBytesRead);
            */
            if (actualBytesRead < bytes.length)
            {
                byte[] newBytes = new byte[actualBytesRead];
                for (int i = 0; i < newBytes.length; i++)
                {
                    newBytes[i] = bytes[i];
                }
                bytes = newBytes;
            }
            downloadInfo.numBytesRead += actualBytesRead;
            boolean isFirstChunk    = (chunkIndex == 0);
            boolean isLastChunk     = 
                (chunkIndex == (downloadInfo.numChunks - 1));
            sLogger.log(Level.FINEST, "chunkIndex = " + chunkIndex + 
                          " isFirstChunk = " + isFirstChunk + 
                          " isLastChunk = " + isLastChunk);
            byteChunk = new ByteChunk(bytes, 
                             downloadInfo.downloadFile.getAbsolutePath(), 
                             isFirstChunk, isLastChunk);
        }
        catch (Exception ioe)
        {
            sLogger.log(Level.FINE, "mbean.download_failed", ioe);
            downloadInfo.reset();
            try
            {
                lock.release();
            }
            catch (Exception e)
            {
                sLogger.log(Level.FINEST, "lock could not be released");
            }
            throw new ControlException(ioe.toString());
        }
        finally
        {
            if (raf != null)
            {
                try { raf.close(); }
                catch (IOException ioe) {}
            }
            if ((byteChunk != null) && (byteChunk.isLast()))
            {
                try
                {
                    downloadInfo.reset();
                    lock.release();
                }
                catch (Exception e)
                {
                    sLogger.log(Level.FINEST, "lock could not be released");
                }
            }
        }
        return byteChunk;
    }
    
    /**
     * validate instance name
     */
    private boolean isBadInstanceName(String instanceName)
    {
/*	try {
	    StaticTest.checkXMLName(instanceName);
	} catch(Exception e) {
	    //log the message.
            sLogger.log(Level.FINE, "Error: The instance name is not a valid xml string");
	    sLogger.log(Level.FINE, e.getMessage(), e);
	    return true;
  	}
	try {
	    StaticTest.checkObjectName(instanceName);
	} catch(Exception e) {
	    //log the message.
            sLogger.log(Level.FINE, "Error: The instance name is not a valid mbean object name");
	    sLogger.log(Level.FINE, e.getMessage(), e);
	    return true;
  	}
        return false;
 **/
        /*
            RAMAKANTH: Uncomment the above code. Returning false for timebeing
            to get startInstance working. RI Milestone 1. 03/20/2003.
         */
        return false;
    }
    
    /** 
        Stops the domain for which this is the ServerController.
        Depending on the parameter passed, the admin-server will
        itself be shutdown. All the instances will be stopped concurrently.
        Note that this is a best attempt.
        @throws ControlException in case there any error before starting the
        stop operation. Once the instances start stopping, if there any
        exceptions, they will be ignored. Thus this is a "fire and forget"
        kind of command, becase not much can be done to restore in case
        of any failure.
    */
    public void stopDomain(boolean stopAdmin) throws ControlException
    {
		String domainName = ServerManager.instance().getDomainName();
        if(stopAdmin)
        {
            sLogger.log(Level.INFO, "domain.stop_domain_admin", domainName);
        }
        else
        {
            sLogger.log(Level.INFO, "domain.stop_domain_noadmin", domainName);
        }
		// First stop all instances sequentially
        stopAllInstances(false);
        // Then if asked for, shutdown the admin server
        if(stopAdmin)
        {
            this.shutdown();
        }
    }
    
    /**
        Returns the array of DomainEntry elements. This does not
        require a Collection or Iterator, for these data structures
        are not Serializable at least because of the internal implementation.
        The DomainRegistry.iterator() returns an Inner Class instance,
        which implemets an Iterator and the same iterator can't be
        passed on, as it is not Serializable.
        @return an array of DomainEntry elements, an empty array if
        there are no domains.
		@throws ControlException in case the DomainRegistry fails
    */
    public DomainEntry[] listDomains() throws ControlException
    {
        DomainEntry[] domains = null;
		try
		{
			DomainRegistry domainRegistry = DomainRegistry.newInstance();
            int size = domainRegistry.size();
            domains = new DomainEntry[size];
			Iterator innerIter = domainRegistry.iterator();
            int i = 0;
            while (innerIter.hasNext())
            {
                domains[i++] = (DomainEntry)innerIter.next();
            }
            assert size == i;
		}
		catch (Exception e)
		{
			throw new ControlException(e.getMessage());
		}
        return ( domains );
    }
	
	/**
		This particular method is called when the stopDomain() is called
		from the UI. Note that it does not call the ManagedServerInstance
		MBean for stopping a particular instance. This is done for the fact
		that the ManagedServerInstanceMBean never caches the state of a 
		running instance. Note that depending upon the parameter passed,
		the instances will be stopped concurrently. A best effort will
		be made to stop the instances.
		@param boolean indicating the method (concurrent/serial) to stop instances.
	*/

	private void stopAllInstances(boolean atOnce)
	{
		String[] instanceIds = getRunningInstanceNames();
		
		if(!atOnce) /*only sequentially */
		{
			for (int i = 0; i < instanceIds.length; i++)
			{
                sLogger.log(Level.INFO, "mbean.stop_instance", instanceIds[i]);
                stopInstance(instanceIds[i]);
			}
	 	}
		else /*concurrently, not implemented as yet 07/21/2002 */
		{
			throw new UnsupportedOperationException("not impl yet");
		}
	} 
    
    /**
        Returns an array of running instances names. Internally the mbean
        representing that mbean is invoked to determine the status.
    */
    public String[] getRunningInstanceNames()
    {
        String[] allInstances   = ServerManager.instance().
                getInstanceNames(false);
        Vector runningInstances = new Vector();
        String[] runners        = null;
        int num                 = 0;
        
        for(int i = 0; i < allInstances.length ; i++)
        {
            if (isInstanceAlive(allInstances[i]))
            {
                runningInstances.add(allInstances[i]);
                num++;
                sLogger.log(Level.FINE, "mbean.instance_up", allInstances[i]);
            }
            else
            {
                sLogger.log(Level.FINE, "mbean.instance_down", allInstances[i]);
            }
        }
        if (num > 0)
        {
            runners = new String[num];
            runners = (String[]) runningInstances.toArray(runners);
        }
        else
        {
            runners = new String[0];
        }
        
        return ( runners );
    }
    
    private void stopInstance(String instanceId)
    {
        ObjectName objName = 
            ObjectNames.getServerInstanceObjectName(instanceId);
        final String kStopMethodName = "stop";
        final Object[] params   = new Integer[]{new Integer(600)};
        final String[] sign     = new String[]{"int"}; 
        try
        {
            invokeMBean(objName, kStopMethodName, params, sign);
        }
        catch(Exception e)
        {
            sLogger.log(Level.CONFIG, "failure in invoking MBean", e);
        }
    }
    
    private boolean isInstanceAlive(String instanceId)
    {
        ObjectName objName = 
            ObjectNames.getServerInstanceObjectName(instanceId);
        final String kStatusMethodName = "getStatus";
        try
        {
        /* method that takes in no params */
            final ServerInstanceStatus status = (ServerInstanceStatus)
                invokeMBean(objName, kStatusMethodName, null, null); 
            return ( status.isRunning() );
        }
        catch (Exception e)
        {
            sLogger.log(Level.CONFIG, "failure in invoking MBean", e);
            return false;
        }
    }
    
    private Object invokeMBean(ObjectName objName, String operationName,
            Object[] params, String[] signature) throws Exception
    {
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        return ( mbs.invoke(objName, operationName, params, signature) );
    }
	
	/**
		Returns the License information about the installed appserver.
		@return String which contains the license information.
	*/

	public String getLicenseInfo() 
	{
            String licenseInfo = localStrings.getString(
                    "admin.server.core.mbean.license_info");
            return licenseInfo;
	}
        
    /** Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object.  */
    protected Object getImplementingMBean() {
        return ( this );
    }
    
}

/**
    A class to stop this very process by giving triggering the
    signal externally. The signal is triggered by calling stop
    on the admin server. In short a process A forks a child
    process B, which in turn shuts process A down. The forked 
    process becomes zombie?
*/

class ShutdownThread implements Runnable
{
	private String mInstanceId;
    /*package*/ ShutdownThread(String instanceId)
    {
		mInstanceId = instanceId;
    }
    
    public void run()
    {
	ServerManager svrMgr	= ServerManager.instance();
	int testPort			= 9000;
	/* testPort is actually not used for stopping the instance,
		it's use is limited to construct the InstanceDefinition
		class
	*/
	try
	{
		InstanceDefinition instanceDef = new InstanceDefinition(mInstanceId,
			testPort);
		svrMgr.stopServerInstance (instanceDef);
	}
	catch (Exception e)
	{
		//This Exception can be ignored.
	}
    }
}
