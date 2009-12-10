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




import java.util.Map;

/**
	 Configuration for the &lt;application&gt; element.
     <p>
     Containees may include unspecified (runtime-determined)
     sub-interfaces of {@link ApplicationConfigConfig}.
     <p>
     To find all Containees of a specific type, eg {@link ApplicationConfigConfig},
        use {@link org.glassfish.admin.amx.base.Util#filterAMX}:
     <pre>
     ApplicationConfig ac = ...;
     Set<AMX> containees = ac.getContaineeSet();
     Set<ApplicationConfigConfig> s = Util.filterAMX(containees, ApplicationConfigConfig.class);
     <pre>
     Alternately, containees may be filtered by j2eeType eg amx.getJ2EEType(), if the j2eeType
     is known specifically.
     
     @since Glassfish V3
*/

public interface ApplicationConfig
    extends AbstractModule, Libraries
{
	/**
	The ContextRoot must match the pattern for the hpath production  
	in RFC 1738 which can be found at:                            
	http://www.w3.org/Addressing/rfc1738.txt. This is flattened   
	to the following regular expression in XML Schema's pattern   
	language:

	<pre>                                     
	([a-zA-Z0-9$\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*(/([
	-zA-Z0-9$\-_.+!*'(),]|%[0-9A-Fa-f][0-9A-Fa-f]|;|:|&|=)*)*
	</pre>
	  
	Note that this includes the null or empty context root and    
	permits but does not require a context root to start with the 
	'/' character (including a context root which is simply the   
	'/' character).
	@see #setContextRoot                                       
	*/
      
	public String	getContextRoot();
	
	/**
	@see #getContextRoot                                       
	*/
	public void		setContextRoot( String value );

	/**
		Controls whether availability is enabled    
		for HTTP Session Persistence. If this is "false", then all    
		session persistence is disabled for the given web module. If  
		it is "true" (and providing that all the availability-enabled 
		attributes above in precedence are also "true", then the web  
		module may be ha enabled. Finer-grained control exists at     
		lower level (see sun-web.xml).                               

		@see #setAvailabilityEnabled
	 */
    
	public String	getAvailabilityEnabled();

	/**
		@see #getAvailabilityEnabled
	*/       
	public void		setAvailabilityEnabled( String enabled );
}



