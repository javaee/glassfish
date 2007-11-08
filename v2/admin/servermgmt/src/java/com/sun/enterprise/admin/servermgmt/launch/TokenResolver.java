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

/*
 * TokenResolver.java
 *
 * Created on April 20, 2007, 11:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.launch;

import java.util.*;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
/**
 * Here is the contract:
 * You give me a Properties object.
 * Then you can call  resolve(List<String>) and/or resolve(String)
 * I will find and replace the tokens, e.g.,  ${foo} with the value of "foo" in the properties.
 * If the token has no such property -- then I leave the token as is.
 * It purposely does not handle nested tokens.  E.g. if the "foo" property has another
 * token embedded in the value -- it will not be further resolved.
 * This is the KISS principle in action...
 * @author bnevins
 */
class TokenResolver
{
    TokenResolver(Properties p)
    {
        props = p;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    void resolve(List<String> list)
    {
        for(int i = 0; i < list.size(); i++)
        {
            String s = list.get(i);
            
            if(hasToken(s))
                list.set(i, resolve(s));
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    String resolve(String s)
    {
        List<Token> tokens = getTokens(s);
        String resolved = s;
        
        for(Token token : tokens)
            resolved = StringUtils.replace(resolved, token.token, token.value);
        
        return resolved;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private List<Token> getTokens(String s)
    {
        int index = 0;        
        List<Token> tokens = new ArrayList<Token>();
     
        while(true)
        {
            Token token = getToken(s, index);
            
            if(token == null)
                break;
            
            tokens.add(token);
            index = token.start + Token.TOKEN_START.length();
        }

        return tokens;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private Token getToken(String s, int index)
    {
        if(s == null || index >= s.length())
            return null;
        
        Token token = new Token();
        token.start = s.indexOf(token.TOKEN_START, index);
        token.end   = s.indexOf(token.TOKEN_END, token.start + 2);
        
        if(token.end <= 0 || token.start < 0)
            return null;
        
        token.token = s.substring(token.start, token.end + 1);
        token.name = s.substring(token.start + Token.TOKEN_START.length(), token.end);
        
        // if the token exists, but it's value is null -- then set the value 
        // back to the token.

        token.value = props.getProperty(token.name, token.token);
        return token;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean hasToken(String s)
    {
        return s != null && s.indexOf(Token.TOKEN_START) >= 0;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final Properties props;

    private static class Token
    {
        int                 start;
        int                 end;
        String              token;
        String              name;
        String              value;
        final static String TOKEN_START = SystemPropertyConstants.OPEN;
        final static String TOKEN_END = SystemPropertyConstants.CLOSE;
        public String toString()
        {
            return "name: " + name + ", token: " + token + ", value: " + value;
        }
    }
}
