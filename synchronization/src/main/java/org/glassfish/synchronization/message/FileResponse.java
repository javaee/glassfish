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
package org.glassfish.synchronization.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.glassfish.synchronization.util.FileUtils;

public class FileResponse implements Serializable {
	/**
	 * This class was created in order to enable sending large files. The
	 * serialize and de-serialize methods of this class have been overridden so
	 * that when a this object is serialized pieces of the file is brought into
	 * mem and then pushed out a bit at a time. This is done because the entire
	 * file cannot be brought into mem.
	 * 
	 * @author Behrooz Khorashadi
	 */
	private static final long serialVersionUID = 1L;
	public static final int NONE = 0;
	public static final int ZIP = 1;
	public static final int TXT = 2;
	public static final int UKNOWN = 3;
	private File responseFile = null;

	public FileResponse(File file) {
		responseFile = file;
	}

	public File getFile() {
		return responseFile;
	}

	/**
	 * This function overrides the default function for when a object is
	 * serialized Streams file across connection so all of it is not loaded into
	 * memory
	 * 
	 * @param out
	 *            the output stream that the file must be written to
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// then stream file
		if (responseFile == null) {
			out.writeInt(FileResponse.NONE);
			return;
		}
		if (responseFile.getName().endsWith(".txt"))
			out.writeInt(FileResponse.TXT);
		else if (responseFile.getName().endsWith(".zip"))
			out.writeInt(FileResponse.ZIP);
		else
			out.writeInt(FileResponse.UKNOWN);
		out.writeLong(responseFile.length());
		FileInputStream in = new FileInputStream(responseFile);
		BufferedInputStream bin = new BufferedInputStream(in);
		int bytesRead;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((bytesRead = bin.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		bin.close();
	}

	/**
	 * This function overrides the default function for object serialization
	 * Reads in the file stream and writes to disk creating a temporary file and
	 * the saves a the File pointer to that file.
	 * 
	 * @param in
	 *            the input stream from which you get the file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		byte[] buffer = new byte[BUFFER_SIZE];// byte buffer
		int fileType = in.readInt();
		int bytesToRead;
		if (fileType == FileResponse.NONE) {
			responseFile = null;
			return;
		} else {
			if (fileType == FileResponse.TXT)
				responseFile = FileUtils.getTempFile("txt");
			else if (fileType == FileResponse.ZIP)
				responseFile = FileUtils.getTempFile("zip");
			else
				responseFile = FileUtils.getTempFile("file");
			long filesize = in.readLong();
			FileOutputStream fout = new FileOutputStream(responseFile);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			while (true) {
				// System.out.println("read " + bytesToRead);
				bytesToRead = in.read(buffer, 0, (int) Math.min(filesize,
						buffer.length));
				if (bytesToRead == -1)
					break;
				bos.write(buffer, 0, bytesToRead);

			}
			// System.out.println("read " + bytesToRead);
			bos.close();
			bos = null;
			// System.out.println("close file");

		}
	}

	private static final int BUFFER_SIZE = 65536; // 64 KB
}
