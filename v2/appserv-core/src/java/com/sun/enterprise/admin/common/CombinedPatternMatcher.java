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

package com.sun.enterprise.admin.common;

//Admin imports
import com.sun.enterprise.admin.util.IPatternMatcher;
import com.sun.enterprise.admin.util.RegExpMatcher;
import com.sun.enterprise.admin.util.GeneralPatternMatcher;
import com.sun.enterprise.admin.util.Logger;

// i18n import 
import com.sun.enterprise.admin.util.SOMLocalStringsManager;

public class CombinedPatternMatcher implements IPatternMatcher
{
	private String          mPattern		= null;
	private String          mTestString		= null;
	private IPatternMatcher mRealMatcher    = null;

	// i18n SOMLocalStringsManager
	private static SOMLocalStringsManager localizedStrMgr =
		SOMLocalStringsManager.getManager( CombinedPatternMatcher.class );

	/** 
		Creates new CombinedPatternMatcher with pattern string and
		string on which the patten has to be matched. None of the
		strings may be null.
		
		@param patternString the Pattern string.
		@param testString the string on which pattern should be applied.
	*/
    
	public CombinedPatternMatcher(String patternString, String testString)
	{
        if (patternString == null || testString == null)
        {
			String msg = localizedStrMgr.getString( "admin.common.combinedpattermatcher_null_arg" );
            throw new IllegalArgumentException( msg );
        }
		mPattern	= patternString;
		mTestString	= testString;
        if (isJDK14())
        {
//            Logger.log("using the jdk 1.4 regular expression facility");
            mPattern = translateFromJMXToJDK14(patternString);
            mRealMatcher = new RegExpMatcher(mPattern, testString);
        }
        else
        {
//            Logger.log("using pattern matcher without jdk 1.4");
            mRealMatcher = new GeneralPatternMatcher(patternString, testString);
        }
    }

	public boolean matches()
	{
        return ( mRealMatcher.matches() );
	}
	
	public boolean isJDK14()
	{
		String javaSpecVersion = System.getProperty("java.specification.version");
		
		return ( javaSpecVersion.startsWith("1.4") );
	}
    
    private String translateFromJMXToJDK14(String aString)
    {
        String dotEscpapedString    = escapeDots(aString);
        String starReplacedString   = insertDotBeforeStar(dotEscpapedString);
        String qmReplacedString     = insertDotBeforeQM(starReplacedString);
        return ( qmReplacedString );
    }
    
    private String escapeDots(String aString)
    {
        char escape      = Tokens.kEscapeChar;
        char dot         = Tokens.kDelimiterChar;
        return ( insertCharBefore(aString, escape, dot) );
    }
    private String insertDotBeforeStar(String aString)
    {
        char dot        = Tokens.kDelimiterChar;
        char star       = Tokens.kWildCardChar;
        
        return ( insertCharBefore(aString, dot, star) );
    }
    private String insertDotBeforeQM(String aString)
    {
        char dot        = Tokens.kDelimiterChar;
        char qm         = ObjectNames.kSingleMatchChar;
        
        return ( insertCharBefore(aString, dot, qm) );
    }
    private String insertCharBefore(String aString, char insChar, char beforeChar)
    {
        StringBuffer    destBuffer  = new StringBuffer();
        char[]          srcArray    = aString.toCharArray();
        for (int i = 0 ; i < srcArray.length ; i++)
        {
            char ch = srcArray[i];
            if (ch == beforeChar)
            {
                destBuffer.append(insChar);
            }
            destBuffer.append(ch);
        }
        return ( destBuffer.toString() );
    }
}
