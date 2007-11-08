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
 
package com.sun.enterprise.management.offline;

import java.io.File;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.base.AMXDebug;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;

import com.sun.enterprise.config.impl.ConfigContextImpl;


/**
 */
public final class ConfigDelegateFactory
{
    private ConfigContext   mConfigContext;
    private final File      mDomainXML;
    
    private static Map<File,ConfigDelegateFactory>   mFactories;
    
        private
    ConfigDelegateFactory(final File domainXML )
        throws ConfigException
    {
        mDomainXML  = domainXML;
        mConfigContext  = createConfigContext( mDomainXML );
    }
    
        private void
    debug( final Object o )
    {
        AMXDebug.getInstance().getOutput( "ConfigDelegateFactory" ).println( o );
    }
    
        public static synchronized ConfigDelegateFactory
    getInstance( final File domainXML )
        throws ConfigException
    {
        if ( mFactories == null )
        {
            mFactories  = new HashMap<File,ConfigDelegateFactory>();
        }

        ConfigDelegateFactory   instance    = mFactories.get( domainXML );
	    if ( instance == null )
	    {
	        instance    = new ConfigDelegateFactory( domainXML );
	        mFactories.put( domainXML, instance );
	    }
	    
	    return instance;
    }
    
        synchronized ConfigDelegate
    createConfigDelegate( final ConfigBean configBean )
        throws ConfigException
    {
        if ( getConfigContext() != configBean.getConfigContext() )
        {
            throw new IllegalArgumentException( "ConfigBean " +
                configBean.getXPath() + " has mismatched ConfigContext" );
        }

        return new ConfigDelegate( getConfigContext(), configBean );
    }
               
        private ConfigContext
    createConfigContext( final File domainXML ) 
        throws ConfigException
    {
        setPreEnvironment();
        
        final ConfigContext ctx =
            ConfigFactory.createConfigContext( domainXML.toString(),
                true, true, true );
        
        setPostEnvironment( ctx );
        
        return ctx;
    }
    
        public ConfigContext
    getConfigContext()
    {
        return mConfigContext;
    }


        private void
    setPreEnvironment()
    {
        System.setProperty(
              "com.sun.enterprise.config.config_environment_factory_class", 
              "com.sun.enterprise.config.serverbeans.AppserverConfigEnvironmentFactory"
         );
    }

        private void
    setPostEnvironment( final ConfigContext ctx)
    {
        try
        {
            ((ConfigContextImpl)ctx).setXPathInAllBeans();
        }
        catch(ConfigException e)
        {
            throw new RuntimeException( e );
        }
    }
}
























