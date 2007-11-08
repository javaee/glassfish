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


package com.sun.enterprise.admin.mbeans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Hashtable;
import java.lang.reflect.Method;

import javax.management.MBeanInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.DynamicMBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;

import com.sun.enterprise.admin.common.ByteChunk;
import com.sun.enterprise.admin.common.DownloadRequestInfo;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.mbean.meta.MBeanInfoBuilder;
import com.sun.enterprise.admin.common.exception.*;
import com.sun.enterprise.admin.common.MBeanServerFactory;
//i18n import
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.server.core.AdminService;


import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.admin.mbeanapi.ISystemServicesMBean;

import com.sun.enterprise.util.RelativePathResolver;

/** This mbean provides system services. Currently it provides uploading of a file
 * to server and downloading of client stubs in chunks.
 * @since 8.0
 */
public class SystemServicesMBean implements DynamicMBean, ISystemServicesMBean  {
    
    private static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    /** Map containing id and outputstream and name,value pair */    
    private Map	mStreamTable = new Hashtable();
    /** MBeanInfo for this MBean */    
    private MBeanInfo info = null;
    private static StringManager localStrings =  StringManager.getManager( SystemServicesMBean.class );
    

    /** Uploads the given ByteChunk to the Server. This will be saved
     * in an area defined by the server and then the complete path
     * where the file is saved is returned. This path can later be
     * used for deployment to a specific Server Instance. Argument may not be
     * null.
     * @param byteChunk the ByteChunk instance that contains bytes of a file.
     * @throws MBeanException jmx exception
     * @return String representing the complete path of the file.
     */
    
    public String uploadToServer(ByteChunk byteChunk) throws MBeanException {
        if (byteChunk == null) {
            throw new IllegalArgumentException();
        }
        String fileName  = byteChunk.getChunkedFileName();
        String id = byteChunk.getId();

        File localDir = new File (AdminService.getAdminService().getTempDirPath(),id);
        localDir.mkdirs();
        String localPath = FileUtils.safeGetCanonicalPath(localDir);

        // Begin EE: 4946914 - cluster deployment support

        // use target dir from byte chunk obj 
        String targetDirName = byteChunk.getTargetDir();
        if (targetDirName != null) {
            //File targetDir = new File(localPath, targetDirName);
            File targetDir = new File(targetDirName);

            // if relative to the instance root
            if (!targetDir.isAbsolute()) {
                String instanceRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

                targetDir = new File(instanceRoot, targetDirName);
            }
            else {
                String msg = localStrings.getString( "admin.mbeans.ssmb.absolute_dirs_not_allowed" );
                throw new MBeanException( new IllegalAccessException(), msg );

           }
            if (!targetDir.exists()) {
                targetDir.mkdir();
            }
            localPath = FileUtils.safeGetCanonicalPath(targetDir);
        }

        // End EE: 4946914 - cluster deployment support

        File uploadFile = new File(localPath, fileName);
        localPath = FileUtils.safeGetCanonicalPath(uploadFile);
        if (byteChunk.isFirst()) {
            if (uploadFile.exists()) {
                sLogger.log(Level.INFO, "mbean.temp_upload_file_exists", localPath);
                boolean couldDelete = uploadFile.delete();
                if (couldDelete) {
                    sLogger.log(Level.FINE, "mbean.delete_temp_file_ok", localPath);
                }
                else {
                    sLogger.log(Level.INFO, "mbean.delete_temp_file_failed", localPath);
                }
            }
            OutputStream outStream = createOutputStream(localPath);
            mStreamTable.put(id, outStream);
            sLogger.log(Level.INFO, "mbean.begin_upload", localPath);
        }
        saveFile(localPath, byteChunk);
        // check if this file is a zip file

        return ( localPath );
    }
    
    /**
     * Returns infomation for the download of the file - mainly size and 
     * total number of chunks for this request. This method supports 
     * multiple distributed clients.
     *
     * @param  filepath  path to the download request
     */
    public DownloadRequestInfo mcPrepareDownload(String filePath) 
            throws MBeanException 
    {

        filePath = RelativePathResolver.resolvePath(filePath);

        File dFile = new File(filePath);

        // the following is done for synchronization clients. 
        // If a request comes with a relative path, 
        // server install root is prefixed.
        if (!dFile.isAbsolute())
        {
            String instanceRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
            sLogger.log(Level.CONFIG, "mbean.prep_download", 
                        instanceRoot + filePath);
            dFile = new File(instanceRoot, filePath);
        } 
        else 
        {
            sLogger.log(Level.CONFIG, "mbean.prep_download", filePath);
        }

        // download file must exist
        if (!dFile.exists())
        {
            String msg = localStrings.getString( 
                "admin.mbeans.ssmb.file_doesnot_exist", 
                dFile.getAbsolutePath() );
            throw new MBeanException(new java.io.FileNotFoundException(), msg);
        }

        return new DownloadRequestInfo(dFile);
    }

