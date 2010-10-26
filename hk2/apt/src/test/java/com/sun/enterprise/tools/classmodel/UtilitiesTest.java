package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import org.junit.Test;


public class UtilitiesTest {

  @Test
  public void testSortInhabitants() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service2,index=contract1,index=annotation1\n");
    sb.append("class=service3,index=contract2:name2,index=contract1:name2,index=annotation2,index=annotation1,b=2,a=1\n");
    sb.append("class=service4,a=1\n");
    String expected = sb.toString();
    String output = Utilities.sortInhabitantsDescriptor(testCase(), false);
    assertEquals(expected, output);
  }

  @Test
  public void testSortInhabitantsAndContents() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=annotation1,index=contract1:name1\n");
    sb.append("class=service2,index=annotation1,index=contract1\n");
    sb.append("class=service3,index=annotation1,index=annotation2,index=contract1:name2,index=contract2:name2,a=1,b=2\n");
    sb.append("class=service4,a=1\n");
    String expected = sb.toString();
    String output = Utilities.sortInhabitantsDescriptor(testCase(), true);
    assertEquals(expected, output);
  }
  
  @Test
  public void testEmptySort() {
    String output = Utilities.sortInhabitantsDescriptor("", true);
    assertEquals("", output);
  }
  
  private String testCase() {
    StringBuilder sb = new StringBuilder();
    sb.append("# comment\n");
    sb.append("class=service2,index=contract1,index=annotation1\n");
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service4,a=1\n");
    sb.append("class=service3,index=contract2:name2,index=contract1:name2,index=annotation2,index=annotation1,b=2,a=1\n");
    return sb.toString();
  }

}
