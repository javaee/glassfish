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
package org.glassfish.admin.amx.config;

import com.sun.appserv.management.util.misc.CollectionUtil;
import org.glassfish.admin.amx.loader.AMXConfigVoid;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.api.amx.AMXCreatorInfo;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;

import java.util.*;


/**
    Gets information about available contained types (interfaces)
 */
final class ContainedTypeInfo
{
private static void cdebug( final String s ) { System.out.println(s); }
    
    final ConfigBean                 mConfigBean;
    final Map<String,Class<? extends ConfigBeanProxy>>  mMappings;

    /**
        Info will be about the sub-types of this ConfigBean
     */
    public ContainedTypeInfo( final ConfigBean configBean )
    {
        mConfigBean    = configBean;
        mMappings = findAllContainedJ2EETypes();
    }

        private static List<Class<? extends ConfigBeanProxy>>
    getSubTypes( final ConfigBean cb )
    {
         List<Class<? extends ConfigBeanProxy>> result = null;
        try
        {
        cdebug( "Calling ConfigSupport.getSubElementsTypes" );
            final Class<?>[] subTypes = ConfigSupport.getSubElementsTypes( cb );
        cdebug( "Calling ConfigSupport.getSubElementsTypes DONE" );
            result = new ArrayList();
            for( final Class<?> theClass : subTypes )
            {
                result.add( (Class<? extends ConfigBeanProxy>)theClass );
            }
        }
        catch( Exception e )
        {
            // OK
            result = Collections.emptyList();
            e.printStackTrace();
        }
        
        
        return result;
    }

        public Class<? extends ConfigBeanProxy>
    getConfigBeanProxyClassFor( final String j2eeType )
    {
        return mMappings.get( j2eeType );
    }
    
        AMXConfigInfoResolver
    getAMXConfigInfoResolverFor( final String j2eeType )
    {
        final Class<? extends ConfigBeanProxy>   proxyIntf = getConfigBeanProxyClassFor( j2eeType );
        
        if ( proxyIntf == null )
        {
            throw new IllegalArgumentException( "No such subtype for AMX j2eeType: " + j2eeType );
        }
        
        final AMXConfigInfo amxConfigInfo = proxyIntf.getAnnotation( AMXConfigInfo.class );
        
        return new AMXConfigInfoResolver( amxConfigInfo );
    }
    
        List<Class<? extends ConfigBeanProxy>>
    getAllContainedConfigBeanProxyTypes()
    {
        final ConfigBean cb = mConfigBean;
        
        final List<Class<? extends ConfigBeanProxy>> result = new ArrayList<Class<? extends ConfigBeanProxy>>();

        final List<Class<? extends ConfigBeanProxy>> candidates = getSubTypes( cb );
        result.addAll( candidates );
        
        // see if there is an explicit supplementary annotation
        final Class<? extends ConfigBeanProxy> proxyClass = cb.getProxyType();
        final AMXCreatorInfo creatorInfo = proxyClass.getAnnotation( AMXCreatorInfo.class );
        if ( creatorInfo != null )
        {
            final Class<? extends ConfigBeanProxy>[] creatables = creatorInfo.creatables();
            CollectionUtil.addAll( result, creatables );
        }
        
        return result;
    }

    /**
        @return Map keyed by AMX j2eeType to the matching ConfigBeanProxy interface
     */
        Map<String,Class<? extends ConfigBeanProxy>>
    findAllContainedJ2EETypes()
    {
       final String amxVoid = AMXConfigVoid.class.getName();
       final  Map<String,Class<? extends ConfigBeanProxy>> m = new HashMap<String,Class<? extends ConfigBeanProxy>>();
        
       final List<Class<? extends ConfigBeanProxy>>  candidates = getAllContainedConfigBeanProxyTypes();
       for( final Class<? extends ConfigBeanProxy> intf : candidates )
       {
            final AMXConfigInfo amxConfigInfo = intf.getAnnotation(AMXConfigInfo.class);
            if ( amxConfigInfo != null && ! amxVoid.equals(amxConfigInfo.amxInterfaceName()) )
            {
                final AMXConfigInfoResolver r = new AMXConfigInfoResolver( amxConfigInfo );
                final String j2eeType = r.j2eeType();
                m.put( j2eeType, intf);
            }
       }
       
       return m;
    }
}




















