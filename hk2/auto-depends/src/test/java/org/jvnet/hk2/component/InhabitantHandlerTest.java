package org.jvnet.hk2.component;

import java.util.concurrent.Executor;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.HabitatListener.EventType;

/**
 * For internal sanity checks only.  InhabitatHandler is a test-only
 * helper construct.
 * 
 * @author Jeff Trent
 */
public class InhabitantHandlerTest extends TestCase {

  TestHabitat h = new TestHabitat(new Executor() {
    @Override
    public void execute(Runnable runnable) {
      runnable.run();
    }
  });
  
  TestHabitatListener hl = new TestHabitatListener();

  public void testCreate_commitLifecycle() {
    h.addHabitatListener(hl);
    hl.calls.clear();
    
    InhabitantHandle<?> ih = InhabitantHandlerImpl.
    create(h, false, this,
        "atestcase",
        null,
        new String[] {
          Test.class.getName(),
          TestCase.class.getName(),
        });
    assertNotNull(ih);
    assertEquals("habitat calls (precommitted): " + hl.calls, 0, hl.calls.size());
    
    ih.addIndex("index", "name");
    assertEquals("habitat calls (precommitted): " + hl.calls, 0, hl.calls.size());

    assertFalse("committed", ih.isCommitted());
    
    ih.commit();
    assertTrue("committed", ih.isCommitted());
    assertEquals("habitat calls (precommitted): " + hl.calls, 4, hl.calls.size());
    HabitatListenerTest.assertCall(h, hl.calls.get(0), EventType.INHABITANT_ADDED, this, null, null);
    HabitatListenerTest.assertCall(h, hl.calls.get(1), EventType.INHABITANT_INDEX_ADDED, this, Test.class.getName(), null);
    HabitatListenerTest.assertCall(h, hl.calls.get(2), EventType.INHABITANT_INDEX_ADDED, this, TestCase.class.getName(), null);
    HabitatListenerTest.assertCall(h, hl.calls.get(3), EventType.INHABITANT_INDEX_ADDED, this, "index", "name");

    assertNotNull(ih.getMetadata());
    assertNotNull(ih.getMetadata().get("name"));
    assertEquals(1, ih.getMetadata().get("name").size());
    assertEquals("atestcase", ih.getMetadata().get("name").get(0));
    
    try {
      ih.addIndex("index2", "name2");
      fail("expected exception in committed state");
    } catch (ComponentException e) {
      // expected
    }
  }

  public void testCreate_releaseLifecycle() {
    h.addHabitatListener(hl);
    hl.calls.clear();

    InhabitantHandle<?> ih = InhabitantHandlerImpl.
    create(h, true, this,
        "atestcase",
        null,
        new String[] {
            Test.class.getName(),
            TestCase.class.getName(),
        }
      );
    assertEquals("habitat calls (committed): " + hl.calls, 3, hl.calls.size());
    hl.calls.clear();

    assertTrue("committed", ih.isCommitted());
    
    ih.release();
    assertFalse("committed", ih.isCommitted());
    assertEquals("habitat calls (committed): " + hl.calls, 3, hl.calls.size());
    HabitatListenerTest.assertCall(h, hl.calls.get(0), EventType.INHABITANT_INDEX_REMOVED, this, Test.class.getName(), null);
    HabitatListenerTest.assertCall(h, hl.calls.get(1), EventType.INHABITANT_REMOVED, this, null, null);
    HabitatListenerTest.assertCall(h, hl.calls.get(2), EventType.INHABITANT_INDEX_REMOVED, this, TestCase.class.getName(), null);

    // should me modifiable again
    hl.calls.clear();
    ih.addIndex("index2", "name2");
    assertEquals("habitat calls (precommitted): " + hl.calls, 0, hl.calls.size());
  }

  public void testCreate_repeatLifecycle() {
    h.addHabitatListener(hl);
    hl.calls.clear();

    InhabitantHandle<?> ih = InhabitantHandlerImpl.
    create(h, true, this,
        "atestcase",
        null,
        new String[] {
            Test.class.getName(),
            TestCase.class.getName(),
        }
      );
    assertEquals("habitat calls (committed): " + hl.calls, 3, hl.calls.size());
    hl.calls.clear();

    ih.release();
    hl.calls.clear();
    ih.release(); // extra

    ih.commit();
    assertEquals("habitat calls (committed): " + hl.calls, 3, hl.calls.size());
    assertTrue("committed", ih.isCommitted());

    hl.calls.clear();
    ih.commit(); // extra
    assertEquals("habitat calls (committed): " + hl.calls, 0, hl.calls.size());
  }

}
