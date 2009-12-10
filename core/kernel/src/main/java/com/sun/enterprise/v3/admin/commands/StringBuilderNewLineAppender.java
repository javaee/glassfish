/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin.commands;
import java.io.*;

/**
 */
class StringBuilderNewLineAppender {
    
    private  StringBuilder sb;
    static final String SEP = System.getProperty("line.separator");
    /** Creates a new instance of StringBuilderNewLineAppender */
    StringBuilderNewLineAppender(final StringBuilder sb) {
        this.sb = sb;
    }
    StringBuilderNewLineAppender append(final String s) {
        sb.append(s);
        sb.append(SEP);
        return ( this );
    }
    public String toString() {
        return ( sb.toString() );
    }
    public String toString(String... filterOut) {
        String sbString = sb.toString();
        BufferedReader in = new BufferedReader(new StringReader(sbString));
		sb = new StringBuilder();
		
		try
		{
			readloop:
			for(String s = in.readLine(); s != null; s = in.readLine()){
				for(String filter : filterOut){
					if(s.startsWith(filter))
						continue readloop; // continue to outer loop
				}
				append(s);
			}
		}
		catch(Exception e)
		{
			// bail
			return sbString;
		}
        
		return toString();
    }

}
