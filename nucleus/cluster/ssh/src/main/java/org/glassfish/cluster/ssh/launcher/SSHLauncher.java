/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.cluster.ssh.launcher;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.RelativePathResolver;
import org.jvnet.hk2.annotations.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rajiv Mordani, Krishna Deepak
 */


@Service(name="SSHLauncher")
@PerLookup
public class SSHLauncher {

    private static final String SSH_DIR = ".ssh" + File.separator;
    private static final String AUTH_KEY_FILE = "authorized_keys";
    private static final int DEFAULT_TIMEOUT_MSEC = 120000; // 2 minutes
    private static final String SSH_KEYGEN = "ssh-keygen";
    private static final char LINE_SEP = System.getProperty("line.separator").charAt(0);

    /**
     * The host name which to connect to via ssh
     */
    private String host;

    /**
     * The port on which the ssh daemon is running
     */
    private int port;

    /**
     * The user name to use for authenticating with the ssh daemon
     */
    private String userName;

    /**
     * The name of private key file.
     */
    private String keyFile;

    /**
     * The connection object that represents the connection to the host
     * via ssh
     */
    private Session session;

    private String authType;

    private String keyPassPhrase;

    private String knownHostsLocation;

    private Logger logger;

    private String password;

    // Password before it has been expanded. Used for debugging.
    private String rawPassword = null;
    private String rawKeyPassPhrase = null;

    public void init(Logger logger) {
        this.logger = logger;
    }

    /**
     * Initialize the SSHLauncher use a Node config object
     * @param node
     * @param logger 
     */
    public void init(Node node, Logger logger) {
        this.logger = logger;
        int port;
        String host;

        SshConnector connector = node.getSshConnector();

        host = connector.getSshHost();
        if (SSHUtil.checkString(host) != null) {
            this.host = host;
        } else {
            this.host = node.getNodeHost();
        }
        if (logger.isLoggable(Level.FINE)) {
	    logger.fine("Connecting to host " + host); 
        }

        //XXX Why do we need this again?  This is already done above and set to host
        String sshHost = connector.getSshHost();
        if (sshHost != null)
            this.host = sshHost;
        
        SshAuth sshAuth = connector.getSshAuth();
        String userName = null;
        if (sshAuth != null) {
            userName = sshAuth.getUserName();
            this.keyFile = sshAuth.getKeyfile();
            this.rawPassword = sshAuth.getPassword();
            this.rawKeyPassPhrase = sshAuth.getKeyPassphrase();
        }
        try {
            port = Integer.parseInt(connector.getSshPort());
        } catch(NumberFormatException nfe) {
            port = 22;
        }

        init(userName, this.host, port, this.rawPassword, keyFile,
                this.rawKeyPassPhrase, logger);
    }

    /**
     * Initialize the SSHLauncher using a private key file
     * 
     * @param userName
     * @param host
     * @param port
     * @param password
     * @param keyFile
     * @param keyPassPhrase
     * @param logger 
     */
    public void init(String userName, String host, int port, String password, String keyFile, String keyPassPhrase, Logger logger) {
        this.port = port == 0 ? 22 : port;

        this.host = host;
        this.keyFile = (keyFile == null) ? SSHUtil.getExistingKeyFile(): keyFile;
        this.logger = logger;

        this.userName = SSHUtil.checkString(userName) == null ?
                    System.getProperty("user.name") : userName;

        
        this.rawPassword = password;
        this.password = expandPasswordAlias(password);
        this.rawKeyPassPhrase = keyPassPhrase;
        this.keyPassPhrase = expandPasswordAlias(keyPassPhrase);

        File home = new File(System.getProperty("user.home"));
        File knownHosts = new File(home,".ssh/known_hosts");
        if (knownHosts.exists()) {
            knownHostsLocation = knownHosts.getAbsolutePath();
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("SSH info is " + toString());
        }
    }

