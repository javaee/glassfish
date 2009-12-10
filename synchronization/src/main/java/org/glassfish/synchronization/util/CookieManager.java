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

/**
 * Handles the cookie which indicates when the last sync was completed.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class CookieManager {
	SyncContext context;

	public CookieManager(SyncContext c) {
		context = c;
		c.setCookieManager(this);
	}

	public long getCookie() {
		long returnValue = -1;
		String str = "";
		try {
			// System.out.println(StaticSyncInfo.getCookieFilePath());
			BufferedReader in = new BufferedReader(new FileReader(context
					.getStaticSyncInfo().getCookieFilePath()));
			str = in.readLine();
			in.close();
			returnValue = Long.parseLong(str);
		} catch (IOException e) {
			return -1;
		}
		return returnValue;
	}

	/**
	 * When sync process has been completed and verified create a cookie with
	 * the time in the manifest that was acquired.
	 * 
	 * @throws IOException
	 */
	public void createCookie() throws IOException {
		File cook = new File(context.getStaticSyncInfo().getCookieFilePath());
		if (!cook.exists()) {
			cook.createNewFile();
		}
		cook.delete();
		BufferedWriter out = new BufferedWriter(new FileWriter(context
				.getStaticSyncInfo().getCookieFilePath()));
		out.write("" + context.getManifestManager().getManVersion());
		out.close();
	}

	public void createCookieFromManifest() throws IOException {
		File cook = new File(context.getStaticSyncInfo().getCookieFilePath());
		if (!cook.exists()) {
			cook.createNewFile();
		}
		cook.delete();
		BufferedReader in = new BufferedReader(new FileReader(context
				.getStaticSyncInfo().getManifestFilePath()));
		String str = in.readLine();
		String[] split = str.split(",");
		long cookieValue = Long.parseLong(split[2]);
		BufferedWriter out = new BufferedWriter(new FileWriter(context
				.getStaticSyncInfo().getCookieFilePath()));
		out.write("" + cookieValue);
		out.close();
	}

	public void deleteCookie() {
		File cookie = new File(context.getStaticSyncInfo().getCookieFilePath());
		cookie.delete();
	}
}
