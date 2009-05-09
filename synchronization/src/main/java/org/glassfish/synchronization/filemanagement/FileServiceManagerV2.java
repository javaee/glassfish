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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.synchronization.client.SyncContext;
import org.glassfish.synchronization.message.FileResponse;
import org.glassfish.synchronization.message.FileVersionResponse;
import org.glassfish.synchronization.message.ZipMessage;

/**
 * This file manager class is used to mimic v2 behavior. ie on an incomming
 * request all needed files are zipped and sent.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class FileServiceManagerV2 {
	private LRUCache fileHash = new LRUCache();
	private SyncContext context;

	public FileServiceManagerV2(SyncContext c) {
		context = c;
	}

	public FileVersionResponse createReply(long timestamp) throws IOException {
		MyFile zip;
		synchronized (fileHash) {
			zip = fileHash.get(timestamp);
			if (zip == null) {
				zip = new MyFile();
				fileHash.put(timestamp, new MyFile());
			}
		}
		synchronized (zip) {
			if (!zip.isCreated()) {
				BitSet content = new BitSet(context.getManifestManager()
						.getnumFiles());
				BitSet needs = createBitSet(timestamp);
				if (needs.isEmpty()) {
					zip.setFile(null);
					fileHash.put(timestamp, zip);
					return new FileVersionResponse(null, context
							.getManifestManager().getManVersion());
				}
				zip.setFile(context.getZipUtility().createZip(needs, content));
				fileHash.put(timestamp, zip);
			}
		}
		return new FileVersionResponse(zip.file, context.getManifestManager()
				.getManVersion());
	}

	private BitSet createBitSet(long version) throws IOException {
		BitSet fileIndicator = new BitSet(context.getManifestManager()
				.getnumFiles());
		BufferedReader in = null;
		in = new BufferedReader(new FileReader(context.getManifestManager()
				.getManifest()));
		String str;
		String[] split;
		int lineCount = 0;
		str = in.readLine();// get rid of the first line
		while ((str = in.readLine()) != null) {
			split = str.split(",");
			if (verifyfile(split, version)) {
				fileIndicator.set(lineCount);
			}
			lineCount++;
		}
		return fileIndicator;
	}

	private boolean verifyfile(String[] split, long v) {
		Long l = Long.parseLong(split[1]);
		return v < l;
	}

	private class LRUCache extends LinkedHashMap<Long, MyFile> {
		private static final long serialVersionUID = 1L;
		private final int capacity = 10;

		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() >= capacity;
		}
	}

	private class MyFile {
		public File file;
		private boolean created = false;

		public MyFile() {
			file = null;
		}

		public void setFile(File f) {
			created = true;
			file = f;
		}

		public boolean isCreated() {
			return created;
		}
	}
}
