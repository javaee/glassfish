/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.cb;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import javax.xml.registry.*; 
import javax.xml.registry.infomodel.*; 

public class RetailPriceList implements Serializable {

    private ArrayList retailPriceItems;
    private ArrayList distributors;

    public RetailPriceList() {
      String RegistryURL = URLHelper.getQueryURL();
      String RPCDistributor = "JAXRPCCoffeeDistributor";
      retailPriceItems = new ArrayList();
      distributors = new ArrayList();

      JAXRQueryByName jq = new JAXRQueryByName();
      Connection connection =  jq.makeConnection(RegistryURL, RegistryURL);
      Collection orgs = jq.executeQuery(RPCDistributor);
      Iterator orgIter = orgs.iterator();
      // Display organization information
      try {
        while (orgIter.hasNext()) {
          Organization org = (Organization) orgIter.next();
          System.out.println("Org name: " + jq.getName(org));
          System.out.println("Org description: " + jq.getDescription(org));
          System.out.println("Org key id: " + jq.getKey(org));

          // Display service and binding information
          Collection services = org.getServices();
          Iterator svcIter = services.iterator();
          while (svcIter.hasNext()) {
            Service svc = (Service) svcIter.next();
            System.out.println(" Service name: " + jq.getName(svc));
            System.out.println(" Service description: " + jq.getDescription(svc));
            Collection serviceBindings = svc.getServiceBindings();
            Iterator sbIter = serviceBindings.iterator();
            while (sbIter.hasNext()) {
              ServiceBinding sb = (ServiceBinding) sbIter.next();
              String distributor = sb.getAccessURI();
              System.out.println("  Binding Description: " + jq.getDescription(sb));
              System.out.println("  Access URI: " + distributor);

              // Get price list from service at distributor URI
              PriceListBean priceList = PriceFetcher.getPriceList(distributor);
             
              PriceItemBean[] items = priceList.getPriceItems();
              retailPriceItems = new ArrayList();
              distributors = new ArrayList();
              BigDecimal price = new BigDecimal("0.00");
              for (int i = 0; i < items.length; i++) {
                price = items[i].getPricePerPound().multiply(new BigDecimal("1.35")).setScale(2, BigDecimal.ROUND_HALF_UP);
                RetailPriceItem pi = new RetailPriceItem(items[i].getCoffeeName(), items[i].getPricePerPound(), price , distributor);
                retailPriceItems.add(pi);
              }
              distributors.add(distributor); 
            }
          }
          // Print spacer between organizations
          System.out.println(" --- ");
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } finally  {
        // At end, close connection to registry
        if (connection != null) {
          try {
          	connection.close();
          } catch (JAXRException je) {}
        }
      }
      String SAAJPriceListURL = URLHelper.getSaajURL() + "/getPriceList";
      String SAAJOrderURL = URLHelper.getSaajURL() + "/orderCoffee";
      PriceListRequest plr = new PriceListRequest(SAAJPriceListURL);
      PriceListBean priceList = plr.getPriceList();;
      PriceItemBean[] priceItems = priceList.getPriceItems();
      for (int i = 0; i < priceItems.length; i++ ) {
        PriceItemBean pib = priceItems[i];
        BigDecimal price = pib.getPricePerPound().multiply(new BigDecimal("1.35")).setScale(2, BigDecimal.ROUND_HALF_UP);
        RetailPriceItem rpi = new RetailPriceItem(pib.getCoffeeName(), pib.getPricePerPound(), price, SAAJOrderURL);
        retailPriceItems.add(rpi);
      }
      distributors.add(SAAJOrderURL); 
 		}

    public ArrayList getItems() {
        return retailPriceItems;
    }

    public ArrayList getDistributors() {
        return distributors;
    }

    public void setItems(ArrayList priceItems) {
        this.retailPriceItems = priceItems;
    }

    public void setDistributors(ArrayList distributors) {
        this.distributors = distributors;
    }
}
