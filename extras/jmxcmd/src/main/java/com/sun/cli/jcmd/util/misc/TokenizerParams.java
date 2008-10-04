/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/TokenizerParams.java,v 1.2 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:24 $
 */
 
package com.sun.cli.jcmd.util.misc;


/**
	Parameters that TokenizerImpl accepts.
 */
public class TokenizerParams
{
	/**
		Delimiters are characters that separate tokens.
	 */
	public String		mDelimiters;
	
	/**
		When multiple delimiters abut each other, are they all treated as
		a single delimiter, or as multiples with implied empty tokens
		between them?
	 */
	public boolean		mMultipleDelimsCountAsOne;
	
	/**
		The escape char, allowed to be anything, but typically should
		be BACKSLASH.
	 */
	public char			mEscapeChar;
	
	/**
		Characters which may be escaped over and above the standard ones.
	 */
	public String		mEscapableChars;
	
	/**
		When an invalid escape sequence is encountered, either an exception
		may be thrown, or the sequence may be emitted literally.
	 */
	public boolean		mEmitInvalidEscapeSequencesLiterally;
	
	public final static char	BACKSLASH	= '\\';
	public final static char	COMMA	= ',';
	public final static char	DEFAULT_ESCAPE_CHAR	= BACKSLASH;
	public final static String	DEFAULT_DELIMITERS	= "" + COMMA;
	
		public
	TokenizerParams()
	{
		mDelimiters					= DEFAULT_DELIMITERS;
		mMultipleDelimsCountAsOne	= true;
		mEscapeChar					= DEFAULT_ESCAPE_CHAR;
		mEscapableChars				= "" + DEFAULT_ESCAPE_CHAR;
		mEmitInvalidEscapeSequencesLiterally	= false;
	}
	
		public void
	ensureDelimitersEscapable()
	{
		for( int i = 0; i < mDelimiters.length(); ++i )
		{
			final char	delim	= mDelimiters.charAt( i );
			
			if ( mEscapableChars.indexOf( delim ) < 0 )
			{
				mEscapableChars	= mEscapableChars + delim;
			}
		}
	}
}

