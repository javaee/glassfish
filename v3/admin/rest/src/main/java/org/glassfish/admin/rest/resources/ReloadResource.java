/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.resources;

import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.glassfish.admin.rest.adapter.Reloader;
import org.glassfish.internal.api.ServerContext;

/**
 *
 * @author ludo
 */
@Path("reload")
public class ReloadResource {

    @POST
    public void reload(@Context Reloader r, @Context ResourceConfig rc ,  @Context ServerContext sc) {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader apiClassLoader = sc.getCommonClassLoader();
            Thread.currentThread().setContextClassLoader(apiClassLoader);
            rc.getClasses().add(org.glassfish.admin.rest.resources.StaticResource.class);
            r.reload();
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }
}
