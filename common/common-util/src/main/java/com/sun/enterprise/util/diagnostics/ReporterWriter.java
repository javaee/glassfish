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

package com.sun.enterprise.util.diagnostics;

import java.io.*;
import java.util.*;
import com.sun.enterprise.util.StringUtils;

/** The basic mechanism used to "report".
 *
 * sends output to a Swing window.
 */
public class ReporterWriter implements IReporterEnum
{
    /** Create a new place (or mechanism) for routing output from the Reporter classes.
     *
     * @param title The title of the output window
     */    
	protected ReporterWriter(String title)
	{
		this.title = title;
		out = System.out;
	}

	///////////////////////////////////////////////////////////////
	
        /** puts a message in the window.
         *
         * Subclasses should redefine this function to change the report output destination.
         * @param severity The severity level of the message.  This is translated into one of the named constants
         * for output.
         * @param s The text of the message to "report".
         */        
	protected void println(int severity, String s)
	{
		String type;
		String msg;
		
		/*
		if(severity > WARN)
		{
			type = "<B>" + severityNames[severity] + "</B>";
			msg = StringUtils.padRight(type, longestSeverityLength + 7) + s;
		}
        else
		*/
		{
			type = severityNames[severity];
			msg = StringUtils.padRight(severityNames[severity], longestSeverityLength) + s;
		}

        out.println(msg);
		getFrame().pr(msg);
	}

	///////////////////////////////////////////////////////////////
	
	private ReporterFrame getFrame()
	{
		if(frame == null)
		{
			// first call!!
			frame = new ReporterFrame(title);
			frame.show();
			calcLongestString();
		}

		return frame;
	}

	///////////////////////////////////////////////////////////////
	
	private void calcLongestString()
	{
		int maxLen = 0;

		for(int i = 0; i < severityNames.length; i++)
		{
			int len = severityNames[i].length();

			if(len > maxLen)
				maxLen = len;
		}
		longestSeverityLength = maxLen + 2;
	}

	///////////////////////////////////////////////////////////////
	
	private PrintStream		out;
	private	ReporterFrame	frame	= null;
	private	int				longestSeverityLength;
	private String			title;

}

