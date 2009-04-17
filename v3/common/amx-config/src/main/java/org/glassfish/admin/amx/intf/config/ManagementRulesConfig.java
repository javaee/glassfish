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
package org.glassfish.admin.amx.intf.config;



import org.glassfish.admin.amx.base.Singleton;


import java.util.Map;
import java.util.Properties;


/**
    Manipulates management rules.
    
	@since Appserver 9.0
*/

public interface ManagementRulesConfig
    extends ConfigElement, Enabled, Singleton
{
    public static final String AMX_TYPE = "management-rules";
    
	public Map<String,ManagementRuleConfig> getManagementRule();
	
	/** For use with createManagementRule() specifying description of the rule (String)*/
	public static final String RULE_DESCRIPTION_KEY  = "Description";
	
	/** For use with createManagementRule() specifying whether the rule is to be enabled (String)*/
	public static final String RULE_ENABLED_KEY      = "Enabled";
	
	/** For use with createManagementRule() specifying whether event logging is to be enabled (String)*/
	public static final String EVENT_LOG_ENABLED_KEY  = "EventLogEnabled";
	
	/** For use with createManagementRule() specifying Event Level [default=INFO] (String)*/
	public static final String EVENT_LEVEL_KEY  = "EventLevel";

	/**
        <b>DO NOT USE; ignored if used.</b>
        @deprecated ignored
     */
	public static final String EVENT_PROPERTIES_KEY  = "EventProperties";
	
	/** For use with createManagementRule() specifying Event description (String)*/
	public static final String EVENT_DESCRIPTION_KEY  = "EventDescription";

//     /**
//         Use the newer method to be able to specify event properties.
//         @deprecated use the new variant
//      */
// 	    public ManagementRuleConfig createManagementRuleConfig(
//                 String  ruleName,
//                 String  eventType,
//                 String  actionMBeanName,
//                 Map<String,String> optional );
//                 
//     /**
//         Create new self management rule. In addition to the formal parameters, the following
//         optional parameters may be specified:
//         <ul>
//         <li>{@link #RULE_DESCRIPTION_KEY}</li>
//         <li>{@link #RULE_ENABLED_KEY}</li>
//         <li>{@link #EVENT_DESCRIPTION_KEY}</li>
//         <li>{@link #EVENT_LOG_ENABLED_KEY}</li>
//         <li>{@link #EVENT_LEVEL_KEY}</li>
//         </ul>
//         
//         @param ruleName          name of the management rule. Required.
//         @param eventType         one of the values defined in {@link EventTypeValues}
//         @param actionMBeanName   actionMbeanName associated with rule  (optional)
//         @param optional optional additional values
// 
//         @return ObjectName of created management-rule
//         @throws Exception
//         @see EventTypeValues
//      */
// 
//     public ManagementRuleConfig createManagementRuleConfig(
//                 String  ruleName,
//                 String  eventType,
//                 String  actionMBeanName,
//                 Properties  eventProperties,
//                 Map<String,String> optional );
// 	                                    
// 	public void removeManagementRuleConfig( String name );
}












