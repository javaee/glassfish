/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * An InhabitantHandle represents one Inhabitant and its lifecycle into
 * and out of the habitat.
 * 
 * @author Jeff Trent
 */
public class InhabitantHandleImpl<T> implements InhabitantHandle<T> {

  protected final Habitat h;
  protected final Inhabitant<T> i;
  private ArrayList<Index> indices;
  private volatile boolean committed;
  
  @SuppressWarnings("unchecked")
  /*public*/ InhabitantHandleImpl(Habitat h, T component, String name,
      Map<String, Object> properties, String... classNames) {
    this.h = h;
    this.i = new ExistingSingletonInhabitant(
        component.getClass(),
        component,
        metadata(component, name, properties));
    if (null != classNames) {
      for (String clazz : classNames) {
        addIndex(clazz);
      }
    }
  }

  protected MultiMap<String, String> metadata(T component, String name,
      Map<String, Object> properties) {
    MultiMap<String, String> meta = new MultiMap<String, String>();
    if (null != name) {
      meta.add("name", name);
    }
    if (null != properties) {
      for (Entry<String, Object> e : properties.entrySet()) {
        if (null != e.getValue()) {
          meta.add(e.getKey(), e.getValue().toString());
        }
      }
    }
    return meta;
  }

  @Override
  public Inhabitant<T> getInhabitant() {
    return i;
  }

  @Override
  public MultiMap<String, String> getMetadata() {
    return i.metadata();
  }

  @Override
  public boolean isCommitted() {
    return committed;
  }
  
  @Override
  public void addIndex(String index) throws ComponentException {
    addIndex(index, null);
  }

  @Override
  public void addIndex(String index, String name) throws ComponentException {
    checkCommitted();

    if (null == indices) {
      indices = new ArrayList<Index>();
    }
    
    indices.add(new Index(index, name));
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<String> getIndices() {
    if (null == indices) {
      return Collections.EMPTY_LIST;
    }

    ArrayList<String> ret = new ArrayList<String>();
    for (Index idx : indices) {
      ret.add(idx.index);
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  public void commit() {
    if (!committed) {
      committed = true;

      h.add(i);
      if (null != indices) {
        for (Index idx : indices) {
          h.addIndex(i, idx.index, idx.name);
        }
      }
    }
  }

  protected void checkCommitted() throws ComponentException {
    if (committed) throw new ComponentException("read-only; inhabitant was committed");
  }

  @Override
  public void release() {
    if (committed) {
      if (null != indices) {
        for (Index idx : indices) {
          h.removeIndex(idx.index, i.get());
        }
      }
      
      h.remove(i);
      
      committed = false;
    }
  }

  
  protected class Index {
    protected final String index;
    protected final String name;
    
    protected Index(String index, String name) {
      this.index = index;
      this.name = name;
    }
  }

}
