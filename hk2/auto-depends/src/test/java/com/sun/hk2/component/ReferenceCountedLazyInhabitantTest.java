package com.sun.hk2.component;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;

@SuppressWarnings("unchecked")
public class ReferenceCountedLazyInhabitantTest {

  @Test
  public void basics() {
    final AtomicInteger obj = new AtomicInteger(0);
    Inhabitant i = new ExistingSingletonInhabitant(obj) {
      @Override
      public Object get(Inhabitant onBehalfOf) {
        obj.incrementAndGet();
        return super.get(onBehalfOf);
      }
    };
    ReferenceCountedLazyInhabitant i2 = new ReferenceCountedLazyInhabitant(i);
    ReferenceCountedLazyInhabitant i3 = new ReferenceCountedLazyInhabitant(i2);

    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(0, obj.intValue());
    
    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(2, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    i3.release();
    assertEquals(1, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(1, obj.intValue());

    i3.release();
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(2, obj.intValue());

    i3.release();
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(2, obj.intValue());
    
    i3.release();
    i3.release();
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(2, obj.intValue());
  }
  
  @Test
  public void startingRefCountOfOne() {
    final AtomicInteger obj = new AtomicInteger(0);
    Inhabitant i = new ExistingSingletonInhabitant(obj) {
      @Override
      public Object get(Inhabitant onBehalfOf) {
        obj.incrementAndGet();
        return super.get(onBehalfOf);
      }
    };
    ReferenceCountedLazyInhabitant i2 = new ReferenceCountedLazyInhabitant(i);
    ReferenceCountedLazyInhabitant i3 = new ReferenceCountedLazyInhabitant(i2, 1);

    assertEquals(0, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(0, obj.intValue());
    
    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(2, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(3, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    i3.release();
    assertEquals(1, i2.getRefCount());
    assertEquals(2, i3.getRefCount());
    assertEquals(1, obj.intValue());

    i3.release();
    assertEquals(1, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    i3.release();
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(1, obj.intValue());
    
    i3.release();
    i3.release();
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
    assertEquals(1, obj.intValue());

    assertSame(obj, i3.get());
    assertEquals(1, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    assertEquals(2, obj.intValue());
  }
  
  @Test
  public void scopedClone() {
    final AtomicInteger obj = new AtomicInteger(0);
    Inhabitant i = new ExistingSingletonInhabitant(obj) {
      @Override
      public Object get(Inhabitant onBehalfOf) {
        obj.incrementAndGet();
        return super.get(onBehalfOf);
      }
    };
    
    ReferenceCountedLazyInhabitant i2 = new ReferenceCountedLazyInhabitant(i);
    ReferenceCountedLazyInhabitant i3 = (ReferenceCountedLazyInhabitant) i2.scopedClone();
    assertSame(i, i3.real);
    assertEquals(0, obj.intValue());
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
  }
  
  @Test
  public void exceptionFromGet() {
    final Object obj = new Object();
    Inhabitant i = new ExistingSingletonInhabitant(obj) {
      @Override
      public Object get(Inhabitant onBehalfOf) {
        throw new ComponentException("forced");
      }
    };

    ReferenceCountedLazyInhabitant i2 = new ReferenceCountedLazyInhabitant(i);
    ReferenceCountedLazyInhabitant i3 = (ReferenceCountedLazyInhabitant) i2.scopedClone();
    
    try {
      fail("expected exception but got: " + i3.get());
    } catch (Exception e) {
      // expected
    }
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
  }
  
  @Test
  public void exceptionFromRelease() {
    final Object obj = new Object();
    Inhabitant i = new ExistingSingletonInhabitant(obj) {
      @Override
      public void release() {
        throw new ComponentException("forced");
      }
    };

    ReferenceCountedLazyInhabitant i2 = new ReferenceCountedLazyInhabitant(i);
    ReferenceCountedLazyInhabitant i3 = (ReferenceCountedLazyInhabitant) i2.scopedClone();
    
    i3.get();
    assertEquals(0, i2.getRefCount());
    assertEquals(1, i3.getRefCount());
    
//    i3.release();
//    assertEquals(0, i2.getRefCount());
//    assertEquals(0, i3.getRefCount());
    
    try {
      i3.release();
      fail("expected exception");
    } catch (Exception e) {
      // expected
    }
    
    assertEquals(0, i2.getRefCount());
    assertEquals(0, i3.getRefCount());
  }
  
  
}
