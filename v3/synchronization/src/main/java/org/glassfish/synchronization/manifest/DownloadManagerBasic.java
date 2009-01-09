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

import java.util.BitSet;

/**
 * Class is responsible for which files to download next
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class DownloadManagerBasic implements DownloadManagerInterface {
	// private int blocked = 0;
	/** An array corresponding to each files length */
	long[] file_lengths;
	/**
	 * Maintains the set of bits correspondig to the set of files that are
	 * currently being requeste
	 */
	private BitSet pendingReqBits;
	// private BitSet pendingReqBitsSnapShot = null;
	/** A pointer back to the manifest manager */
	ManifestManager m;

	public DownloadManagerBasic(ManifestManager man, long[] file_lengths) {
		m = man;
		pendingReqBits = new BitSet(m.getnumFiles());
		this.file_lengths = file_lengths;
	}

	public synchronized BitSet getNextRequestSet(BitSet server_has) {
		BitSet possible_req = new BitSet(m.getnumFiles());
		BitSet iHave = m.getBitManifest();
		iHave.or(pendingReqBits);
		possible_req.set(0, m.getnumFiles());// set all the bits up to the number
											// of files
		possible_req.xor(iHave); // reqstBits now contains what this machine does
								// not have
		if (server_has != null)
			possible_req.and(server_has); // all the Bits I can get from server
		// checkForBlocks(reqstBits);TODO: figure out how to check for blocked
		// requests
		
		BitSet req_bits = new BitSet(m.getnumFiles());
		long total_size=0;
		int i = 0;
		while(total_size <= MAX_FILE_BUNDLE_SIZE) {
			i = possible_req.nextSetBit(i);
			if(i == -1)
				break;
			req_bits.set(i);
			total_size += file_lengths[i];
			i++;
		}
//		System.out.println("total number of files requested is " + total_size + " at a size of " + req_bits.cardinality());
//		
//		while (possible_req.cardinality() > MAX_NUMBER_REQUESTING_FILES) {
//			i = possible_req.nextSetBit(i);
//			possible_req.flip(i);
//		}
		pendingReqBits.or(req_bits);
		return req_bits; // bits I am requesting.
	}

	public synchronized void resetPendingBits(BitSet reset) {
		pendingReqBits.andNot(reset);
		// System.out.println("resetting " + reset.toString());
	}

	public synchronized void releasePendingBits() {
		pendingReqBits.clear();
	}
	/** The maximum bytes to request at once */
	private static final long MAX_FILE_BUNDLE_SIZE = 10485760;// 10 MBytes
	private static final int MAX_NUMBER_REQUESTING_FILES = 30;
}
