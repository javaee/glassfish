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
 *	This generated bean class SunConnector matches the schema element sun-connector
 *
 *	Generated on Thu Jul 31 18:16:39 PDT 2003
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	sun-connector : SunConnector
 *		resource-adapter : ResourceAdapter
 *			[attr: jndi-name CDATA #REQUIRED ]
 *			[attr: max-pool-size CDATA 32]
 *			[attr: steady-pool-size CDATA 4]
 *			[attr: max-wait-time-in-millis CDATA 10000]
 *			[attr: idle-timeout-in-seconds CDATA 1000]
 *			description : String?
 *			property : Boolean[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				EMPTY : String
 *		role-map : RoleMap?
 *			[attr: map-id CDATA #REQUIRED ]
 *			description : String?
 *			map-element : MapElement[0,n]
 *				principal : Principal[1,n]
 *					[attr: user-name CDATA #REQUIRED ]
 *					description : String?
 *				backend-principal : Boolean
 *					[attr: user-name CDATA #REQUIRED ]
 *					[attr: password CDATA #REQUIRED ]
 *					[attr: credential CDATA #REQUIRED ]
 *					EMPTY : String
 *
 */

package com.sun.enterprise.tools.common.dd.connector;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class SunConnector extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String RESOURCE_ADAPTER = "ResourceAdapter";	// NOI18N
	static public final String ROLE_MAP = "RoleMap";	// NOI18N

	public SunConnector() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public SunConnector(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("sun-connector");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "sun-connector"));
		}
		Node n = GraphManager.getElementNode("sun-connector", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "sun-connector", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public SunConnector(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("sun-connector", "SunConnector",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, SunConnector.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("resource-adapter", 	// NOI18N
			RESOURCE_ADAPTER, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceAdapter.class);
		this.createAttribute(RESOURCE_ADAPTER, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(RESOURCE_ADAPTER, "max-pool-size", "MaxPoolSize", 
						AttrProp.CDATA,
						null, "32");
		this.createAttribute(RESOURCE_ADAPTER, "steady-pool-size", "SteadyPoolSize", 
						AttrProp.CDATA,
						null, "4");
		this.createAttribute(RESOURCE_ADAPTER, "max-wait-time-in-millis", "MaxWaitTimeInMillis", 
						AttrProp.CDATA,
						null, "10000");
		this.createAttribute(RESOURCE_ADAPTER, "idle-timeout-in-seconds", "IdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "1000");
		this.createProperty("role-map", 	// NOI18N
			ROLE_MAP, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RoleMap.class);
		this.createAttribute(ROLE_MAP, "map-id", "MapId", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is mandatory
	public void setResourceAdapter(ResourceAdapter value) {
		this.setValue(RESOURCE_ADAPTER, value);
	}

	//
	public ResourceAdapter getResourceAdapter() {
		return (ResourceAdapter)this.getValue(RESOURCE_ADAPTER);
	}

	// This attribute is optional
	public void setRoleMap(RoleMap value) {
		this.setValue(ROLE_MAP, value);
	}

	//
	public RoleMap getRoleMap() {
		return (RoleMap)this.getValue(ROLE_MAP);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static SunConnector createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new SunConnector(doc, Common.NO_DEFAULT_VALUES);
	}

	public static SunConnector createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static SunConnector createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static SunConnector createGraph() {
		try {
			return new SunConnector();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property resourceAdapter
		if (getResourceAdapter() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getResourceAdapter() == null", "resourceAdapter", this);	// NOI18N
		}
		getResourceAdapter().validate();
		// Validating property roleMap
		if (getRoleMap() != null) {
			getRoleMap().validate();
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			init(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ResourceAdapter");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceAdapter();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(RESOURCE_ADAPTER, 0, str, indent);

		str.append(indent);
		str.append("RoleMap");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getRoleMap();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ROLE_MAP, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SunConnector\n");	// NOI18N
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
