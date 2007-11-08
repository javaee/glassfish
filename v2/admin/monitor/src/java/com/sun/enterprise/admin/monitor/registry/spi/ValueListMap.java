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

/* ValueListMap.java
 * $Id: ValueListMap.java,v 1.4 2006/11/04 01:41:23 dpatil Exp $
 * $Revision: 1.4 $
 * $Date: 2006/11/04 01:41:23 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim - 
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio - 
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import com.sun.enterprise.util.i18n.StringManager;
/**
 * A {@link Map} to maintain a map of listeners keyed on a particular MonitoredObjectType.
 * It may so happen that multiple listeners are interested in listening for same
 * type. This class is designed mainly to address this requirement. It is also
 * possible that one listener is interested in various types such that it is
 * existing in the mapping for multiple keys (MonitoredObjectType instances). This class
 * also provides for this requirement.
 * <P>
 * Note the documentation of implementations of various {@link Map} interface methods in this class,
 * because it has subtle connotations.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.4 $
 */
public final class ValueListMap implements Map {
	
	public final static Class LISTENER_CLASS = com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener.class;
	
	private static final StringManager sm = StringManager.getManager(ValueListMap.class);
	private final Class valueClass;
	private final Map map;
	
	/**
	 * Constructs the instance of this final class. It is a custom implementation of
	 * {@link Map} interface. It will not be possible to add mappings into this map
	 * for the values that denote objects that don't implement the class represented by the parameter.
	 * @param valueClass	denotes the class of the values that will be put into this map. May
	 * not be null. <b> It is an assumption that the class of key and value is not the same. </b>
	 * @throws {@link IllegalArgumentException} if the class is null.
	 */ 
	public ValueListMap(Class valueClass) {
		if (valueClass == null) {
			final String msg = sm.getString("gen.illegal_arg");
			throw new IllegalArgumentException (msg);
		}
		this.map		= new HashMap();
		this.valueClass = valueClass;
	}
	
	/**
	 * The default constructor to be used in most cases. The values to be
	 * added are of type {@link MonitoringLevelListener}.
	 */
	public ValueListMap () {
		this(LISTENER_CLASS);
	}
	
	public void clear() {
		map.clear();
	}
	
	public boolean containsKey(Object key) {
		return ( map.containsKey(key) );
	}
	
	/**
	 * Throws {@link UnsupportedOperationException}, as there is no real application.
	 */
	public boolean containsValue(Object value){
		throw new UnsupportedOperationException("ValueListMap:containsValue() - Not supported");
	}
	
	public Set entrySet() {
		return ( map.entrySet() );
	}
	
	/**
	 * Always returns a map or null if there is no mapping that exists for this key. Thus
	 * for unmapped keys, the behavior resembles the {@link HashMap}. In case there is a
	 * mapping, it returns a map such that the keys in returned map are the values
	 * that were put using the put call.
	 * @return a Map of objects with keys that are put earlier.
	 */
	public Object get(Object key) {
		return ( map.get(key) );
	}
	
	public boolean isEmpty() {
		return ( map.isEmpty() );
	}
	
	public Set keySet() {
		return ( map.keySet() );
	}
	
	/**
	 * If the key already exists, a new mapping is created which will be put in an
	 * inner map for that key. If the key does not exist, a new key is created and
	 * then an inner map is created. The inner map is actually for the faster lookup.
	 * Thus the method renders the map like:
	 * <pre>
	 * key1 -> Map1
	 * key2 -> Map2
	 * 
	 * Map1 -> value1, value2, value3 ... -> 1 . This 1 is an arbitrary mapping.
	 * </pre>
	 */
	public Object put(Object key, Object value) {
		String msg = null;
		if (key == null || value == null) {
			msg = sm.getString("sm.illegal_arg");
			throw new IllegalArgumentException ("Null Argument");
		}
		if (! implementsValueClass(value)) {
			msg = sm.getString("sm.illegal_arg_class", valueClass.getName());
			throw new IllegalArgumentException(msg);
		}
		Map mm = (Map) map.get(key);  // the mapped map
		if (mm == null) {
			mm = addNewKey(key);
		}
		return mm.put(value, Integer.valueOf(1)); // the value can be mapped arbitrarily to anything!
	}
	
	/**
	 * Throws {@link UnsupportedOperationException}
	 */
	public void putAll(Map t) {
		throw new UnsupportedOperationException("ValueListMap:putAll() - Not supported");
	}
	
	/**
	 * Either removes the key (mapping) from the outer map or removes a key
	 * from inner mapping. Please see #put.
	 */
	public Object remove(Object keyOrValue) {
		Collection removed = null;
		if (valueClass.isAssignableFrom(keyOrValue.getClass())) {
			final Object value = keyOrValue; //indicating that we should try to remove this value
			removed = removeValues(value);
		}
		else {
			final Object key = keyOrValue; //indicating that we should try to remove this key
			removed = removeKeyedValues(key);
			map.remove(key);
		}
		return ( removed );
	}
	
	private Collection removeValues(Object value) {
		final Collection list = new ArrayList();
		final Iterator  iter  = map.keySet().iterator();
		while (iter.hasNext()) {
			final Map mm = (Map) map.get(iter.next()); //has to be map
			mm.remove(value);  // this will return the value Integer(1), which is ignored
			list.add(value);
		}
		return ( list );
	}
	
	private Collection removeKeyedValues(Object key) {
		Collection list = new ArrayList();
		final Object value = this.get(key);
		if (value != null && value instanceof Map) {
			list = ((Map)value).keySet();
		}
		return ( list );
	}
	
	public int size() {
		return ( map.size() );
	}
	
	/** 
	 * Returns a Collection of ALL the values that were put by put calls till 
	 * this point in time.
	 * @return Collection (ArrayList) of all the values put
	 */
	public Collection values() {
		//return all the mapped map's keys
		final Collection values = new ArrayList();
		final Iterator iter = this.keySet().iterator();
		while (iter.hasNext()) {
			final Map mm = (Map) map.get(iter.next());
			values.addAll(mm.keySet());
		}
		return ( values );
	}
	
	private Map addNewKey(Object key) {
		final Map v = new HashMap();
		map.put(key, v);
		return ( v );
	}
	
	private boolean implementsValueClass(Object value) {
		boolean ivc = false;
		final Class[] ics = value.getClass().getInterfaces();
		for (int i = 0 ; i < ics.length ; i++) {
			if (valueClass.equals(ics[i])) {
				ivc = true;
				break;
			}
		}
		return ( ivc );
	}
}
