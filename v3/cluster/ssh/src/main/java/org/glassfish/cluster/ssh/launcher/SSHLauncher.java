/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.io.*;

import com.sun.enterprise.util.StringUtils;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.SCPClient;
import org.glassfish.internal.api.RelativePathResolver;
import org.glassfish.cluster.ssh.util.HostVerifier;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.Node;
import org.glassfish.cluster.ssh.sftp.SFTPClient;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(name="SSHLauncher")
@Scoped(PerLookup.class)
public class SSHLauncher {

    private static final String SSH_DIR = ".ssh/";
    private static final String AUTH_KEY_FILE = "authorized_keys";
  /**
     * Database of known hosts.
     */
    private static KnownHosts knownHostsDatabase = new KnownHosts();

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
    private Connection connection;

    private String authType;

    private String keyPassPhrase;

    private File knownHosts;

    private Logger logger;

    private String password;

    // Password before it has been expanded. Used for debugging.
    private String rawPassword = null;
    private String rawKeyPassPhrase = null;

    @Inject
    private Habitat habitat;


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

    public void init(String userName, String host, int port, String password, String keyFile, String keyPassPhrase, Logger logger) {

        this.port = port == 0 ? 22 : port;

        this.host = host;
        this.keyFile = (keyFile == null) ? findDefaultKeyFile(): keyFile;
        this.logger = logger;

        this.userName = SSHUtil.checkString(userName) == null ?
                    System.getProperty("user.name") : userName;

        
        this.rawPassword = password;
        this.password = expandPasswordAlias(password);
        this.rawKeyPassPhrase = keyPassPhrase;
        this.keyPassPhrase = expandPasswordAlias(keyPassPhrase);

        if (knownHosts == null) {
            File home = new File(System.getProperty("user.home"));
            knownHosts = new File(home,".ssh/known_hosts");
        }
        if (knownHosts.exists()) {
            try {
                knownHostsDatabase.addHostkeys(knownHosts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine ("SSH info is " + toString());
        }
    }


  /**
     * Opens the connection to the host and authenticates with public
     * key.
     * 
     */
    private void openConnection() throws IOException {

    boolean isAuthenticated = false;
    connection = new Connection(host, port);

        connection.connect(new HostVerifier(knownHostsDatabase));
        if(SSHUtil.checkString(keyFile) == null && SSHUtil.checkString(password) == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("keyfile and password are null. Will try to authenticate with default key file if available");
            }
            // check the default key locations if no authentication
            // method is explicitly configured.
            File home = new File(System.getProperty("user.home"));
            for (String keyName : Arrays.asList("id_rsa","id_dsa",
                                                "identity"))
            {
                File key = new File(home,".ssh/"+keyName);
                if (key.exists()) {
                    isAuthenticated =
                        connection.authenticateWithPublicKey(userName,
                                                             key, null);
                }
                if (isAuthenticated) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Authentication successful using key " + keyName);
                    }
                    break;
                }

            }
        }
        if (!isAuthenticated && SSHUtil.checkString(keyFile) != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Specified key file is " + keyFile);
            }
            File key = new File(keyFile);
            if (key.exists()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Specified key file exists at " + key);
                }

                //See if the key file is protected with passphrase
                
