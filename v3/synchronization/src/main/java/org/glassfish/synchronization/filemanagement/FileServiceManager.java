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

package org.glassfish.synchronization.filemanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.BitSet;
import java.util.logging.Level;

import org.glassfish.synchronization.client.SyncContext;
import org.glassfish.synchronization.message.ZipMessage;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.glassfish.synchronization.util.ZipUtility;

/**
 * Handles received files during synchronization for client. Also handles
 * creation of zip files for synchronization server.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class FileServiceManager {
	private FileCacheInterface fileCache = new SimpleFileCache();
	private SyncContext context;

	public FileServiceManager(SyncContext c) {
		context = c;
		context.setFileManager(this);
	}

	public synchronized File getZippedManifest() throws IOException {
		// create a zipped manifest if we don't have one
		File manifestZipped = fileCache.getZippedManifest(context
				.getManifestManager().getManVersion());
		if (manifestZipped == null) {
			return createZippedManifest();
		} else if (context.getLogger().isLoggable(Level.FINE)) {
			String log = "Reusing Manifest zippFile";
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().fine(log);
		}
		return manifestZipped;

	}

	public synchronized ZipMessage createReply(BitSet needs) throws IOException {
		ZipAndContent zip = createZipFromNeeds(needs);
		ZipMessage reply = new ZipMessage(zip, context.getManifestManager()
				.getBitManifest());
		return reply;
	}

	public ZipAndContent createZipFromNeeds(BitSet needs) throws IOException {
		if (needs == null || needs.isEmpty())
			return new ZipAndContent(null, new BitSet());
		// BitSet content = new
		// BitSet(context.getManifestManager().getnumFiles());
		BitSet zippedBits = context.getManifestManager().getBitManifest();
		zippedBits.and(needs);// gets the bits this instance can serve
		// File zip = null;
		if (zippedBits.isEmpty()) {
			return new ZipAndContent(null, null);
		}
		ZipInfo z = fileCache.getZippedContAndFile(zippedBits, context
				.getManifestManager().getManVersion(), context.getZipUtility());
		return z;
	}

	public void handleReceivedFile(ZipAndContent zip)
			throws FileNotFoundException, IOException {
		if (zip.file == null || zip.content.isEmpty()) {
			return;
		}
		context.getZipUtility().unzipit(zip.file,
				context.getStaticSyncInfo().getBasePath());
		context.getManifestManager().updateMyBits(zip.content);
		File f = zip.file;
		// System.out.println(f.getAbsolutePath());
		File moveTo = new File(context.getStaticSyncInfo().getTempFolder()
				+ f.getName());
		moveTo.getParentFile().mkdirs();

		boolean succeed = f.renameTo(moveTo);
		if (!succeed) {
			f.delete();
			return;
		}
		zip.file = moveTo;
		fileCache.addToCache(zip, context.getManifestManager().getManVersion());
	}

	private void regulatContent(BitSet serving) {
		int nextClear = 0;
		while (serving.cardinality() > MAX_FILE_SERVICE) {
			nextClear = serving.nextSetBit(nextClear);
			serving.clear(nextClear);
		}
	}

	private File createZippedManifest() throws IOException {
		long manVersion = context.getManifestManager().getManVersion();
		String manifestZipName = manVersion + StaticSyncInfo.getManFileName()
				+ ".zip";
		File manifestZipped = new File(context.getStaticSyncInfo()
				.getTempFolder()
				+ manifestZipName);
		manifestZipped.getParentFile().mkdirs();
		ZipUtility z = context.getZipUtility();
		// System.out.println("manifest path is " +
		// manifestZipped.getAbsolutePath());
		z.createManifestZip(context.getStaticSyncInfo().getManifestFilePath(),
				manifestZipped);
		fileCache.handleManifest(manifestZipped, manVersion);
		return manifestZipped;
	}

	/**
	 * returns the hit-rate of the internal cache
	 * 
	 * @return
	 */
	public double getCacheHitRate() {
		return fileCache.getHitRate();
	}

	private static final int MAX_FILE_SERVICE = Integer.MAX_VALUE;
}
