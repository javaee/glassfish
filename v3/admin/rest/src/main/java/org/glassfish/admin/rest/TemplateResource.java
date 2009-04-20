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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * @author Ludovic Champenois ludo@dev.java.net
 */
public class TemplateResource<E extends ConfigBeanProxy> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected E entity;

    /** Creates a new instance of xxxResource */
    public TemplateResource() {
    }


    public void setEntity(E p) {
        entity = p;
    }

    public E getEntity() {
        return entity;
    }

    @GET
    @Produces({"application/json","text/html","application/xml","application/x-www-form-urlencoded"})
    public Dom get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {


        return Dom.unwrap(getEntity());

    }
    public ConfigBean getConfigBean(){
        return (ConfigBean) Dom.unwrap(getEntity());
    }

    @POST
    @Produces("text/html")
    public Dom updateDataItem(Hashtable<String, String> data) {
       data.remove("submit");
       for (String name : data.keySet()) {
            System.out.println("updateDataItem name=" + name);
            System.out.println("value=" + data.get(name));
        }
        //data.get("submit");
        Map<ConfigBean, Map<String, String>> mapOfChanges = new HashMap<ConfigBean, Map<String, String>> ();
        mapOfChanges.put(getConfigBean (), data);

        try {
            RestService.configSupport.apply(mapOfChanges); //throws TransactionFailure
        } catch (TransactionFailure ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Dom.unwrap(getEntity());
    }

    @PUT
    @Consumes("application/json")
    public void updateEntity(InputStream data) {
        try {
            //  Customer customer = buildCustomer(null, customerData);
            //  long customerId = persist(customer, 0);
            //   return Response.created(URI.create("/" + id)).build();
            System.out.println("JSON PUT IS CALLED" + data);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    public void delete() {
             System.out.println("Delete IS CALLED for" + getEntity());
       
    }


}
