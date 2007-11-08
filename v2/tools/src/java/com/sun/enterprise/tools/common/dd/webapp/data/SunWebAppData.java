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
 *	This generated bean class SunWebAppData matches the schema element sun-web-app-data
 *
 *	Generated on Tue Aug 12 18:27:47 PDT 2003
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	sun-web-app-data : SunWebAppData
 *		session-param : SessionParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		cookie-param : CookieParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		jsp-param : JspParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		extra-param : ExtraParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		manager-param : ManagerParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		store-param : StoreParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		persistence-param : PersistenceParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *		helper-class-param : HelperClassParam[0,n]
 *			param-name : String
 *			param-type : String?
 *			param-values : String[0,n]
 *			default-value : String?
 *			helpID : String?
 *
 */

package com.sun.enterprise.tools.common.dd.webapp.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class SunWebAppData extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String SESSION_PARAM = "SessionParam";	// NOI18N
	static public final String COOKIE_PARAM = "CookieParam";	// NOI18N
	static public final String JSP_PARAM = "JspParam";	// NOI18N
	static public final String EXTRA_PARAM = "ExtraParam";	// NOI18N
	static public final String MANAGER_PARAM = "ManagerParam";	// NOI18N
	static public final String STORE_PARAM = "StoreParam";	// NOI18N
	static public final String PERSISTENCE_PARAM = "PersistenceParam";	// NOI18N
	static public final String HELPER_CLASS_PARAM = "HelperClassParam";	// NOI18N

	public SunWebAppData() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public SunWebAppData(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("sun-web-app-data");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "sun-web-app-data"));
		}
		Node n = GraphManager.getElementNode("sun-web-app-data", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "sun-web-app-data", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public SunWebAppData(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("sun-web-app-data", "SunWebAppData",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, SunWebAppData.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("session-param", 	// NOI18N
			SESSION_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SessionParam.class);
		this.createProperty("cookie-param", 	// NOI18N
			COOKIE_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CookieParam.class);
		this.createProperty("jsp-param", 	// NOI18N
			JSP_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JspParam.class);
		this.createProperty("extra-param", 	// NOI18N
			EXTRA_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ExtraParam.class);
		this.createProperty("manager-param", 	// NOI18N
			MANAGER_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ManagerParam.class);
		this.createProperty("store-param", 	// NOI18N
			STORE_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StoreParam.class);
		this.createProperty("persistence-param", 	// NOI18N
			PERSISTENCE_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PersistenceParam.class);
		this.createProperty("helper-class-param", 	// NOI18N
			HELPER_CLASS_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HelperClassParam.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array, possibly empty
	public void setSessionParam(int index, SessionParam value) {
		this.setValue(SESSION_PARAM, index, value);
	}

	//
	public SessionParam getSessionParam(int index) {
		return (SessionParam)this.getValue(SESSION_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setSessionParam(SessionParam[] value) {
		this.setValue(SESSION_PARAM, value);
	}

	//
	public SessionParam[] getSessionParam() {
		return (SessionParam[])this.getValues(SESSION_PARAM);
	}

	// Return the number of properties
	public int sizeSessionParam() {
		return this.size(SESSION_PARAM);
	}

	// Add a new element returning its index in the list
	public int addSessionParam(com.sun.enterprise.tools.common.dd.webapp.data.SessionParam value) {
		return this.addValue(SESSION_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSessionParam(com.sun.enterprise.tools.common.dd.webapp.data.SessionParam value) {
		return this.removeValue(SESSION_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setCookieParam(int index, CookieParam value) {
		this.setValue(COOKIE_PARAM, index, value);
	}

	//
	public CookieParam getCookieParam(int index) {
		return (CookieParam)this.getValue(COOKIE_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setCookieParam(CookieParam[] value) {
		this.setValue(COOKIE_PARAM, value);
	}

	//
	public CookieParam[] getCookieParam() {
		return (CookieParam[])this.getValues(COOKIE_PARAM);
	}

	// Return the number of properties
	public int sizeCookieParam() {
		return this.size(COOKIE_PARAM);
	}

	// Add a new element returning its index in the list
	public int addCookieParam(com.sun.enterprise.tools.common.dd.webapp.data.CookieParam value) {
		return this.addValue(COOKIE_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCookieParam(com.sun.enterprise.tools.common.dd.webapp.data.CookieParam value) {
		return this.removeValue(COOKIE_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setJspParam(int index, JspParam value) {
		this.setValue(JSP_PARAM, index, value);
	}

	//
	public JspParam getJspParam(int index) {
		return (JspParam)this.getValue(JSP_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setJspParam(JspParam[] value) {
		this.setValue(JSP_PARAM, value);
	}

	//
	public JspParam[] getJspParam() {
		return (JspParam[])this.getValues(JSP_PARAM);
	}

	// Return the number of properties
	public int sizeJspParam() {
		return this.size(JSP_PARAM);
	}

	// Add a new element returning its index in the list
	public int addJspParam(com.sun.enterprise.tools.common.dd.webapp.data.JspParam value) {
		return this.addValue(JSP_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJspParam(com.sun.enterprise.tools.common.dd.webapp.data.JspParam value) {
		return this.removeValue(JSP_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setExtraParam(int index, ExtraParam value) {
		this.setValue(EXTRA_PARAM, index, value);
	}

	//
	public ExtraParam getExtraParam(int index) {
		return (ExtraParam)this.getValue(EXTRA_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setExtraParam(ExtraParam[] value) {
		this.setValue(EXTRA_PARAM, value);
	}

	//
	public ExtraParam[] getExtraParam() {
		return (ExtraParam[])this.getValues(EXTRA_PARAM);
	}

	// Return the number of properties
	public int sizeExtraParam() {
		return this.size(EXTRA_PARAM);
	}

	// Add a new element returning its index in the list
	public int addExtraParam(com.sun.enterprise.tools.common.dd.webapp.data.ExtraParam value) {
		return this.addValue(EXTRA_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeExtraParam(com.sun.enterprise.tools.common.dd.webapp.data.ExtraParam value) {
		return this.removeValue(EXTRA_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setManagerParam(int index, ManagerParam value) {
		this.setValue(MANAGER_PARAM, index, value);
	}

	//
	public ManagerParam getManagerParam(int index) {
		return (ManagerParam)this.getValue(MANAGER_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setManagerParam(ManagerParam[] value) {
		this.setValue(MANAGER_PARAM, value);
	}

	//
	public ManagerParam[] getManagerParam() {
		return (ManagerParam[])this.getValues(MANAGER_PARAM);
	}

	// Return the number of properties
	public int sizeManagerParam() {
		return this.size(MANAGER_PARAM);
	}

	// Add a new element returning its index in the list
	public int addManagerParam(com.sun.enterprise.tools.common.dd.webapp.data.ManagerParam value) {
		return this.addValue(MANAGER_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeManagerParam(com.sun.enterprise.tools.common.dd.webapp.data.ManagerParam value) {
		return this.removeValue(MANAGER_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setStoreParam(int index, StoreParam value) {
		this.setValue(STORE_PARAM, index, value);
	}

	//
	public StoreParam getStoreParam(int index) {
		return (StoreParam)this.getValue(STORE_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setStoreParam(StoreParam[] value) {
		this.setValue(STORE_PARAM, value);
	}

	//
	public StoreParam[] getStoreParam() {
		return (StoreParam[])this.getValues(STORE_PARAM);
	}

	// Return the number of properties
	public int sizeStoreParam() {
		return this.size(STORE_PARAM);
	}

	// Add a new element returning its index in the list
	public int addStoreParam(com.sun.enterprise.tools.common.dd.webapp.data.StoreParam value) {
		return this.addValue(STORE_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeStoreParam(com.sun.enterprise.tools.common.dd.webapp.data.StoreParam value) {
		return this.removeValue(STORE_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setPersistenceParam(int index, PersistenceParam value) {
		this.setValue(PERSISTENCE_PARAM, index, value);
	}

	//
	public PersistenceParam getPersistenceParam(int index) {
		return (PersistenceParam)this.getValue(PERSISTENCE_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setPersistenceParam(PersistenceParam[] value) {
		this.setValue(PERSISTENCE_PARAM, value);
	}

	//
	public PersistenceParam[] getPersistenceParam() {
		return (PersistenceParam[])this.getValues(PERSISTENCE_PARAM);
	}

	// Return the number of properties
	public int sizePersistenceParam() {
		return this.size(PERSISTENCE_PARAM);
	}

	// Add a new element returning its index in the list
	public int addPersistenceParam(com.sun.enterprise.tools.common.dd.webapp.data.PersistenceParam value) {
		return this.addValue(PERSISTENCE_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePersistenceParam(com.sun.enterprise.tools.common.dd.webapp.data.PersistenceParam value) {
		return this.removeValue(PERSISTENCE_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setHelperClassParam(int index, HelperClassParam value) {
		this.setValue(HELPER_CLASS_PARAM, index, value);
	}

	//
	public HelperClassParam getHelperClassParam(int index) {
		return (HelperClassParam)this.getValue(HELPER_CLASS_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setHelperClassParam(HelperClassParam[] value) {
		this.setValue(HELPER_CLASS_PARAM, value);
	}

	//
	public HelperClassParam[] getHelperClassParam() {
		return (HelperClassParam[])this.getValues(HELPER_CLASS_PARAM);
	}

	// Return the number of properties
	public int sizeHelperClassParam() {
		return this.size(HELPER_CLASS_PARAM);
	}

	// Add a new element returning its index in the list
	public int addHelperClassParam(com.sun.enterprise.tools.common.dd.webapp.data.HelperClassParam value) {
		return this.addValue(HELPER_CLASS_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeHelperClassParam(com.sun.enterprise.tools.common.dd.webapp.data.HelperClassParam value) {
		return this.removeValue(HELPER_CLASS_PARAM, value);
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
	public static SunWebAppData createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new SunWebAppData(doc, Common.NO_DEFAULT_VALUES);
	}

	public static SunWebAppData createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static SunWebAppData createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static SunWebAppData createGraph() {
		try {
			return new SunWebAppData();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property sessionParam
		for (int _index = 0; _index < sizeSessionParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.SessionParam element = getSessionParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property cookieParam
		for (int _index = 0; _index < sizeCookieParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.CookieParam element = getCookieParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property jspParam
		for (int _index = 0; _index < sizeJspParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.JspParam element = getJspParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property extraParam
		for (int _index = 0; _index < sizeExtraParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.ExtraParam element = getExtraParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property managerParam
		for (int _index = 0; _index < sizeManagerParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.ManagerParam element = getManagerParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property storeParam
		for (int _index = 0; _index < sizeStoreParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.StoreParam element = getStoreParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property persistenceParam
		for (int _index = 0; _index < sizePersistenceParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.PersistenceParam element = getPersistenceParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property helperClassParam
		for (int _index = 0; _index < sizeHelperClassParam(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.data.HelperClassParam element = getHelperClassParam(_index);
			if (element != null) {
				element.validate();
			}
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
		str.append("SessionParam["+this.sizeSessionParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeSessionParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSessionParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SESSION_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("CookieParam["+this.sizeCookieParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeCookieParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCookieParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(COOKIE_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("JspParam["+this.sizeJspParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeJspParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJspParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JSP_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("ExtraParam["+this.sizeExtraParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeExtraParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getExtraParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EXTRA_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("ManagerParam["+this.sizeManagerParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeManagerParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getManagerParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MANAGER_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("StoreParam["+this.sizeStoreParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeStoreParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getStoreParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(STORE_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("PersistenceParam["+this.sizePersistenceParam()+"]");	// NOI18N
		for(int i=0; i<this.sizePersistenceParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPersistenceParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PERSISTENCE_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("HelperClassParam["+this.sizeHelperClassParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeHelperClassParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getHelperClassParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(HELPER_CLASS_PARAM, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SunWebAppData\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XML Spy v4.1 U (http://www.xmlspy.com) by A Gaur (Sun Microsystems) -->
<!ELEMENT sun-web-app-data (session-param*, cookie-param*, jsp-param*, extra-param*, manager-param*, store-param*, persistence-param*, helper-class-param*)>
<!ELEMENT session-param (param-name, param-type?,  param-values*, default-value?, helpID?)>
<!ELEMENT cookie-param (param-name, param-type?,  param-values*, default-value?, helpID?)>
<!ELEMENT jsp-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT extra-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT manager-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT store-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT persistence-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT helper-class-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT param-name (#PCDATA)>
<!ELEMENT param-type (#PCDATA)>
<!ELEMENT param-values (#PCDATA)>
<!ELEMENT default-value (#PCDATA)>
<!ELEMENT helpID (#PCDATA)>

*/
