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
package org.glassfish.synchronization.manifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.logging.Level;

import org.glassfish.synchronization.client.SyncContext;

/**
 * This class is responsible for managing the manifest and all the files that
 * are needed and have already been acquired
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class ManifestManager {
	/** A bitset which indicates which files have already been acquired*/
	BitSet fileIndicator = new BitSet();
	/** an array which indicates the length of each file*/
	long[] file_lengths;
	/** the manifest version */
	private long _manifestVersion;
	/** num files in manifest */
	private int _numFiles;
	/** total bytes of files */
	private long _totalBytes;
	/** The manifest */
	private File _manifest = null;
	/** file names loaded into mem */
	private String[] fileNames = null;
	/** Download manager */
	private DownloadManagerInterface dm;
	/** Synchronization Context */
	private SyncContext context;

	public ManifestManager(SyncContext c) {
		context = c;
		c.setManifestManager(this);
	}

	/**
	 * Should only be used by DAS
	 * 
	 * @param manName
	 * @throws IOException
	 */
	public ManifestManager(String manifestPath, SyncContext c)
															throws IOException {
		this(c);
		_manifest = new File(manifestPath);
		parseManifest();
		c.setManifestManager(this);
	}

	/**
	 * Takes a manifest file and creates all the objects and local variables
	 * used by this object. This method modifies fileIndicator but does not need
	 * to be synchronized because at this point only one thread is running
	 * 
	 * @throws IOException
	 *             if reading the manifest fails
	 */
	private void parseManifest() throws IOException {
		long t1 = System.currentTimeMillis();
		BufferedReader in = null;
		try {
			synchronized (fileIndicator) {
				long lastSync = context.getCookieManager().getCookie();
				in = new BufferedReader(new FileReader(_manifest
						.getAbsolutePath()));
				String str;
				String[] split;
				int lineCount = 0;
				str = in.readLine();
				processfileHeader(str);
				while ((str = in.readLine()) != null) {
					split = str.split(",");
					if (verifyfile(split, lastSync)) {
						fileIndicator.set(lineCount);
					}
					file_lengths[lineCount] = Long.parseLong(split[2]);
					lineCount++;
				}
			}
			// System.out.println(fileIndicator.toString());
			// System.out.println(CookieManager.getCookie());
		} finally {
			if (in != null)
				in.close();
		}
		if (context.getLogger().isLoggable(Level.FINER)) {
			long t2 = System.currentTimeMillis();
			String log = "Time it takes to read file: " + (t2 - t1);
			context.getLogger().finer(log);
		}
	}

	/**
	 * Processes just the header parsing the fields in the first line of the
	 * manifest
	 * 
	 * @param str
	 *            first line of manifest
	 */
	private void processfileHeader(String str) {
		String[] split = str.split(",");
		_numFiles = Integer.parseInt(split[0]);
		fileIndicator = new BitSet(_numFiles);
		_totalBytes = Long.parseLong(split[1]);
		this._manifestVersion = Long.parseLong(split[2]);
		file_lengths = new long[_numFiles];
		dm = new DownloadManagerBasic(this, file_lengths);
	}

	/** returns a COPY of the manifest bitset */
	public BitSet getBitManifest() {
		synchronized (fileIndicator) {
			return (BitSet) fileIndicator.clone();
		}
	}

	/**
	 * 
	 * @return file pointer to the current manifest
	 */
	public File getManifest() {
		return _manifest;
	}

	/**
	 * Returns the manifest version
	 * 
	 * @return
	 */
	public long getManVersion() {
		return this._manifestVersion;
	}

	/**
	 * Sets the manifest. This should be called everytime a new manifest is
	 * received
	 * 
	 * @param manifestPath
	 *            the path to the manifest
	 * @throws IOException
	 */
	public void setManifest(String manifestPath) throws IOException {
		_manifest = new File(manifestPath);
		parseManifest();
	}

	/**
	 * Returns the number of files in this manifest
	 * 
	 * @return
	 */
	public int getnumFiles() {
		return _numFiles;
	}

	/**
	 * The total sum of all bytes of all the files combined
	 * 
	 * @return
	 */
	public long getTotalBytes() {
		return _totalBytes;
	}

	/**
	 * Update the Bits this GF Instance has. This is called when a new file is
	 * received.
	 * 
	 * @param received
	 */
	public void updateMyBits(BitSet received) {
		synchronized (fileIndicator) {
			fileIndicator.or(received);
			// System.out.println("addig bits " + received.toString());
		}
	}

	/**
	 * Checks to see if synchronization is completed
	 * 
	 * @return
	 */
	public synchronized boolean finished() {
		synchronized (fileIndicator) {
			return fileIndicator.nextClearBit(0) == _numFiles;
		}
	}

	/**
	 * Returns rank of this GF Instance
	 * 
	 * @return
	 */
	public int getRank() {
		synchronized (fileIndicator) {
			return fileIndicator.cardinality();
		}
	}

	/**
	 * Forwards this call to the DownloadManager
	 * 
	 * @param server_Has
	 * @return
	 */
	public BitSet getnextDownloadBits(BitSet server_Has) {
		return dm.getNextRequestSet(server_Has);
	}

	public void resetPendingBits(BitSet reset) {
		dm.resetPendingBits(reset);
	}

	/**
	 * tells downlaod manager to release all the pending bits. Should be used
	 * only when a reset is needed.
	 */
	public void releasePendingBits() {
		dm.releasePendingBits();
	}

	/**
	 * This is called by the zip utility when attempting to zip a file and
	 * discovery that it is missing.
	 * 
	 * @param index
	 */
	public void missingFileUpdate(int index) {
		synchronized (fileIndicator) {
			fileIndicator.clear(index);
		}
	}

	public void deleteManifest() {
		_manifest.delete();
	}

	/**
	 * Used to verify whether this file needs to be acquired again.
	 * 
	 * @param fileInfo
	 *            The properties of the file [0] = fileName [1] = lastModified
	 *            [2] file length
	 * @param lastSync
	 *            the cookie value of the last completed sync time
	 * @return returns true if file is up to date and false otherwise
	 */
	private boolean verifyfile(String[] fileInfo, long lastSync) {
		File f = new File(context.getStaticSyncInfo().getBasePath()
				+ fileInfo[0]);
		return (f.exists() && Long.parseLong(fileInfo[1]) < lastSync && Long
				.parseLong(fileInfo[2]) == f.length());
	}
}
