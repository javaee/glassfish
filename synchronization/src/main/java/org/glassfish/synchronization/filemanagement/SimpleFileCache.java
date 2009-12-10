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
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.ZipUtility;

/**
 * This file Cache object uses a simple LRU file cache
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SimpleFileCache implements FileCacheInterface {
	// private LinkedList<ZipInfo> fileQueue = new LinkedList<ZipInfo>();
	private final int cacheSize = 100;
	private HashMapFileCache lruCache = new HashMapFileCache(cacheSize);
	private File zippedManifest = null;
	private long manVersion = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see loadBalancer.FileCacheInterface#getZippedFile(java.util.BitSet)
	 */
	public ZipInfo getZippedContAndFile(BitSet b, long manV, ZipUtility zutil)
			throws IOException {
		ZipInfo zipC = null;
		synchronized (lruCache) {
			zipC = lruCache.get(b);
			if (zipC == null || (zipC.file != null && !zipC.file.exists())
					|| zipC.manVersion != manV || !zipC.content.equals(b)) {
				// System.out.println("had to create for " + b.toString());
				zipC = new ZipInfo(manV);
				lruCache.put(b, zipC);
			}
		}
		// This is done so multiple identical zips of the same file
		// are not made
		BitSet content = new BitSet();
		synchronized (zipC) {
			if (!zipC.isCreated()) { // create the file
				zipC.setFile(zutil.createZip(b, content));
				zipC.file.deleteOnExit();
				zipC.content = content;
			}
			// else{
			// System.out.println("reusing file " + zipC.file.getName());
			// }
		}
		return zipC;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see loadBalancer.FileCacheInterface#handleGeneratedFile(loadBalancer.FileServiceManager.ZipContent)
	 */
	public void handleGeneratedFile(ZipInfo zc) {
		synchronized (lruCache) {
			if (lruCache.containsKey(zc.content)) {// hash key conflict
				lruCache.remove(zc.content);
			}
			lruCache.put(zc.content, zc);
			zc.file.deleteOnExit();
		}
	}

	public File getZippedManifest(long manV) {
		if (manVersion == manV)
			return zippedManifest;
		else {
			if (zippedManifest != null)
				zippedManifest.delete();
			zippedManifest = null;
			manVersion = -1;
			return null;
		}
	}

	public void handleManifest(File zManifest, long manV) {
		zippedManifest = zManifest;
		zippedManifest.deleteOnExit();
		manVersion = manV;
	}

	public void addToCache(ZipAndContent zip, long manV) {
		handleGeneratedFile(new ZipInfo(zip, manV));
	}

	public double getHitRate() {
		return lruCache.getHitPercentage();
	}

}
