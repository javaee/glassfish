/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License.  You can
 *  obtain a copy of the License at
 *  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *  or packager/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at packager/legal/LICENSE.txt.
 *
 *  GPL Classpath Exception:
 *  Oracle designates this particular file as subject to the "Classpath"
 *  exception as provided by Oracle in the GPL Version 2 section of the License
 *  file that accompanied this code.
 *
 *  Modifications:
 *  If applicable, add the following below the License Header, with the fields
 *  enclosed by brackets [] replaced by your own identifying information:
 *   "Portions Copyright [year] [name of copyright owner]"
 *
 *  Contributor(s):
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package com.sun.enterprise.util.io;

import java.io.*;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 *
 * @author Byron Nevins
 */
public final class WindowsRemoteFile {
    private SmbFile smbFile;
    private WindowsRemoteFileSystem wrfs;
    private String smbPath;

    public WindowsRemoteFile(WindowsRemoteFile parent, String path) throws MalformedURLException {
        wrfs = parent.wrfs;
        smbPath = parent.smbPath + removeLeadingAndTrailingSlashes(path) + "/";
        smbFile = new SmbFile(smbPath, wrfs.getAuthorization());
    }
    public WindowsRemoteFile(WindowsRemoteFileSystem wrfs, String path) throws MalformedURLException, UnknownHostException {
        if (wrfs == null || path == null || path.isEmpty())
            throw new NullPointerException();

        if (path.indexOf(":") < 0)
            throw new IllegalArgumentException("Non-absolute path.  No colon in the path");

        this.wrfs = wrfs;
        //  this.isDir = isDir;

        // replace backslashes with forward slashes
        // replace drive designator(e:) with the default admin share for the drive (e$)

        path = path.replace('\\', '/').replace(':', '$');

        StringBuilder sb = new StringBuilder("smb://");
        sb.append(wrfs.getHost()).append("/").append(path);

        if (!path.endsWith("/"))
            sb.append('/');

        smbPath = sb.toString();
        //SmbFile remoteRoot = new SmbFile("smb://" + name + "/" + path.replace('\\', '/').replace(':', '$')+"/",createSmbAuth());

        smbFile = new SmbFile(smbPath, wrfs.getAuthorization());
    }

    public final boolean exists() throws SmbException {
        return smbFile.exists();
    }

    public final String[] list() throws SmbException {
        return smbFile.list();
    }

    public final void createNewFile() throws SmbException {
        smbFile.createNewFile();
    }

    public final void copyTo(WindowsRemoteFile wf) throws SmbException {
        smbFile.copyTo(wf.smbFile);
    }

    public final void delete() throws SmbException {
        smbFile.delete();
    }

    public final void mkdirs() throws SmbException {
        if(exists())
            delete();
        smbFile.mkdirs();
    }

    public final void copyFrom(File from, WindowsRemoteFileCopyProgress progress) throws SmbException, IOException {
        if (from == null || !from.isFile())
            throw new IllegalArgumentException("copyFrom file arg is bad: " + from);

        if (!exists())
            createNewFile();

        long filesize = from.length();
        OutputStream sout = new BufferedOutputStream(smbFile.getOutputStream());
        InputStream sin = new BufferedInputStream(new FileInputStream(from));

        final int bufsize = 1048576;
        byte[] buf = new byte[bufsize];
        long numBytes = 0;
        long totalBytesCopied = 0;
        
        while ((numBytes = sin.read(buf)) >= 0) {
            sout.write(buf);
            totalBytesCopied += numBytes;
            progress.callback(totalBytesCopied, filesize);
        }

        try {
            sin.close();
        }
        catch (Exception e) {
            // nothing can be done!
        }
        try {
            sout.close();
        }
        catch (Exception e) {
            // nothing can be done!
        }
    }

    // note that the path is ALWAYS appended with one and only one slash!!
    // THis is important for smb calls...
    public final String getPath() {
        return smbPath;
    }

    private String removeLeadingAndTrailingSlashes(String path) {
        while(path.startsWith("/") || path.startsWith("\\"))
            path = path.substring(1);

        while(path.endsWith("/") || path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);

        return path;
    }
}
