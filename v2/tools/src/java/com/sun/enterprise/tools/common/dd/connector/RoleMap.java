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
 *	This generated bean class RoleMap matches the schema element role-map
 *
 *	Generated on Thu Jul 31 18:16:39 PDT 2003
 */

package com.sun.enterprise.tools.common.dd.connector;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class RoleMap extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String MAPID = "MapId";	// NOI18N
	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String MAP_ELEMENT = "MapElement";	// NOI18N

	public RoleMap() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public RoleMap(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("map-element", 	// NOI18N
			MAP_ELEMENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MapElement.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is mandatory
	public void setMapId(java.lang.String value) {
		setAttributeValue(MAPID, value);
	}

	//
	public java.lang.String getMapId() {
		return getAttributeValue(MAPID);
	}

	// This attribute is optional
	public void setDescription(String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public String getDescription() {
		return (String)this.getValue(DESCRIPTION);
	}

	// This attribute is an array, possibly empty
	public void setMapElement(int index, MapElement value) {
		this.setValue(MAP_ELEMENT, index, value);
	}

	//
	public MapElement getMapElement(int index) {
		return (MapElement)this.getValue(MAP_ELEMENT, index);
	}

	// This attribute is an array, possibly empty
	public void setMapElement(MapElement[] value) {
		this.setValue(MAP_ELEMENT, value);
	}

	//
	public MapElement[] getMapElement() {
		return (MapElement[])this.getValues(MAP_ELEMENT);
	}

	// Return the number of properties
	public int sizeMapElement() {
		return this.size(MAP_ELEMENT);
	}

	// Add a new element returning its index in the list
	public int addMapElement(com.sun.enterprise.tools.common.dd.connector.MapElement value) {
		return this.addValue(MAP_ELEMENT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMapElement(com.sun.enterprise.tools.common.dd.connector.MapElement value) {
		return this.removeValue(MAP_ELEMENT, value);
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
		// Validating property mapId
		if (getMapId() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMapId() == null", "mapId", this);	// NOI18N
		}
		// Validating property description
		if (getDescription() != null) {
		}
		// Validating property mapElement
		for (int _index = 0; _index < sizeMapElement(); ++_index) {
			com.sun.enterprise.tools.common.dd.connector.MapElement element = getMapElement(_index);
			if (element != null) {
				element.validate();
			}
		}
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
		s = this.getDescription();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("MapElement["+this.sizeMapElement()+"]");	// NOI18N
		for(int i=0; i<this.sizeMapElement(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMapElement(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MAP_ELEMENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("RoleMap\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!-- 
  XML DTD for Sun ONE Application Server specific J2EE Resource Adapter
  deployment descriptor. This is a companion DTD to connector_1_5.xsd

  $Revision: 1.3 $
-->

<!-- Each deployed Resource Adapter (RAR) Module, will have ias-ra.xml 
     associated with it, which specifies several dynamic configuration 
     properties. Key aspects are pool sizing and security role maps.
 -->
<!ELEMENT sun-connector (resource-adapter, role-map?)>

<!-- Resource adapter configuration 

     jndi-name          name by which, this adapter will appear in JNDI tree
     max-pool-size      maximum size of connection to EIS
     steady-pool-size   initial and minimum number of connections to be maintained
     max-wait-in-millis if a connection is not readily found, caller will have to 
                        wait this long, before a connection is created. A value of
                        0 implies, wait till a connection becomes available. If the
                        pool is completely utilized and the timer expires, an 
                        exception will be delivered to the application.

     idle-timeout-in-seconds A timer thread periodically removed unused connections.
                             The interval at which this thread runs. All idle 
                             connections will be removed, while mainataining
                             the configured steady-pool-size.
-->
<!ELEMENT resource-adapter (description?, property*)>
<!ATTLIST resource-adapter jndi-name                        CDATA     #REQUIRED
                           max-pool-size                    CDATA     "32"
                           steady-pool-size                 CDATA     "4" 
                           max-wait-time-in-millis          CDATA     "10000" 
                           idle-timeout-in-seconds          CDATA     "1000">

<!-- Perform mapping from principal received during Servlet/EJB 
     authentication, to credentials accepted by the EIS. This 
     mapping is optional. The map consists of several 2-tuples
     map-id is the name of the mapping
 -->
<!ELEMENT role-map (description?, map-element*)>
<!ATTLIST role-map map-id   CDATA  #REQUIRED>
                

<!-- It is possible to map multiple (server) principal to the 
     same backend principal. 
-->
<!ELEMENT map-element (principal+, backend-principal)>

<!-- Principal of the Servlet and EJB client -->
<!ELEMENT principal (description?)>
<!ATTLIST principal user-name CDATA #REQUIRED>
 
<!-- Backend EIS principal -->
<!ELEMENT backend-principal EMPTY> 
<!ATTLIST backend-principal user-name  CDATA #REQUIRED
                            password   CDATA #REQUIRED
                            credential CDATA #REQUIRED>

<!ELEMENT description (#PCDATA)>

<!-- Syntax for supplying properties as name value pairs -->
<!ELEMENT property EMPTY>
<!ATTLIST property name  CDATA  #REQUIRED
                   value CDATA  #REQUIRED>

*/
