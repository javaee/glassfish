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