    /** 
     * Prepares the download of the file
     *
     * @param filePath The path to the file to be downloaded
     * @return DownloadInfo contains info about the file to be downloaded.
     * @throws MBeanException when the mbean fails to acquire lock for 
     *         the file to be downloaded
     *
     * @deprecated replaced by {@link #mcPrepareDownload(java.lang.String)}
     */
    public Object prepareDownload(String filePath) throws MBeanException
    {
        //Temporary fix - Ramakanth
        filePath = RelativePathResolver.resolvePath(filePath);

        File downloadFile = new File(filePath);
        // the following is done for synchronization clients. 
        // If a request comes with a relative path, server install root is prefixed.
        if (!downloadFile.isAbsolute())
        {
            String instanceRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
            sLogger.log(Level.CONFIG, "mbean.prep_download", 
                        instanceRoot + filePath);
            downloadFile = new File(instanceRoot, filePath);
        }
        else {
            sLogger.log(Level.CONFIG, "mbean.prep_download", filePath);
        }
        if (!downloadFile.exists())
        {
            String msg = localStrings.getString( "admin.mbeans.ssmb.file_doesnot_exist", downloadFile.getAbsolutePath() );
            throw new MBeanException( new java.io.FileNotFoundException(), msg );
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
            throw new MBeanException( ie, msg );
        }
        if (downloadInfo == null)
        {
            downloadInfo = new DownloadInfo();
        }
        downloadInfo.downloadFile = downloadFile;
        final long size   = downloadInfo.downloadFile.length();
        final long chunkSize = (long)ByteChunk.kChunkMaxSize;
        long chunks = size / chunkSize;
        final long leftovers = size % chunkSize;
        
        if(leftovers > 0)
            chunks += 1;
        
        downloadInfo.numChunks = (int)chunks;
        /*
          Debug.println("File=" + downloadInfo.downloadFile.getAbsolutePath() + 
                      ", " +  "size=" + size + ", " + 
                      "Num chunks=" + downloadInfo.numChunks);
        */
        downloadInfo.isPrepared = true;
        return null;
    }

