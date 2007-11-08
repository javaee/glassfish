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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.File;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.appserv.management.config.TransformationRuleConfig;
import com.sun.appserv.management.config.TransformationRuleConfigKeys;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.base.Util;
import com.sun.enterprise.management.support.oldconfig.OldWebServiceEndpointConfigMBean;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

public final class TransformationRuleConfigFactory  extends ConfigFactory
{

    private final OldWebServiceEndpointConfigMBean
        mOldWebServiceEndpointConfigMBean;

    private TransformerFactory _tFactory;
    
public
	TransformationRuleConfigFactory(
		final ConfigFactoryCallback callbacks )
	{
		super( callbacks );

         _tFactory = TransformerFactory.newInstance();

        mOldWebServiceEndpointConfigMBean =
        getOldWebServiceEndpointConfigMBean();
	}

    private final OldWebServiceEndpointConfigMBean
    getOldWebServiceEndpointConfigMBean() 
    {
        final String name   = getFactoryContainer().getName();
		return getOldConfigProxies().getOldWebServiceEndpointConfigMBean( name );

    }
                
  /**
		The caller is responsible for dealing with any Properties.
	 */
		protected ObjectName
	createOldChildConfig(
		final AttributeList translatedAttrs )
	{
            final ObjectName oldObjectName = (ObjectName)
	        getCallbacks().getDelegate().invoke(
	            CREATE_TRANSFORMATION_RULE, new Object[] { translatedAttrs }, 
                CREATE_TRANSFORMATION_RULE_SIG );
		
		return oldObjectName;

	}
          
		public ObjectName
	create(
        final String    name,
        final String    ruleFileLocation,
        final boolean   enabled, 
        final String   applyTo, 
        final Map<String,String> reserved  )
	{

         String appId = getFactoryContainer().getContainer().getName(); 

         String finalRuleFileLocation = null;
         
         try {
             StreamSource stylesource =
                   new StreamSource(new File(ruleFileLocation));
            Transformer transformer = _tFactory.newTransformer(stylesource);

             finalRuleFileLocation = WebServiceMgrBackEnd.getManager().
                moveFileToRepository(ruleFileLocation, appId);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
         
         Map<String,String>  optional = new HashMap<String,String>();
         optional.put(TransformationRuleConfigKeys.ENABLED_KEY,
         Boolean.toString(enabled));
         optional.put(TransformationRuleConfigKeys.APPLY_TO_KEY, applyTo);
         final String[] requiredParams = new String[] {
            TransformationRuleConfigKeys.RULE_FILE_LOCATION_KEY,
            finalRuleFileLocation };

        if (reserved != null) {
            optional.putAll(reserved);
        }

        final Map<String,String> params = initParams(name, requiredParams, optional);

        trace( "params as processed: " + stringify( params ) );

		final ObjectName	amxName	= createChild( params );
		
		return( amxName );
    }

        protected final void
	removeByName(String name)
	{
        String appId = getFactoryContainer().getContainer().getName(); 
        String epName = getFactoryContainer().getName(); 
        WebServiceMgrBackEnd.getManager().
                removeFileFromRepository(appId,epName,name);
		mOldWebServiceEndpointConfigMBean.removeTransformationRuleByName(name);
	}


	private final Set<String>	LEGAL_OPTIONAL_KEYS	= 
		GSetUtil.newUnmodifiableStringSet(
		TransformationRuleConfigKeys.ENABLED_KEY,
		TransformationRuleConfigKeys.APPLY_TO_KEY);
	
	    protected Set<String>
	getLegalOptionalCreateKeys()
	{
		return( LEGAL_OPTIONAL_KEYS );
	}

    private static final String CREATE_TRANSFORMATION_RULE  =
            "createTransformationRule";
    private static final String[] CREATE_TRANSFORMATION_RULE_SIG  = 
            new String[] { AttributeList.class.getName() };
    private static final String REMOVE_TRANSFORMATION_RULE  =
            "removeTransformationRuleByName";
    private static final String[] REMOVE_TRANSFORMATION_RULE_SIG  = 
            new String[] { String.class.getName() };

}





