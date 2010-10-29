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
      super(habitatFactoryClass, ipFactoryClass, true);
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
