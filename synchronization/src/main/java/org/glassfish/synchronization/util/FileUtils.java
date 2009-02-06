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

import java.io.*;

import org.glassfish.synchronization.client.SyncContext;

/**
 * This class is used only to create temporary zip files. Also has a clean up
 * function which deletes all temp files.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class FileUtils {
	/** The temp directory used by the utility object */
	private final String tmpName;
	private static final String global_directory = "/tmp/servertmpFiles/";
	public FileUtils(SyncContext c) {
		tmpName = c.getStaticSyncInfo().getTempFolder();
		c.setFileUtils(this);
		cleanUpTempFiles();
	}

	/**
	 * Returns a file handle to a temporary file.
	 * 
	 * @return a file handle to a temporary file
	 */
	public synchronized File getTempZipFile() {
		long ts = System.currentTimeMillis();
		File f = new File(tmpName, new Long(ts).toString() + ".zip");
		while (f.exists()) {
			ts += 1;
			f = new File(tmpName, new Long(ts).toString() + ".zip");
		}
		f.getParentFile().mkdirs();
		return f;
	}

	/**
	 * Returns a file handle to a temporary file.
	 * 
	 * @return a file handle to a temporary file
	 * @throws IOException
	 */
	public synchronized static File getTempFile(String type) throws IOException {
		long ts = System.currentTimeMillis();
		File f = new File(global_directory, new Long(ts).toString() + "."
				+ type);
		while (f.exists()) {
			ts += 1;
			f = new File(global_directory, new Long(ts).toString() + "." + type);
		}
		f.getParentFile().mkdirs();
		f.createNewFile();
		return f;
	}

	public static String formatPath(String path) {
		return path.replace("\\", "/");
	}

	public synchronized void cleanUpTempFiles() {
		File dir = new File(tmpName);
		// System.out.println(dir.getAbsolutePath());
		File[] files = dir.listFiles();
		if (files == null) {
			// dir.delete();
			return;
		}
		for (int i = 0; i < files.length; i++) {
			// System.out.println("deleting " + files[i].getAbsolutePath());
			files[i].delete();
		}
		// dir.delete();
	}

}