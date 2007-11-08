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
package com.sun.enterprise.diagnostics.util;
import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.sun.enterprise.diagnostics.Constants;

/**
 * Sorts server.log files in the ascending order by date.
 * @author Manisha Umbarje
 */
public class LogNameComparator implements Comparator  {

    private static final SimpleDateFormat dateFormat = 
    new SimpleDateFormat(Constants.DATE_PATTERN);
    
    public LogNameComparator() {
    }

    public int compare(Object obj1 , Object obj2) {
	String name1 = ((File)obj1).getName();
	String name2 = ((File)obj2).getName();

	if (name1 == null || name2 == null)
	    return 0;

	//Log Files are in the format server.log_yyyy-mm-ddThh-mm-ss
	int name1DateBeginIndex = name1.indexOf
				(Constants.FILENAME_DATE_SEPARATOR)  + 1;
	int name2DateBeginIndex = name2.indexOf
				(Constants.FILENAME_DATE_SEPARATOR) + 1;

	// obj1 represents server.log i.e latest file
	if (name1DateBeginIndex <=  0) {
	    return 1;
	}

	// obj2 represents server.log i.e latest file
	if(name2DateBeginIndex <= 0) {
	    return -1;
	}

	try {
	    Date name1Date = dateFormat.parse
		(name1.substring
		(name1DateBeginIndex,name1DateBeginIndex + 
		Constants.ENTRY_DATE_LENGTH));
	    
	    Date name2Date = dateFormat.parse
		(name2.substring
		(name2DateBeginIndex,name1DateBeginIndex + 
		Constants.ENTRY_DATE_LENGTH));

	    if (name1Date.after(name2Date))
		return 1;
	    else
		return -1;
	} catch (ParseException exc) {
	    return 0;
	}
    }
}
