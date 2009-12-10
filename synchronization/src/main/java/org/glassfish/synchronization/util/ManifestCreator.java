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
package org.glassfish.synchronization.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.glassfish.synchronization.client.SyncContext;

// import org.glassfish.synchronization.hashing.MD5;

/**
 * This class generates the manifest used for synchronization
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class ManifestCreator {

	private static final String fs = File.separator;
	public static final String[] FOLDERS = { "applications" + fs,
			"generated" + fs, "config" + fs, "docroot" + fs, "lib" + fs };
    
	SyncContext context;

	public ManifestCreator(SyncContext c) {
		context = c;
	}

	public ManifestCreator(StaticSyncInfo s) throws SecurityException,
			IOException {
		context = new SyncContext();
		context.setStaticSyncInfo(s);
	}

	private DirInfo handleFolder(File dir, BufferedWriter out,
			String currentPath) {
		String[] children = dir.list();
		String hash = "";
		File f;
		DirInfo tempDirInfo;
		int numfiles = 0;
		long size = 0;
		for (int j = 0; j < children.length; j++) {
			f = new File(dir.getPath() + File.separator + children[j]);
			if (f.isFile()) {
				try {
					String path = FileUtils.formatPath(currentPath
							+ f.getName());
					if (context.getStaticSyncInfo().getHashVerify()) {
						// hash = MD5.asHex(MD5.getHash(f));
						out.write(path + "," + f.lastModified() + ","
								+ f.length() + "," + hash + "\n");
					} else
						out.write(path + "," + f.lastModified() + ","
								+ f.length() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				numfiles++;
				size += f.length();
			} else if (f.isDirectory()) {
				tempDirInfo = handleFolder(f, out, currentPath + f.getName()
						+ File.separator);
				size += tempDirInfo.bytes;
				numfiles += tempDirInfo.numFiles;
			}
		}

		return new DirInfo(numfiles, size);
	}

	public void start() throws IOException {
		int numfiles = 0;
		long size = 0;
		DirInfo dirInfo;
		File dir;
		File tempManFile = new File(context.getStaticSyncInfo().getTempFolder()
				+ "tempManifest.txt");
		tempManFile.delete();
		long manCreationTime = System.currentTimeMillis();
		BufferedWriter out = null;
		try {
			tempManFile.getParentFile().mkdirs();
			tempManFile.createNewFile();
			tempManFile.deleteOnExit();
			out = new BufferedWriter(new FileWriter(tempManFile
					.getAbsolutePath()));
			for (int i = 0; i < FOLDERS.length; i++) {
				dir = new File(context.getStaticSyncInfo().getBasePath()
						+ FOLDERS[i]);
				if (dir.exists()) {
					dirInfo = handleFolder(dir, out, FOLDERS[i]);
					size += dirInfo.bytes;
					numfiles += dirInfo.numFiles;
				}
			}
			out.close();
		} finally {
			if (out != null)
				out.close();
		}
		String manPath = context.getStaticSyncInfo().getManifestFilePath();
		File manFile = new File(manPath);
		manFile.delete();
		BufferedReader in = null;
		try {
			out = new BufferedWriter(new FileWriter(manFile.getAbsolutePath()));
			in = new BufferedReader(new FileReader(tempManFile
					.getAbsolutePath()));
			String str;
			out.write(numfiles + "," + size + "," + manCreationTime + "\n");
			while ((str = in.readLine()) != null) {
				out.write(str + "\n");
			}
			tempManFile.delete();
		} finally {
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}

		System.out.println("Manifest has been created at "
				+ manFile.getAbsolutePath());
		if (context.getCookieManager() != null)
			context.getCookieManager().createCookieFromManifest();
	}


	private class DirInfo {
		int numFiles;
		long bytes;

		public DirInfo(int n, long b) {
			numFiles = n;
			bytes = b;
		}
	}
}
