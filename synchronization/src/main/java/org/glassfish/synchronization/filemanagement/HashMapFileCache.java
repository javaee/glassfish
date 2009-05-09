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

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple LRU hashmap
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class HashMapFileCache extends LinkedHashMap<BitSet, ZipInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3002324006361955001L;
	private final int capacity;
	private long accessCount = 0;
	private long hitCount = 0;

	public HashMapFileCache(int capacity) {
		super(capacity + 1, 1.1f, true);
		this.capacity = capacity;
	}

	@Override
	public ZipInfo get(Object key) {
		accessCount++;
		if (containsKey(key)) {
			hitCount++;
		}
		ZipInfo value = super.get(key);
		return value;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		if (size() > capacity) {
			ZipInfo z = (ZipInfo) eldest.getValue();
			z.file.delete();
			return true;
		}
		return false;
	}

	@Override
	public ZipInfo remove(Object key) {
		ZipInfo z = null;
		if ((z = get(key)) != null)
			z.file.delete();
		return super.remove(key);
	}

	public long getAccessCount() {
		return accessCount;
	}

	public long getHitCount() {
		return hitCount;
	}

	public double getHitPercentage() {
		double hitP = (double) hitCount / accessCount;
		return hitP;
	}

}
