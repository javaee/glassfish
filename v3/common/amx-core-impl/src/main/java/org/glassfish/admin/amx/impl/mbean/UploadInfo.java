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
package org.glassfish.admin.amx.impl.mbean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class UploadInfo extends UpDownInfo
{
    private final String mName;

    private final long mTotalSize;

    private FileOutputStream mOutputStream;

    private long mWrittenSoFar;

    public UploadInfo(
            final Object id,
            final String name,
            final long totalSize)
            throws IOException
    {
        super(id, createTempFile(id, name, totalSize));

        mName = name;

        mTotalSize = totalSize;

        getFile().createNewFile();
        getFile().deleteOnExit();
        mOutputStream = new FileOutputStream(getFile());

        mWrittenSoFar = 0;
    }

    private static File createTempFile(final Object id, final String name, final long totalSize)
            throws IOException
    {
        final String tempName = (name != null) ? name : id + "_" + totalSize;
        File actual = new File(tempName);
        if (actual.exists())
        {
            actual = File.createTempFile(tempName, null);
        }
        return (actual);
    }

    public boolean isDone()
    {
        return (mWrittenSoFar == mTotalSize);
    }

    /**
    @return true if done, false otherwise
     */
    public boolean write(final byte[] bytes)
            throws IOException
    {
        if (isDone() || mWrittenSoFar + bytes.length > mTotalSize)
        {
            throw new IllegalArgumentException("too many bytes");
        }
        getOutputStream().write(bytes);

        mWrittenSoFar += bytes.length;

        if (isDone())
        {
            mOutputStream.close();
            mOutputStream = null;
        }

        accessed();

        return (isDone());
    }

    public long getTotalSize()
    {
        return (mTotalSize);
    }

    public void cleanup()
            throws IOException
    {
        if (mOutputStream != null)
        {
            mOutputStream.close();
        }

        getFile().delete();
    }

    private FileOutputStream getOutputStream()
    {
        return (mOutputStream);
    }

}