               isAuthenticated = connection.authenticateWithPublicKey(
                                            userName, key, keyPassPhrase);

            }
        } if (!isAuthenticated && SSHUtil.checkString(password) != null) {
              if (logger.isLoggable(Level.FINE)) {
                  logger.fine("Authenticating with password " + getPrintablePassword(password));
              }

          isAuthenticated = connection.authenticateWithPassword(userName, password);
      }

        if (!isAuthenticated && !connection.isAuthenticationComplete()) {
            connection.close();
            connection = null;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Could not authenticate");
            }
            throw new IOException("Could not authenticate");
        }
        SSHUtil.register(connection);

    }

    public int runCommand(String command, OutputStream os) throws IOException,
                                            InterruptedException 
    {
        command = SFTPClient.normalizePath(command);
        logger.fine("Running command " + command + " on host: " + this.host);

        openConnection();

        int status = connection.exec(command, os);

        // XXX: Should we close connection after each command or cache it
        // and re-use it?
        SSHUtil.unregister(connection);
        connection = null;
        return status;
    }

    public void validate(String host, int port,
                             String userName, String password,
                             String keyFile, String keyPassPhrase,
                             String nodeHome, Logger logger) throws IOException
    {
        boolean validNodeHome = false;
        init(userName, host,  port, password, keyFile, keyPassPhrase, logger);

        openConnection();
        logger.fine("Connection settings valid");
        //Validate if nodeHome exists
        SFTPClient sftpClient = new SFTPClient(connection);
        validNodeHome = sftpClient.exists(nodeHome);
        logger.fine("Node home validated");
        SSHUtil.unregister(connection);

        connection = null;

        if (!validNodeHome) {
            throw new FileNotFoundException("Could not find " +
                    nodeHome + " on " + host);
        }
    }

    public SFTPClient getSFTPClient() throws IOException {
        openConnection();
        SFTPClient sftpClient = new SFTPClient(connection);
        return sftpClient;
    }

    public SCPClient getSCPClient() throws IOException {
        openConnection();
        return new SCPClient(connection);
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

     public void setupKey(String node, String pubKeyFile) throws IOException {
        boolean connected = false;

        File key = new File(keyFile);
        logger.fine("Key = " + keyFile);
        if (key.exists()) {
            if (checkConnection()) {
                logger.fine("SSH key already setup");
                return;
            }
            if ( pubKeyFile == null) {
                pubKeyFile = findDefaultKeyFile() + ".pub";
            }
        } else {
            throw new IOException("SSH key pair not present. Please generate a key pair manually and re-run the command.");
        }


        if (password == null) {
            throw new IOException("SSH password is required for distributing the public key. You can specify the SSH password alias in a password file and pass it through --passwordfile option.");
        }
        try {
            connection = new Connection(node, port);
            connection.connect();
            connected = connection.authenticateWithPassword(userName, password);
        } catch (Exception ex) {
            //logger.printExceptionStackTrace(ex);
            throw new IOException("SSH password authentication failed for user " + userName + " on host " + node);
        }

        if(!connected) {
            throw new IOException("SSH password authentication failed for user " + userName + " on host " + node);
        }
        //initiate scp client
        SCPClient scp = new SCPClient(connection);

        if (key.exists()) {

            File pubKey = new File(pubKeyFile);
            if(!pubKey.exists()) {
                throw new IOException("Public key file " + pubKeyFile + " does not exist.");
            }

            ByteArrayOutputStream out = null;
            OutputStream oStream = null;
            InputStream sendFile = null;
            File f = null;

            try {
                out = new ByteArrayOutputStream();
                try {
                    scp.get(SSH_DIR + AUTH_KEY_FILE, out);
                } catch (IOException io) {
                    //ignore this, we will anyway send across the key
                    //logger.printExceptionStackTrace(io);
                    //logger.printDebugMessage("The auth file probably doesn't exist");
                }

                logger.fine("Got the remote authorized_keys file");
                File home = new File(System.getProperty("user.home"));
                f = new File(home,SSH_DIR + AUTH_KEY_FILE +".temp");
                oStream = new FileOutputStream(f);
                out.writeTo(oStream);
                logger.fine("Wrote the temp file");

                appendPublicKey(pubKey, f);

                sendFile = new FileInputStream(f);
                byte[] theBytes = new byte[sendFile.available()];
                sendFile.read(theBytes);

                scp.put(theBytes, AUTH_KEY_FILE, SSH_DIR);

                logger.fine("Sent the merged file");
            } catch (FileNotFoundException fne) {
                //logger.printExceptionStackTrace(fne);
            } catch (IOException ioe) {
                //logger.printExceptionStackTrace(ioe);
            } finally {
                //remove the temp file
                f.delete();
                //clean up streams
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException io){
                        //logger.printExceptionStackTrace(io);
                    }
                }

                if(oStream != null) {
                    try {
                        oStream.close();
                    } catch (IOException io) {
                        //logger.printExceptionStackTrace(io);
                    }
                }
                if(sendFile != null) {
                    try {
                        sendFile.close();
                    } catch (IOException io) {
                        //logger.printExceptionStackTrace(io);
                    }
                }
            }

        }
    }

    private void appendPublicKey(File pubKey, File f) {
        try {
            //open file in append mode
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
            InputStream in = new FileInputStream(pubKey);

            int c;
            while ((c = in.read()) != -1) {
               out.write(c);
            }
            out.close();

        } catch (Exception ex) {
            //logger.printExceptionStackTrace(ex);
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

    public boolean checkConnection() {
        boolean status = false;
        Connection c = null;
        try {
            c = new Connection(host, port);
            c.connect();
            File f = new File(keyFile);
            status = c.authenticateWithPublicKey(userName, f, null);
        } catch(IOException ioe) {
            //logger.printExceptionStackTrace(ioe);
        }
        c.close();
        return status;
    }

    private String findDefaultKeyFile() {
        String key = null;
        for (String keyName : Arrays.asList("id_rsa","id_dsa",
                                                "identity"))
        {
            String h = System.getProperty("user.home") + File.separator;
            File f = new File(h+".ssh/"+keyName);
            if (f.exists()) {
                key =  h  + ".ssh/" + keyName;
                break;
            }
        }
        return key;
    }

    @Override
    public String toString() {

        String knownHostsPath  = "null";
        if (knownHosts != null) {
            try {
                knownHostsPath = knownHosts.getCanonicalPath();
            } catch (IOException e) {
                knownHostsPath = knownHosts.getAbsolutePath();
            }
        }

        String displayPassword = getPrintablePassword(rawPassword);
        String displayKeyPassPhrase = getPrintablePassword(rawKeyPassPhrase);

        return String.format("host=%s port=%d user=%s password=%s keyFile=%s keyPassPhrase=%s authType=%s knownHostFile=%s",
            host, port, userName, displayPassword, keyFile,
            displayKeyPassPhrase, authType, knownHostsPath);
    }
}
