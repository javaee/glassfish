/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest.resources.custom;

import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.config.dom.Protocol;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.config.Dom;

/**
 *
 * @author jasonlee
 */
public class FindHttpProtocolResource {
    @Context
    protected HttpHeaders requestHeaders;
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected Dom entity;
    protected Dom parent;

    public void setEntity(Dom p) {
        entity = p;
    }

    public Dom getEntity() {
        return entity;
    }

    @GET
    @Produces({"text/html;qs=2",MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public ActionReportResult get() {
        Dom dom = getEntity();
        NetworkListener nl = dom.createProxy(NetworkListener.class);
        Protocol p = nl.findHttpProtocol();
        ActionReport ar = RestService.getHabitat().getComponent(ActionReport.class, ResourceUtil.getResultType(requestHeaders));
        ar.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ar.getTopMessagePart().getProps().put("protocol", p.getName());

        ActionReportResult result = new ActionReportResult("find-http-protocal", ar, new OptionsResult());

        return result;
    }
}