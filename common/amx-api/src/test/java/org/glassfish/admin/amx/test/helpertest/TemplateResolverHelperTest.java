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
package org.glassfish.admin.amx.test.helpertest;

import com.sun.appserv.management.config.TemplateResolver;
import com.sun.appserv.management.helper.TemplateResolverHelper;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.Map;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public final class TemplateResolverHelperTest
        extends junit.framework.TestCase
{
    private final Map<String,String>     mPairings;
    private final MyResolver             mResolver;
    private final TemplateResolverHelper mHelper;

    public TemplateResolverHelperTest()
    {
        mPairings = createPairings();
        mResolver = new MyResolver( mPairings );
        mHelper = new TemplateResolverHelper( mResolver );
    }
    
    /**
      Mock object for unit test.
     */
    private static final class MyResolver implements TemplateResolver
    {
        public final Map<String,String> mPairings;
        
        MyResolver( final Map<String,String> pairings )
        {
            mPairings = pairings;
        }
        public String resolveTemplateString( final String t )
        {
            if ( ! TemplateResolverHelper.isTemplateString(t) ) return t;
            
            final String key = t.trim();
            return mPairings.containsKey(key ) ? mPairings.get( key ) : null;
        }
    }
    
    
    private Map<String,String>
    createPairings()
    {
        return MapUtil.newMap( new String[]
            {
                "${str-1}", "/foo/bar",
                "${int-1}", "8080",
                "${boolean-true}", "true",
                "${boolean-false}", "false",
            });
    }
    
    @Test
    public void testResolveTypes()
    {
        for( final String key : mPairings.keySet() )
        {
            assertEquals( mPairings.get(key), mHelper.resolve(key) );
            
            // white space should be ignored, too
            assertEquals( mPairings.get(key), mHelper.resolve(" " + "\t" + key) );
            assertEquals( mPairings.get(key), mHelper.resolve(" " + "\t" + key + " \n") );
        }
        
        String key = "${int-1}";
        assertEquals( "" + mPairings.get(key), "" + mHelper.resolveInt(key) );
        
        key = "${boolean-true}";
        assertEquals( "true", "" + mHelper.resolveBoolean(key) );
        
        key = "${boolean-false}";
        assertEquals( "false", "" + mHelper.resolveBoolean(key) );
    }
    
    @Test
    public void testNullRemainsNull()
    {
        final String key = null;
        assertEquals( key, mHelper.resolve(key));
    }
    
    @Test
    public void testEmptyRemainsEmpty()
    {
        final String key = "";
        assertEquals( key, mHelper.resolve(key));
    }
    
    @Test
    public void testDoesNotResolve()
    {
        final String key = "${does-not-exist}";
        assertEquals( null, mHelper.resolve(key));
    }
    
    @Test
    public void testLiterals()
    {
        // literals should resolve to themselves
        String key = "hello";
        assertEquals( key, mHelper.resolve(key));
        
        // literals should resolve to themselves
        key = "true";
        assertEquals( key, mHelper.resolve(key));
    }
    
    @Test
    public void testMalformed()
    {
        final String key1 = "${does-not-exist";
        assertEquals( key1, mHelper.resolve(key1));
        
        final String key2 = "$does-not-exist}}";
        assertEquals( key2, mHelper.resolve(key2));
    }

}














