/**
 *	This generated bean class Cmp matches the schema element cmp
 *
 *	Generated on Thu Mar 25 11:25:27 PST 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Cmp extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String MAPPING_PROPERTIES = "MappingProperties";	// NOI18N
	static public final String IS_ONE_ONE_CMP = "IsOneOneCmp";	// NOI18N
	static public final String ONE_ONE_FINDERS = "OneOneFinders";	// NOI18N
	static public final String PREFETCH_DISABLED = "PrefetchDisabled";	// NOI18N

	public Cmp() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Cmp(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("mapping-properties", 	// NOI18N
			MAPPING_PROPERTIES, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("is-one-one-cmp", 	// NOI18N
			IS_ONE_ONE_CMP, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("one-one-finders", 	// NOI18N
			ONE_ONE_FINDERS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			OneOneFinders.class);
		this.createProperty("prefetch-disabled", 	// NOI18N
			PREFETCH_DISABLED, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PrefetchDisabled.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is optional
	public void setMappingProperties(String value) {
		this.setValue(MAPPING_PROPERTIES, value);
	}

	//
	public String getMappingProperties() {
		return (String)this.getValue(MAPPING_PROPERTIES);
	}

	// This attribute is optional
	public void setIsOneOneCmp(String value) {
		this.setValue(IS_ONE_ONE_CMP, value);
	}

	//
	public String getIsOneOneCmp() {
		return (String)this.getValue(IS_ONE_ONE_CMP);
	}

	// This attribute is optional
	public void setOneOneFinders(OneOneFinders value) {
		this.setValue(ONE_ONE_FINDERS, value);
	}

	//
	public OneOneFinders getOneOneFinders() {
		return (OneOneFinders)this.getValue(ONE_ONE_FINDERS);
	}

	// This attribute is optional
	public void setPrefetchDisabled(PrefetchDisabled value) {
		this.setValue(PREFETCH_DISABLED, value);
	}

	//
	public PrefetchDisabled getPrefetchDisabled() {
		return (PrefetchDisabled)this.getValue(PREFETCH_DISABLED);
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
		// Validating property mappingProperties
		if (getMappingProperties() != null) {
		}
		// Validating property isOneOneCmp
		if (getIsOneOneCmp() != null) {
		}
		// Validating property oneOneFinders
		if (getOneOneFinders() != null) {
			getOneOneFinders().validate();
		}
		// Validating property prefetchDisabled
		if (getPrefetchDisabled() != null) {
			getPrefetchDisabled().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("MappingProperties");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMappingProperties();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MAPPING_PROPERTIES, 0, str, indent);

		str.append(indent);
		str.append("IsOneOneCmp");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getIsOneOneCmp();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IS_ONE_ONE_CMP, 0, str, indent);

		str.append(indent);
		str.append("OneOneFinders");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getOneOneFinders();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ONE_ONE_FINDERS, 0, str, indent);

		str.append(indent);
		str.append("PrefetchDisabled");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPrefetchDisabled();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PREFETCH_DISABLED, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Cmp\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


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
