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

import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.ZipUtility;

/**
 * Interface for file caches
 * 
 * @author Behrooz Khorashadi
 * 
 */
public interface FileCacheInterface {
	/**
	 * Deals with a zipped files and caches them
	 * 
	 * @param zc
	 *            the object that holds both the pointer to the zip file and the
	 *            bitset that indicates content
	 */
	public void handleGeneratedFile(ZipInfo zc);

	/**
	 * returns the ZipContent object which contains the file and bitset that
	 * indicates content
	 * 
	 * @param b
	 *            the bitset that is needed
	 * @return the ZipContent object and null if nothing exists
	 */
	public ZipInfo getZippedContAndFile(BitSet b, long manV, ZipUtility zutil)
			throws IOException;

	/**
	 * Caches the zipped up manifest file
	 * 
	 * @param zippedManifest
	 */
	public void handleManifest(File zippedManifest, long manV);

	/**
	 * returns the zipped manifest file
	 * 
	 * @param the
	 *            version on the manifest
	 * @return
	 */
	public File getZippedManifest(long manV);

	/**
	 * Move a received file to temp directory add it to the cached files if
	 * appropriate
	 * 
	 * @param zip
	 */
	public void addToCache(ZipAndContent zip, long manV);

	/**
	 * Calculates the hit rate of the internally implemented cache
	 * 
	 * @return a double representation of the hit rate
	 */
	public double getHitRate();
}
