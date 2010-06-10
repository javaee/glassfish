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
