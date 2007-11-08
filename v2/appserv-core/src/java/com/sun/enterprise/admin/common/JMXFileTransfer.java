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

package com.sun.enterprise.admin.common;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.MBeanServerConnection;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.StringValidator;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.admin.common.exception.*;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;

/** Utility class to transfer a given file from and to the server in chunks using jmx
 * with s1ashttp protocol.
 */
public class JMXFileTransfer implements FileTransfer {
    
    /** Provider package string for s1ashttp protocol */    
    public static final String S1ASHTTP_PROVIDER_PACKAGES = "com.sun.enterprise.admin.jmx.remote.protocol";
    private static final String UPLOAD_OPERATION        = "uploadToServer";
    private static final String GET_STUB_FILE_LOCATION  = "getClientStubJarLocation";
    private static final String GET_WSDL_FILE_LOCATION  = "getWsdlFileLocation";
    private static final String PREPARE_DOWNLOAD        = "prepareDownload";
    private static final String MCPREPARE_DOWNLOAD      = "mcPrepareDownload";
    private static final String DOWNLOAD_FILE           = "downloadFile"; 
    private static final String MCDOWNLOAD_FILE         = "mcDownloadFile"; 
    
    private JMXServiceURL url ;
    private String user;
    private String password;
    private MBeanServerConnection mbsc;
    private String targetServer = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
    private static final StringManager _localStrMgr = 
            StringManager.getManager(JMXFileTransfer.class);

    /** uploads the given file to the server running at the url with
     * authentication. Assumes jmx-remote with s1ashttp protocol.
     * @param url the jmx url to the server in the format <CODE>service:jmx:s1ashttp://host:port</CODE>
     * @param user user name to authenticate with the server
     * @param password the password for the user
     * @throws IOException for connectionrelated exceptions
     */    
    public JMXFileTransfer(JMXServiceURL url, String user, String password) throws IOException {
        this.url = url;
        this.user = user;
        this.password = password;
        setConnection();
    }
       
    /** uploads the given file to the server running at the url with
     * authentication. Assumes jmx-remote with s1ashttp protocol.
     * @param url the jmx url to the server in the format <CODE>service:jmx:s1ashttp://host:port</CODE>
     * @param user user name to authenticate with the server
     * @param password the password for the user
     * @param doConnect true-- connect now. false-- connect when setConnection is called explicitly
     * @throws IOException for connectionrelated exceptions
     */    
    public JMXFileTransfer(JMXServiceURL url, String user, String password, boolean doConnect) throws IOException {
        this.url = url;
        this.user = user;
        this.password = password;
        if (doConnect == true)
            setConnection();
    }
    
    /** uploads the given file to the server running at the host:port with
     * authentication. Assumes jmx-remote with s1ashttp protocol.
     * @param mbsc the connection to the mbean server
     * @throws IOException for connection related exceptions
     */    
    public JMXFileTransfer(MBeanServerConnection mbsc) throws IOException{
        this.mbsc = mbsc;
    }    
    
    /**
     *  This method returns whether JMXFileTransfer a valid connection
     *  to system services MBean
     *
     *  @return boolean true- if there is connection, false, if not
     */
    public MBeanServerConnection getMBeanServerConnection () {
        return mbsc;
    }
    
    /**
     *  This method set the MBean Server connection
     *  
     *  @param  mbsc The new MBean server connection value
     */
     public void setMBeanServerConnection(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
     }
     
