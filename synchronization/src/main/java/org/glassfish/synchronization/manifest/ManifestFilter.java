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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.synchronization.util.FileUtils;

/**
 * This class is used to filter files during manifest creation and during
 * the clean up phase which is done after a synchronization on glassfish 
 * instances. 
 * @author Behrooz Khorashadi
 *
 */
public class ManifestFilter implements FileFilter {
	/** Check files to see if they start with */
	List<String> startsWith = new LinkedList<String>();
	/** Check files to see if they end with these */
	List<String> endsWith = new LinkedList<String>();
	/** check to see if the file path is  */
	List<String> is = new LinkedList<String>();
	/** check if file path contains any of these strings */
	List<String> contains = new LinkedList<String>();
	
	public ManifestFilter(File filterList) throws IOException {
		createFilterStrings(filterList);
	}
	/**
	 * Takes in a file and creates the pathname string filters.
	 * @param filterList the file containing the filter strings
	 * @throws IOException 
	 */
	private void createFilterStrings(File filterList) 
												throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filterList));
	    String str;
	    boolean start, end;
	    while ((str = in.readLine()) != null) {
	    	str = FileUtils.formatPath(str);
	    	start = str.startsWith("*");
	    	end = str.endsWith("*");
	    	if(start && end){
	    		str = str.replace("*", "");
	    		contains.add(str);
	    	} else if(start) {
	    		str = str.substring(1);
	    		endsWith.add(str);
	    	} else if(end) {
	    		str = str.substring(0, str.length()-1);
	    		startsWith.add(str);
	    	} else {
	    		is.add(str);
	    	}
	    }
	    in.close();
	}
	public boolean accept(File pathname) {
		String path = FileUtils.formatPath(pathname.getAbsolutePath());
		for (String start : startsWith) {
			if(path.startsWith(start))
				return false;
		}
		for (String end : endsWith) {
			if(path.endsWith(end))
				return false;
		}
		for (String contain : contains) {
			if(path.contains(contain))
				return false;
		}
		for (String equal : is) {
			if(path.equals(equal))
				return false;
		}
		return true;
	}

}
