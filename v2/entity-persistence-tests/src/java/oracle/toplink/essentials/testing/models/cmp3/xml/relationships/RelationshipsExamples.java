/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.


package oracle.toplink.essentials.testing.models.cmp3.xml.relationships;

public class RelationshipsExamples {
  public static Customer customerExample1() {
    Customer customer1 = new Customer();
    customer1.setName("John Smith");
    customer1.setCity("Ottawa");
    return customer1;
  }
  
  public static Customer customerExample2() {
    Customer customer2 = new Customer();
    customer2.setName("Jane Smith");
    customer2.setCity("Orleans");
    return customer2;
  }
  
  public static Customer customerExample3() {
    Customer customer3 = new Customer();
    customer3.setName("Karen McDonald");
    customer3.setCity("Nepean");
    return customer3;
  }
  
  public static Customer customerExample4() {
    Customer customer4 = new Customer();
    customer4.setName("Robert Sampson");
    customer4.setCity("Manotick");
    return customer4;
  }
  
  public static Item itemExample1() {
    Item item = new Item();
    item.setName("item1");
    item.setDescription("Item1 description");
    return item;
  }
  
  public static Item itemExample2() {
    Item item = new Item();
    item.setName("item2");
    item.setDescription("Item2 description");
    return item;
  }
  
  public static Item itemExample3() {
    Item item = new Item();
    item.setName("item3");
    item.setDescription("Item3 description");
    return item;
  }
  
  public static Item itemExample4() {
    Item item = new Item();
    item.setName("item4");
    item.setDescription("Item4 description");
    return item;
  }
  
  public static Order orderExample1() {
    Order order = new Order();
    order.setQuantity(70);
    order.setShippingAddress("100 Argyle Street");
    return order;
  }
  
  public static Order orderExample2() {
    Order order = new Order();
    order.setQuantity(680);
    order.setShippingAddress("500 Oracle Parkway");
    return order;
  }
  
  public static Order orderExample3() {
    Order order = new Order();
    order.setQuantity(22);
    order.setShippingAddress("240 Queen Street");
    return order;
  }
  
  public static Order orderExample4() {
    Order order = new Order();
    order.setQuantity(1);
    order.setShippingAddress("50 O'Connor");
    return order;
  }
}
