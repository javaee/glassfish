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

package org.glassfish.cluster.ssh.sftp;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.glassfish.cluster.ssh.util.SSHUtil;

public class SFTPClient {

    private Session session = null;

    private ChannelSftp sftpChannel = null;

    public SFTPClient(Session session) throws JSchException {
        this.session = session;
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        SSHUtil.register(session);
    }

    public ChannelSftp getSftpChannel() {
        return sftpChannel;
    }

    /**
     * Close the SFTP connection and free any resources associated with it.
     * close() should be called when you are done using the SFTPClient
     */
    public void close() {
        if (session != null) {
            SSHUtil.unregister(session);
            session = null;
        }
    }

    /**
     * Checks if the given path exists.
     */
    public boolean exists(String path) throws SftpException {
        return _stat(normalizePath(path))!=null;
    }

    /**
     * Graceful stat that returns null if the path doesn't exist.
     */
    public SftpATTRS _stat(String path) throws SftpException {
        try {
            return sftpChannel.stat(normalizePath(path));
        } catch (SftpException e) {
            int c = e.id;
            if (c == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                return null;
            else
                throw e;
        }
    }

    /**
     * Makes sure that the directory exists, by creating it if necessary.
     */
    public void mkdirs(String path, int posixPermission) throws SftpException {
        // remove trailing slash if present
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        path = normalizePath(path);
        SftpATTRS attrs = _stat(path);
        if (attrs != null && attrs.isDir())
            return;

        int idx = path.lastIndexOf("/");
        if (idx>0)
            mkdirs(path.substring(0,idx), posixPermission);
        sftpChannel.mkdir(path);
        sftpChannel.chmod(posixPermission, path);
    }

    public void chmod(String path, int permissions) throws SftpException {
        path = normalizePath(path);
        sftpChannel.chmod(permissions, path);
    }

    // Commands run in a shell on Windows need to have forward slashes.
    public static String normalizePath(String path){
        return path.replaceAll("\\\\","/");
    }

    public void cd(String path) throws SftpException {
        path = normalizePath(path);
        sftpChannel.cd(path);
    }
}
