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
package testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

// import org.glassfish.synchronization.hashing.MD5;

/**
 * this class is for testing purposes only
 * 
 * @author behrooz
 * 
 */
public class Verify {
	private static int count = 0;

	public static void main(String[] args) {
		handleFolder(new File("/files/"), "Instances/I--1155869325/");

		System.out.println("Total files are " + count);
	}

	private static void handleFolder(File dir, String BASE) {
		String[] children = dir.list();
		File f1;
		File f2;
		long size;
		for (int j = 0; j < children.length; j++) {
			f1 = new File(dir.getPath() + File.separator + children[j]);
			if (f1.isFile()) {
				count++;
				size = f1.length();
				f2 = new File(BASE + f1.getPath());
				if (!(f2.exists() && f2.length() == size)) {
					System.out.println(f2.getPath());
				} else {
					System.out.println("ok: " + f2.getPath());
				}
			} else if (f1.isDirectory()) {
				handleFolder(f1, BASE);
			}
		}
	}

	public static boolean verify(File manifest, String base,
			boolean hash_verify, Logger logger) {
		String str;
		String[] split;
		File f;
		boolean complete = true;
		int count = -1;
		long size;
		// String md5Hash;
		try {
			BufferedReader in = new BufferedReader(new FileReader(manifest));
			str = in.readLine();
			while ((str = in.readLine()) != null) {
				count++;
				split = str.split(",");
				f = new File(base + split[0]);
				size = Long.parseLong(split[2]);
				if (hash_verify && split.length > 3) {
					// md5Hash = split[3];
					// if(!f.exists() || f.length() != size ||
					// !md5Hash.equals(MD5.asHex(MD5.getHash(f)))){
					// complete = false;
					// logger.warning(f.getPath() + " manifest index: " +count +
					// " was not aqcuired or is corrupted");
					// }
				} else {
					if (!f.exists() || f.length() != size) {
						complete = false;
						logger.warning(f.getPath() + " manifest index: "
								+ count + " was not aqcuired or is corrupted");
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return complete;
	}
}
