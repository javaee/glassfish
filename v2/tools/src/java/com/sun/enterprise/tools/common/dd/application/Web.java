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

/**
 *	This generated bean class Web matches the schema element web
 *
 *	Generated on Thu Jul 31 15:31:46 PDT 2003
 */

package com.sun.enterprise.tools.common.dd.application;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Web extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String WEB_URI = "WebUri";	// NOI18N
	static public final String CONTEXT_ROOT = "ContextRoot";	// NOI18N

	public Web() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Web(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("web-uri", 	// NOI18N
			WEB_URI, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("context-root", 	// NOI18N
			CONTEXT_ROOT, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setWebUri(String value) {
		this.setValue(WEB_URI, value);
	}

	//
	public String getWebUri() {
		return (String)this.getValue(WEB_URI);
	}

	// This attribute is mandatory
	public void setContextRoot(String value) {
		this.setValue(CONTEXT_ROOT, value);
	}

	//
	public String getContextRoot() {
		return (String)this.getValue(CONTEXT_ROOT);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property webUri
		if (getWebUri() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getWebUri() == null", "webUri", this);	// NOI18N
		}
		// Validating property contextRoot
		if (getContextRoot() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getContextRoot() == null", "contextRoot", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("WebUri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getWebUri();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WEB_URI, 0, str, indent);

		str.append(indent);
		str.append("ContextRoot");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getContextRoot();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTEXT_ROOT, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Web\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for Sun ONE Application Server specific J2EE Application
  deployment descriptor. This is a companion DTD to application_1.4.xsd

  $Revision: 1.3 $
-->

<!--
This is the root element of the runtime descriptor document.
-->
<!ELEMENT sun-application (web*, pass-by-reference?, unique-id?, security-role-mapping*, realm?) >

<!ELEMENT web (web-uri, context-root)>
<!ELEMENT web-uri (#PCDATA)>
<!ELEMENT context-root (#PCDATA)>

<!-- Pass by Reference semantics:  EJB spec requires pass by value,
     which will be the default mode of operation. This can be set 
     to true for non-compliant and possibly higher performance. 
     For a stand-alone, this can be set at this level. By setting 
     a similarly named element at sun-application, it can apply to 
     all the enclosed ejb modules. Allowed values are true and 
     false. Default will be false.
 -->
<!ELEMENT pass-by-reference (#PCDATA)>

<!-- Automatically generated and updated at deployment/redeployment 
     Needs to be unqiue in the system.
  -->
<!ELEMENT unique-id (#PCDATA)>

<!ELEMENT security-role-mapping (role-name, (principal-name | group-name)+)>

<!ELEMENT role-name (#PCDATA)>
<!ELEMENT principal-name (#PCDATA)>
<!ELEMENT group-name (#PCDATA)>

<!-- 
  realm: Allows specifying an optional authentication realm name which will
    be used to process all authentication requests associated with this
    application. If this element is not specified (or if it is given but
    does not match the name of a configured realm) then the default realm
    set in the server instances security-service element will be used
    instead.
-->
<!ELEMENT realm (#PCDATA)>

<!-- This information is used, only by Studio-plugin;it is NOT the part of the real DTD.
-->
<!ELEMENT plugin-data ( auto-generate-sql?, client-jar-path?, client-args? )>
<!ELEMENT auto-generate-sql (#PCDATA)>
<!ELEMENT client-jar-path (#PCDATA)>
<!ELEMENT client-args (#PCDATA)>

*/