    /** 
     * Downloads the file in chunks
     * 
     * @param chunkInteger The index to the chunk array.
     * @return ByteChunk the byte chunk corresponding to the passed chunk index
     *
     * @throws MBeanException when exceptions occur, the exception 
     *         that caused will be in the exception chain.
     *
     * @deprecated replaced by {@link #mcDownloadFile(DownloadRequestInfo)}
     */
    public ByteChunk downloadFile(Integer chunkInteger) throws MBeanException
    {
		int chunkIndex = chunkInteger.intValue();
        sLogger.log(Level.FINE, "mbean.begin_download");
        if (downloadInfo == null)
        {
            String msg = localStrings.getString( "admin.mbeans.ssmb.call_preparedownload_first" );
            throw new MBeanException( new java.lang.IllegalStateException( msg ) );
        }
        else if (!downloadInfo.isPrepared)
        {
            String msg = localStrings.getString( "admin.mbeans.ssmb.call_preparedownload_first" );
            throw new MBeanException( new java.lang.IllegalStateException( msg ) );
        }
        // if it is a file of 0, return null bytes
        ByteChunk           byteChunk   = null;
        if (downloadInfo.downloadFile.length() == 0 ) {
            byte[] bytes = new byte[ByteChunk.kChunkMinSize];
            byteChunk = new ByteChunk(bytes, 
                             downloadInfo.downloadFile.getAbsolutePath(), 
                             true, true);
            unlockAndReset();
            return byteChunk;
        }

        if ((chunkIndex >= downloadInfo.numChunks) || (chunkIndex < 0))
        {
            String msg = localStrings.getString( "admin.mbeans.ssmb.invalid_chunk_index" );
            unlockAndReset();
            throw new MBeanException( new java.lang.IllegalStateException( msg ) );
        }
        RandomAccessFile    raf         = null;
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
                System.arraycopy(bytes, 0, newBytes, 0, newBytes.length);
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
        catch (IOException ioe)
        {
            sLogger.log(Level.FINE, "mbean.download_failed", ioe);
            unlockAndReset();
            throw new MBeanException(ioe);
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
                unlockAndReset();
            }
        }
        return byteChunk;
    }
    
    /** 
     * Downloads the file in chunks. This method supports multiple distributed
     * clients.
     * 
     * @param info download request information
     *
     * @return the byte chunk corresponding to the current chunk index
     *
     * @throws MBeanException when exceptions occur, the exception 
     *         that caused will be in the exception chain.
     */
    public DownloadRequestInfo mcDownloadFile(
            DownloadRequestInfo info) throws MBeanException
    {
        sLogger.log(Level.FINE, "mbean.begin_download");

        // verifies the information for this download request
        if (info == null)
        {
            String msg = localStrings.getString( 
                "admin.mbeans.ssmb.call_preparedownload_first");
            throw new MBeanException(new java.lang.IllegalStateException(msg));
        }
        else if (!info.isPrepared())
        {
            String msg = localStrings.getString( 
                "admin.mbeans.ssmb.call_preparedownload_first");
            throw new MBeanException(new java.lang.IllegalStateException(msg));
        }

        // download file
        File targetFile = new File(info.getDownloadFilePath());

        // if it is a file of 0, return null bytes
        ByteChunk byteChunk  = null;
        if (targetFile.length() == 0) 
        {
            byte[] bytes = new byte[ByteChunk.kChunkMinSize];
            String fPath = info.getDownloadFilePath();
            byteChunk = new ByteChunk(bytes, fPath, true, true, fPath, 0);
            info.setChunk(byteChunk);

            return info;
        }

        // verifies that chunk index is valid
        if ( (info.getChunkIndex() >= info.getNumberOfChunks()) 
                || (info.getChunkIndex() < 0) )
        {
            String msg = localStrings.getString(
                "admin.mbeans.ssmb.invalid_chunk_index" );
            throw new MBeanException(new java.lang.IllegalStateException(msg));
        }

        // reads the chunk into a byte chunk object
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(targetFile, "r");
            byte[] bytes = new byte[ByteChunk.kChunkMaxSize];
            raf.seek(info.getNumberOfBytesSent());

            int actualBytesRead = raf.read(bytes, 0, ByteChunk.kChunkMaxSize);
            if (actualBytesRead < bytes.length)
            {
                byte[] newBytes = new byte[actualBytesRead];
                System.arraycopy(bytes, 0, newBytes, 0, newBytes.length);
                bytes = newBytes;
            }

            // increments the counter
            info.incrementNumberOfBytesSent(bytes.length);

            String path = info.getDownloadFilePath();
            byteChunk = new ByteChunk(bytes, 
                             path, info.isFirstChunk(), info.isLastChunk(),
                             path, targetFile.length());
        }
        catch (IOException ioe)
        {
            sLogger.log(Level.FINE, "mbean.download_failed", ioe);
            throw new MBeanException(ioe);
        }
        finally
        {
            if (raf != null)
            {
                try { raf.close(); }
                catch (IOException ioe) {}
            }
        }
        info.setChunk(byteChunk);

        return info;
    }
    
    // The following function releases the lock and reset the downloadinfo
    // this clean up is needed in case of exceptions
    private void unlockAndReset()
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
    
    /** Returns the location of the client stub jar that is generated by EJBC
     * during deployment of the given application.
     * @param appName application or module name by which an application
     * or an EJB module has been deployed.
     * @return Returns the absolute path to the client-stub-jar file.
     * @throws MBeanException The wrapper exception for any exceptions. The getCause will return the causing
     * exception.
     */
    public String getClientStubJarLocation(String appName)
        throws MBeanException
    {
        String clientJarLocation = null;
        
        try
        {
            final com.sun.enterprise.server.ServerContext sc  = com.sun.enterprise.server.ApplicationServer.getServerContext();
            InstanceEnvironment iEnv = sc.getInstanceEnvironment();
            ObjectName appsObjName = new ObjectName("com.sun.appserv:type=applications,category=config");
            String appLocation = null;
            ObjectName appObjName = null;
            try {
                appObjName = (ObjectName)getMBeanServer().invoke(appsObjName, "getJ2eeApplicationByName", new Object[] {appName}, new String[] {"java.lang.String"});
                AppsManager appsManager = new AppsManager(iEnv);
                appLocation = appsManager.getGeneratedXMLLocation(appName);
            } catch(Exception ee) {
                // no application by this name
            }
            if(appObjName == null) {
                try {
                    appObjName = (ObjectName)getMBeanServer().invoke(appsObjName, "getEjbModuleByName", new Object[] {appName}, new String[] {"java.lang.String"});
                    EjbModulesManager ejbManager = new EjbModulesManager(iEnv);
                    appLocation = ejbManager.getGeneratedXMLLocation(appName);
                } catch (Exception ejbe) {
                    // no ejb module by this name
                }
                if(appObjName == null) {
                    try {
                        appObjName = (ObjectName)getMBeanServer().invoke(appsObjName, "getAppclientModuleByName", new Object[] {appName}, new String[] {"java.lang.String" });
                        AppclientModulesManager appClientManager = 
                            new AppclientModulesManager(iEnv);
                        appLocation = 
                            appClientManager.getGeneratedXMLLocation(appName);
                    } catch(Exception ace) {
                        // no appclient module by this name
                    }
                }
                // the app name matches none of the applicable modules
                // throw an exception here
                if(appObjName == null) {
                    throw new IllegalArgumentException(localStrings.getString( "admin.mbeans.ssmb.invalid_appname", appName ));
                }
            }
 
            // for upgrade scenario, we fall back to the original location
            if (appLocation == null || 
                !FileUtils.safeIsDirectory(appLocation)) {
                appLocation = (String)getMBeanServer().getAttribute(appObjName, 
                    "location");
            }
            clientJarLocation = appLocation + java.io.File.separator +
                                DeploymentServiceUtils.getClientJarPath(appName);
            
            sLogger.log(Level.INFO, "mbean.cl_jar_loc", clientJarLocation);
        }
        catch (Exception e)
        {
            sLogger.log(Level.WARNING, e.toString(), e);
            throw new MBeanException(e);
        }
        return clientJarLocation;
    }
    
    protected MBeanServer getMBeanServer()
    {
        return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
    }
    
    protected String getDomainName() {
        return com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getDomainName();
    }
    
    
    public Object getAttribute(String str) throws javax.management.AttributeNotFoundException, javax.management.MBeanException, javax.management.ReflectionException {
        throw new AttributeNotFoundException(str);
    }
    
    public javax.management.AttributeList getAttributes(String[] str) {
        return new AttributeList();
    }
    
    /** Even if a particular server's configuration is in server's configuration (domain.xml), the
     * actual values that are taken by certain Java System Properties are always available in a server's runtime alone.
     * For example, only running DAS knows what absolute location on the disk is, where DAS is installed, only 
     * running instance "server1" knows what absolute location on the disk is, where "server1"'s configuration 
     * is stored and so on and so forth. 
     * <P>
     * This method is extremely useful in finding out the values of Java System Properties in the runtime of a process, from
     * runtime of other processes.
     * <P>
     * It is recommended that remote callers use the "names" of the Java System Properties from ${link SystemPropertyConstants}
     * class (rather than hardcoding), because for most of the properties, what changes is values, not names.
     * @param name name of the Java System Property
     * @retun the value of the given property in the context of host server that is usually (but not necessarily) remote
     * to the caller.
     */
    public String getHostServerSystemPropertyValue(final String name) {
        return ( System.getProperty(name) );
    }
    
    public javax.management.MBeanInfo getMBeanInfo() {
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[ 1 ];
        MBeanNotificationInfo[] notifications = null;
        MBeanAttributeInfo[] attributes = null;
        MBeanOperationInfo[] operations = new MBeanOperationInfo[ 4 ];
        
        try{
            MBeanConstructorInfo cinfo = new MBeanConstructorInfo(
            "Main constructor",
            this.getClass().getConstructor() );
            constructors[ 0 ] = cinfo;
        }catch(Exception e){
                sLogger.log(Level.WARNING, e.getLocalizedMessage() );
        }
        
        MBeanParameterInfo[] parameters = new MBeanParameterInfo[ 1 ];
        parameters[ 0 ] = new MBeanParameterInfo( "byteChunk", ByteChunk.class.getName(), 
                                            "byte chunk" );
        operations[ 0 ] = new MBeanOperationInfo( "uploadToServer", "upload file to admin server", 
                                            parameters, String.class.getName(), MBeanOperationInfo.ACTION );

        MBeanParameterInfo[] parameters1 = new MBeanParameterInfo[ 1 ];
        parameters1[ 0 ] = new MBeanParameterInfo( "fileName", String.class.getName(), 
                                            "file name" );
        operations[ 1 ] = new MBeanOperationInfo( "prepareDownload", "prepare file download from admin server", 
                                            parameters1, Object.class.getName(), MBeanOperationInfo.ACTION );

        MBeanParameterInfo[] parameters2 = new MBeanParameterInfo[ 1 ];
        parameters2[ 0 ] = new MBeanParameterInfo( "chunkIndex", "java.lang.Integer", 
                                            "chunk index" );
        operations[ 2 ] = new MBeanOperationInfo( "downloadFile", "download file from admin server", 
                                            parameters2, ByteChunk.class.getName(), MBeanOperationInfo.ACTION );

        MBeanParameterInfo[] parameters3 = new MBeanParameterInfo[ 1 ];
        parameters3[ 0 ] = new MBeanParameterInfo( "appName", String.class.getName(), 
                                            "application name" );
        operations[ 3 ] = new MBeanOperationInfo( "getClientStubJarLocation", "get stub file location from admin server", 
                                            parameters3, String.class.getName(), MBeanOperationInfo.ACTION );

        return new MBeanInfo( this.getClass().getName(), 
                                        "Manages System Services" , attributes, constructors, operations, notifications );
    }
    
    public Object invoke(String str, Object[] args, String[] sig) throws javax.management.MBeanException, javax.management.ReflectionException {
        try {
            String methodName = str;
            Class types[] = new Class[ sig.length ];
            for( int i=0; i<types.length; i++ )
                types[i] = Class.forName(sig[i]);
            
            Method m = this.getClass().getMethod(methodName, types);
            return m.invoke( this, args );
        } catch ( Exception e ) {
            sLogger.log(Level.WARNING,e.toString(),e);
            throw new MBeanException( e );
        }
    }
    
    public void setAttribute(javax.management.Attribute attribute) throws javax.management.AttributeNotFoundException, javax.management.InvalidAttributeValueException, javax.management.MBeanException, javax.management.ReflectionException {
        throw new AttributeNotFoundException();
    }
    
    public javax.management.AttributeList setAttributes(javax.management.AttributeList attributeList) {
        return new AttributeList();
    }
    
    /** writes the bytes to the output stream got by lookup up the Map with the id as
     * the key.
     */    
    private void saveFile(String filePath, ByteChunk aChunk)
    throws MBeanException {
        OutputStream sOut = null;
        String id = aChunk.getId();
        try {
            sOut			= (OutputStream) mStreamTable.get(id);
            byte[] bytes	= aChunk.getBytes();
            sOut.write(bytes);
        }
        catch(Exception e) {
            sLogger.log(Level.WARNING,e.getLocalizedMessage(),e);
            throw new MBeanException(e);
        }
        finally {
            try {
                if (aChunk.isLast()) {
                    sOut.close();
                    mStreamTable.remove(id);
                    if(aChunk.getTotalFileSize()>0) {
                        if ( new File(filePath).length() != aChunk.getTotalFileSize() ) {
                            sLogger.log(Level.WARNING, "mbean.upload_failed", filePath);
                            String msg = localStrings.getString( "admin.mbeans.upload_failed", filePath );
                            throw new MBeanException( new java.lang.RuntimeException (msg) );
                        }
                    } else {
                        sLogger.log(Level.INFO, "mbean.filesize_notverified", filePath);
                    }
                    sLogger.log(Level.INFO, "mbean.upload_done", filePath);
                }
            }
            catch(Exception fe) {
                sLogger.log(Level.WARNING,fe.toString(),fe);
                throw new MBeanException(fe);
            }
        }
    }
    
    /** creates the outputstream and puts it in the Map. */    
    private OutputStream createOutputStream(String filePath) throws MBeanException {
        OutputStream fOut = null;
        
        try {
            fOut = new FileOutputStream(filePath);
        }
        catch(Exception e) {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            }
            catch (Exception ce) {
                sLogger.log(Level.WARNING, "mbean.upload_failed", filePath);
            }
            
            throw new MBeanException(e);
        }
        return ( fOut );
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
    
    
}