    /**
     *
     */
    private void openConnection() throws JSchException {
        assert session == null;
        JSch jsch = new JSch();
        // TODO: Logger?
        // jsch.setLogger(logger);

        // Client Auth
        String message = "";
        boolean triedAuthentication = false;
        // Private key file is provided - Public Key Authentication
        if (SSHUtil.checkString(keyFile) != null) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Specified key file is " + keyFile);
            }
            File key = new File(keyFile);
            if (key.exists()) {
                triedAuthentication = true;
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Specified key file exists at " + key);
                }
                jsch.addIdentity(key.getAbsolutePath(), keyPassPhrase);
            }
            else {
                message = "Specified key file does not exist \n";
            }
        }
        else if(SSHUtil.checkString(password) == null) {
            message += "No key or password specified - trying default keys \n";
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("keyfile and password are null. Will try to authenticate with default key file if available");
            }
            // check the default key locations if no authentication
            // method is explicitly configured.
            File home = new File(System.getProperty("user.home"));
            for (String keyName : Arrays.asList("id_rsa", "id_dsa", "identity")) {
                message += "Tried to authenticate using " + keyName + "\n";
                File key = new File(home, ".ssh/" + keyName);
                if (key.exists()) {
                    triedAuthentication = true;
                    jsch.addIdentity(key.getAbsolutePath());
                }
            }
        }

        session = jsch.getSession(userName, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        // Password Auth
        if (SSHUtil.checkString(password) != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Authenticating with password " + getPrintablePassword(password));
            }
            triedAuthentication = true;
            session.setPassword(password);
        }
        if (!triedAuthentication) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Could not authenticate");
            }
            throw new JSchException("Could not authenticate. " + message);
        }
        SSHUtil.register(session);
        session.connect();
    }

    /**
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * @param command the command to execute in the form of an argv style list
     * @param os stream to receive the output from the command
     * @param stdinLines optional data to be sent to the process's System.in
     *        stream; null if no input should be sent
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int runCommand(List<String> command, OutputStream os,
            List<String> stdinLines) throws JSchException, IOException,
                                            InterruptedException
    {
        return runCommand(commandListToQuotedString(command), os, stdinLines);
    }

    public int runCommand(List<String> command, OutputStream os)
                                            throws JSchException, IOException,
                                            InterruptedException
    {
        return runCommand(command, os, null);
    }

    /**
     * WARNING! This method does not handle paths with spaces in them.
     * To use this method you must make sure all paths in the command string
     * are quoted correctly.  Otherwise use the methods that take command as
     * a list instead.
     */
    public int runCommand(String command, OutputStream os) throws JSchException, IOException,
                                            InterruptedException
    {
        return runCommand(command, os, null);
    }

    /**
     * Executes a command on the remote system via ssh, optionally sending
     * lines of data to the remote process's System.in.
     *
     * WARNING! This method does not handle paths with spaces in them.
     * To use this method you must make sure all paths in the command string
     * are quoted correctly.  Otherwise use the methods that take command as
     * a list instead.
     *
     * @param command the command to execute
     * @param os stream to receive the output from the command
     * @param stdinLines optional data to be sent to the process's System.in stream; null if no input should be sent
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int runCommand(String command, OutputStream os,
            List<String> stdinLines) throws JSchException, IOException,
                                            InterruptedException
    {
        command = SFTPClient.normalizePath(command);
        return runCommandAsIs(command, os, stdinLines);
    }
    
    /**
     * Executes a command on the remote system via ssh without normalizing 
     * the command line
     * 
     * @param command the command to execute
     * @param os stream to receive the output from the command
     * @param stdinLines optional data to be sent to the process's System.in 
     *        stream; null if no input should be sent
     * @return
     * @throws IOException
     * @throws InterruptedException
     **/
    public int runCommandAsIs(List<String> command, OutputStream os,
            List<String> stdinLines) throws JSchException, IOException,
                                            InterruptedException
    {
        return runCommandAsIs(commandListToQuotedString(command), os, stdinLines);
    }
    
    private int runCommandAsIs(String command, OutputStream os,
            List<String> stdinLines) throws JSchException, IOException,
                                            InterruptedException
    {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Running command " + command + " on host: " + this.host);
        }
        boolean createNewSession = false;
        if (session == null)
            createNewSession = true;
        if(createNewSession)
            openConnection();

        int status = exec(command, os, listInputStream(stdinLines));

        if(createNewSession) {
            SSHUtil.unregister(session);
            session = null;
        }
        return status;
    }

    /**
     * To be called for after opening the connection using openConnection()
     *
     * @param command
     * @param os
     * @param is
     * @return
     * @throws JSchException
     * @throws IOException
     * @throws InterruptedException
     */
    private int exec(final String command, final OutputStream os,
                     final InputStream is)
            throws JSchException, IOException, InterruptedException {
        ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
        try {
            execChannel.setInputStream(is);
            execChannel.setCommand(command);
            InputStream in = execChannel.getInputStream();
            execChannel.connect();
            PumpThread t1 = new PumpThread(in, os);
            t1.start();
            PumpThread t2 = new PumpThread(execChannel.getErrStream(), os);
            t2.start();

            t1.join();
            t2.join();

            return execChannel.getExitStatus();
        } finally {
            execChannel.disconnect();
        }
    }

    /**
     * To be called for after opening the connection using openConnection()
     */
    private int exec(final String command, final OutputStream os)
            throws JSchException, IOException, InterruptedException {
        return exec(command, os, null);
    }

    private InputStream listInputStream(final List<String> stdinLines) throws IOException {
        if (stdinLines == null) {
            return null;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String line : stdinLines) {
            baos.write(line.getBytes());
            baos.write(LINE_SEP);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Pumps {@link InputStream} to {@link OutputStream}.
     *
     * @author Kohsuke Kawaguchi
     */
    private static final class PumpThread extends Thread {
        private final InputStream in;
        private final OutputStream out;

        public PumpThread(InputStream in, OutputStream out) {
            super("pump thread");
            this.in = in;
            this.out = out;
        }

        public void run() {
            byte[] buf = new byte[1024];
            try {
                while(true) {
                    int len = in.read(buf);
                    if(len<0) {
                        in.close();
                        return;
                    }
                    out.write(buf,0,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pingConnection() throws JSchException, InterruptedException
    {
        logger.fine("Pinging connection for host: " + this.host);
        openConnection();
        SSHUtil.unregister(session);
        session = null;
    }

    /* validate user provided ars
     *          check connecton to host
     *          check that the install dir is correct
     *          landmarkPath must be relative to the installdir
     */

    public void validate(String host, int port,
                             String userName, String password,
                             String keyFile, String keyPassPhrase,
                             String installDir, String landmarkPath,
                             Logger logger) throws IOException
    {
        boolean validInstallDir = false;
        init(userName, host,  port, password, keyFile, keyPassPhrase, logger);

        try {
            openConnection();
            logger.fine("Connection settings valid");
            String testPath = installDir;
            if (StringUtils.ok(testPath)) {
                // Validate if installDir exists
                SFTPClient sftpClient = new SFTPClient(session);
                if (sftpClient.exists(testPath)) {
                    // installDir exists. Now check for landmark if provided
                    if (StringUtils.ok(landmarkPath)) {
                        testPath = installDir + "/" + landmarkPath;
                    }
                    validInstallDir = sftpClient.exists(testPath);
                } else {
                    validInstallDir = false;
                }
                SSHUtil.unregister(session);
                session = null;

                if (!validInstallDir) {
                    String msg = "Invalid install directory: could not find " +
                            testPath + " on " + host;
                    throw new FileNotFoundException(msg);
                }
                logger.fine("Node home validated");
            }
        } catch (JSchException ex) {
            throw new IOException(ex);
        } catch (SftpException ex) {
            throw new IOException(ex);
        }
    }

    public void validate(String host, int port,
                             String userName, String password,
                             String keyFile, String keyPassPhrase,
                             String installDir, Logger logger) throws IOException
    {
        // Validate with no landmark file
        validate(host, port, userName, password, keyFile, keyPassPhrase,
                             installDir, null, logger);
    }

    public SFTPClient getSFTPClient() throws JSchException {
        openConnection();
        return new SFTPClient(session);
    }

    public String expandPasswordAlias(String alias) {

        String expandedPassword = null;

        if (alias == null) {
            return null;
        }

        try {
            expandedPassword = RelativePathResolver.getRealPasswordFromAlias(alias);
        } catch (Exception e) {
            logger.warning(StringUtils.cat(": ", alias, e.getMessage()));
            return null;
        }

        return expandedPassword;
    }

    public boolean isPasswordAlias(String alias) {
        // Check if the passed string is specified using the alias syntax
        String aliasName = RelativePathResolver.getAlias(alias);
        return (aliasName != null);
    }

    /**
     * Return a version of the password that is printable.
     * @param p  password string
     * @return   printable version of password
     */
    private String getPrintablePassword(String p) {
        // We only display the password if it is an alias, else
        // we display "<concealed>".
        String printable = "null";
        if (p != null) {
            if (isPasswordAlias(p)) {
                printable = p;
            } else {
                printable = "<concealed>";
            }
        }
        return printable;
    }

    /**
     * Setting up the key involves the following steps:
     * -If a key exists and we can connect using the key, do nothing.
     * -Generate a key pair if there isn't one
     * -Connect to remote host using password auth and do the following:
     *  1. create .ssh directory if it doesn't exist
     *  2. copy over the key as key.tmp
     *  3. Append the key to authorized_keys file
     *  4. Remove the temporary key file key.tmp
     *  5. Fix permissions for home, .ssh and authorized_keys
     * @param node        - remote host
     * @param pubKeyFile  - .pub file
     * @param generateKey - flag to indicate if key needs to be generated or not
     * @param passwd      - ssh user password
     * @throws IOException
     * @throws InterruptedException
     */
    public void setupKey(String node, String pubKeyFile, boolean generateKey, String passwd)
             throws IOException, InterruptedException {
        boolean connected = false;

        File key = new File(keyFile);
        if(logger.isLoggable(Level.FINER))
            logger.finer("Key = " + keyFile);
        if (key.exists()) {
            if (checkConnection()) {
                throw new IOException("SSH public key authentication is already configured for " + userName + "@" + node);
            }
        } else {
            if (generateKey) {
                if(!generateKeyPair()) {
                    throw new IOException("SSH key pair generation failed. Please generate key manually.");
                }
            } else {                
                throw new IOException("SSH key pair not present. Please generate a key pair manually or specify an existing one and re-run the command.");
            }
        }

        //password is must for key distribution
        if (passwd == null) {
            throw new IOException("SSH password is required for distributing the public key. You can specify the SSH password in a password file and pass it through --passwordfile option.");
        }
        try {
            JSch jsch = new JSch();
            Session s1 = jsch.getSession(userName, host, port);
            s1.setConfig("StrictHostKeyChecking", "no");
            s1.setPassword(passwd);
            s1.connect();

            if (!s1.isConnected()) {
                throw new IOException("SSH password authentication failed for user " + userName + " on host " + node);
            }
            SFTPClient sftp = new SFTPClient(s1);
            ChannelSftp sftpChannel = sftp.getSftpChannel();

            this.session = s1;

            if (key.exists()) {

                //fixes .ssh file mode
                setupSSHDir();

                if (pubKeyFile == null) {
                    pubKeyFile = keyFile + ".pub";
                }

                File pubKey = new File(pubKeyFile);
                if (!pubKey.exists()) {
                    throw new IOException("Public key file " + pubKeyFile + " does not exist.");
                }

                try {
                    if (!sftp.exists(SSH_DIR)) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine(SSH_DIR + " does not exist");
                        }
                        sftpChannel.cd(sftpChannel.getHome());
                        sftpChannel.mkdir(".ssh");
                        sftpChannel.chmod(0700, ".ssh");
                    }
                } catch (Exception e) {
                    if (logger.isLoggable(Level.FINER)) {
                        e.printStackTrace();
                    }
                    throw new IOException("Error while creating .ssh directory on remote host:" + e.getMessage());
                }

                //copy over the public key to remote host
                //scp.put(pubKey.getAbsolutePath(), "key.tmp", ".ssh", "0600");
                try {
                    sftpChannel.cd(".ssh");
                    sftpChannel.put(pubKey.getAbsolutePath(), "key.tmp");
                    sftpChannel.chmod(0600, "key.tmp");
                } catch (SftpException ex) {
                    throw new IOException("Unable to copy the public key", ex);
                }

                //append the public key file contents to authorized_keys file on remote host
                String mergeCommand = "cd .ssh; cat key.tmp >> " + AUTH_KEY_FILE;
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("mergeCommand = " + mergeCommand);
                }
                if (exec(mergeCommand, new ByteArrayOutputStream()) != 0) {
                    throw new IOException("Failed to propogate the public key " + pubKeyFile + " to " + host);
                }
                logger.info("Copied keyfile " + pubKeyFile + " to " + userName + "@" + host);

                //remove the public key file on remote host
                if (exec("rm .ssh/key.tmp", new ByteArrayOutputStream()) != 0) {
                    logger.warning("WARNING: Failed to remove the public key file key.tmp on remote host " + host);
                }
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Removed the temporary key file on remote host");
                }

                //Lets fix all the permissions
                //On MKS, chmod doesn't work as expected. StrictMode needs to be disabled
                //for connection to go through
                logger.info("Fixing file permissions for home(755), .ssh(700) and authorized_keys file(644)");
                try {
                    sftpChannel.cd(sftpChannel.getHome());
                    sftpChannel.chmod(0755, ".");
                    sftpChannel.chmod(0700, ".ssh");
                    sftpChannel.chmod(0644, SSH_DIR + AUTH_KEY_FILE);
                } catch (SftpException ex) {
                    throw new IOException("Unable to fix file permissions", ex);
                }
                //release the connections
                sftp.close();
            }
        } catch (JSchException ex) {
            throw new IOException(ex);
        }
    }

    public static byte[] toByteArray( InputStream input )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = input.read(buf)) >= 0) {
           output.write(buf, 0, len);
        }
        byte[] o = output.toByteArray();
        output.close();
        return o;
    }

    /**
     * Check if we can authenticate using public key auth
     * @return true|false
     */
    public boolean checkConnection() {
        boolean status = false;
        JSch jsch = new JSch();
        Session sess = null;

        try {
            File f = new File(keyFile);
            if(logger.isLoggable(Level.FINER)) {
                logger.finer("Checking connection...");
            }
            jsch.addIdentity(f.getAbsolutePath(), rawKeyPassPhrase);
            sess = jsch.getSession(userName, host, port);
            sess.setConfig("StrictHostKeyChecking", "no");
            sess.connect();
            status = sess.isConnected();
            if (status) {
                logger.info("Successfully connected to " + userName + "@" + host + " using keyfile " + keyFile);
            }
        }
        catch (JSchException ex) {
            Throwable t = ex.getCause();
            if (t != null) {
                String msg = t.getMessage();
                logger.warning("Failed to connect or authenticate: " + msg);
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Failed to connect or autheticate: ", ex);
            }
        } finally {
            if (sess != null)
                sess.disconnect();
        }
        return status;
    }

    /**
     * Check if we can connect using password auth
     * @return true|false
     */
    public boolean checkPasswordAuth() {
        boolean status = false;
        JSch jsch = new JSch();
        Session sess = null;

        try {
            if(logger.isLoggable(Level.FINER)) {
                logger.finer("Checking connection...");
            }
            sess = jsch.getSession(userName, host, port);
            sess.setConfig("StrictHostKeyChecking", "no");
            sess.setPassword(password);
            sess.connect();
            status = sess.isConnected();
            if (status) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Successfully connected to " + userName + "@" + host + " using password authentication");
                }
            }
        }
        catch (JSchException ex) {
            Throwable t = ex.getCause();
            if (t != null) {
                String msg = t.getMessage();
                logger.warning("Failed to connect or authenticate: " + msg);
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Failed to connect or autheticate: ", ex);
            }
        } finally {
            if (sess != null)
                sess.disconnect();
        }
        return status;
    }

    /**
      * Invoke ssh-keygen using ProcessManager API
      */
    private boolean generateKeyPair() throws IOException {
        String keygenCmd = findSSHKeygen();
        if(logger.isLoggable(Level.FINER)) {
            logger.finer("Using " + keygenCmd + " to generate key pair");
        }

        if (!setupSSHDir()) {
            throw new IOException("Failed to set proper permissions on .ssh directory");
        }

        StringBuffer k = new StringBuffer();
        List<String> cmdLine = new ArrayList<String>();
        cmdLine.add(keygenCmd);
        k.append(keygenCmd);
        cmdLine.add("-t");
        k.append(" ").append("-t");
        cmdLine.add("rsa");
        k.append(" ").append("rsa");
        cmdLine.add("-N");
        k.append(" ").append("-N");

        if (rawKeyPassPhrase != null && rawKeyPassPhrase.length() > 0) {
            cmdLine.add(rawKeyPassPhrase);
            k.append(" ").append(getPrintablePassword(rawKeyPassPhrase));
        } else {
            //special handling for empty passphrase on Windows
            if(OS.isWindows()) {
                cmdLine.add("\"\"");
                k.append(" ").append("\"\"");
            } else {
                cmdLine.add("");
                k.append(" ").append("");
            }
        }
        cmdLine.add("-f");
        k.append(" ").append("-f");
        cmdLine.add(keyFile);
        k.append(" ").append(keyFile);
        //cmdLine.add("-vvv");

        ProcessManager pm = new ProcessManager(cmdLine);

        if(logger.isLoggable(Level.FINER)) {
            logger.finer("Command = " + k);
        }
        pm.setTimeoutMsec(DEFAULT_TIMEOUT_MSEC);

        if (logger.isLoggable(Level.FINER))
            pm.setEcho(true);
        else
            pm.setEcho(false);
        int exit;

        try {
            exit = pm.execute();            
        }
        catch (ProcessManagerException ex) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Error while executing ssh-keygen: " + ex.getMessage());
            }
            exit = 1;
        }
        if (exit == 0){
            logger.info(keygenCmd + " successfully generated the identification " + keyFile);
        } else {
            if(logger.isLoggable(Level.FINER)) {
                logger.finer(pm.getStderr());
            }
            logger.info(keygenCmd + " failed");
        }

        return (exit == 0) ? true : false;
    }

    /**
     * Method to locate ssh-keygen. If found in path, return the same or else look
     * for it in a pre defined list of search paths.
     * @return ssh-keygen command
     */
    private String findSSHKeygen() {
        List<String> paths = new ArrayList<String>(Arrays.asList(
                    "/usr/bin/",
                    "/usr/local/bin/"));

        if (OS.isWindows()) {
            paths.add("C:/cygwin/bin/");
            //Windows MKS Toolkit install path
            String mks = System.getenv("ROOTDIR");
            if (mks != null) {
                paths.add(mks + "/bin/");
            }
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Paths = " + paths);
        }
        
        File exe = ProcessUtils.getExe(SSH_KEYGEN);
        if( exe != null){
            return exe.getPath();
        }

        for (String s :paths) {
            File f = new File(s + SSH_KEYGEN);
            if (f.canExecute()) {
                return f.getAbsolutePath();
            }
        }
        return SSH_KEYGEN;
    }

    /**
      * Create .ssh directory and set the permissions correctly
      */
    private boolean setupSSHDir() throws IOException {
        boolean ret = true;
        File home = new File(System.getProperty("user.home"));
        File f = new File(home, SSH_DIR);

        if(!FileUtils.safeIsDirectory(f)) {
            if (!f.mkdirs()) {
                throw new IOException("Failed to create " + f.getPath());
            }
            logger.info("Created directory " + f.toString());
        }
        
        if (!f.setReadable(false, false) || !f.setReadable(true)) {
            ret = false;
        }
        
        if (!f.setWritable(false,false) || !f.setWritable(true)) {
            ret = false;
        }

        if (!f.setExecutable(false, false) || !f.setExecutable(true)) {
            ret = false;
        }

        if(logger.isLoggable(Level.FINER)) {
            logger.finer("Fixed the .ssh directory permissions to 0700");
        }
        return ret;
    }
    
    @Override
    public String toString() {

        String displayPassword = getPrintablePassword(rawPassword);
        String displayKeyPassPhrase = getPrintablePassword(rawKeyPassPhrase);

        return String.format("host=%s port=%d user=%s password=%s keyFile=%s keyPassPhrase=%s authType=%s knownHostFile=%s",
            host, port, userName, displayPassword, keyFile,
            displayKeyPassPhrase, authType, knownHostsLocation);
    }

    /**
     * Take a command in the form of a list and convert it to a command string.
     * If any string in the list has spaces then the string is quoted before
     * being added to the final command string.
     *
     * @param command
     * @return
     */
    private static String commandListToQuotedString(List<String> command) {
        if(command.size()==1) return command.get(0);
        StringBuilder commandBuilder  = new StringBuilder();
        boolean first = true;

        for (String s : command) {
            if (!first) {
                commandBuilder.append(" ");
            } else {
                first = false;
            }
            if (s.contains(" ")) {
                // Quote parts of the command that contain a space
                commandBuilder.append(FileUtils.quoteString(s));
            } else {
                commandBuilder.append(s);
            }
        }
        return commandBuilder.toString();
    }
}
