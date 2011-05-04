/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package j2eeguide.product;

import java.util.*;
import javax.ejb.*;

public class ProductEJB implements EntityBean {

   public String productId;
   public String description;
   public double price;

   private EntityContext context;

   public void setPrice(double price) {

      this.price = price;
   }

   public double getPrice() {

      return price;
   }

   public String getDescription() {

      return description;
   }

   public String ejbCreate(String productId, String description, 
      double price) throws CreateException {

      if (productId == null) {
         throw new CreateException("The productId is required.");
      }

      this.productId = productId;
      this.description = description;
      this.price = price;

      return null;
   }

   public void setEntityContext(EntityContext context) {

      this.context = context;
   }

   public void ejbActivate() { 

      productId = (String)context.getPrimaryKey();
   }

   public void ejbPassivate() {

      productId = null;
      description = null;
   }

   public void ejbRemove() { }
   public void ejbLoad() { }
   public void ejbStore() { }
   public void unsetEntityContext() { }
   public void ejbPostCreate(String productId, String description, 
      double balance) { }

} // ProductEJB 
