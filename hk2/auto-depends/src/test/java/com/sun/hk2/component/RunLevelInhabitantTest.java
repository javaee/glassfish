package com.sun.hk2.component;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.junit.Hk2Runner;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.LazyInhabitant;
import com.sun.hk2.component.RunLevelInhabitant;

@SuppressWarnings("unchecked")
@RunWith(Hk2Runner.class)
public class RunLevelInhabitantTest {

  @Inject
  Habitat h;
  
  final Holder<ClassLoader> clh = new Holder.Impl<ClassLoader>(RunLevelInhabitantTest.class.getClassLoader());

  final MultiMap<String,String> md = new MultiMap<String,String>();
  
  @Test
  public void testSufficientLevel() {
    RunLevelState state = new TestRunLevelState(5, 10);

    LazyInhabitant<?> i = new LazyInhabitant(h, clh, getClass().getName(), md);
    assertFalse(i.isInstantiated());
    RunLevelInhabitant rli = new RunLevelInhabitant(i, 5, state);
    assertEquals(getClass().getName(), rli.typeName());
    assertFalse(rli.isInstantiated());
    assertNotNull(rli.get());
    assertSame(rli.get(), rli.get());
    assertTrue(rli.isInstantiated());
    assertTrue(i.isInstantiated());
    assertEquals(getClass(), rli.type());
    rli.release();
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.metadata(), rli.metadata());
  }

  @Test
  public void testInsufficientLevel() {
    RunLevelState state = new TestRunLevelState(null, 10);
    
    LazyInhabitant<?> i = new LazyInhabitant(h, clh, getClass().getName(), md);
    assertFalse(i.isInstantiated());
    RunLevelInhabitant rli = new RunLevelInhabitant(i, 15, state);
    assertEquals(getClass().getName(), rli.typeName());
    assertFalse(rli.isInstantiated());
    try {
      fail("expected exception but got: " + rli.get());
    } catch (ComponentException e) {
    }
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.type(), rli.type());
    assertFalse(i.isInstantiated());
    // should have no affect
    rli.release();
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.metadata(), rli.metadata());
  }
  
}
