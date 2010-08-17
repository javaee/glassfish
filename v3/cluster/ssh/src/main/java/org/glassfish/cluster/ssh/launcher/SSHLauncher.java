/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.cluster.ssh.launcher;

import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.util.StringUtils;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.KnownHosts;
import org.glassfish.internal.api.RelativePathResolver;
import org.glassfish.cluster.ssh.util.HostVerifier;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.internal.api.MasterPassword;
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
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Formatter;

@Service(name="SSHLauncher")
@Scoped(PerLookup.class)
public class SSHLauncher {

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
    private String rawPassword;
    private String rawKeyPassPhrase;

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

        String sshHost = connector.getSshHost();
        if (sshHost != null)
            this.host = sshHost;
        
        SshAuth sshAuth = connector.getSshAuth();
        String userName = null;
        if (sshAuth != null) {
            userName = sshAuth.getUserName();
            this.keyFile = sshAuth.getKeyfile();
        }
        try {
            port = Integer.parseInt(connector.getSshPort());
        } catch(NumberFormatException nfe) {
            port = 22;
        }

        init(userName, port, sshAuth.getPassword(), sshAuth.getKeyPassphrase());

    }

    private void init(String userName, int port, String password, String keyPassPhrase) {

        this.port = port == 0 ? 22 : port;

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
                    logger.fine("Authentication successful");
                    break;
                }

            }
        }
        if (!isAuthenticated && SSHUtil.checkString(keyFile) != null) {
            File key = new File(keyFile);
            if (key.exists()) {
                //See if the key file is protected with passphrase
                
               isAuthenticated = connection.authenticateWithPublicKey(
                                            userName, key, keyPassPhrase);

            }
        } if (!isAuthenticated && SSHUtil.checkString(password) != null) {
          isAuthenticated = connection.authenticateWithPassword(userName, password);
      }

        if (!isAuthenticated && !connection.isAuthenticationComplete()) {
            connection.close();
            connection = null;
            logger.fine("Could not authenticate");
            throw new IOException("Could not authenticate");
        }
        SSHUtil.register(connection);

    }

    public int runCommand(String command, OutputStream os) throws IOException,
                                            InterruptedException 
    {
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
        this.host = host;
        this.keyFile = keyFile;
        this.logger = logger;
        boolean validNodeHome = false;
        init(userName, port, password, keyPassPhrase);

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
