/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.ejte.ccl.webrunner.webtest;

public class HttpResponse
{
	int statusCode;
	String reasonPhrase;
	MimeHeader mh;
	static String CRLF="\r\n";

	void parse(String request)
	{
		int fsp=request.indexOf(' ');
		int nsp=request.indexOf(' ',fsp+1);
		int eol=request.indexOf('\n');
		String protocol=request.substring(0,fsp);
		statusCode=Integer.parseInt(request.substring(fsp+1,nsp));
		reasonPhrase=request.substring(nsp+1,eol);
		String raw_mime_header=request.substring(eol+1);
		mh=new MimeHeader(raw_mime_header);
	}

	HttpResponse(String request)
	{
		parse(request);
	}

	HttpResponse(int code,String reason,MimeHeader m)
	{
		statusCode=code;
		reasonPhrase=reason;
		mh=m;
	}

	public String toString()
	{
		return "HTTP/1.0 "+ statusCode +" " + reasonPhrase + CRLF + mh + CRLF;
        }
}
