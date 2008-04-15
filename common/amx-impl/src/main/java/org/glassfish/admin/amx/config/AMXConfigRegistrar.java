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

package org.glassfish.admin.amx.config;


import org.glassfish.admin.amx.util.SingletonEnforcer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigBean;


/**
    Called when ConfigBeans come into the habitat.  They are passed along
    to the AMXConfigLoader, which might queue them (if AMX has not been requested)
    or might register them as MBeans (if AMX has been requested already).
    
 * @author llc
 */
@Service(name="AMXConfigRegistrar")
public final class AMXConfigRegistrar implements CageBuilder, PostConstruct
{
    private static void debug( final String s ) { System.out.println(s); }
    private final AMXConfigLoader  mConfigLoader;
    
    /**
        Singleton: there should be only one instance and hence a private constructor.
        But the framework using this wants to instantiate things with a public constructor.
     */
    public AMXConfigRegistrar()
    {
        mConfigLoader = new AMXConfigLoader();
        SingletonEnforcer.register( mConfigLoader.getClass(), mConfigLoader );
    }
    
    public void postConstruct()
    {
        SingletonEnforcer.register( this.getClass(), mConfigLoader );
    }
    
    /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }
    
    public void onEntered(Inhabitant<?> inhabitant)
    {
        final ConfigBean cb = asConfigBean(inhabitant);
        if ( cb != null )
        {
            //final ConfigBean parent = asConfigBean(cb.parent());
        //debug( "AMXConfigRegistrar.onEntered: " + cb.getProxyType().getName() + " with parent " + (parent == null ? "null" : parent.getProxyType().getName()) );
        
            mConfigLoader.handleConfigBean( cb, false );
        }
    }
    
        public AMXConfigLoader
    getAMXConfigLoader()
    {
        return mConfigLoader;
    }
}


















