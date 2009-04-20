/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.glassfish.admin.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

/**
 *
 * @author Ludovic Champenois ludo@dev.java.net
 */
public  class TemplateListOfResource<E extends ConfigBeanProxy> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected List<E> entity;

    /** Creates a new instance of xxxResource */
    public TemplateListOfResource() {
    }

     @GET
     public List<Dom> get(@QueryParam("expandLevel")
             @DefaultValue("1") int expandLevel) {

         List<Dom> domList = new ArrayList();
         List<E> entities = getEntity();
//         try {
             Iterator iterator = entities.iterator();
             E e;
             while (iterator.hasNext()) {
                 e = (E) iterator.next();
                 domList.add(Dom.unwrap(e));
             }
//         } catch (Exception e) {
//             System.out.println(e.getMessage());
//         }

         return domList;




}



  //  public abstract List<E> getEntity();

        public void setEntity(List<E> p) {
        entity = p;
    }

    public List<E> getEntity() {
        return entity;
    }


     @POST
    @Consumes("application/json")
    public Response createEntity(InputStream data) {

        try {


                 // Example creating a new http-listener element under http-service
           Map<String, String> attributes = new HashMap<String, String>();
           attributes.put("id", "jerome-listener");
           attributes.put("enabled", "true");
           ///ConfigSupport.createAndSet(getEntity()., HttpListener.class, attributes);

            //  Customer customer = buildCustomer(null, customerData);
            //  long customerId = persist(customer, 0);
            System.out.println("POST IS CALLED" + data);

            return Response.created(URI.create("/" + "HGHGHGHGHGHGHGKEYKEY!!!!!!")).build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }


    }


    private String introspect(Dom proxy) {
        Set<String> ss = proxy.getAttributeNames();
        String ret = "";
        //    System.out.println( "--------" + proxy.model.key);
        boolean first = true;
        ret = ret + "{\n";
        for (String a : ss) {
            if (first == false) {
                ret = ret + ",\n";
            }
            ret = ret + "'";
            ret = ret + a;
            ret = ret + "' : '";

            ret = ret + proxy.attribute(a);
            ret = ret + "'";

            first = false;


        }
        ret = ret + "}";
        return ret;
    }

    private String introspectElements(Dom proxy) {
        Set<String> elem = proxy.getElementNames();
        //System.out.println(id+"set size is" + elem.size());

        String ret = "";
        //    System.out.println( "--------" + proxy.model.key);
        boolean first = true;
        ret = ret + "{\n";
        for (String a : elem) {
            if (first == false) {
                ret = ret + ",\n";
            }
            ret = ret + "'";
            ret = ret + a;
            ret = ret + "' : '";

            ret = ret + uriInfo.getAbsolutePath() + a;
            ret = ret + "'";

            first = false;


        }
        ret = ret + "}";
        return ret;


//        for (String bb : elem) {
//            System.out.println("<" + bb + ">");
//            org.jvnet.hk2.config.ConfigModel.Property prop = proxy.model.getElement(bb);
//            if (prop != null && proxy.model.getElement(bb).isLeaf()) {
//                System.out.println("-1-1-1- " + proxy.leafElement(bb));
//            } else {
//                introspect(out, proxy.element(bb));
//            }
//
//            System.out.println("</" + bb + ">");
//            System.out.println("    ");
//            ///  }



    }
}
