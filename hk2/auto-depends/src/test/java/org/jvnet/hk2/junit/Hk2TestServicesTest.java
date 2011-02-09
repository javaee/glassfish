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
package org.jvnet.hk2.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatFactory;
import org.jvnet.hk2.component.InhabitantsParserFactory;

import com.sun.hk2.component.InhabitantsParser;

/**
 * Unit tests for the {@link Hk2TestServices} class.
 *
 * @author Mason Taube
 */
public class Hk2TestServicesTest {

  @Test
  public void testFactories() {
    new TestHk2TestServices(TestHabitatFactory.class, TestInhabitantsParserFactory.class);
    assertEquals(1, TestHabitatFactory.calls);
    assertEquals(1, TestInhabitantsParserFactory.calls);
  }

  @Test
  public void testHabitatInitialization() {
    TestHk2TestServices target = new TestHk2TestServices(TestHabitatFactory.class, TestInhabitantsParserFactory.class);
    assertNotNull(target.getHabitat());
    assertSame(target.getHabitat(), target.getHabitat());
    assertTrue(target.getHabitat().isInitialized());
  }
  
  static class TestHk2TestServices extends Hk2TestServices {
    public TestHk2TestServices(Class<? extends HabitatFactory> habitatFactoryClass,
        Class<? extends InhabitantsParserFactory> ipFactoryClass) {
      super(habitatFactoryClass, ipFactoryClass, true, true);
    }
    
    @Override
    protected void populateHabitat(Habitat habitat, InhabitantsParser ip) {
      // do nothing
    }
  }
  
  static class TestHabitatFactory implements HabitatFactory {
    private static int calls;
    
    @Override
    public Habitat newHabitat() throws ComponentException {
      calls++;
      return new Habitat();
    }
  }

  static class TestInhabitantsParserFactory implements InhabitantsParserFactory {
    private static int calls;

    @Override
    public InhabitantsParser createInhabitantsParser(Habitat habitat) {
      calls++;
      return null;
    }
  }
}
