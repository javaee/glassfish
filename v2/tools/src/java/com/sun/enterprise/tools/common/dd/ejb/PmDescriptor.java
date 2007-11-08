/**
 *	This generated bean class PmDescriptor matches the schema element pm-descriptor
 *
 *	Generated on Wed Mar 03 14:29:48 PST 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PmDescriptor extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String PM_IDENTIFIER = "PmIdentifier";	// NOI18N
	static public final String PM_VERSION = "PmVersion";	// NOI18N
	static public final String PM_CONFIG = "PmConfig";	// NOI18N
	static public final String PM_CLASS_GENERATOR = "PmClassGenerator";	// NOI18N
	static public final String PM_MAPPING_FACTORY = "PmMappingFactory";	// NOI18N

	public PmDescriptor() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PmDescriptor(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("pm-identifier", 	// NOI18N
			PM_IDENTIFIER, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("pm-version", 	// NOI18N
			PM_VERSION, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("pm-config", 	// NOI18N
			PM_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("pm-class-generator", 	// NOI18N
			PM_CLASS_GENERATOR, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("pm-mapping-factory", 	// NOI18N
			PM_MAPPING_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
					
	}

	// This attribute is mandatory
	public void setPmIdentifier(String value) {
		this.setValue(PM_IDENTIFIER, value);
	}

	//
	public String getPmIdentifier() {
		return (String)this.getValue(PM_IDENTIFIER);
	}

	// This attribute is mandatory
	public void setPmVersion(String value) {
		this.setValue(PM_VERSION, value);
	}

	//
	public String getPmVersion() {
		return (String)this.getValue(PM_VERSION);
	}

	// This attribute is optional
	public void setPmConfig(String value) {
		this.setValue(PM_CONFIG, value);
	}

	//
	public String getPmConfig() {
		return (String)this.getValue(PM_CONFIG);
	}

	// This attribute is optional
	public void setPmClassGenerator(String value) {
		this.setValue(PM_CLASS_GENERATOR, value);
	}

	//
	public String getPmClassGenerator() {
		return (String)this.getValue(PM_CLASS_GENERATOR);
	}

	// This attribute is optional
	public void setPmMappingFactory(String value) {
		this.setValue(PM_MAPPING_FACTORY, value);
	}

	//
	public String getPmMappingFactory() {
		return (String)this.getValue(PM_MAPPING_FACTORY);
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
		// Validating property pmIdentifier
		if (getPmIdentifier() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPmIdentifier() == null", "pmIdentifier", this);	// NOI18N
		}
		// Validating property pmVersion
		if (getPmVersion() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPmVersion() == null", "pmVersion", this);	// NOI18N
		}
		// Validating property pmConfig
		if (getPmConfig() != null) {
		}
		// Validating property pmClassGenerator
		if (getPmClassGenerator() != null) {
		}
		// Validating property pmMappingFactory
		if (getPmMappingFactory() != null) {
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PmIdentifier");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPmIdentifier();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PM_IDENTIFIER, 0, str, indent);

		str.append(indent);
		str.append("PmVersion");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPmVersion();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PM_VERSION, 0, str, indent);

		str.append(indent);
		str.append("PmConfig");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPmConfig();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PM_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("PmClassGenerator");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPmClassGenerator();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PM_CLASS_GENERATOR, 0, str, indent);

		str.append(indent);
		str.append("PmMappingFactory");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPmMappingFactory();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PM_MAPPING_FACTORY, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PmDescriptor\n");	// NOI18N
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
