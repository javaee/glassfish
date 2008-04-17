/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.lang.jstl;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * <p>This is a Map implementation driven by a data source that only
 * provides an enumeration of keys and a getValue(key) method.  This
 * class must be subclassed to implement those methods.
 *
 * <p>Some of the methods may incur a performance penalty that
 * involves enumerating the entire data source.  In these cases, the
 * Map will try to save the results of that enumeration, but only if
 * the underlying data source is immutable.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public abstract class EnumeratedMap
  implements Map
{
  //-------------------------------------
  // Member variables
  //-------------------------------------

  Map mMap;

  //-------------------------------------
  public void clear ()
  {
    throw new UnsupportedOperationException ();
  }

  //-------------------------------------
  public boolean containsKey (Object pKey)
  {
    return getValue (pKey) != null;
  }

  //-------------------------------------
  public boolean containsValue (Object pValue)
  {
    return getAsMap ().containsValue (pValue);
  }

  //-------------------------------------
  public Set entrySet ()
  {
    return getAsMap ().entrySet ();
  }

  //-------------------------------------
  public Object get (Object pKey)
  {
    return getValue (pKey);
  }

  //-------------------------------------
  public boolean isEmpty ()
  {
    return !enumerateKeys ().hasMoreElements ();
  }

  //-------------------------------------
  public Set keySet ()
  {
    return getAsMap ().keySet ();
  }

  //-------------------------------------
  public Object put (Object pKey, Object pValue)
  {
    throw new UnsupportedOperationException ();
  }

  //-------------------------------------
  public void putAll (Map pMap)
  {
    throw new UnsupportedOperationException ();
  }

  //-------------------------------------
  public Object remove (Object pKey)
  {
    throw new UnsupportedOperationException ();
  }

  //-------------------------------------
  public int size ()
  {
    return getAsMap ().size ();
  }

  //-------------------------------------
  public Collection values ()
  {
    return getAsMap ().values ();
  }

  //-------------------------------------
  // Abstract methods
  //-------------------------------------
  /**
   *
   * Returns an enumeration of the keys
   **/
  public abstract Enumeration enumerateKeys ();

  //-------------------------------------
  /**
   *
   * Returns true if it is possible for this data source to change
   **/
  public abstract boolean isMutable ();

  //-------------------------------------
  /**
   *
   * Returns the value associated with the given key, or null if not
   * found.
   **/
  public abstract Object getValue (Object pKey);

  //-------------------------------------
  /**
   *
   * Converts the MapSource to a Map.  If the map is not mutable, this
   * is cached
   **/
  public Map getAsMap ()
  {
    if (mMap != null) {
      return mMap;
    }
    else {
      Map m = convertToMap ();
      if (!isMutable ()) {
	mMap = m;
      }
      return m;
    }
  }

  //-------------------------------------
  /**
   *
   * Converts to a Map
   **/
  Map convertToMap ()
  {
    Map ret = new HashMap ();
    for (Enumeration e = enumerateKeys (); e.hasMoreElements (); ) {
      Object key = e.nextElement ();
      Object value = getValue (key);
      ret.put (key, value);
    }
    return ret;
  }

  //-------------------------------------
}
