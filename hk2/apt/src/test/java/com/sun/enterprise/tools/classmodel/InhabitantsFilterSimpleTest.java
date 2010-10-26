package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.sun.enterprise.tools.InhabitantsDescriptor;

/**
 * Tests for InhabitantsFilter.
 * 
 * @author Jeff Trent
 *
 */
public class InhabitantsFilterSimpleTest {

  /**
   * Test basic processing logic.
   */
  @Test
  public void process() {
    InhabitantsDescriptor inDescriptor = testDescriptor();
    inDescriptor.enableDateOutput(false);
    InhabitantsDescriptor outDescriptor = new InhabitantsDescriptor();
    outDescriptor.enableDateOutput(false);
    Filter filter = new Filter();
    InhabitantsFilter.process(inDescriptor, outDescriptor, filter);
    String expected = expected();
    String output = clean(outDescriptor.toString());
    
    assertEquals("output equality expected", expected, output);
    
    assertNotNull(inDescriptor.keySet().toString(), inDescriptor.remove("notincluded_service2"));
    assertEquals("object equality expected (keys)", inDescriptor.keySet(), outDescriptor.keySet());
    assertEquals("object equality expected", inDescriptor, outDescriptor);
  }
  
  private InhabitantsDescriptor testDescriptor() {
    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
    descriptor.setComment("test descriptor line 1");
    descriptor.appendComment("test descriptor line 2");
    descriptor.putAll("service1", 
        Collections.singleton("contract1"), Collections.singleton("annotation1"), "name1", null);
    descriptor.putAll("notincluded_service2", 
        Collections.singleton("contract1"), Collections.singleton("annotation1"), "name1", null);
    descriptor.putAll("service3", 
        Arrays.asList(new String[] {"contract1", "contract2"}), Collections.singleton("annotation2"), null, null);
    descriptor.putAll("service4", 
        null, null, null, Collections.singletonMap("a", "1"));
    return descriptor;
  }
  
  private String expected() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service3,index=contract1,index=contract2,index=annotation2\n");
    sb.append("class=service4,a=1\n");
    return sb.toString();
  }
  
  private String clean(String str) {
    return str.replace("\r", "");
  }

  private static class Filter extends CodeSourceFilter {
    public Filter() {
      super(null);
    }
    
    @Override
    public boolean matches(String str) {
      return !str.startsWith("notincluded_");
    }
  }
}
