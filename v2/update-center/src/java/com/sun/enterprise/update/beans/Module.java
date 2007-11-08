/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
/**
 *	This generated bean class Module matches the schema element 'module'.
 *  The root bean class is ModuleUpdates
 *
 *	Generated on Tue Oct 03 15:15:44 PDT 2006
 * @Generated
 */

package com.sun.enterprise.update.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Module extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String NAME = "Name";	// NOI18N
	static public final String HOMEPAGE = "Homepage";	// NOI18N
	static public final String DISTRIBUTION = "Distribution";	// NOI18N
	static public final String LICENSE = "License";	// NOI18N
	static public final String DOWNLOADSIZE = "Downloadsize";	// NOI18N
	static public final String NEEDSRESTART = "Needsrestart";	// NOI18N
	static public final String MODULEAUTHOR = "Moduleauthor";	// NOI18N
	static public final String RELEASEDATE = "Releasedate";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String MANIFEST = "Manifest";	// NOI18N
	static public final String MANIFESTMODULE = "ManifestModule";	// NOI18N
	static public final String MANIFESTMODULENAME = "ManifestModuleName";	// NOI18N
	static public final String MANIFESTMODULETYPE = "ManifestModuleType";	// NOI18N
	static public final String MANIFESTMODULESPECIFICATIONVERSION = "ManifestModuleSpecificationVersion";	// NOI18N
	static public final String MANIFESTMODULEIMPLEMENTATIONVERSION = "ManifestModuleImplementationVersion";	// NOI18N
	static public final String MANIFESTMODULEMODULEDEPENDENCIES = "ManifestModuleModuleDependencies";	// NOI18N
	static public final String MANIFESTMODULESHORTDESCRIPTION = "ManifestModuleShortDescription";	// NOI18N
	static public final String MANIFESTMODULELONGDESCRIPTION = "ManifestModuleLongDescription";	// NOI18N
	static public final String L10N = "L10n";	// NOI18N
	static public final String L10NLANGCODE = "L10nLangcode";	// NOI18N
	static public final String L10NMODULESPECVERSION = "L10nModuleSpecVersion";	// NOI18N
	static public final String L10NMODULEMAJORVERSION = "L10nModuleMajorVersion";	// NOI18N
	static public final String L10NMODULENAME = "L10nModuleName";	// NOI18N
	static public final String L10NMODULELONGDESCRIPTION = "L10nModuleLongDescription";	// NOI18N

	public Module() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Module(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("manifest", 	// NOI18N
			MANIFEST, Common.SEQUENCE_OR | 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(MANIFEST, "Module", "Module", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Name", "ModuleName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Type", "ModuleType", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Specification-Version", "ModuleSpecificationVersion", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Implementation-Version", "ModuleImplementationVersion", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Module-Dependencies", "ModuleModuleDependencies", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Short-Description", "ModuleShortDescription", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANIFEST, "Module-Long-Description", "ModuleLongDescription", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("l10n", 	// NOI18N
			L10N, Common.SEQUENCE_OR | 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(L10N, "langcode", "Langcode", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(L10N, "module_spec_version", "ModuleSpecVersion", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(L10N, "module_major_version", "ModuleMajorVersion", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(L10N, "Module-Name", "ModuleName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(L10N, "Module-Long-Description", "ModuleLongDescription", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is optional
	public void setHomepage(java.lang.String value) {
		setAttributeValue(HOMEPAGE, value);
	}

	//
	public java.lang.String getHomepage() {
		return getAttributeValue(HOMEPAGE);
	}

	// This attribute is mandatory
	public void setDistribution(java.lang.String value) {
		setAttributeValue(DISTRIBUTION, value);
	}

	//
	public java.lang.String getDistribution() {
		return getAttributeValue(DISTRIBUTION);
	}

	// This attribute is optional
	public void setLicense(java.lang.String value) {
		setAttributeValue(LICENSE, value);
	}

	//
	public java.lang.String getLicense() {
		return getAttributeValue(LICENSE);
	}

	// This attribute is mandatory
	public void setDownloadsize(java.lang.String value) {
		setAttributeValue(DOWNLOADSIZE, value);
	}

	//
	public java.lang.String getDownloadsize() {
		return getAttributeValue(DOWNLOADSIZE);
	}

	// This attribute is optional
	public void setNeedsrestart(java.lang.String value) {
		setAttributeValue(NEEDSRESTART, value);
	}

	//
	public java.lang.String getNeedsrestart() {
		return getAttributeValue(NEEDSRESTART);
	}

	// This attribute is optional
	public void setModuleauthor(java.lang.String value) {
		setAttributeValue(MODULEAUTHOR, value);
	}

	//
	public java.lang.String getModuleauthor() {
		return getAttributeValue(MODULEAUTHOR);
	}

	// This attribute is optional
	public void setReleasedate(java.lang.String value) {
		setAttributeValue(RELEASEDATE, value);
	}

	//
	public java.lang.String getReleasedate() {
		return getAttributeValue(RELEASEDATE);
	}

	// This attribute is optional
	public void setDescription(String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public String getDescription() {
		return (String)this.getValue(DESCRIPTION);
	}

	// This attribute is mandatory
	public void setManifest(boolean value) {
		this.setValue(MANIFEST, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
		if (value != false) {
			// It's a mutually exclusive property.
			setL10n(false);
		}
	}

	//
	public boolean isManifest() {
		Boolean ret = (Boolean)this.getValue(MANIFEST);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is mandatory
	public void setManifestModule(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "Module", value);
	}

	//
	public java.lang.String getManifestModule() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "Module");
		}
	}

	// This attribute is mandatory
	public void setManifestModuleName(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleName", value);
	}

	//
	public java.lang.String getManifestModuleName() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleName");
		}
	}

	// This attribute is mandatory
	public void setManifestModuleType(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleType", value);
	}

	//
	public java.lang.String getManifestModuleType() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleType");
		}
	}

	// This attribute is mandatory
	public void setManifestModuleSpecificationVersion(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleSpecificationVersion", value);
	}

	//
	public java.lang.String getManifestModuleSpecificationVersion() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleSpecificationVersion");
		}
	}

	// This attribute is optional
	public void setManifestModuleImplementationVersion(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleImplementationVersion", value);
	}

	//
	public java.lang.String getManifestModuleImplementationVersion() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleImplementationVersion");
		}
	}

	// This attribute is optional
	public void setManifestModuleModuleDependencies(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleModuleDependencies", value);
	}

	//
	public java.lang.String getManifestModuleModuleDependencies() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleModuleDependencies");
		}
	}

	// This attribute is optional
	public void setManifestModuleShortDescription(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleShortDescription", value);
	}

	//
	public java.lang.String getManifestModuleShortDescription() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleShortDescription");
		}
	}

	// This attribute is optional
	public void setManifestModuleLongDescription(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MANIFEST) == 0) {
			setValue(MANIFEST, java.lang.Boolean.TRUE);
		}
		setAttributeValue(MANIFEST, "ModuleLongDescription", value);
	}

	//
	public java.lang.String getManifestModuleLongDescription() {
		// If our element does not exist, then the attribute does not exist.
		if (size(MANIFEST) == 0) {
			return null;
		} else {
			return getAttributeValue(MANIFEST, "ModuleLongDescription");
		}
	}

	// This attribute is mandatory
	public void setL10n(boolean value) {
		this.setValue(L10N, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
		if (value != false) {
			// It's a mutually exclusive property.
			setManifest(false);
		}
	}

	//
	public boolean isL10n() {
		Boolean ret = (Boolean)this.getValue(L10N);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setL10nLangcode(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(L10N) == 0) {
			setValue(L10N, java.lang.Boolean.TRUE);
		}
		setAttributeValue(L10N, "Langcode", value);
	}

	//
	public java.lang.String getL10nLangcode() {
		// If our element does not exist, then the attribute does not exist.
		if (size(L10N) == 0) {
			return null;
		} else {
			return getAttributeValue(L10N, "Langcode");
		}
	}

	// This attribute is optional
	public void setL10nModuleSpecVersion(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(L10N) == 0) {
			setValue(L10N, java.lang.Boolean.TRUE);
		}
		setAttributeValue(L10N, "ModuleSpecVersion", value);
	}

	//
	public java.lang.String getL10nModuleSpecVersion() {
		// If our element does not exist, then the attribute does not exist.
		if (size(L10N) == 0) {
			return null;
		} else {
			return getAttributeValue(L10N, "ModuleSpecVersion");
		}
	}

	// This attribute is optional
	public void setL10nModuleMajorVersion(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(L10N) == 0) {
			setValue(L10N, java.lang.Boolean.TRUE);
		}
		setAttributeValue(L10N, "ModuleMajorVersion", value);
	}

	//
	public java.lang.String getL10nModuleMajorVersion() {
		// If our element does not exist, then the attribute does not exist.
		if (size(L10N) == 0) {
			return null;
		} else {
			return getAttributeValue(L10N, "ModuleMajorVersion");
		}
	}

	// This attribute is optional
	public void setL10nModuleName(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(L10N) == 0) {
			setValue(L10N, java.lang.Boolean.TRUE);
		}
		setAttributeValue(L10N, "ModuleName", value);
	}

	//
	public java.lang.String getL10nModuleName() {
		// If our element does not exist, then the attribute does not exist.
		if (size(L10N) == 0) {
			return null;
		} else {
			return getAttributeValue(L10N, "ModuleName");
		}
	}

	// This attribute is optional
	public void setL10nModuleLongDescription(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(L10N) == 0) {
			setValue(L10N, java.lang.Boolean.TRUE);
		}
		setAttributeValue(L10N, "ModuleLongDescription", value);
	}

	//
	public java.lang.String getL10nModuleLongDescription() {
		// If our element does not exist, then the attribute does not exist.
		if (size(L10N) == 0) {
			return null;
		} else {
			return getAttributeValue(L10N, "ModuleLongDescription");
		}
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
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("Manifest");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isManifest()?"true":"false"));
		this.dumpAttributes(MANIFEST, 0, str, indent);

		str.append(indent);
		str.append("L10n");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isL10n()?"true":"false"));
		this.dumpAttributes(L10N, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Module\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!-- -//Glassfish//DTD Update Center Catalog 1.0//EN -->
<!-- XML representation of Update Center Modules/Updates Catalog -->

<!ELEMENT module_updates ((module_group|module)*, license*)>
<!ATTLIST module_updates timestamp CDATA #REQUIRED>

<!ELEMENT module_group ((module_group|module)*)>
<!ATTLIST module_group name CDATA #REQUIRED>

<!ELEMENT module (description?, (manifest | l10n) )>
<!ATTLIST module name CDATA #REQUIRED
                 homepage     CDATA #IMPLIED
                 distribution CDATA #REQUIRED
                 license      CDATA #IMPLIED
                 downloadsize CDATA #REQUIRED
                 needsrestart CDATA #IMPLIED
                 moduleauthor CDATA #IMPLIED
                 releasedate  CDATA #IMPLIED>

<!ELEMENT description (#PCDATA)>

<!ELEMENT manifest EMPTY>
<!ATTLIST manifest Module CDATA #REQUIRED
                   Module-Name CDATA #REQUIRED
                   Module-Type CDATA #REQUIRED
                   Module-Specification-Version CDATA #REQUIRED
                   Module-Implementation-Version CDATA #IMPLIED
                   Module-Module-Dependencies CDATA #IMPLIED
                   Module-Short-Description CDATA #IMPLIED
                   Module-Long-Description CDATA #IMPLIED>

<!ELEMENT l10n EMPTY>
<!ATTLIST l10n   langcode             CDATA #IMPLIED
                 module_spec_version  CDATA #IMPLIED
                 module_major_version CDATA #IMPLIED
                 Module-Name  CDATA #IMPLIED
                 Module-Long-Description CDATA #IMPLIED>

<!ELEMENT license (#PCDATA)>
<!ATTLIST license name CDATA #REQUIRED>

*/
