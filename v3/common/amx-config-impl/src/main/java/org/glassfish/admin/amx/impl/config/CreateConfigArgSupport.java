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
package org.glassfish.admin.amx.impl.config;

import org.glassfish.admin.amx.intf.config.PropertiesAccess;
import org.glassfish.admin.amx.intf.config.SystemPropertiesAccess;
import org.glassfish.admin.amx.util.TypeCast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
    Gets information about available contained types (interfaces)
 */
final class CreateConfigArgSupport
{
    /** values are 'Object'; must support both String and String[] */
    private final Map<String,Object> mAttrs;
    private final Map<String,String> mProperties;
    private final Map<String,String> mSystemProperties;
    private final Object[] mArgs;

    private static void debug( final String s ) { System.out.println(s); }
    
    
    public CreateConfigArgSupport( final Map<String,Object> params )
    {
        mArgs = null;
        
        final Map<String,Object> attrs = params == null ? new HashMap<String,Object>() : new HashMap<String,Object>(params);
        mProperties = extractProperties( attrs, PropertiesAccess.PROPERTY_PREFIX);
        mSystemProperties = extractProperties( attrs, SystemPropertiesAccess.SYSTEM_PROPERTY_PREFIX);
        mAttrs = stringifyMap(attrs);
    }
    
    public CreateConfigArgSupport( 
        final Object[] argsIn,
        String[]	   types )
    {
        if ( argsIn == null ) throw new IllegalArgumentException();
        
        /*
          Determine if this create has an optional Map as the last argument; could be of the form:
                createFooConfig(p1, p2, ..., Map optional)
                createFooConfig(p1, p2, ..., pN)
                createFooConfig(optional)
                createFooConfig()
         */
        boolean haveOptionalAttrsArg = false;
        Map<String,Object> optionalAttrs = null;
        if ( argsIn.length >= 1 )
        {
            Object lastArg = argsIn[argsIn.length-1];
            if ( lastArg instanceof Map || types[types.length-1].equals( Map.class.getName()) )
            {
                optionalAttrs   = TypeCast.checkMap( TypeCast.asMap(lastArg), String.class, Object.class);
                haveOptionalAttrsArg = true;
            }
        }
        //cdebug( "createConfig: " + operationName + ", args = " + StringUtil.toString(args) + ", types = " + StringUtil.toString(types) + " ===> numRequiredArgs = " + numRequiredArgs + ", optionalAttrs = " + MapUtil.toString(optionalAttrs) );
        if ( haveOptionalAttrsArg )
        {
            mArgs = new Object[argsIn.length - 1];
            System.arraycopy( argsIn, 0, mArgs, 0, mArgs.length );
        }
        else
        {
            mArgs = argsIn;
        }
        
        final Map<String,Object> attrs = new HashMap<String,Object>();
        if ( optionalAttrs != null )
        {
            attrs.putAll( optionalAttrs );
        }
        mProperties = extractProperties( attrs, PropertiesAccess.PROPERTY_PREFIX);
        mSystemProperties = extractProperties( attrs, SystemPropertiesAccess.SYSTEM_PROPERTY_PREFIX);
        
        mAttrs = stringifyMap( attrs );
    }
    
    public int numArgs() { return mArgs == null ?  0 :mArgs.length; }
    
    public Map<String,Object> getAttrs() { return mAttrs; }
    public Map<String,String> getProperties() { return mProperties; }
    public Map<String,String> getSystemProperties() { return mSystemProperties; }
    
    public void addExplicitAttrs( final String[] names )
    {
        if ( names.length < mArgs.length ) throw new IllegalArgumentException();
        
        for ( int i = 0; i < mArgs.length; ++i )
        {
            if ( mAttrs.containsKey( names[i] ) )
            {
                throw new IllegalArgumentException();
            }
            
            mAttrs.put( names[i], asStringType( mArgs[i] ) );
        }
    }
    
        public static Object
    asStringType( final Object o )
    {
        Object result = o;
        
        if ( o != null )
        {
            if ( o == null || (o instanceof String) )
            {
                result = o;
            }
            else if ( (o instanceof Boolean) || (o instanceof Integer) || (o instanceof Long) )
            {
                result = "" + o;
            }
            else if ( ! (o instanceof String[]) )
            {
                throw new IllegalArgumentException( "Illegal value: " + o + " of class " + o.getClass().getName() );
            }
        }
        
        return result;
    }
    
       /**
        Extract properties beginning with the specified prefix.
        @return Map<String,String> containing property name, value
     */
        private Map<String,String>
    extractProperties(
        final Map<String,Object> optionalAttrs,
        final String  prefix )
    {
        Map<String,String> result = new HashMap<String,String>();
        if ( optionalAttrs != null )
        {
            final Set<String> toRemove = new HashSet<String>();
            
            for( final String key : optionalAttrs.keySet() )
            {
                if ( key.startsWith( prefix ) )
                {
                    final String propertyName = key.substring( prefix.length(), key.length() );
                    if ( propertyName.length() == 0 )
                    {
                        throw new IllegalArgumentException("Property names must be non-zero length" );
                    }
                    //cdebug( "put property: " + propertyName + " = " + optionalAttrs.get(key)  );
                    result.put( propertyName, "" + optionalAttrs.get(key) );
                    toRemove.add( key );
                }
            }
            
            // remove all items we extracted
            //cdebug( "removing properties: " + CollectionUtil.toString( toRemove ) );
            optionalAttrs.keySet().removeAll( toRemove );
        }
        return result;
    }
    
    /** 
       Ensure that all arguments are String or String[]
    */
        private Map<String,Object>
    stringifyMap( final Map<String,Object> m )
    {
        final Map<String,Object> result = new HashMap<String,Object>();
        if ( m != null )
        {
            for ( final String attrName : m.keySet() )
            {
                final Object value = asStringType(m.get(attrName));
                result.put( attrName, value );
            }
        }
        return result;
    }
}



























