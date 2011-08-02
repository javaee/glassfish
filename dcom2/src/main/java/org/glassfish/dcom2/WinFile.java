/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.dcom2;

import java.io.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 *
 * @author Byron Nevins
 */
public final class WinFile {
    private SmbFile smbFile;
    private WindowsRemoteFileSystem wrfs;
    private String smbPath;

    WinFile(WindowsRemoteFileSystem wrfs, String path) throws MalformedURLException, UnknownHostException {
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

    void createNewFile() throws SmbException {
        smbFile.createNewFile();
    }

    void copyTo(WinFile wf) throws SmbException {
        smbFile.copyTo(wf.smbFile);
    }
    void copyFrom(File from) throws SmbException, IOException {
        if(from == null || !from.isFile())
            throw new IllegalArgumentException("copyFrom file arg is bad: " + from);

        if(!exists())
            createNewFile();

        OutputStream sout = new BufferedOutputStream(smbFile.getOutputStream());
        InputStream sin = new BufferedInputStream(new FileInputStream(from));
        byte[] buf = new byte[256*256];

        while(sin.read(buf) >= 0) {
            sout.write(buf);
        }
        try {
            sin.close();
        }
        catch(Exception e) {
            // nothing can be done!
        }
        try {
            sout.close();
        }
        catch(Exception e) {
            // nothing can be done!
        }
    }
}