    /**
     *  This method can be used to set the MBean server connection explicitly,
     *  instead of setting connection from constructor.
     *
     *  This method is written to be used by SynchornizationClient.
     *
     */
    public void setConnection() throws IOException {
        final Map env = new  HashMap();
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,  S1ASHTTP_PROVIDER_PACKAGES);
        env.put(DefaultConfiguration.ADMIN_USER_ENV_PROPERTY_NAME, user);
        env.put(DefaultConfiguration.ADMIN_PASSWORD_ENV_PROPERTY_NAME, password);
        env.put(DefaultConfiguration.HTTP_AUTH_PROPERTY_NAME, DefaultConfiguration.DEFAULT_HTTP_AUTH_SCHEME);
        env.put(JMXConnector.CREDENTIALS, new String[] {user, password});
        JMXConnector conn = JMXConnectorFactory.connect(url, env);
        mbsc = conn.getMBeanServerConnection();
    }
    
    /** Sets the target server to the server where the transfer should occur from/to.
     * May not be null.
     * @param s String representing the name of server instance in a given domain
     * that is the target for transfer (to/from). May not be null.
     */ 
    public void setTargetServer(final String s) {
        this.targetServer = s;
    }
    // Begin EE: 4946914 - cluster deployment support
    public String uploadFile(String filePath) throws IOException {

        return uploadFile(filePath, null);
    }
    // End EE: 4946914 - cluster deployment support

    public String uploadFile (String filePath, String targetDir)
            throws IOException {
            
        final File f = new File(filePath);
        if(!f.exists() || !f.canRead())
            throw new FileNotFoundException(filePath);
       
       return uploadFile(f, targetDir);
    }
    
    public String uploadFile(File f, String targetDir ) 
            throws IOException {


        if( !f.isFile() || (mbsc==null))
            throw new IllegalArgumentException();
        
        String remoteLocation = null;

        ObjectName mbeanName = ObjectNames.getPerInstanceSystemServicesObjectName(targetServer);
        String      operationName   = UPLOAD_OPERATION;

        final RandomAccessFile file       = new RandomAccessFile(f, "r");
        byte[]      bytes           = new byte[ByteChunk.kChunkMaxSize];
        int         bytesRead       = 0;
        long        totalBytesRead  = 0;
        boolean     isFirstChunk    = true;
        boolean     isLastChunk     = false;
        final long        fileSize        = file.length();
        Random      random          = new Random();
        final String id = String.valueOf(random.nextInt(9999999));

        try {
            int         index           = 0;
            while ((bytesRead = file.read(bytes)) != -1)
            {
                totalBytesRead += bytesRead;
                if (bytesRead < bytes.length)
                {
                    byte[] realBytes = new byte[bytesRead];
                    for (int i = 0; i < bytesRead; i++)
                    {
                        realBytes[i] = bytes[i];
                    }
                    bytes = realBytes;
                }
                if (totalBytesRead == fileSize)
                {
                    isLastChunk = true;
                }
                ByteChunk aChunk = new ByteChunk(bytes, f.getName(),
                                                 isFirstChunk, isLastChunk, id, fileSize);
                // Begin EE: 4946914 - cluster deployment support
                aChunk.setTargetDir(targetDir);
                // End EE: 4946914 - cluster deployment support

                ParamInfo paramInfo = new ParamInfo(operationName, aChunk);
                try {
                    remoteLocation = (String)mbsc.invoke(mbeanName, 
                                            paramInfo.getOperationName(),
                                            paramInfo.getParams(),
                                            paramInfo.getSignature());
                }catch(Exception e){
                    e.printStackTrace();
                    throw (IOException)(new IOException(e.getLocalizedMessage()).initCause(e));
                }
                index++;
                isFirstChunk = false;
            }
        }
		catch(Exception e) {
            e.printStackTrace();
			throw new IOException(e.getClass().getName() + ":" + e.getMessage());
		}
		finally  {
			try {
				file.close();
			}
			catch(Exception e){}
       }
 
        return remoteLocation;
    }
    
    
    public String downloadClientStubs(String  appName, String  destDir) 
        throws IOException
    {

        if (mbsc == null )
            throw new IllegalArgumentException("MBean Server connection not set");
     
        ArgChecker.checkValid(appName, "appName", //noi18n
                              StringValidator.getInstance());
        String msg="";
        File f = new File(destDir);
        if (!f.exists())
        {
			//String msg = localizedStrMgr.getString( "admin.servermodel.controller.desstdir_does_not_exist", destDir );
            throw new FileNotFoundException( msg );
        }
        else if (!f.isDirectory())
        {
			//String msg = localizedStrMgr.getString( "admin.servermodel.controller.desstdir_is_not_directory", destDir );
            throw new IllegalArgumentException( msg );
        }
        else if (!f.canWrite())
        {
			//String msg = localizedStrMgr.getString( "admin.servermodel.controller.cannot_write_to_destdir", destDir );
            throw new IllegalArgumentException( msg );
        }

        Object[] params     = new Object[] {appName};
        String[] signature  = new String[] {"java.lang.String"};
        ObjectName mbeanName = ObjectNames.getPerInstanceSystemServicesObjectName(targetServer);
        try {
            String filePath = (String) mbsc.invoke(mbeanName, GET_STUB_FILE_LOCATION, params, signature);
            //String fileName = appName + AdminConstants.CLIENT_JAR;
            downloadFile(filePath, destDir);
            String exportedFileLocation = new File(destDir, new File(filePath).getName()).getAbsolutePath();
            return exportedFileLocation;
        }catch(Exception e) {
            throw (IOException)(new IOException(e.getLocalizedMessage()).initCause(e));
        }
    }
    
     /**
     * Exports a wsdl file that is created by the server during deployment time
     * 
     * @param appName the name of the application of EJB/WAR module that is 
     * deployed to the server
     * @param moduleName if application this is the 
     * module uri within the application, null otherwise
     * @param wsdlFileName the requested wsdl uri
     * @param destDir the absolute path to the directory where the wsdl file
     * should be copied
     * @throws IOException
     */
    public String exportWsdlFile(String appName,
                                 String moduleName, 
                                 String wsdlFileUri,
                                 String destDir) 
        throws IOException
    {
        if (mbsc == null )
            throw new IllegalArgumentException("MBean Server connection not set");
 
        ArgChecker.checkValid(appName, "appName", //noi18n
                              StringValidator.getInstance());        
        File f = new File(destDir);
        String msg="";
        String exportedFileLocation;
        f.mkdirs();
        if (!f.exists()) {
            throw new FileNotFoundException( f.toString() );
        }
        else if (!f.isDirectory())
        {    
            throw new IllegalArgumentException( f.toString() );
        }
        else if (!f.canWrite())
        {            
            throw new IllegalArgumentException( f.toString() );
        }
        String filePath = null;       
        
        
        Object[] params     = new Object[] {appName, moduleName, wsdlFileUri};
        String[] signature  = new String[] {"java.lang.String", "java.lang.String","java.lang.String"};
        ObjectName mbeanName = ObjectNames.getPerInstanceSystemServicesObjectName(targetServer);
        try{
            filePath = (String) mbsc.invoke(mbeanName, GET_WSDL_FILE_LOCATION, params, signature);
            if (wsdlFileUri.lastIndexOf('/')!=-1) {
                String wsdlDir ;
                if (wsdlFileUri.startsWith("META-INF/wsdl")) {
                    wsdlDir = "META-INF/wsdl/";
                } else {
                    wsdlDir = "WEB-INF/wsdl/";
                }
                File absolutePath;
                if (wsdlDir.length()<wsdlFileUri.lastIndexOf('/')) {                
                    String intermediateDirs = wsdlFileUri.substring(wsdlDir.length(), wsdlFileUri.lastIndexOf('/'));
                    absolutePath = new File(destDir, intermediateDirs);
                } else {
                absolutePath = new File(destDir);
                }
                absolutePath.mkdirs();
                destDir = absolutePath.getAbsolutePath();
                wsdlFileUri = wsdlFileUri.substring(wsdlFileUri.lastIndexOf('/')+1);
            }
            downloadFile(filePath, destDir, wsdlFileUri);
            exportedFileLocation = new File(destDir, wsdlFileUri).getAbsolutePath();
        }catch(Exception e) {
            throw (IOException)(new IOException(e.getLocalizedMessage()).initCause(e));
        }
        
        return exportedFileLocation;  
    }
    
    public String downloadFile(String filePath, String destinationDirPath) 
        throws IOException
    {
        return downloadFile(filePath, destinationDirPath, null) ;
    }
    
    public String downloadFile(String filePath, String destinationDirPath, String appName) 
        throws IOException
    {
        File destPath = null;
        
        if(appName == null)
            destPath = new File(destinationDirPath, new File(filePath).getName());
        else
            destPath = new File(destinationDirPath,appName);

        return downloadFile(filePath, destPath);
    }
    
    public String downloadFile(String filePath,File destPath ) 
        throws IOException
    {
        if (mbsc == null )
            throw new IllegalArgumentException("MBean Server connection not set");
 
         
        String msg="";
        final File destDir = destPath.getParentFile();
        if (!destDir.exists())
        {
            throw new FileNotFoundException(destDir.getName());
        }
        FileOutputStream fos = null;
        try
        {
            ObjectName mbeanName = ObjectNames.getPerInstanceSystemServicesObjectName(targetServer);
            mbsc.invoke(mbeanName, PREPARE_DOWNLOAD, 
                              new Object[] {filePath}, 
                              new String[] {"java.lang.String"});
            fos = new FileOutputStream(destPath);
            boolean lastChunk   = false;
            int     chunkIndex  = 0;
            int     curSize = 0;
            long     totalFileSize = 0;
            while (!lastChunk)
            {
                Object[]    params = new Object[] {new Integer(chunkIndex)};
                String[]    signature   = new String[] {"java.lang.Integer"};
                ByteChunk chunk = (ByteChunk)mbsc.invoke(mbeanName,
                                        DOWNLOAD_FILE,
                                        params,
                                        signature);
                ++chunkIndex;
                lastChunk = chunk.isLast();
                byte[] bytes = chunk.getBytes();
                fos.write(bytes, 0, bytes.length);
                curSize =+ bytes.length;
                totalFileSize = chunk.getTotalFileSize();
            }

            if ( curSize < totalFileSize)
                throw new IOException("Checksum error, download incomplete, total file size is " +  totalFileSize + " only gotton " + curSize + " bytes.");
        } catch (Exception e) {
            throw (IOException)(new IOException(e.getLocalizedMessage()).initCause(e));           
        }
        finally
        {
            if (fos != null)
            {
                try { fos.close(); }
                catch (Exception e) {}
            }

        }
        return destPath.getPath();
    }
 
    public synchronized String mcDownloadFile(String filePath, File destPath) 
        throws IOException
    {
        // verifies that mbean server connection is set
        if (mbsc == null ) 
        {
            String msg = _localStrMgr.getString("admin.common.nombsc");
            throw new IllegalArgumentException(msg);
        }
         
        // verifies that the parent dir exists
        final File destDir = destPath.getParentFile();
        if (!destDir.exists())
        {
            throw new FileNotFoundException(destDir.getName());
        }

        FileOutputStream fos = null;
        try
        {
            ObjectName mbeanName = 
              ObjectNames.getPerInstanceSystemServicesObjectName(targetServer);

            DownloadRequestInfo info = (DownloadRequestInfo) 
                    mbsc.invoke(mbeanName, MCPREPARE_DOWNLOAD, 
                              new Object[] {filePath}, 
                              new String[] {"java.lang.String"});

            fos = new FileOutputStream(destPath);
            boolean lastChunk      = false;
            int     chunkIndex     = 0;
            long    curSize        = 0;
            long    totalFileSize  = info.getTotalFileSize();

            while (!lastChunk)
            {
                Object[] params = new Object[] {info};
                String[] signature = new String[] {
                    "com.sun.enterprise.admin.common.DownloadRequestInfo"};

                info = (DownloadRequestInfo) mbsc.invoke(mbeanName,
                                    MCDOWNLOAD_FILE, params, signature);

                if (chunkIndex != info.getChunkIndex()) {
                    String msg = _localStrMgr.getString("admin.common.chunkidx",
                                    Integer.toString(chunkIndex), 
                                    Integer.toString(info.getChunkIndex()));
                    throw new IOException(msg);
                }

                ByteChunk chunk = info.getChunk();

                // sets the chunk index for next iteration
                ++chunkIndex;
                info.setChunkIndex(chunkIndex);

                lastChunk = chunk.isLast();

                byte[] bytes = chunk.getBytes();
                fos.write(bytes, 0, bytes.length);

                // increment the current size 
                curSize = (long) (curSize + bytes.length);

                // total size of this download
                long tFileSize = chunk.getTotalFileSize();
                if (tFileSize != totalFileSize) {
                    String msg = _localStrMgr.getString("admin.common.totalfs",
                                    Long.toString(totalFileSize), 
                                    Long.toString(tFileSize));
                    throw new IOException(msg);
                }

                // verifies current number of received bytes
                if (curSize != info.getNumberOfBytesSent()) 
                {
                    String msg = _localStrMgr.getString("admin.common.curfs",
                                    Long.toString(curSize), 
                                    Long.toString(info.getNumberOfBytesSent()));
                    throw new IOException(msg);
                }
            }

            // verifies total number of received bytes
            if ( curSize < totalFileSize)
            {
                String msg=_localStrMgr.getString("admin.common.checksumerror",
                                Long.toString(totalFileSize), 
                                Long.toString(curSize));
                throw new IOException(msg);
            }

        } 
        catch (Exception e) 
        {
            throw (IOException)(new IOException(e.getLocalizedMessage())
                                .initCause(e));           
        }
        finally
        {
            if (fos != null)
            {
                try { fos.close(); }
                catch (Exception e) {}
            }

        }

        return destPath.getPath();
    }
}
