package org.jvnet.hk2.component;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.jvnet.hk2.test.impl.OneSimple;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.LazyInhabitant;

import com.sun.hk2.component.Holder;

/**
 * General Inhabitant-type testing.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
public class InhabitantTest {

  @Test
  public void verifyInstancingAndInhabitatRequested() {
    int inhabitantRequested = 0;
    
    for (Inhabitant i : createTestInhabitants()) {
      assertSame(i.toString(), i.get(), i.get());
      
      Object obj = i.get();
      if (InhabitantRequested.class.isInstance(obj)) {
        assertSame(OneSimple.class, obj.getClass());
        OneSimple os = OneSimple.class.cast(obj);
        assertNotNull(os.self);
        inhabitantRequested++;
        
        os.self = null;
        assertSame(i.toString(), obj, i.get());
        assertNull(os.self);
      }
    }
    
    assertTrue(inhabitantRequested > 0);
  }

  /**
   * Verifies the behavior in post construct ComponentException
   */
  @Test
  public void postConstructFailure() {
    Habitat h = new Habitat();
    Holder<ClassLoader> cl = new Holder.Impl(getClass().getClassLoader());
    LazyInhabitant li = new LazyInhabitant(h, cl,
        AFailingPostConstructService.class.getName(),
        new MultiMap<String, String>());
    h.add(li);
    h.addIndex(li, PostConstruct.class.getName(), "test");
    
    try {
      Object obj = h.getComponent(PostConstruct.class.getName(), "test");
      fail("expected ComponentException but got: " + obj);
    } catch (ComponentException e) {
      // expected
    }
    
    Inhabitant i = h.getInhabitantByContract(PostConstruct.class.getName(), "test");
    assertSame(li, i);
    assertFalse(i.isInstantiated());
  }
  
  List<Inhabitant<?>> createTestInhabitants() {
    Habitat h = new Habitat();
    
    ArrayList<Inhabitant<?>> list = new ArrayList<Inhabitant<?>>();
    list.add(new ExistingSingletonInhabitant(this));
    
    Holder<ClassLoader> cl = new Holder.Impl(getClass().getClassLoader());
    LazyInhabitant li = new LazyInhabitant(h, cl,
        OneSimple.class.getName(),
        new MultiMap<String, String>());
    list.add(li);

    return list;
  }


  public static class AFailingPostConstructService implements PostConstruct {

    @Override
    public void postConstruct() {
      throw new ComponentException("forced exception in test");
    }
    
  }
}
